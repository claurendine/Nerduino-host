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


import com.nerduino.nodes.TreeNode;
import com.nerduino.processing.app.Board;
import com.nerduino.processing.app.BoardManager;
import com.nerduino.processing.app.BuilderTopComponent;
import com.nerduino.processing.app.CompileCommand;
import com.nerduino.processing.app.IBuildTask;
import com.nerduino.processing.app.ICompileCallback;
import com.nerduino.processing.app.Preferences;
import com.nerduino.processing.app.Sketch;
import com.nerduino.processing.app.SketchManager;
import com.nerduino.services.ServiceManager;
import com.nerduino.xbee.BitConverter;
import com.nerduino.xbee.ZigbeeFrame;
import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.awt.Component;
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
    static ArrayList s_nerduinos = new ArrayList();
    static short s_nextRoutingIndex = 1;
	
	boolean m_receivedMetaData = false;
	boolean m_receivedGetPoint = false;
	boolean m_receivedGetPoints = false;
	boolean m_pinged = false;
	
	boolean m_loading = false;
	boolean m_interactive = true;
	String m_sketch;
	String m_boardType;
    NerduinoStatusEnum m_status = NerduinoStatusEnum.Uninitialized;
	byte m_configurationToken = 0;
	
	Address m_address;
	
	DeviceTypeEnum m_deviceType;
	short m_pointCount = 0;
	long m_lastResponseMillis;
	boolean m_engaged = false;
	boolean m_checkedIn = false;

	Context m_context;
	Scriptable m_scope;
	CommandResponse m_commandResponse = new CommandResponse();

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
	
	// Methods
	public Object executeScript(String script)
	{
		if (m_context == null)
		{
			m_context = Context.enter();
			m_scope = m_context.initStandardObjects();

			// load up all services
			if (ServiceManager.Current != null)
				ServiceManager.Current.applyServices(this);
		}
		
		return m_context.evaluateString(m_scope, script, "Script", 1, null );
	}
	
	
	CompileCommand compileButton;
	
	@Override
	public Component getAction1()
	{
		if (compileButton == null)
		{
			compileButton = new CompileCommand(this);
			compileButton.setEngaged(m_engaged);
		}
		
		return compileButton;
	}
	
	public void compile()
	{
		Board board = BoardManager.Current.getBoard(m_boardType);

		if (board != null)
		{
			Preferences.set("target", "arduino");
			Preferences.set("board", board.getShortName());

			Sketch sketch = SketchManager.Current.getSketch(m_sketch);

			if (sketch != null)
			{
				sketch.compile((ICompileCallback) this);
			}
		}
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
			
			//NerduinoTreeViewOld.Current.modelUpdated(this);
		}
    }
	
	public void touch()
	{
		m_lastResponseMillis = System.currentTimeMillis();
		
		if (m_status != NerduinoStatusEnum.Online)
			setStatus(NerduinoStatusEnum.Online);
	}

	public byte getConfigurationToken()
	{
		return m_configurationToken;
	}
	
	public void setConfigurationToken(byte token)
    {
    	if (m_configurationToken != token)
		{
			m_configurationToken = token;
			
			// request meta data
		}
    }
	
	public short getPointCount()
	{
		return m_pointCount;
	}
	
	public void setPointCount(short count)
	{
		if (m_pointCount != count)
		{
			m_pointCount = count;
			
			// query for point metadata
		}
	}
		
	public boolean validateName(String name)
	{
		if (!getName().equals(name))
		{
			sendSetName();
		}
		
		return true;
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
		Sketch sketch = SketchManager.Current.getSketch(m_sketch);
		
		if (sketch != null)
			return sketch.getTopComponent();
		
		return null;
	}
	
	public boolean getInteractive()
	{
		return m_interactive;
	}
	
	public void setInteractive(boolean value)
	{
		m_interactive = value;
		
		save();
	}
	
	public String getSketch()
	{
		return m_sketch;
	}
	
	public void setSketch(String value)
	{
		m_sketch = value;
		
		save();
		
		m_topComponent = null;
	}
	
	public String getBoardType()
	{
		return m_boardType;
	}
	
	public void setBoardType(String value)
	{
		m_boardType = value;
		
		save();
	}

	public DeviceTypeEnum getDeviceType()
	{
		return m_deviceType;
	}

	public void setDeviceType(DeviceTypeEnum deviceType)
	{
		m_deviceType = deviceType;
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
        	RemoteDataPoint point = (RemoteDataPoint) m_points.get(i);
        	
            if (point == null || !point.Validated)
                m_points.remove(i);
        }
    }
	
    public boolean getMetaData()
    {
		m_receivedMetaData = false;
		sendGetMetaData(null, (short) 0);
		
		float wait = 0.0f;
		
		while (!m_receivedMetaData && wait < 5.0f)
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
		
		return m_receivedMetaData;
    }

	
    public boolean ping()
    {
		m_pinged = false;
		sendPing(null, (short) 0);
		
		float wait = 0.0f;
		
		while (!m_pinged && wait < 5.0f)
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
		
		return m_pinged;
    }

	public void checkStatus() 
	{
		// abstract method to be overriden by derived classes
		
		// TODO see the NerduinoXbee implementation.. this may need to be the 
		// base implementation
	}

	
	@Override
	public void onSelected()
	{
		super.onSelected();
		
		if (BuilderTopComponent.Current != null)
			BuilderTopComponent.Current.setNerduino(this);
	}

	public String upload(Sketch sketch)
	{
		// abstract method to be overridden by derived classes
		return null;
	}	
	
	public String engage(IBuildTask task)
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
		touch();
		
		byte status = 0;
		byte configuration = 0;
		
		if (data != null)
		{
			status = data[offset++];
			configuration = data[offset++];
		}
		
		setStatus(NerduinoStatusEnum.valueOf(status));
		setConfigurationToken(configuration);
		
		m_checkedIn = true;
		
		// reset the context, in case the serial port has reset and 
		// is now receiving on a new thread
		m_context = null;
	}
	
	public void onExecuteCommand(byte[] data, int offset)
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
		
		try
		{
			if (m_context == null)
			{
				m_context = Context.enter();
				m_scope = m_context.initStandardObjects();

				// load up all services
				if (ServiceManager.Current != null)
					ServiceManager.Current.applyServices(this);
			}
			
			
			Object val = m_context.evaluateString(m_scope, command, "Script", 1, null);
			
			if (val instanceof Undefined)
			{
				sendExecuteCommandResponse(responseToken, ResponseStatusEnum.RS_UndefinedResponse.Value(), (byte) 0, (byte) 0, null);
				return;
			}
			
			// if the response type is string.. parse into the desired datatype
			if (val instanceof String)
			{
				String v = (String) val;
				
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
				{
					fval = (Float) val;
				}
				else if (val instanceof Double)
				{
					double d = (double) (Double) val;
					fval = (float) d;
				}
				
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
				}
				else if (val instanceof Byte)
				{
					Byte v = (Byte) val;

					l = v.longValue();				
				}
				else if (val instanceof Short)
				{
					Short v = (Short) val;
					
					l = v.longValue();
				}
				else if (val instanceof Integer)
				{
					Integer v = (Integer) val;

					l = v.longValue();
				}
				else if (val instanceof Long)
				{
					l = (Long) val;
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
		}
		catch(Exception e)
		{
			// send not recognized response
			sendExecuteCommandResponse(responseToken, ResponseStatusEnum.RS_Failed.Value(), (byte) 0, (byte) 0, null);
			
			return;
		}
		
		sendExecuteCommandResponse(responseToken, ResponseStatusEnum.RS_Complete.Value(), dataType, responseLength, response);
	}

	public void onExecuteCommandResponse(byte[] data, int offset)
	{
		touch();
		
		short responseToken = BitConverter.GetShort(data, offset);
		offset += 2;

		ResponseStatusEnum rstatus = ResponseStatusEnum.valueOf(data[offset++]);
		m_commandResponse.DataType = DataTypeEnum.valueOf(data[offset++]);
		m_commandResponse.DataLength = data[offset++];

		for (int j = 0; j < m_commandResponse.DataLength; j++)
		{
			m_commandResponse.Data.add(data[offset++]);
		}

		m_commandResponse.Status = rstatus;
	}
	
	public void onGetAddress(byte[] data, int offset)
	{
		short responseToken = BitConverter.GetShort(data, offset);
		offset += 2;

		byte plength = data[offset++];

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < plength; i++)
		{
			sb.append((char) data[offset++]);
		}

		String path = sb.toString();
		
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
				// if not found, look for a local data point with this name
				LocalDataPoint point = PointManager.Current.getPoint(pointName);

				if (point != null)
				{
					pointIndex = point.Id;
					status = AddressStatusEnum.PointFound;
				}
				else
				{
					// if it does not exist, return an unknown address
					status = AddressStatusEnum.AddressUnknown;
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
	
	public void onGetDeviceStatus(byte[] data, int offset)
	{
	}
	
	public void onLightDeclarePoint(byte[] data, int offset)
	{
	}
	
	public void onLightRegisterPoint(byte[] data, int offset)
	{
	}
	
	public void onLightSetProxyData(byte[] data, int offset)
	{
	}
	
	public void onLightGetProxyData(byte[] data, int offset)
	{
	}
	
	public void onLightSetPointValue(byte[] data, int offset)
	{
	}
	
	public void onLightRegisterAddress(byte[] data, int offset)
	{
	}

	/*
	public void onGetNamedMetaData(byte[] data, int offset)
	{
		short responseToken = BitConverter.GetShort(data, offset);
		offset += 2;

		byte slength = data[offset++];

		StringBuilder sb = new StringBuilder();

		for (int j = 0; j < slength; j++)
		{
			sb.append((char) data[offset++]);
		}

		String name = sb.toString();

		// store this name away to notify this nerd that the 
		// requested nerduino has restarted

		NerduinoBase nerd = NerduinoManager.Current.getNerduino(name);

		if (nerd != null)
		{
			// send nerd's metadata
			sendGetMetaDataResponse(nerd, responseToken);
		}
	}
	*/

	public void onGetMetaDataResponse(byte[] data, int offset)
	{
		touch();
		
		byte slength = data[offset++];

		StringBuilder sb = new StringBuilder();

		for (int j = 0; j < slength; j++)
		{
			sb.append((char) data[offset++]);
		}

		String name = sb.toString();

		setStatus(NerduinoStatusEnum.valueOf(data[offset++]));

		byte configurationToken = data[offset++];
		short pcount = BitConverter.GetShort(data, offset);
		offset += 2;

		setPointCount(pcount);

		setDeviceType(DeviceTypeEnum.valueOf(data[offset++]));
		setConfigurationToken(configurationToken);

		validateName(name);

		m_receivedMetaData = true;
	}
	
	public void onGetPoint(byte[] data, int offset)
	{
		short responseToken = BitConverter.GetShort(data, offset);
		offset += 2;

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

			// look for a LDP with this name
			LocalDataPoint point = PointManager.Current.getPoint(name);

			sendGetPointResponse(responseToken, point);
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
			short index = (short) (data[offset++] * 0x100 + data[offset++]);

			if (index == -1)
			{
				// remove points that were not validated
				for (Object obj : m_points.toArray())
				{
					RemoteDataPoint point = (RemoteDataPoint) obj;

					if (!point.Validated)
					{
						m_points.remove(point);
					}
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
	
	public void onGetPointValue(byte[] data, int offset)
	{
		short responseToken = BitConverter.GetShort(data, offset);
		offset += 2;

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

			point = PointManager.Current.getPoint(pname);
		}
		else // search by index
		{
			short index = BitConverter.GetShort(data, offset);

			point = PointManager.Current.getPoint(index);
		}

		if (point != null)
		{
			point.sendGetPointValueResponse(this, responseToken);
		}
		else
		{
			// notify that the point was not found
			sendGetPointValueResponse(responseToken, (short) -1, (byte) 2, DataTypeEnum.DT_Byte, (byte) 0, data);
		}
	}
	
	public void onGetPointValueResponse(byte[] data, int offset)
	{
		touch();
		
		//short responseToken = BitConverter.GetShort(data, offset);
		offset += 2;
		
		short index = BitConverter.GetShort(data, offset);
		offset += 2;
		
		// lookup this index, to see if this point is already known
		RemoteDataPoint point = (RemoteDataPoint) getPoint(index);
		
		if (point != null)
		{
			point.onGetPointValueResponse(data);
		}
	}
	
	public void onRegisterPointCallback(byte[] data, int offset)
	{
		short responseToken = BitConverter.GetShort(data, offset);
		offset += 2;

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

				break;
			case 1: // by index
				short index = BitConverter.GetShort(data, offset);
				offset += 2;

				// lookup this index, to see if this point is already known
				point = PointManager.Current.getPoint(index);

				break;
		}

		if (point != null)
		{
			point.onRegisterPointCallback(this, data);
		}
	}
	
	public void onUnregisterPointCallback(byte[] data, int offset)
	{
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

				break;
			case 1: // by index
				short index = BitConverter.GetShort(data, offset);
				offset += 2;

				// lookup this index, to see if this point is already known
				point = PointManager.Current.getPoint(index);

				break;
		}

		if (point != null)
		{
			point.onUnregisterPointCallback(this);
		}
	}
	
	public void onPingResponse(byte[] data, int offset)
	{
		touch();
		
		offset += 2; // skip response token
		
		setStatus(NerduinoStatusEnum.valueOf(data[offset++]));
		setConfigurationToken(data[offset++]);
		
		m_pinged = true;
	}
	
	public void onResetRequest()
	{
		// This message is sent to the Host or proxy to request the  
		// nerduino to be reset.  There’s no known way to soft reset from 
		// within Arduino, so this is a request to reset externally.
	}
	
	public void onSetPointValue(byte[] data, int offset)
	{
		
	}
	
	public void sendPing(NerduinoBase requestedBy, short responseToken)
    {
	}
    
	public void sendPingResponse(short responseToken, byte status, byte configurationToken)
    {
	}
    
    public void sendGetMetaData(NerduinoBase requestedBy, short responseToken)
    {
    }
    
    public void sendGetMetaDataResponse(NerduinoBase nerduino, short responseToken)
    {
    }
    
	public void sendSetName()
	{	
	}
	
	public CommandResponse sendExecuteCommand(NerduinoBase requestedBy, short responseToken, byte responseDataType, byte length, byte[] command)
	{
		return null;
	}
	
	public void sendExecuteCommandResponse(short responseToken, byte status, byte dataType, byte length, byte[] response)
	{
	}
	
	public void sendGetPoints(NerduinoBase requestedBy, short responseToken)
	{
	}

    public void sendGetPoint(NerduinoBase requestedBy, short responseToken, short index)
    {
    }
    
    public void sendGetPoint(NerduinoBase requestedBy, short responseToken, String name)
    {
    }

	public void sendGetPoint(NerduinoBase requestedBy, short responseToken, byte idtype, byte idlength, byte[] id)
	{
	}

	
	public void sendGetPointResponse(short responseToken, LocalDataPoint point)
	{
	}
    
	
    public void sendGetPointValue(NerduinoBase requestedBy, short responseToken, short index)
    {
    }
	
	public void sendGetPointValue(NerduinoBase requestedBy, short responseToken, String name)
	{
	}
	
	public void sendGetPointValueResponse(short responseToken, short id, byte status, 
					DataTypeEnum dataType, byte dataLength, byte[] value)
	{
	}
	
	public void sendSetPointValue(short index, boolean value)
	{
		sendSetPointValue(index, DataTypeEnum.DT_Boolean, (byte) 1, value);
	}
    
    public void sendSetPointValue(short index, byte value)
    {
		sendSetPointValue(index, DataTypeEnum.DT_Byte, (byte) 1, value);
    }

    public void sendSetPointValue(short index, short value)
    {
		sendSetPointValue(index, DataTypeEnum.DT_Short, (byte) 2, value);
    }

    public void sendSetPointValue(short index, int value)
    {
		sendSetPointValue(index, DataTypeEnum.DT_Integer, (byte) 4, value);
    }
    
    public void sendSetPointValue(short index, float value)
    {
		sendSetPointValue(index, DataTypeEnum.DT_Float, (byte) 4, value);
    }

	/*
    public void sendSetPointValue(short index, byte[] value)
    {
		sendSetPointValue(index, DataTypeEnum.DT_Array, (byte) value.length, value);
    }

    public void sendSetPointValue(short index, String value)
    {
		sendSetPointValue(index, DataTypeEnum.DT_String, (byte) value.length(), value);
    }
	*/
	
	public void sendSetPointValue(short index, DataTypeEnum dataType, byte dataLength, Object m_value)
	{	
	}
	
	public void sendRegisterPointCallback(NerduinoBase requestedBy, short responseToken, short index, byte filterType, byte filterLength, byte[] filterValue) 
	{	
	}
	
    public void sendUnregisterPointCallback(NerduinoBase requestedBy, short index)
    {
    }
	
	public void sendGetAddressResponse(short responseToken, AddressStatusEnum status, Address address, short pointIndex)
	{
	}
	
	public void sendGetDeviceStatusResponse(long serialNumber, short networkAddress, short responseToken)
	{
	}

	public void sendFrame(ZigbeeFrame frame)
	{
	}
	
	@Override
	public Action[] getActions(boolean context)
	{
		// A list of actions for this node
		return new Action[]
				{
					new TreeNodeAction(getLookup()),
					new NerduinoBase.RenameAction(getLookup()),
					new NerduinoBase.DeleteAction(getLookup()),
				};
	}
	
	public final class RenameAction extends AbstractAction
	{
		private NerduinoBase node;

		public RenameAction(Lookup lookup)
		{
			node = lookup.lookup(NerduinoBase.class);

			putValue(AbstractAction.NAME, "Rename Nerduino");
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
		
	public final class DeleteAction extends AbstractAction
	{
		private NerduinoBase node;

		public DeleteAction(Lookup lookup)
		{
			node = lookup.lookup(NerduinoBase.class);

			putValue(AbstractAction.NAME, "Delete Nerduino");
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
				{
					file.delete();
				}
				
				destroy();
			}
			catch(IOException ex)
			{
			}
		}
	}
}
