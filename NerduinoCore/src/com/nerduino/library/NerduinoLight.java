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

import com.nerduino.processing.app.IBuildTask;
import com.nerduino.xbee.BitConverter;
import java.util.ArrayList;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;

public class NerduinoLight extends NerduinoFull
{
	ArrayList<LocalDataPoint> m_localDataPoints = new ArrayList<LocalDataPoint>();
	ArrayList<RemoteDataPoint> m_remoteDataPoints = new ArrayList<RemoteDataPoint>();
	ArrayList<PointBase> m_points = new ArrayList<PointBase>();
	
	byte m_nextPointID = 0;
	
	public NerduinoLight(String name, String icon)
	{
		super(name, icon);
	}

	@Override
	public void sendPing(NerduinoBase requestedBy, short responseToken)
	{
		m_pinged = false;
		
		// if the device has checked in then ping the light client and respond
		// on behalf of the client

		if (m_checkedIn)
		{
			// TODO if the serial port is still active then report a ping response
			if (m_serialBase.getEnabled())
			{
				m_pinged = false;
				
				byte[] data = new byte[2];
		
				data[0] = (byte) (responseToken / 0x100);
				data[1] = (byte) (responseToken & 0xff);

				sendMessage(MessageEnum.MSG_Ping.Value(), (byte) 2, data);
				
				// wait for ping response
				float wait = 0.0f;
				
				while (!m_pinged && wait < 5.0f)
				{
					try
					{
						Thread.sleep(20);
						wait += 0.02f;
					}
					catch(InterruptedException ex)
					{
						Exceptions.printStackTrace(ex);
					}
				}
				
				if (m_pinged && requestedBy != null)
				{
					// ping originated from another nerduino.. so respond 
					requestedBy.sendPingResponse(responseToken, m_status.Value(), m_configurationToken);
				}
			}
		}
	}
	
	@Override
	public void sendPingResponse(short responseToken, byte status, byte configurationToken)
	{
		// notify the client of the response
		byte[] data = new byte[4];
		
		data[0] = (byte) (responseToken / 0x100);
		data[1] = (byte) (responseToken & 0xff);
		data[2] = status;
		data[3] = configurationToken;
		
		sendMessage(MessageEnum.MSG_PingResponse.Value(), (byte) 4, data);
	}
	
	@Override
	public void sendGetMetaData(NerduinoBase requestedBy, short responseToken)
	{
		m_receivedMetaData = true;

		if (requestedBy != null)
		{
			// originated from another nerduino.. so respond 
			requestedBy.sendGetMetaDataResponse(this, responseToken);
		}
	}
	
	@Override
    public void sendGetMetaDataResponse(NerduinoBase nerduino, short responseToken)
    {
		//nothing to do.. light client should not have requested this
    }


	@Override
	public void sendSetName()
	{
		// store metadata, do not froward to the client
		// nothing todo?  name changes are already handled
	}

	@Override
	public CommandResponse sendExecuteCommand(NerduinoBase requestedBy, short responseToken, byte responseDataType, byte commandlength, byte[] command)
	{
		// send the light format
		byte length = (byte) (3 + commandlength);
		
		byte[] data = new byte[length];
		
		data[0] = (byte) (responseToken / 0x100);
		data[1] = (byte) (responseToken & 0xff);
		data[2] = commandlength;
		
		System.arraycopy(command, 0, data, 3, commandlength);
		
		sendMessage(MessageEnum.MSG_ExecuteCommand.Value(), length, data);
		
		if (responseDataType >= 0 && responseDataType < 5)
		{
			m_commandResponse.Status = ResponseStatusEnum.RS_PartialResult;
			m_commandResponse.Data.clear();

			float wait = 0.0f;

			while (m_commandResponse.Status == ResponseStatusEnum.RS_PartialResult
					&& wait < 5.0f)
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

			if (m_commandResponse.Status == ResponseStatusEnum.RS_PartialResult)
			{
				m_commandResponse.Status = ResponseStatusEnum.RS_Timeout;
			}

			return m_commandResponse;	
		}
		
		return null;
	}

	@Override
	public void sendGetPoints(NerduinoBase requestedBy, short responseToken)
	{
		// no need to ask the nerduino for the points because they are managed
		// by this class
		m_receivedGetPoints = true;
		
		if (requestedBy != null)
		{
			for(int i = 0; i < m_localDataPoints.size(); i++)
			{
				LocalDataPoint ldp = m_localDataPoints.get(i);
				
				requestedBy.sendGetPointResponse(responseToken, ldp);
			}
			
			// done
			requestedBy.sendGetPointResponse(responseToken, null);
		}
	}

	@Override
	public void sendGetPoint(NerduinoBase requestedBy, short responseToken, String name)
	{
		// provide response on behalf of the client

		for(int i = 0; i < m_localDataPoints.size(); i++)
		{
			LocalDataPoint ldp = m_localDataPoints.get(i);
			
			if (ldp.getName().equals(name))
			{
				if (requestedBy != null)
					requestedBy.sendGetPointResponse(responseToken, ldp);
				
				return;
			}
		}
		
		// not found
		requestedBy.sendGetPointResponse(responseToken, null);
	}

	@Override
	public void sendGetPointValue(NerduinoBase requestedBy, short responseToken, short id)
	{
		// provide response on behalf of the client
		if (requestedBy != null)
		{
			for(int i = 0; i < m_localDataPoints.size(); i++)
			{
				LocalDataPoint ldp = m_localDataPoints.get(i);

				if (ldp.Id == id)
				{
					if (requestedBy != null)
						requestedBy.sendGetPointValueResponse(responseToken, ldp.Id, ldp.Status, ldp.DataType, ldp.DataLength, ldp.getBytes());
					
					return;
				}
			}

			// not found
			requestedBy.sendGetPointResponse(responseToken, null);
		}
	}
	
	@Override
	public void sendGetPointValue(NerduinoBase requestedBy, short responseToken, String name)
	{
		// provide response on behalf of the client
		if (requestedBy != null)
		{
			for(int i = 0; i < m_localDataPoints.size(); i++)
			{
				LocalDataPoint ldp = m_localDataPoints.get(i);

				if (ldp.getName().equals(name))
				{
					if (requestedBy != null)
						requestedBy.sendGetPointValueResponse(responseToken, ldp.Id, ldp.Status, ldp.DataType, ldp.DataLength, ldp.getBytes());
					
					return;
				}
			}

			// not found
			requestedBy.sendGetPointResponse(responseToken, null);
		}
	}
	
	@Override
	public void sendGetPointValueResponse(short responseToken, short id, byte status, DataTypeEnum dataType, byte dataLength, byte[] value)
	{
		// validate that the id is the right range
		// validate the data type / data length
		
		// forward the value change to the client
		
		byte length = (byte) (2 + dataLength);
		
		byte[] data = new byte[length];
		
		data[0] = (byte) (id / 0x100);
		data[1] = (byte) (id & 0xff);
		
		System.arraycopy(value, 0, data, 2, dataLength);
		
		// TODO validate message format
		sendMessage(MessageEnum.LMSG_SetPointValue.Value(), length, data);
	}

	@Override
	public void sendSetPointValue(short index, DataTypeEnum dataType, byte dataLength, Object value)
	{
		// validate that the index is the right range
		// validate the data type / data length
		
		// cache the value
		// forward the value change to the client
		byte[] bytes = null;
		
		switch(dataType)
		{
			case DT_Boolean:
				bytes = BitConverter.GetBytes((Boolean) value);
				break;
			case DT_Byte:
				bytes = BitConverter.GetBytes((Byte) value);
				break;
			case DT_Short:
				bytes = BitConverter.GetBytes((Short) value);
				break;
			case DT_Integer:
				bytes = BitConverter.GetBytes((Integer) value);
				break;
			case DT_Float:
				bytes = BitConverter.GetBytes((Float) value);
				break;
		}
		
		byte length = (byte) (2 + dataLength);
		
		byte[] data = new byte[length];
		
		data[0] = (byte) (index / 0x100);
		data[1] = (byte) (index & 0xff);
		
		System.arraycopy(bytes, 0, data, 2, dataLength);
		
		// TODO validate message format
		sendMessage(MessageEnum.LMSG_SetPointValue.Value(), length, data);

	}

	@Override
	public void sendRegisterPointCallback(NerduinoBase requestedBy, short responseToken, short index, byte filterType, byte filterLength, byte[] filterValue)
	{
		// perform this on behalf of the client
		
		// lookup the data point associated with the requested index
		for(int i = 0; i < m_localDataPoints.size(); i++)
		{
			LocalDataPoint ldp = m_localDataPoints.get(i);
			
			if (ldp.Id == index)
			{
				ldp.onRegisterPointCallback(requestedBy, responseToken, filterType, filterLength, filterValue);
			}
		}
	}

	@Override
	public void sendGetMetaDataResponse(long address, short networkAddress, short responseToken, NerduinoBase nerduino)
	{
		// perform this on behalf of the client?
		// TODO
	}
	
	@Override
	public void sendGetPointResponse(short responseToken, LocalDataPoint point)
	{
		// store response and forward the value change to the client
		// TODO
	}

	@Override
	public void sendUnregisterPointCallback(NerduinoBase requestedBy, short index)
	{
		// perform this on behalf of the client
		// lookup the data point associated with the requested index
		for(int i = 0; i < m_localDataPoints.size(); i++)
		{
			LocalDataPoint ldp = m_localDataPoints.get(i);
			
			if (ldp.Id == index)
			{
				ldp.onUnregisterPointCallback(requestedBy);
			}
		}
	}
	
	@Override
	public void sendGetAddressResponse(short responseToken, AddressStatusEnum status, Address address, short pointIndex)
	{
		// send the light message to the client
		
	}

	@Override
	public void sendGetDeviceStatusResponse(long serialNumber, short networkAddress, short responseToken)
	{
		//NerduinoHost.Current.sendGetDeviceStatusResponse(serialNumber, networkAddress, responseToken, m_signalStrength, m_configurationToken, (short) getTimeSinceLastResponse());
	}
	
	@Override
	public void sendExecuteCommandResponse(short responseToken, byte status, byte dataType, byte responselength, byte[] response)
	{
		// send the light message to the client
		byte length = (byte) (responselength + 5);
		
		byte[] data = new byte[length];
		
		data[0] = (byte) (responseToken / 0x100);
		data[1] = (byte) (responseToken & 0xff);
		data[2] = status;
		data[3] = dataType;
		data[4] = responselength;
		
		if (response != null)
			System.arraycopy(response, 0, data, 5, responselength);
		
		// TODO validate message format
		sendMessage(MessageEnum.MSG_ExecuteCommandResponse.Value(), length, data);
	}
	
	
	public void sendMessage(byte message, byte length, byte[] data)
	{
		
	}
		
	@Override
	public void onPingResponse(byte[] data, int offset)
	{
		touch();
		
		m_pinged = true;
	}
	
	public void onDeclarePoint(byte datatype, byte readonly, byte publish, String pointname, Object value)
	{
		// TODO create a new point and add to the light client local data 
		// point collection
		
		LocalDataPoint ldp = new LocalDataPoint();
		ldp.Id = m_nextPointID++;
		ldp.setDataType(DataTypeEnum.valueOf(datatype));
		ldp.setName(pointname);
		ldp.Attributes = readonly;
		ldp.m_nerduino = this;
		ldp.setValue(value);
		

		m_localDataPoints.add(ldp);
		m_pointCount = (short) m_localDataPoints.size();
		
		m_points.add(ldp);
		
		if (publish != 0)
		{
			// TODO if the point is marked for publish then publish the point 
			// with the host
			String name = getName() + "_" + pointname;
			
			// look for an existing LDP with this name
			LocalDataPoint proxy = PointManager.Current.getPoint(name);
			LocalDataPoint newpoint = null;
			
			if (proxy == null)
			{
				// create a local data point that is a proxy to this remote data point
				proxy = new LocalDataPoint();
				
				proxy.setName(name);
				
				newpoint = proxy;
			}

			proxy.Proxy = ldp;
			proxy.Attributes = ldp.Attributes;
			proxy.DataLength = ldp.DataLength;
			proxy.DataType = ldp.DataType;
			proxy.Id = ldp.Id;
			proxy.setValue(value);
			
			if (newpoint != null)
				PointManager.Current.addChild(newpoint);
		}
	}
	
	@Override
	public void onRegisterPoint(byte datatype, String pointpath)
	{
		// TODO create a new remote point and add to the light client remote data 
		// point collection		
	}
	
	@Override
	public void onLightSetPointValue(byte[] data, int offset)
	{
		short pointindex = (short) ((int) data[offset++] * 0x100 + (int) data[offset++]);
		byte datalength = data[offset++];
		
		if (pointindex >= 0 && pointindex < m_nextPointID)
		{
			PointBase pb = m_points.get(pointindex);
			
			if (pb != null && !(pb instanceof RemoteDataPoint && pb.isReadOnly()))
			{
				// make sure that the point is not a remote data point marked as readonly
				
				if (pb.DataLength == datalength)
				{
					// parse the data value
					Object value = NerduinoHost.parseValue(data, offset, pb.DataType, datalength);
					
					pb.setValue(value);
					// make sure that local points trigger callbacks
					// make sure that remote points push the updated value (if it has changed)
				}
			}
		}
	}
	
	@Override
	public void onRegisterAddress(byte index, String nerduinoname)
	{
		// TODO make sure that this address is not already registered and known
		
		// create an uninitialize address object and place it in the 
		// list of registered addresses
		// lookup the address of the nerduino
		// if the name is not recognized then revisit the address as nerduinos
		// are added to the host and resolve at a later time
	}
	
	@Override
	public String engage(IBuildTask task)
	{
		m_engaged = false;
		
		// clear the locally managed data
		m_localDataPoints.clear();
		m_remoteDataPoints.clear();
		m_points.clear();
		m_nextPointID = 0;
		m_checkedIn = false;
		
		
		if (task != null)
		{
			task.setProgress(0);
		}

		setActive(false);
		setActive(true);

		// assert that the serial port was opened
		if (!getActive())
		{
			if (task != null)
			{
				task.setSuccess(false);
			}

			if (compileButton != null)
			{
				compileButton.setEngaged(false);
			}

			return "Error encountered while opening the serial port!";
		}

		// Wait for check in
		if (task != null)
		{
			task.setProgress(20);
		}

		// resetting the serial port should trigger a reset in the arduino.
		// the setup routine should initiate a checkin message.
		// wait up to 5 seconds for the checkin to occure, if checkin doesn't 
		// occure then the setup routine failed or did not properly call the 
		// nerduino.begin() method.. or could be configured with an inapropriate
		// baud rate
		float wait = 0.0f;

		while (!m_checkedIn && wait < 5.0f)
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

		if (!m_checkedIn)
		{
			if (task != null)
			{
				task.setSuccess(false);
			}

			if (compileButton != null)
			{
				compileButton.setEngaged(false);
			}

			return "This sketch failed to check in on start up, verify that nerduino.begin() was called in the sketch's setup() routine and that it was properly configured!";
		}

		// wait for a ping response
		if (task != null)
		{
			task.setProgress(40);
		}

		// wait up to 5 seconds for a ping response.  if none is received then 
		// the loop routine is not processing messages.  It may not be calling 
		// process or processIncoming or it may be in a blocking state.

		sendPing(null, (short) 0);

		wait = 0.0f;

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

		if (!m_pinged)
		{
			if (task != null)
			{
				task.setSuccess(false);
			}

			if (compileButton != null)
			{
				compileButton.setEngaged(false);
			}

			return "This sketch failed to respond to a ping message, verify that nerduino.process() or nerduino.processIncoming() was called in the sketch's loop() routine and that this routine is not in a blocked state!";
		}

		if (task != null)
		{
			task.setProgress(100);
		}

		if (task != null)
		{
			task.setSuccess(true);
		}

		if (compileButton != null)
		{
			compileButton.setEngaged(true);
		}
		
		m_engaged = true;
		
		return null;
	}
	
	@Override
	public Node.PropertySet[] getPropertySets()
	{
		final Sheet.Set nerduinoSheet = Sheet.createPropertiesSet();
		final Sheet.Set pointsSheet = Sheet.createPropertiesSet();
		
		nerduinoSheet.setDisplayName("Nerduino Settings");
		addProperty(nerduinoSheet, short.class, null, "PointCount", "The number of points reported by the nerduino.");
		addProperty(nerduinoSheet, DeviceTypeEnum.class, null, "DeviceType", "The device type reported by the nerduino.");
		addProperty(nerduinoSheet, NerduinoStatusEnum.class, null, "Status", "The status reported by the nerduino.");
		
		if (getPointCount() > 0)
		{
			pointsSheet.setDisplayName("Nerduino Points");
			
			for (int i = 0; i < getPointCount(); i++)
			{
				if (i < m_localDataPoints.size())
				{
					PointBase point = m_localDataPoints.get(i);

					try
					{
						PropertySupport.Reflection prop = null;

						if (point.DataType != null)
						{
							switch(point.DataType)
							{
								case DT_Boolean:
									prop = new PropertySupport.Reflection(point, boolean.class, "Boolean");
									break;
								case DT_Byte:
									prop = new PropertySupport.Reflection(point, byte.class, "Byte");
									break;
								case DT_Short:
									prop = new PropertySupport.Reflection(point, short.class, "Short");
									break;
								case DT_Integer:
									prop = new PropertySupport.Reflection(point, int.class, "Int");
									break;
								case DT_Float:
									prop = new PropertySupport.Reflection(point, float.class, "Float");
									break;
//								case DT_String:
//									prop = new PropertySupport.Reflection(point, String.class, "String");
//									break;
							}

							prop.setName(point.getName());

							pointsSheet.put(prop);
						}
					}
					catch(NoSuchMethodException ex)
					{
						Exceptions.printStackTrace(ex);
					}
				}
			}
		}
		else
		{
			pointsSheet.setDisplayName("No Points");
		}

		return new Node.PropertySet[]
			{
				pointsSheet, nerduinoSheet
			};
	}
	
	@Override
	public PointBase getPoint(String name)
    {
		for(PointBase point : m_points)
		{
			if (point.getName().equals(name))
				return point;
		}

		return null;
    }
	
}
