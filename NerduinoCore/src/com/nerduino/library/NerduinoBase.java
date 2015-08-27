/*
 Part of the Nerduino IOT project - http://nerduino.com

 Copyright (c) 2013 Chase Laurendine

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.nerduino.library;


import com.nerduino.core.ExplorerTopComponent;
import com.nerduino.nodes.TreeNode;
import com.nerduino.arduino.StatusUpdateEventClass;
import com.nerduino.arduino.StatusUpdateEventListener;
import com.nerduino.services.ServiceManager;
import com.nerduino.xbee.BitConverter;
import com.nerduino.xbee.Serial;
import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.TopComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class NerduinoBase extends TreeNode
{
    // Declarations
	int PROCESS_CHECKIN = 1;
    
    static ArrayList<NerduinoBase> s_nerduinos = new ArrayList<NerduinoBase>();
    static short s_nextRoutingIndex = 1;
	
	Scriptable m_nerduinoScope;
	boolean m_receivedGetPoint = false;
	boolean m_receivedGetPoints = false;
	boolean m_pinged = false;
	int m_state = 0;
	long m_startTime = 0L;
	int m_pingFailCount = 0;
	
	boolean m_loading = false;
	boolean m_interactive = true;
    NerduinoStatusEnum m_status = NerduinoStatusEnum.Uninitialized;
	
	Address m_address;
	
	private final List<StatusUpdateEventListener> m_statusUpdateListeners = new ArrayList<StatusUpdateEventListener>();
	private final List<CommandEventListener> m_commandListeners = new ArrayList<CommandEventListener>();
	private final List<UpdateEventListener> m_updateListeners = new ArrayList<UpdateEventListener>();
	
	DeviceTypeEnum m_deviceType;
	public short m_pointCount = 0;
	long m_lastResponseMillis;
	
	boolean m_engaged = false;
	boolean m_engaging = false;
	boolean m_checkedIn = false;
	boolean m_verbose = false;
	boolean m_active = false;

	Context m_context;
	Scriptable m_scope;
	CommandResponse m_commandResponse = new CommandResponse();
	NerduinoDashboardTopComponent m_dashboard;
	
    public List<RemoteDataPoint> m_points = new ArrayList<RemoteDataPoint>();
	
    // Constructors
    public NerduinoBase(String baseName, String icon)
    {
        super(new Children.Array(), baseName, icon);
        
        m_canCopy = false;
        m_canDelete = true;
        m_canDrag = true;
        m_canRename = true;
        m_hasEditor = true;
        m_showPropertyBrowser = true;
		
		m_name = baseName;
		
		m_address = new Address();
		m_address.RoutingIndex = s_nextRoutingIndex++;
		
		s_nerduinos.add(this);
    }
	
	public boolean getActive()
	{
		return m_active;
	}

	public void setActive(Boolean value)
	{
		if (m_active != value)
		{
			m_active = value;

			fireUpdate();
			save();
		}
	}	

	
	// Methods
	public void executeCommand(String command)
	{
		if ("ping".equals(command))
			ping();
		else if ("reset".equals(command))
			reset();
		else if ("engage".equals(command))
			engage();
		else if ("test".equals(command))
			test();
	}
	
	public void resetBoard()
	{
	}
	
	public void reset()
	{
		resetBoard();
		
		engage();
	}
	
	public Object executeScript(String script)
	{
		if (m_context == null)
		{
			m_context = Context.enter();
			m_scope = m_context.initStandardObjects();
			
			// load up all services
			if (ServiceManager.Current != null)
				ServiceManager.Current.applyServices(m_context);
		}
		
		return m_context.evaluateString(m_scope, script, "Script", 1, null );
	}
	
	public boolean configureNewNerduino()
	{
		return false;
	}

	public synchronized void addUpdateEventListener(UpdateEventListener listener)  
	{
		m_updateListeners.add(listener);
	}

	public synchronized void removeUpdateEventListener(UpdateEventListener listener)   
	{
		m_updateListeners.remove(listener);
	}
	
	protected synchronized void fireUpdate() 
	{
		EventObject event = new EventObject(this);
		
		Iterator<UpdateEventListener> i = m_updateListeners.iterator();
		
		while(i.hasNext())  
		{
			i.next().handleUpdateEvent(event);
		}
	}

	public synchronized void addCommandEventListener(CommandEventListener listener)  
	{
		m_commandListeners.add(listener);
	}

	public synchronized void removeCommandEventListener(CommandEventListener listener)   
	{
		m_commandListeners.remove(listener);
	}
	
	protected synchronized void fireCommandUpdate(NerduinoBase requestedBy, String command, CommandMessageTypeEnum type) 
	{
		String src = (requestedBy == null) ? "H" : requestedBy.getName();
		
		fireCommandUpdate(src + ": " + command, type);
	}
	
	protected synchronized void fireCommandUpdate(String command, CommandMessageTypeEnum type) 
	{
		CommandEventClass event = new CommandEventClass(this, command, type);
		
		Iterator<CommandEventListener> i = m_commandListeners.iterator();
		
		while(i.hasNext())  
		{
			i.next().handleCommandEvent(event);
		}
	}

	public synchronized void addStatusUpdateEventListener(StatusUpdateEventListener listener)  
	{
		m_statusUpdateListeners.add(listener);
	}

	public synchronized void removeStatusUpdateEventListener(StatusUpdateEventListener listener)   
	{
		m_statusUpdateListeners.remove(listener);
	}
	

	protected synchronized void fireEngageStatusUpdate(boolean engaging, boolean engaged, int percentComplete, String error) 
	{
		StatusUpdateEventClass event = new StatusUpdateEventClass(this);
		
		event.statusType = StatusUpdateEventClass.ENGAGE;
		event.pending = engaging;
		event.succeeded = engaged;
		event.percentComplete = percentComplete;
		event.error = error;
		
		Iterator<StatusUpdateEventListener> i = m_statusUpdateListeners.iterator();
		
		while(i.hasNext())  
		{
			i.next().handleStatusUpdateEvent(event);
		}
	}

	public boolean getVerbose()
	{
		return m_verbose;
	}
	
	public void setVerbose(Boolean value)
	{
		m_verbose = value;
	}
	
	public Serial getSerialMonitor()
	{
		return null;
	}
	
	public NerduinoStatusEnum getStatus()
	{
		return m_status;
	}
	
    public void setStatus(NerduinoStatusEnum status)
    {
    	// TODO detect when the status has changed and notify callbacks of the status change
    	if (m_status != status)
		{
			m_status = status;
			
			ExplorerTopComponent.Current.repaint();
			fireUpdate();
		}
    }
	
	public void touch()
	{
		m_lastResponseMillis = System.currentTimeMillis();
		m_pingFailCount = 0;
		
		if (m_status != NerduinoStatusEnum.Online)
			setStatus(NerduinoStatusEnum.Online);
	}
	
	public short getPointCount()
	{
		return m_pointCount;
	}
	
	public void save()
	{
		if (m_loading)
			return;

		try
		{
			Document xmldoc = new DocumentImpl();
			Element root = xmldoc.createElement("root");
			
			writeXML(xmldoc, root);
			
			xmldoc.appendChild(root);



			FileOutputStream fos = new FileOutputStream(getFileName());
			OutputFormat of = new OutputFormat("XML", "ISO-8859-1", true);

			of.setIndent(1);
			of.setIndenting(true);
			//of.setDoctype(null,"users.dtd");

			XMLSerializer serializer = new XMLSerializer(fos, of);

			serializer.asDOMSerializer();
			serializer.serialize(xmldoc.getDocumentElement());

			fos.close();
		}
		catch(IOException ex)
		{
		}
	}

	String getFileName()
	{
		return NerduinoManager.Current.getFilePath() + "/" + m_name + ".nerd";
	}
	
	@Override
	public TopComponent getTopComponent()
	{
		return null;
	}
	
	public DeviceTypeEnum getDeviceType()
	{
		return m_deviceType;
	}

	public void setDeviceType(DeviceTypeEnum deviceType)
	{
		m_deviceType = deviceType;
		
		fireUpdate();
		save();
	}

    public Scriptable getScope()
	{
		if (m_nerduinoScope == null)
		{
//			if (m_context == null)
//			{
//				m_context = Context.enter();
//			}
			
			
			m_nerduinoScope = new NerduinoScope(this);
//			m_nerduinoScope = m_context.initStandardObjects(new NerduinoScope(this));
		}
		
		return m_nerduinoScope;
	}
	
	public PointBase getPoint(short index)
    {
		for(RemoteDataPoint point : m_points)
		{
			if (point.Id == index)
				return point;
		}
		
		return null;
    }
    
    public PointBase getPoint(String name)
    {
		for(RemoteDataPoint point : m_points)
		{
			if (point.getName().equals(name))
				return point;
		}

		return null;
    }
	
	public void getPoints()
    {
        // mark all of the existing points as waiting for validation
        for(PointBase point : m_points)
        {
        	if (point != null)
        		((RemoteDataPoint) point).Validated = false;
        }
        
        byte[] data = new byte[0];
        
    	sendGetPoint(this, (short) 0, (byte) 1, (byte) 0, data);    	
    }
	
	public void removeUnusedPoints()
    {
        for (int i = m_points.size() - 1; i >= 0; i--)
        {
        	RemoteDataPoint point = m_points.get(i);
        	
            if (point == null || !point.Validated)
                m_points.remove(i);
        }
    }
	
	public void processMessage(NerduinoBase originator, byte[] data)
	{
	}
	
	public void forwardMessage(NerduinoBase originator, byte[] data)
	{
	}
	
    public boolean ping()
    {
		m_pinged = false;
		sendPing(null, (short) 0);
		
		float wait = 0.0f;
		
		while (!m_pinged && wait < 2.0f)
		{
			try
			{
				Thread.sleep(100);
				wait += 0.1f;
			}
			catch(InterruptedException ex)
			{
				Exceptions.printStackTrace(ex);
			}
		}
		
		if (!m_pinged)
		{
			m_pingFailCount++;
			
			if (m_pingFailCount == 5)
			{
				engage();
				m_pingFailCount = 0;
			}
		}
		else
		{
			m_pingFailCount = 0;
		}
		
		return m_pinged;
    }

	public void checkStatus() 
	{
		// abstract method to be overriden by derived classes
		
		// TODO see the NerduinoXbee implementation.. this may need to be the 
		// base implementation
	}
	
	public String engage()
	{
		// abstract method to be overridden by derived classes
		return null;
	}
	
	public double getTimeSinceLastResponse()
    {
    	long millis = System.currentTimeMillis();
    	
    	double dsecs = (double) (millis - m_lastResponseMillis) / 1000.0;
    	
    	return dsecs;
    }
	
	public void onCheckin(byte[] data, int offset)
	{
		if (m_verbose)
			fireCommandUpdate("N: Checkin", CommandMessageTypeEnum.IncomingCommand);
		
		touch();
		
		byte status = 0;
		
		if (data != null)
			status = data[offset++];
		
		setStatus(NerduinoStatusEnum.valueOf(status));
		
		m_state = 2;
		m_checkedIn = true;
		
		// reset the context, in case the serial port has reset and 
		// is now receiving on a new thread
		m_context = null;
	}

	public void onExecuteCommand(NerduinoBase originator, byte[] data, int offset)
	{
		short responseToken = (short) ((int) data[offset++] * 0x100 + (int) data[offset++]);

		byte returndatatype = data[offset++];
		byte commandlength = data[offset++];
		
		byte[] bytes = new byte[commandlength];
		
		System.arraycopy(data, offset, bytes, 0, commandlength);
		
		CommandResponse response = sendExecuteCommand(originator, responseToken, returndatatype, commandlength, bytes);
		
		// TODO report the response to the client
		byte[] rdata = new byte[response.DataLength];
		
		if (response.Status == ResponseStatusEnum.RS_Complete)
		{
			for(int i = 0; i < response.DataLength; i++)
			{
				rdata[i] = response.Data.get(i);
			}
		}
		
		originator.sendExecuteCommandResponse(responseToken, response.Status.Value(), response.DataType.Value(), (byte) response.DataLength, rdata);
	}
		
	public void onHostExecuteCommand(byte[] data, int offset)
	{
		short responseToken = (short) (data[offset++] * 0x100 + data[offset++]);
		byte dataType = data[offset++];
		byte length = data[offset++];
		
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < length; i++)
		{
			sb.append((char) data[offset++]);
		}
		
		String command = sb.toString();
		byte responseLength = (byte) 0;
		byte[] response = null;
		
		InputOutput io = IOProvider.getDefault().getIO("Build", false);
		io.getOut().println(command);		
		
		if (m_verbose)
			fireCommandUpdate("N: Execute " + command, CommandMessageTypeEnum.IncomingCommand);
		else
			fireCommandUpdate(command, CommandMessageTypeEnum.IncomingCommand);
		
		try
		{
			if (m_context == null)
			{
				m_context = Context.enter();
				m_scope = m_context.initStandardObjects();

				// load up all services
				if (ServiceManager.Current != null)
					ServiceManager.Current.applyServices(m_context);
			}
			
			
			Object val = m_context.evaluateString(m_scope, command, "Script", 1, null);

			if (dataType > 5) // not expecting a response
				return;
			
			if (val instanceof Undefined)
			{
				sendExecuteCommandResponse(responseToken, ResponseStatusEnum.RS_UndefinedResponse.Value(), (byte) 0, (byte) 0, null);
				return;
			}
			
			String responsestr = "";
			
			// if the response type is string.. parse into the desired datatype
			if (val instanceof String)
			{
				String v = (String) val;
				responsestr = v;
				
				try
				{
					switch (dataType)
					{
						case 0: // boolean
						{
							Boolean b = Boolean.parseBoolean(v);
							
							responseLength = 1;
							response = new byte[1];
							
							if (b)
								response[0] = 1;
							else
								response[0] = 0;
							
							break;
						}
						case 1: // byte
						{
							Byte b = Byte.parseByte(v);
							
							responseLength = 1;
							response = new byte[1];
							response[0] = b;
							
							break;
						}
						case 2: // short
						{
							Short b = Short.parseShort(v);
							
							responseLength = 2;
							response = BitConverter.GetBytes(b);
							
							break;
						}
						case 3: // integer
						{
							Integer b = Integer.parseInt(v);
							
							responseLength = 4;
							response = BitConverter.GetBytes(b);
							
							break;
						}
						case 4: // float
						{
							Float b = Float.parseFloat(v);
							
							responseLength = 4;
							response = BitConverter.GetBytes(b);
							
							break;
						}
						case 5: // string
						{
							responseLength =  (byte) v.length();
							response = v.getBytes();
							
							break;
						}
					}
				}
				catch(Exception e)
				{
					
				}
			}
			
			if (responseLength == 0 && (val instanceof Float) || (val instanceof Double))
			{
				Float fval = 0.0f;
				
				// if the response type is float or double.. convert to float
				if (val instanceof Float)
					fval = (Float) val;
				else if (val instanceof Double)
				{
					double d = (float) (double) (Double) val;
					fval = (float) d;
				}
				
				responsestr = fval.toString();
				
				switch (dataType)
				{
					case 0: // boolean
					{
						responseLength = 1;
						response = new byte[1];
						
						if (fval == 0.0f)
							response[0] = 0;
						else
							response[0] = 1;
						
						break;
					}
					case 1: // byte
					{
						responseLength = 1;
						response = new byte[1];
						response[0] = fval.byteValue();
						
						break;
					}
					case 2: // short
					{
						Short b = fval.shortValue();
						
						responseLength = 2;
						response = BitConverter.GetBytes(b);
						
						break;
					}
					case 3: // integer
					{
						Integer b = fval.intValue();
						
						responseLength = 4;
						response = BitConverter.GetBytes(b);
						
						break;
					}
					case 4: // float
					{
						responseLength = 4;
						response = BitConverter.GetBytes(fval);

						break;
					}
					case 5: // string
					{
						String str = fval.toString();
						
						responseLength =  (byte) str.length();
						response = str.getBytes();

						break;
					}
				}
			}
			
			// if the response type is bool, byte, short, int, or long.. convert to long
			if (responseLength == 0)
			{
				Long l = 0L;
				
				if (val instanceof Boolean)
				{
					Boolean v = (Boolean) val;
					
					if (v)
						l = 1L;
					
					responsestr = v.toString();
				}
				else if (val instanceof Byte)
				{
					Byte v = (Byte) val;

					l = v.longValue();

					responsestr = v.toString();
				}
				else if (val instanceof Short)
				{
					Short v = (Short) val;
					
					l = v.longValue();
					
					responsestr = v.toString();
				}
				else if (val instanceof Integer)
				{
					Integer v = (Integer) val;

					l = v.longValue();
					
					responsestr = v.toString();
				}
				else if (val instanceof Long)
				{
					l = (Long) val;
					
					responsestr = l.toString();
				}
				
				switch (dataType)
				{
					case 0: // boolean
					{
						responseLength = 1;
						response = new byte[1];
						
						if (l == 0L)
							response[0] = 0;
						else
							response[0] = 1;
						
						break;
					}
					case 1: // byte
					{
						Byte b = l.byteValue();
						
						responseLength = 1;
						response = new byte[1];
						response[0] = (byte) b;
						
						break;
					}
					case 2: // short
					{
						Short b = l.shortValue();
						
						responseLength = 2;
						response = BitConverter.GetBytes(b);
						
						break;
					}
					case 3: // integer
					{
						Integer b = l.intValue();
						
						responseLength = 4;
						response = BitConverter.GetBytes(b);
						
						break;
					}
					case 4: // float
					{
						Float b = l.floatValue();
						
						responseLength = 4;
						response = BitConverter.GetBytes(b);
						
						break;
					}
					case 5: // string
					{
						String str = l.toString();
						
						responseLength =  (byte) str.length();
						response = str.getBytes();
						
						break;
					}
				}
			}
			
			if (m_verbose)
				fireCommandUpdate(null, "Response   " + responsestr, CommandMessageTypeEnum.Response);
			else
				fireCommandUpdate(responsestr, CommandMessageTypeEnum.Response);
		}
		catch(Exception e)
		{
			if (dataType > 5) // not expecting a response
				return;

			// send not recognized response
			sendExecuteCommandResponse(responseToken, ResponseStatusEnum.RS_Failed.Value(), (byte) 0, (byte) 0, null);
			
			return;
		}

		sendExecuteCommandResponse(responseToken, ResponseStatusEnum.RS_Complete.Value(), dataType, responseLength, response);
	}

	public void onExecuteCommandResponse(byte[] data, int offset)
	{
		touch();
		
		//short responseToken = (short) (data[offset++] * 0x100 + data[offset++]);
		
		ResponseStatusEnum rstatus = ResponseStatusEnum.valueOf(data[offset++]);
		m_commandResponse.DataType = DataTypeEnum.valueOf(data[offset++]);
		
		m_commandResponse.DataLength = m_commandResponse.DataType.getLength();
//		m_commandResponse.DataLength = data[offset++];
		/*
		if (m_commandResponse.DataLength != 0)
		{
			// override the returned length
			switch(m_commandResponse.DataType)
			{
				case DT_Boolean:
					m_commandResponse.DataLength = 1;
					break;
				case DT_Byte:
					m_commandResponse.DataLength = 1;
					break;
				case DT_Short:
					m_commandResponse.DataLength = 2;
					break;
				case DT_Integer:
					m_commandResponse.DataLength = 4;
					break;
				case DT_Float:
					m_commandResponse.DataLength = 4;
					break;
			}
		}
		*/
		m_commandResponse.Data.clear();

		for (int j = 0; j < m_commandResponse.DataLength; j++)
		{
			m_commandResponse.Data.add(data[offset++]);
		}

		m_commandResponse.Status = rstatus;
		
		if (m_commandResponse.Status != ResponseStatusEnum.RS_Complete)
		{
			if (m_verbose)
				fireCommandUpdate("N: Response   " + m_commandResponse.Status.toString(), CommandMessageTypeEnum.Error);
			else
				fireCommandUpdate(m_commandResponse.Status.toString(), CommandMessageTypeEnum.Error);
		}
		else
		{
			Object val = m_commandResponse.getResponseValue();
			
			if (val == null)
			{
				if (m_verbose)
					fireCommandUpdate("N: Response   null", CommandMessageTypeEnum.Response);
				else
					fireCommandUpdate("null", CommandMessageTypeEnum.Response);					
			}
			else
			{
				if (m_verbose)
					fireCommandUpdate("N: Response   " + val.toString(), CommandMessageTypeEnum.Response);
				else
					fireCommandUpdate(val.toString(), CommandMessageTypeEnum.Response);
			}
		}
	}
	
	public void onGetAddress(byte[] data, int offset)
	{
		short responseToken = (short) (data[offset++] * 0x100 + data[offset++]);
		byte plength = data[offset++];

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < plength; i++)
		{
			sb.append((char) data[offset++]);
		}

		String path = sb.toString();
		
		if (m_verbose)
			fireCommandUpdate("N: GetAddress '" + path + "'", CommandMessageTypeEnum.IncomingCommand);
		
		String name;
		String pointName = null;
		AddressStatusEnum status = AddressStatusEnum.FormatError;
		NerduinoBase nerduino;
		short pointIndex = -1;
		Address address = new Address();

		// if the path includes a '.' delimiter then lookup an associated point
		if (path.contains("."))
		{
			int pos = path.indexOf(".");

			name = path.substring(0, pos);
			pointName = path.substring(pos + 1);
		}
		else
		{
			name = path;
		}		

		if (name != null)
		{
			// look for a nerduino with this name
			nerduino = NerduinoManager.Current.getNerduino(name);

			if (nerduino == null)
			{
				// if it does not exist, return an unknown address
				status = AddressStatusEnum.AddressUnknown;
				
				if (PointManager.Current != null)
				{
					// if not found, look for a local data point with this name
					LocalDataPoint point = PointManager.Current.getPoint(pointName);

					if (point != null)
					{
						//pointIndex = point.Id;
						pointIndex = point.Id;
						status = AddressStatusEnum.PointFound;
					}
				}
			}
			else
			{
				address = nerduino.m_address;

				// nerduino found, if the pointname is provided, lookup the point
				if (pointName != null)
				{
					PointBase point = nerduino.getPoint(pointName);

					if (point != null)
					{
						pointIndex = point.Id;
						status = AddressStatusEnum.PointFound;
					}
					else
					{
						// not found, so set the status to point not found
						status = AddressStatusEnum.PointUnknown;
					}
				}
				else
				{
					status = AddressStatusEnum.AddressFound;
				}
			}
		}

		sendGetAddressResponse(responseToken, status, address, pointIndex);
	}
	
	public void onLightDeclarePoint(byte[] data, int offset)
	{
		if (m_verbose)
			fireCommandUpdate("N: LightDeclarePoint", CommandMessageTypeEnum.IncomingCommand);
	}
	
	public void onLightRegisterPoint(byte[] data, int offset)
	{
		if (m_verbose)
			fireCommandUpdate("N: LightRegisterPoint", CommandMessageTypeEnum.IncomingCommand);
	}
	
	public void onLightSetProxyData(byte[] data, int offset)
	{
		if (m_verbose)
			fireCommandUpdate("N: LightSetProxyData", CommandMessageTypeEnum.IncomingCommand);
	}
	
	public void onLightGetProxyData(byte[] data, int offset)
	{
		if (m_verbose)
			fireCommandUpdate("N: LightGetProxyData", CommandMessageTypeEnum.IncomingCommand);
	}
	
	public void onLightSetPointValue(byte[] data, int offset)
	{
		if (m_verbose)
			fireCommandUpdate("N: LightSetPointValue", CommandMessageTypeEnum.IncomingCommand);
	}
	
	public void onLightRegisterAddress(byte[] data, int offset)
	{
		if (m_verbose)
			fireCommandUpdate("N: LightRegisterAddress", CommandMessageTypeEnum.IncomingCommand);
	}

	public void onGetPoint(NerduinoBase originator, byte[] data, int offset)
	{
	}
	
	public void onHostGetPoint(NerduinoBase originator, byte[] data, int offset)
	{
		if (PointManager.Current == null)
			return;
		
		
		int initialoffset = offset;
		short responseToken = (short) (data[offset++] * 0x100 + data[offset++]);

		PointIdentifierTypeEnum ptype = PointIdentifierTypeEnum.valueOf(data[offset++]);

		byte ilength = data[offset++];

		if (ptype == PointIdentifierTypeEnum.PIT_Name)
		{
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < ilength; i++)
			{
				sb.append((char) data[offset++]);
			}

			String name = sb.toString();

			if (m_verbose)
				fireCommandUpdate("N: GetPoint '" + name + "'", CommandMessageTypeEnum.IncomingCommand);
			
			int pos = name.indexOf(".");
			
			if (pos > 0)
			{
				String nerdName = name.substring(0, pos);
	
				// look up the nerd
				NerduinoBase nerd = NerduinoManager.Current.getNerduino(nerdName);
				
				if (nerd != null)
				{
					nerd.onGetPoint(originator, data, initialoffset);
				}
			}
			else
			{
				// look for a LDP with this name
				LocalDataPoint point = PointManager.Current.getPoint(name);
				
				sendGetPointResponse(responseToken, point);
			}
		}
	}
	
	public void onGetPointResponse(byte[] data, int offset)
	{
		try
		{
			touch();
			
			// skip response token
			offset +=2;

			// point index
			Short index = (short) (data[offset++] * 0x100 + data[offset++]);

			if (m_verbose)
				fireCommandUpdate("N: GetPointResponse " + index.toString(), CommandMessageTypeEnum.IncomingCommand);
			
			if (index == -1)
			{
				// remove points that were not validated
				for (Object obj : m_points.toArray())
				{
					RemoteDataPoint point = (RemoteDataPoint) obj;

					if (!point.Validated)
						m_points.remove(point);
				}

				m_pointCount = (short) m_points.size();

				m_receivedGetPoints = true;

				return;
			}

			// lookup this index, to see if this point is already known
			RemoteDataPoint point = (RemoteDataPoint) getPoint(index);

			if (point == null)
			{
				point = new RemoteDataPoint(this);

				m_points.add(point);
			}

			point.Validated = true;
			point.Id = index;
			point.Attributes = data[offset++];
			point.Status = data[offset++];

			byte slength = data[offset++];

			StringBuilder sb = new StringBuilder();

			for (int j = 0; j < slength; j++)
			{
				sb.append((char) data[offset++]);
			}

			point.setName(sb.toString());

			point.DataType = DataTypeEnum.valueOf(data[offset++]);

			switch(point.DataType)
			{
				case DT_Boolean:
				{
					point.DataLength = 1;
					offset++;

					boolean val = (data[offset++] != 0);

					point.m_value = val;

					break;
				}
				case DT_Byte:
				{
					point.DataLength = 1;
					offset++;
					point.m_value = data[offset++];

					break;
				}
				case DT_Short:
				{
					point.DataLength = 2;
					offset++;
					point.m_value = BitConverter.GetShort(data, offset);
					offset += 2;

					break;
				}
				case DT_Integer:
				{
					point.DataLength = 4;
					offset++;
					point.m_value = BitConverter.GetInt(data, offset);
					offset += 4;

					break;
				}
				case DT_Float:
				{
					point.DataLength = 4;
					offset++;
					point.m_value = BitConverter.GetFloat(data, offset);
					offset += 4;

					break;
				}
				/*
				case DT_String:
				{
					point.DataLength = data[offset++];

					sb = new StringBuilder();

					for (int j = 0; j < point.DataLength; j++)
					{
						sb.append((char) data[offset++]);
					}

					point.m_value = sb.toString();

					break;
				}
				*/
			}

			point.publish();
		}
		catch(Exception e)
		{
		}
	}
	
	public void onGetPointValue(NerduinoBase originator, byte[] data, int offset)
	{
	}
	
	public void onHostGetPointValue(NerduinoBase originator, byte[] data, int offset)
	{
		int initialoffset = offset;
		short responseToken = (short) (data[offset++] * 0x100 + data[offset++]);
		byte identifierType = data[offset++];
		
		LocalDataPoint point;
		
		if (identifierType == 0) // search by name
		{
			byte slength = data[offset++];
			
			StringBuilder sb = new StringBuilder();
			
			for (int j = 0; j < slength; j++)
			{
				sb.append((char) data[offset++]);
			}
			
			String pname = sb.toString();
		
			int pos = pname.indexOf(".");
			
			if (pos > 0)
			{
				String nerdName = pname.substring(0, pos);
	
				// look up the nerd
				NerduinoBase nerd = NerduinoManager.Current.getNerduino(nerdName);
				
				if (nerd != null)
				{
					nerd.onGetPointValue(originator, data, initialoffset);
					return;
				}
			}
			
			
			if (m_verbose)
				fireCommandUpdate("N: GetPointValue '" + pname + "'", CommandMessageTypeEnum.IncomingCommand);
		
			point = PointManager.Current.getPoint(pname);
		}
		else // search by index
		{
			Short index = (short) (data[offset++] * 0x100 + data[offset++]);
			
			if (m_verbose)
				fireCommandUpdate("N: GetPointValue " + index.toString(), CommandMessageTypeEnum.IncomingCommand);
			
			point = PointManager.Current.getPoint(index);
		}
		
		if (point != null)
			point.sendGetPointValueResponse(originator, responseToken);
		else
			// notify that the point was not found
			originator.sendGetPointValueResponse(responseToken, (short) -1, (byte) 2, DataTypeEnum.DT_Byte, data);
	}
	
	public void onGetPointValueResponse(byte[] data, int offset)
	{
		touch();
		
		offset += 2;
		
		Short index = (short) (data[offset++] * 0x100 + data[offset++]);
		
		// lookup this index, to see if this point is already known
		RemoteDataPoint point = (RemoteDataPoint) getPoint(index);
		
		if (point != null)
		{
			point.onGetPointValueResponse(data);
			
			if (m_verbose)
				fireCommandUpdate("N: GetPointValueResponse  " + index.toString() + " = " + point.getValue().toString(), CommandMessageTypeEnum.IncomingCommand);
		}
		else
		{
			if (m_verbose)
				fireCommandUpdate("N: GetPointValueResponse  Could not find point index " + index.toString(), CommandMessageTypeEnum.IncomingCommand);
		}
	}
	
	public void onRegisterPointCallback(NerduinoBase originator, byte[] data, int offset)
	{
		
	}
	
	public void onHostRegisterPointCallback(byte[] data, int offset)
	{
		short responseToken = (short) (data[offset++] * 0x100 + data[offset++]);

		byte registerFlag = data[offset++];
		byte idtype = data[offset++];
		LocalDataPoint point = null;
		
		switch(idtype)
		{
			case 0: // by name
				byte slength = data[offset++];

				StringBuilder sb = new StringBuilder();

				for(int i = 0; i < slength; i++)
				{
					sb.append((char) data[offset++]);
				}

				String pname = sb.toString();

				point = PointManager.Current.getPoint(pname);

				if (m_verbose)
					fireCommandUpdate("N: RegisterPointCallback  " + pname, CommandMessageTypeEnum.IncomingCommand);
				
				break;
			case 1: // by index
				Short index = (short) (data[offset++] *0x100 + data[offset++]);

				if (m_verbose)
					fireCommandUpdate("N: RegisterPointCallback  " + index.toString(), CommandMessageTypeEnum.IncomingCommand);
				
				// lookup this index, to see if this point is already known
				point = PointManager.Current.getPoint(index);
				
				break;
		}

		if (point != null)
		{
			if (registerFlag == 0)
				point.onRegisterPointCallback(this, data);
			else
				point.onUnregisterPointCallback(this);				
		}
		else
		{
			if (m_verbose)
				fireCommandUpdate("N: RegisterPointCallback  Point not found!", CommandMessageTypeEnum.Error);
		}
	}
	
	public void onPingResponse(byte[] data, int offset)
	{
		if (m_verbose)
			fireCommandUpdate("N: PingResponse", CommandMessageTypeEnum.IncomingCommand);
		
		touch();
		
		offset += 2; // skip response token
		
		setStatus(NerduinoStatusEnum.valueOf(data[offset++]));
		setDeviceType(DeviceTypeEnum.valueOf(data[offset++]));
		
		m_pinged = true;
	}

	public void onPing(NerduinoBase originator, byte[] data, int offset)
	{
		if (m_verbose)
			fireCommandUpdate("N: Ping", CommandMessageTypeEnum.IncomingCommand);
		
		short responseToken = (short) (data[offset++] * 0x100 + data[offset++]);
		
		originator.sendPingResponse(responseToken, m_status.Value(), (byte) 0);
	}
	
	public void onResetRequest()
	{
		if (m_verbose)
			fireCommandUpdate("N: ResetRequest", CommandMessageTypeEnum.IncomingCommand);
		
		// This message is sent to the Host or proxy to request the  
		// nerduino to be reset.  There’s no known way to soft reset from 
		// within Arduino, so this is a request to reset externally.
		
		reset();
	}

	public void onSetPointValue(byte[] data, int offset)
	{
	}	
	
	public void onHostSetPointValue(byte[] data, int offset)
	{
		int initialoffset = offset;
		
		byte identifierType = data[offset++];
		
		LocalDataPoint point = null;
		String pid = "";
		
		if (identifierType == 0) // search by name
		{
			byte slength = data[offset++];
			
			StringBuilder sb = new StringBuilder();
			
			for (int j = 0; j < slength; j++)
			{
				sb.append((char) data[offset++]);
			}
			
			String pname = sb.toString();
			
			int pos = pname.indexOf(".");
			
			if (pos > 0)
			{
				String nerdName = pname.substring(0, pos);
	
				// look up the nerd
				NerduinoBase nerd = NerduinoManager.Current.getNerduino(nerdName);
				
				if (nerd != null)
				{
					nerd.onSetPointValue(data, initialoffset);
					return;
				}
			}
			
			pid = pname;

			point = PointManager.Current.getPoint(pname);
		}
		else // search by index
		{
			if (PointManager.Current != null)
			{
				Short index = (short) (data[offset++] * 0x100 + data[offset++]);
				pid = index.toString();

				point = PointManager.Current.getPoint(index);
			}
		}
		
		if (point != null)
		{
			DataTypeEnum dataType = DataTypeEnum.valueOf(data[offset++]);
			byte datalength = dataType.getLength();

			byte[] bytes = new byte[datalength];

			System.arraycopy(data, offset, bytes, 0, datalength);
			
			Object value = null;
			
			switch(dataType)
			{
				case DT_Boolean:
					value = (bytes[0] != 0);
					break;
				case DT_Byte:
					value = bytes[0];
					break;
				case DT_Short:
					value = BitConverter.GetShort(bytes);
					break;
				case DT_Integer:
					value = BitConverter.GetInt(bytes);
					break;
				case DT_Float:
					value = BitConverter.GetFloat(bytes, 0);
					break;
			}
			
			point.setValue(value);
			
			if (m_verbose && value != null)
				fireCommandUpdate("N: SetPointValue  " + pid + " = " + value.toString(), CommandMessageTypeEnum.IncomingCommand);
		}
		else
		{
			if (m_verbose)
				fireCommandUpdate("N: SetPointValue  " + pid + "   Point not found!", CommandMessageTypeEnum.Error);
		}
	}
	
	public void sendCheckin(NerduinoBase requestedBy)
	{
		if (m_verbose)
			fireCommandUpdate(requestedBy, "Checkin", CommandMessageTypeEnum.OutgoingCommand);
	}
	
	public boolean sendPing(NerduinoBase requestedBy, short responseToken)
    {
		if (m_verbose)
			fireCommandUpdate(requestedBy, "Ping", CommandMessageTypeEnum.OutgoingCommand);
		
		return false;
	}
    
	public void sendPingResponse(short responseToken, byte status, byte configurationToken)
    {
		if (m_verbose)
			fireCommandUpdate(null, "PingResponse", CommandMessageTypeEnum.OutgoingCommand);
	}
    
	public CommandResponse sendCommand(String command)
	{
		return sendCommand(command, DataTypeEnum.DT_None);
	}
	
	public CommandResponse sendCommand(String command, DataTypeEnum datatype)
	{
		return sendExecuteCommand(null, (short) 1, datatype.Value(), (byte) command.length(), command.getBytes());
	}
	
	public CommandResponse sendExecuteCommand(NerduinoBase requestedBy, short responseToken, byte responseDataType, byte length, byte[] command)
	{
		fireCommandUpdate(requestedBy, "ExecuteCommand", CommandMessageTypeEnum.OutgoingCommand);
		
		return null;
	}
	
	public void sendExecuteCommandResponse(short responseToken, byte status, byte dataType, byte length, byte[] response)
	{
		fireCommandUpdate(null, "ExecuteCommandResponse", CommandMessageTypeEnum.OutgoingCommand);
	}
	
	public void sendGetPoints(NerduinoBase requestedBy, short responseToken)
	{
		if (m_verbose)
			fireCommandUpdate(requestedBy, "GetPoints", CommandMessageTypeEnum.OutgoingCommand);
	}

    public void sendGetPoint(NerduinoBase requestedBy, short responseToken, short index)
    {
		if (m_verbose)
			fireCommandUpdate(requestedBy, "GetPoint", CommandMessageTypeEnum.OutgoingCommand);
    }
    
    public void sendGetPoint(NerduinoBase requestedBy, short responseToken, String name)
    {
		if (m_verbose)
			fireCommandUpdate(requestedBy, "GetPoint", CommandMessageTypeEnum.OutgoingCommand);
    }

	public void sendGetPoint(NerduinoBase requestedBy, short responseToken, byte idtype, byte idlength, byte[] id)
	{
		if (m_verbose)
			fireCommandUpdate(requestedBy, "GetPoint", CommandMessageTypeEnum.OutgoingCommand);
	}

	
	public void sendGetPointResponse(short responseToken, LocalDataPoint point)
	{
		if (m_verbose)
			fireCommandUpdate(null, "GetPointResponse", CommandMessageTypeEnum.OutgoingCommand);
	}
    
	
    public void sendGetPointValue(NerduinoBase requestedBy, short responseToken, short index)
    {
		if (m_verbose)	
			fireCommandUpdate(requestedBy, "GetPointValue", CommandMessageTypeEnum.OutgoingCommand);
    }
	
	public void sendGetPointValue(NerduinoBase requestedBy, short responseToken, String name)
	{
		if (m_verbose)
			fireCommandUpdate(requestedBy, "GetPointValue", CommandMessageTypeEnum.OutgoingCommand);
	}
	
	public void sendGetPointValueResponse(short responseToken, short id, byte status, 
					DataTypeEnum dataType, byte[] value)
	{
		if (m_verbose)
			fireCommandUpdate(null, "GetPointValueResponse", CommandMessageTypeEnum.OutgoingCommand);
	}
	
	public void sendSetPointValue(NerduinoBase requestedBy, short index, boolean value)
	{
		sendSetPointValue(requestedBy, index, DataTypeEnum.DT_Boolean, value);
	}
    
    public void sendSetPointValue(NerduinoBase requestedBy, short index, byte value)
    {
		sendSetPointValue(requestedBy, index, DataTypeEnum.DT_Byte, value);
    }

    public void sendSetPointValue(NerduinoBase requestedBy, short index, short value)
    {
		sendSetPointValue(requestedBy, index, DataTypeEnum.DT_Short, value);
    }

    public void sendSetPointValue(NerduinoBase requestedBy, short index, int value)
    {
		sendSetPointValue(requestedBy, index, DataTypeEnum.DT_Integer, value);
    }
    
    public void sendSetPointValue(NerduinoBase requestedBy, short index, float value)
    {
		sendSetPointValue(requestedBy, index, DataTypeEnum.DT_Float, value);
    }
	
	public void sendSetPointValue(NerduinoBase requestedBy, String pointName, boolean value)
	{
		sendSetPointValue(requestedBy, pointName, DataTypeEnum.DT_Boolean, value);
	}
    
    public void sendSetPointValue(NerduinoBase requestedBy, String pointName, byte value)
    {
		sendSetPointValue(requestedBy, pointName, DataTypeEnum.DT_Byte, value);
    }

    public void sendSetPointValue(NerduinoBase requestedBy, String pointName, short value)
    {
		sendSetPointValue(requestedBy, pointName, DataTypeEnum.DT_Short, value);
    }

    public void sendSetPointValue(NerduinoBase requestedBy, String pointName, int value)
    {
		sendSetPointValue(requestedBy, pointName, DataTypeEnum.DT_Integer, value);
    }
    
    public void sendSetPointValue(NerduinoBase requestedBy, String pointName, float value)
    {
		sendSetPointValue(requestedBy, pointName, DataTypeEnum.DT_Float, value);
    }

	public void sendSetPointValue(NerduinoBase requestedBy, short index, DataTypeEnum dataType, Object m_value)
	{
		if (m_verbose)
			fireCommandUpdate(requestedBy, "SetPointValue", CommandMessageTypeEnum.OutgoingCommand);
	}
	
	public void sendSetPointValue(NerduinoBase requestedBy, String pointName, DataTypeEnum dataType, Object m_value)
	{	
		if (m_verbose)
			fireCommandUpdate(requestedBy, "SetPointValue", CommandMessageTypeEnum.OutgoingCommand);
	}
	
	public void sendRegisterPointCallback(NerduinoBase requestedBy, byte addRemove, short responseToken, short index, byte filterType, byte filterLength, byte[] filterValue) 
	{
		if (m_verbose)
			fireCommandUpdate(requestedBy, "RegisterPointCallback", CommandMessageTypeEnum.OutgoingCommand);
	}
	
	public void sendGetAddressResponse(short responseToken, AddressStatusEnum status, Address address, short pointIndex)
	{
		if (m_verbose)
			fireCommandUpdate(null, "GetAddressResponse", CommandMessageTypeEnum.OutgoingCommand);
	}
	
	@Override
	public Action[] getActions(boolean context)
	{
		// A list of actions for this node
		return new Action[]
				{
					new TreeNodeAction(getLookup()),
					new NerduinoBase.DashboardAction(getLookup()),
					new NerduinoBase.RenameAction(getLookup()),
					new NerduinoBase.DeleteAction(getLookup()),
				};
	}
	
	public boolean getEngaged()
	{
		return m_engaged;
	}
	
	public boolean getEngaging()
	{
		return m_engaging;
	}
	
	public void test()
	{
	}
	
	public void process()
	{
		switch (m_state)
		{
			case 0: // PROCESS_IDLE
				return;
			case 1: // PROCESS_CHECKIN
				if (getActive())
				{
					// request meta data
					m_checkedIn = false;
					sendCheckin(null);

					m_state = 2;
					m_startTime = System.currentTimeMillis();
				}
				break;
			case 2: // waiting for checkin response
			{
				long dt = System.currentTimeMillis() - m_startTime;
				
				if (m_checkedIn)
				{
					sendGetPoints(null, (short) 0);
					m_startTime = System.currentTimeMillis();
					m_state = 3;
				}
				else if (dt > 2000) // timeout
				{
					m_state = 1;
				}
			}
				break;
			case 3: // waiting for points
			{
				long dt = System.currentTimeMillis() - m_startTime;
				
				if (m_receivedGetPoints)
				{
					getPoints();
					m_startTime = System.currentTimeMillis();
					m_state = 4;
				}
				else if (dt > 2000) // timeout
				{
					m_state = 1;
				}
			}
				break;
			case 4: // waiting for last point to be reported
			{
				long dt = System.currentTimeMillis() - m_startTime;
				
				if (m_receivedGetPoints)
				{
					m_state = 0;
					
					// notify that the nerduino is fully engaged
					fireEngageStatusUpdate(false, true, 0, null);
				}
				else if (dt > 2000) // timeout
				{
					m_state = 1;
				}
				
			}
				break;
		}
	}
	
	public final class RenameAction extends AbstractAction
	{
		private final NerduinoBase node;

		public RenameAction(Lookup lookup)
		{
			node = lookup.lookup(NerduinoBase.class);

			putValue(AbstractAction.NAME, "Rename");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (node != null)
			{
				try
				{
					node.rename();
				}
				catch(Exception ex)
				{
					//Exceptions.printStackTrace(ex);
				}
			}
		}
	}
	
	public final class DashboardAction extends AbstractAction
	{
		private final NerduinoBase node;

		public DashboardAction(Lookup lookup)
		{
			node = lookup.lookup(NerduinoBase.class);
			
			putValue(AbstractAction.NAME, "Dashboard");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (node != null)
			{
				try
				{
					node.showDashboard();
				}
				catch(Exception ex)
				{
					//Exceptions.printStackTrace(ex);
				}
			}
		}
	}
		
	public final class DeleteAction extends AbstractAction
	{
		private final NerduinoBase node;

		public DeleteAction(Lookup lookup)
		{
			node = lookup.lookup(NerduinoBase.class);

			putValue(AbstractAction.NAME, "Delete");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (node != null)
			{
				try
				{
					node.delete();
				}
				catch(Exception ex)
				{
					//Exceptions.printStackTrace(ex);
				}
			}
		}
	}
	
	public void showDashboard()
	{
		if (m_dashboard == null)
		{
			m_dashboard = new NerduinoDashboardTopComponent();
			m_dashboard.setNerduino(this);
			m_dashboard.open();
		}

		if (m_dashboard != null)
		{
			if (!m_dashboard.isOpened())
				m_dashboard.open();		
			
			m_dashboard.requestActive();
			
			// repeated on purpose to assert the display name
			m_dashboard.requestActive();
		}
	}
	
	public String getHTML()
	{
		String htmlString = "<html>\n"
                          + "<body>\n"
                          + "<h1>Nerduino: " + this.getName() + "  (Base class)</h1>\n"
                          + "</body>\n";	
		
		return htmlString;
	}
	
	@Override
	public void rename()
	{
		String oldname = getName();
		
		String oldfilename = getFileName();
		
		String newname = JOptionPane.showInputDialog(null, "New Name:", oldname);
		
		if (!newname.matches(oldname))
		{
			String fullpath = NerduinoManager.Current.getFilePath() + "/" + newname + ".nerd";
			
			File newfile = new File(fullpath);
			
			if (newfile.exists())
			{
				JOptionPane.showMessageDialog(null, "This name already exists!");
				return;
			}
			
			File file = new File(oldfilename);
			
			file.renameTo(newfile);
			
			setName(newname);
			
			save();
		}
	}

	public void delete()
	{
		// prompt to verify deletion
		int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this nerduino?", "Delete Nerduino", JOptionPane.YES_NO_OPTION);
		
		if (response == JOptionPane.YES_OPTION)
		{
			try
			{				
				File file = new File(getFileName());
				
				if (file.exists())
					file.delete();
				
				destroy();
			}
			catch(IOException ex)
			{
			}
		}
	}
}
