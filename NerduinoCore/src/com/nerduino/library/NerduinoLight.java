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

import com.nerduino.xbee.BitConverter;
import java.util.ArrayList;
import java.util.HashMap;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;

public class NerduinoLight extends NerduinoFull
{
	protected ArrayList<LocalDataPoint> m_localDataPoints = new ArrayList<LocalDataPoint>();
	protected ArrayList<LinkDataPoint> m_linkDataPoints = new ArrayList<LinkDataPoint>();
	protected ArrayList<PointBase> m_points = new ArrayList<PointBase>();
	HashMap<Integer, String> m_unresolvedAddresses = new HashMap<Integer, String>();
	protected NerduinoBase[] m_addresses = new NerduinoBase[255];
	
	
	byte m_nextPointID = 0;
	
	public NerduinoLight(String name, String icon)
	{
		super(name, icon);
	}

	public void openComPort()
	{
	}
	
	public void closeComPort()
	{
	}
	
	public boolean isReadyToSend()
	{
		return false;
	}
	
	@Override
	public void sendCheckin(NerduinoBase requestedBy)
	{
		if (m_verbose)
			fireCommandUpdate(requestedBy, "Checkin", CommandMessageTypeEnum.OutgoingCommand);
		
		m_checkedIn = false;
		
		sendMessage(MessageEnum.MSG_Checkin.Value(), (byte) 0, null);
	}

	
	@Override
	public boolean sendPing(NerduinoBase requestedBy, short responseToken)
	{
		if (m_verbose)
			fireCommandUpdate(requestedBy, "Ping", CommandMessageTypeEnum.OutgoingCommand);

		m_pinged = false;
		
		// if the device has checked in then ping the light client and respond
		// on behalf of the client

//		if (m_checkedIn)
		{
			// TODO if the serial port is still active then report a ping response
			if (isReadyToSend())
			{
				m_pinged = false;
				
				byte[] data = new byte[2];
		
				data[0] = (byte) (responseToken / 0x100);
				data[1] = (byte) (responseToken & 0xff);

				sendMessage(MessageEnum.MSG_Ping.Value(), (byte) 2, data);
				
				// wait for ping response
				float wait = 0.0f;
				
				while (!m_pinged && wait < 2.0f)
				{
					try
					{
						Thread.sleep(5);
						wait += 0.005f;
					}
					catch(InterruptedException ex)
					{
						Exceptions.printStackTrace(ex);
					}
				}
				
				if (m_pinged && requestedBy != null)
				{
					// ping originated from another nerduino.. so respond 
					requestedBy.sendPingResponse(responseToken, m_status.Value(), (byte) 0); // m_configurationToken);
				}
			}
		}
		
		return m_pinged;
	}
	
	@Override
	public void sendPingResponse(short responseToken, byte status, byte deviceType)
	{
		if (m_verbose)
			fireCommandUpdate(null, "PingResponse", CommandMessageTypeEnum.OutgoingCommand);

		// notify the client of the response
		byte[] data = new byte[4];
		
		data[0] = (byte) (responseToken / 0x100);
		data[1] = (byte) (responseToken & 0xff);
		data[2] = status;
		data[3] = deviceType;
		
		sendMessage(MessageEnum.MSG_PingResponse.Value(), (byte) 4, data);
	}
	
	@Override
	public CommandResponse sendExecuteCommand(NerduinoBase requestedBy, short responseToken, byte responseDataType, byte commandlength, byte[] command)
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < commandlength; i++)
		{
			sb.append((char) command[i]);
		}
		
		if (m_verbose)
			fireCommandUpdate(null, "Execute  " + sb.toString(), CommandMessageTypeEnum.OutgoingCommand);
		else
			fireCommandUpdate(sb.toString(), CommandMessageTypeEnum.OutgoingCommand);
		
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
					&& wait < 2.0f)
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
				m_commandResponse.Status = ResponseStatusEnum.RS_Timeout;
			
			return m_commandResponse;	
		}
		
		return null;
	}
	
	@Override
	public void sendGetPoints(NerduinoBase requestedBy, short responseToken)
	{
		if (m_verbose)
			fireCommandUpdate(requestedBy, "GetPoints", CommandMessageTypeEnum.OutgoingCommand);

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
		if (m_verbose)
			fireCommandUpdate(requestedBy, "GetPoint  " + name, CommandMessageTypeEnum.OutgoingCommand);
		
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
		if (m_verbose)
		{
			Short sid = id;
			
			fireCommandUpdate(requestedBy, "GetPointValue  " + sid.toString(), CommandMessageTypeEnum.OutgoingCommand);
		}
		
		// provide response on behalf of the client
		if (requestedBy != null)
		{
			for(int i = 0; i < m_localDataPoints.size(); i++)
			{
				LocalDataPoint ldp = m_localDataPoints.get(i);

				if (ldp.Id == id)
				{
					if (requestedBy != null)
						requestedBy.sendGetPointValueResponse(responseToken, ldp.Id, ldp.Status, ldp.DataType, ldp.getBytes());
					
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
		if (m_verbose)
			fireCommandUpdate(requestedBy, "GetPointValue  " + name, CommandMessageTypeEnum.OutgoingCommand);
		
		// provide response on behalf of the client
		if (requestedBy != null)
		{
			for(int i = 0; i < m_localDataPoints.size(); i++)
			{
				LocalDataPoint ldp = m_localDataPoints.get(i);

				if (ldp.getName().equals(name))
				{
					if (requestedBy != null)
						requestedBy.sendGetPointValueResponse(responseToken, ldp.Id, ldp.Status, ldp.DataType, ldp.getBytes());
					
					return;
				}
			}

			// not found
			requestedBy.sendGetPointResponse(responseToken, null);
		}
	}
	
	@Override
	public void sendGetPointValueResponse(short responseToken, short id, byte status, DataTypeEnum dataType, byte[] value)
	{
		if (m_verbose)
			fireCommandUpdate(null, "GetPointValueResponse", CommandMessageTypeEnum.OutgoingCommand);
		
		// validate that the id is the right range
		// validate the data type / data length
		
		// forward the value change to the client

		byte dataLength = dataType.getLength();
		
		byte length = (byte) (2 + dataLength);
		
		byte[] data = new byte[length];
		
		data[0] = (byte) (responseToken / 0x100);
		data[1] = (byte) (responseToken & 0xff);
		
		System.arraycopy(value, 0, data, 2, dataLength);
		
		sendMessage(MessageEnum.MSG_GetPointValueResponse.Value(), length, data);
	}

	@Override
	public void sendSetPointValue(NerduinoBase requestedBy, short index, DataTypeEnum dataType, Object value)
	{
		if (m_verbose)
		{
			Short sid = index;
			
			fireCommandUpdate(requestedBy, "SetPointValue  " + sid.toString() + " = " + value.toString(), CommandMessageTypeEnum.OutgoingCommand);
		}
		
		// validate that the index is the right range
		// validate the data type / data length
		
		PointBase pb = m_points.get(index);

		if (pb != null)
		{
			Object val = null;
			
			byte[] bytes = null;
			
			if (value instanceof Boolean)
				bytes = BitConverter.GetBytes((Boolean) value);
			if (value instanceof Byte)
				bytes = BitConverter.GetBytes((Byte) value);
			else if (value instanceof Short)
				bytes = BitConverter.GetBytes((Short) value);
			else if (value instanceof Integer)
				bytes = BitConverter.GetBytes((Integer) value);
			else if (value instanceof Float)
				bytes = BitConverter.GetBytes((Float) value);
			else if (value instanceof byte[])
				bytes = (byte[]) value;
			
			switch(pb.DataType)
			{
				case DT_Boolean:
					if (bytes[0] != 0)
						val = true;
					else
						val = false;
					break;
				case DT_Byte:
					val = bytes[0];
					break;
				case DT_Short:
					val = BitConverter.GetShort(bytes);
					break;
				case DT_Integer:
					val = BitConverter.GetInt(bytes);
					break;
				case DT_Float:
					val = BitConverter.GetFloat(bytes, 0);
					break;
			}

			pb.setValue(val);
		}
		
		// cache the value
		// forward the value change to the client
		byte[] bytes = null;
		byte dataLength = dataType.getLength();
		
		if (value instanceof byte[])
		{
			bytes = (byte[]) value;
		}
		else
		{
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
		}
		
		byte length = (byte) (2 + dataLength);
		
		byte[] data = new byte[length];
		
		data[0] = (byte) (index / 0x100);
		data[1] = (byte) (index & 0xff);
		
		System.arraycopy(bytes, 0, data, 2, dataLength);
		
		// TODO validate message format
		sendMessage(MessageEnum.LMSG_SetPointValue.Value(), length, data);
	}
	
	public void sendLinkSetPointValue(NerduinoBase requestedBy, short index, DataTypeEnum dataType, Object value)
	{
		if (m_verbose)
		{
			Short sid = index;
			
			fireCommandUpdate(requestedBy, "SetPointValue  " + sid.toString() + " = " + value.toString(), CommandMessageTypeEnum.OutgoingCommand);
		}
		
		// validate that the index is the right range
		// validate the data type / data length

		PointBase pb = null;
		
		for(PointBase p : m_linkDataPoints)
		{
			if (p.Id == index)
			{
				pb = p;
				break;
			}
		}

		//PointBase pb = m_linkDataPoints.get(index);

		if (pb != null)
		{
			Object val = null;
			
			byte[] bytes = null;
			
			if (value instanceof Boolean)
				bytes = BitConverter.GetBytes((Boolean) value);
			else if (value instanceof Byte)
				bytes = BitConverter.GetBytes((Byte) value);
			else if (value instanceof Short)
				bytes = BitConverter.GetBytes((Short) value);
			else if (value instanceof Integer)
				bytes = BitConverter.GetBytes((Integer) value);
			else if (value instanceof Float)
				bytes = BitConverter.GetBytes((Float) value);
			else if (value instanceof byte[])
				bytes = (byte[]) value;
			
			switch(pb.DataType)
			{
				case DT_Boolean:
					if (bytes[0] != 0)
						val = true;
					else
						val = false;
					break;
				case DT_Byte:
					val = bytes[0];
					break;
				case DT_Short:
					val = BitConverter.GetShort(bytes);
					break;
				case DT_Integer:
					val = BitConverter.GetInt(bytes);
					break;
				case DT_Float:
					val = BitConverter.GetFloat(bytes, 0);
					break;
			}

			pb.setValue(val);
		}
		
		// cache the value
		// forward the value change to the client
		byte[] bytes = null;
		byte dataLength = dataType.getLength();
		
		if (value instanceof byte[])
		{
			bytes = (byte[]) value;
		}
		else
		{
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
	public void sendSetPointValue(NerduinoBase requestedBy, String pointName, DataTypeEnum dataType, Object value)
	{
		if (m_verbose)
			fireCommandUpdate(requestedBy, "SetPointValue  " + pointName + " = " + value.toString(), CommandMessageTypeEnum.OutgoingCommand);
		
		// validate that the pointName
		// validate the data type / data length
		int index = -1;
		
		for(int i = 0; i < m_points.size(); i++)
		{
			PointBase pb = m_points.get(i);
			
			if (pb.getName().equals(pointName))
			{
				index = i;
				Object val = null;
				byte[] bytes = (byte[]) value;
				
				switch(pb.DataType)
				{
					case DT_Boolean:
						if (bytes[0] != 0)
							val = true;
						else
							val = false;
						break;
					case DT_Byte:
						val = bytes[0];
						break;
					case DT_Short:
						val = BitConverter.GetShort(bytes);
						break;
					case DT_Integer:
						val = BitConverter.GetInt(bytes);
						break;
					case DT_Float:
						val = BitConverter.GetFloat(bytes, 0);
						break;
				}
				
				pb.setValue(val);
				break;
			}
		}
		
		if (index < 0)
			return; // invalide point name, so ignore the request
		
		// cache the value
		// forward the value change to the client
		byte[] bytes = null;
		byte dataLength = dataType.getLength();
		
		if (value instanceof byte[])
		{
			bytes = (byte[]) value;
		}
		else
		{
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
	public void sendRegisterPointCallback(NerduinoBase requestedBy, byte addRemove, short responseToken, short index, byte filterType, byte filterLength, byte[] filterValue)
	{
		if (m_verbose)
		{
			Short sid = index;
			
			fireCommandUpdate(requestedBy, "RegisterPointCallback  " + sid.toString() , CommandMessageTypeEnum.OutgoingCommand);
		}
		
		// perform this on behalf of the client
		
		// lookup the data point associated with the requested index
		for(int i = 0; i < m_localDataPoints.size(); i++)
		{
			LocalDataPoint ldp = m_localDataPoints.get(i);
			
			if (ldp.Id == index)
			{
				if (addRemove == 0)
					ldp.onRegisterPointCallback(requestedBy, responseToken, filterType, filterLength, filterValue);
				else
					ldp.onUnregisterPointCallback(requestedBy);
			}
		}
	}
	
	@Override
	public void sendGetPointResponse(short responseToken, LocalDataPoint point)
	{
		if (m_verbose)
			fireCommandUpdate(null, "GetPointResponse", CommandMessageTypeEnum.OutgoingCommand);

		// store response and forward the value change to the client
		// TODO
	}
	
	@Override
	public void sendGetAddressResponse(short responseToken, AddressStatusEnum status, Address address, short pointIndex)
	{
		if (m_verbose)
			fireCommandUpdate(null, "GetAddressResponse", CommandMessageTypeEnum.OutgoingCommand);

		// send the light message to the client
	}

	@Override
	public void sendExecuteCommandResponse(short responseToken, byte status, byte dataType, byte responselength, byte[] response)
	{
		if (m_verbose)
			fireCommandUpdate(null, "ExecuteCommandResponse", CommandMessageTypeEnum.OutgoingCommand);
//		else
//			fireCommandUpdate("ExecuteCommandResponse", CommandMessageTypeEnum.OutgoingCommand);
		
		// send the light message to the client
		byte length = (byte) (responselength + 5);
		
		byte[] data = new byte[length];
		
		data[0] = (byte) (responseToken / 0x100);
		data[1] = (byte) (responseToken & 0xff);
		data[2] = status;
		data[3] = dataType;
		
		if (response != null)
			System.arraycopy(response, 0, data, 4, responselength);
		
		// TODO validate message format
		sendMessage(MessageEnum.MSG_ExecuteCommandResponse.Value(), length, data);
	}
	
	@Override
	public void forwardMessage(NerduinoBase originator, byte[] data)
	{
		processMessage(originator, data);
	}
	
	public void writeData(byte length, byte[] data)
	{
	}
	
	public void sendMessage(byte message, byte length, byte[] data)
	{	
	}
		
	@Override
	public void onPingResponse(byte[] data, int offset)
	{
		if (m_verbose)
			fireCommandUpdate("N: PingResponse", CommandMessageTypeEnum.IncomingCommand);
		
		touch();

		short responseToken = (short) ((int) data[offset++] * 0x100 + (int) data[offset++]);

		byte status = data[offset++];
		byte dt = data[offset++];

		
		sendPingResponse(responseToken, status, dt);
		
		m_pinged = true;
	}
	
	@Override
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
	
	public void onDeclarePoint(byte datatype, byte readonly, byte publish, String pointname, Object value)
	{
		if (m_verbose)
			fireCommandUpdate("N: DeclarePoint  " + pointname, CommandMessageTypeEnum.IncomingCommand);
		
		// create a new point and add to the light client local data 
		// point collection
		
		LocalDataPoint ldp = new LocalDataPoint();
		ldp.Id = m_nextPointID++;
		ldp.setDataType(DataTypeEnum.valueOf(datatype));
		ldp.setName(pointname);
		ldp.Attributes = readonly;
		ldp.m_nerduino = this;
		ldp.initializeValue(value);
		
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
			proxy.m_nerduino = this;
			proxy.initializeValue(value);
			
			if (newpoint != null)
				PointManager.Current.addChild(newpoint);
		}
	}
	
	@Override
	public void onRegisterPoint(byte datatype, String pointpath)
	{
		if (m_verbose)
			fireCommandUpdate("N: RegisterPoint  " + pointpath, CommandMessageTypeEnum.IncomingCommand);
		
		// TODO create a new remote point and add to the link point collection 
		LinkDataPoint ldp = new LinkDataPoint(this, pointpath, m_nextPointID++);
		
		m_linkDataPoints.add(ldp);
	}
	
	@Override
	public void onRegisterPointCallback(NerduinoBase originator, byte[] data, int offset)
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

				point = (LocalDataPoint) getPoint(pname);

				if (m_verbose)
					fireCommandUpdate("N: RegisterPointCallback  " + pname, CommandMessageTypeEnum.IncomingCommand);
				
				break;
			case 1: // by index
				Short index = (short) (data[offset++] *0x100 + data[offset++]);

				if (m_verbose)
					fireCommandUpdate("N: RegisterPointCallback  " + index.toString(), CommandMessageTypeEnum.IncomingCommand);
				
				// lookup this index, to see if this point is already known
				point = (LocalDataPoint) getPoint(index);
				
				break;
		}

		if (point != null)
		{
			if (registerFlag == 0)
				point.onRegisterPointCallback(originator, data);
			else
				point.onUnregisterPointCallback(originator);				
		}
		else
		{
			if (m_verbose)
				fireCommandUpdate("N: RegisterPointCallback  Point not found!", CommandMessageTypeEnum.Error);
		}
	}
	
	
	@Override
	public void onLightRegisterPoint(byte[] data, int offset)
	{
		byte datatype = data[offset++];
		byte namelength = data[offset++];
		
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < namelength; i++)
		{
			sb.append((char) data[offset++]);
		}
		
		String pointname = sb.toString();

		if (m_verbose)
			fireCommandUpdate("N: LightRegisterPoint  " + pointname, CommandMessageTypeEnum.IncomingCommand);
		
		onRegisterPoint(datatype, pointname);
	}
	
	@Override
	public void onGetPoint(NerduinoBase originator, byte[] data, int offset)
	{
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
			
			// look for a LDP with this name
			LocalDataPoint point = (LocalDataPoint) getPoint(name);
			
			if (point != null)
				originator.sendGetPointResponse(responseToken, point);
		}
	}
	
	@Override
	public void onGetPointValue(NerduinoBase originator, byte[] data, int offset)
	{
		short responseToken = (short) (data[offset++] * 0x100 + data[offset++]);
		byte identifierType = data[offset++];
		
		LocalDataPoint point;
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
			
			if (pos >= 0)
				pname = pname.substring(pos + 1);
			
			pid = pname;

			point = (LocalDataPoint) getPoint(pname);
		}
		else // search by index
		{
			Short index = (short) (data[offset++] * 0x100 + data[offset++]);
			pid = index.toString();
			
			point = (LocalDataPoint) getPoint(index);
		}
		
		if (point != null)
		{
			point.sendGetPointValueResponse(originator, responseToken);
			
			if (m_verbose)
				fireCommandUpdate("N: GetPointValue  " + pid, CommandMessageTypeEnum.IncomingCommand);
		}
		else
		{
			// notify that the point was not found
			originator.sendGetPointValueResponse(responseToken, (short) -1, (byte) 2, DataTypeEnum.DT_Byte, data);
			
			if (m_verbose)
				fireCommandUpdate("N: GetPointValue  " + pid, CommandMessageTypeEnum.Error);
		}
	}
	
	@Override
	public void onGetPointValueResponse(byte[] data, int offset)
	{
		touch();
		
		offset += 2;
		
		Short index = (short) (data[offset++] * 0x100 + data[offset++]);
		
		// lookup this index, to see if this point is already known
		PointBase point = (PointBase) getPoint(index);
		
		if (point != null)
		{
			if (point instanceof LinkDataPoint)
			{
				LinkDataPoint ldp = (LinkDataPoint) point;
				
				ldp.onGetPointValueResponse(data);
			}
			else if (point instanceof RemoteDataPoint)
			{
				RemoteDataPoint rdp = (RemoteDataPoint) point;
				
				rdp.onGetPointValueResponse(data);				
			}
			
			if (m_verbose)
				fireCommandUpdate("N: GetPointValueResponse  " + index.toString() + " = " + point.getValue().toString(), CommandMessageTypeEnum.IncomingCommand);
		}
		else
		{
			if (m_verbose)
				fireCommandUpdate("N: GetPointValueResponse  Could not find point index " + index.toString(), CommandMessageTypeEnum.IncomingCommand);
		}
	}

	
	@Override
	public void onSetPointValue(byte[] data, int offset)
	{
		byte identifierType = data[offset++];
		
		LocalDataPoint point;
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
			
			if (pos >= 0)
				pname = pname.substring(pos + 1);
			
			pid = pname;

			point = (LocalDataPoint) getPoint(pname);
		}
		else // search by index
		{
			Short index = (short) (data[offset++] * 0x100 + data[offset++]);
			pid = index.toString();
			
			point = (LocalDataPoint) getPoint(index);
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
					if (bytes[0] == 0)
						value = false;
					else
						value = true;

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
			
			if (m_verbose)
				fireCommandUpdate("N: SetPointValue  " + pid + " = " + value.toString(), CommandMessageTypeEnum.IncomingCommand);
		}
		else
		{
			if (m_verbose)
				fireCommandUpdate("N: SetPointValue  " + pid + "   Point not found!", CommandMessageTypeEnum.Error);
		}
	}

	
	@Override
	public void onLightSetPointValue(byte[] data, int offset)
	{
		short pointindex = (short) ((int) data[offset++] * 0x100 + (int) data[offset++]);
		byte datalength = data[offset++];
		
		if (pointindex >= 0 && pointindex < m_nextPointID)
		{
			PointBase pb = null;
			
			for(PointBase point : m_points)
			{
				if (point.Id == pointindex)
				{
					pb = point;
					break;
				}
			}
			
			if (pb == null)
			{
				for(PointBase point : m_linkDataPoints)
				{
					if (point.Id == pointindex)
					{
						pb = point;
						break;
					}
				}
			}
			
			if (pb != null && !(pb instanceof RemoteDataPoint && pb.isReadOnly()))
			{
				// make sure that the point is not a remote data point marked as readonly
				if (pb.DataLength == datalength)
				{
					// parse the data value
					Object value = NerduinoHost.parseValue(data, offset, pb.DataType, datalength);
					
					pb.setValue(value);
					
					if (m_verbose)
						fireCommandUpdate("N: LightSetPointValue  " + pb.getName() + " = " + value.toString(), CommandMessageTypeEnum.IncomingCommand);
					
					// make sure that local points trigger callbacks
					// make sure that remote points push the updated value (if it has changed)
				}
			}
		}
	}
	
	byte testByte = 1;
	short testShort = 1;
	int testInt = 1;
	float testFloat = 1;
	
	@Override
	public void onLightSetProxyData(byte[] data, int offset)
	{
		if (m_verbose)
			fireCommandUpdate("N: LightSetProxyData", CommandMessageTypeEnum.IncomingCommand);
		
		byte index = data[offset++];
		byte datalength = data[offset++];
		
		switch(index)
		{
			case 0: // test byte
				if (datalength == 1)
					testByte = data[offset];
				
				break;
			case 1: // test short
				if (datalength == 2)
					testShort = BitConverter.GetShort(data, offset);
				
				break;
			case 2: // test long
				if (datalength == 4)
					testInt = BitConverter.GetInt(data, offset);
				
				break;
			case 3: // test float
				if (datalength == 4)
					testFloat = BitConverter.GetFloat(data, offset);
				
				break;
		}
	}
	
	@Override
	public void onLightGetProxyData(byte[] data, int offset)
	{
		if (m_verbose)
			fireCommandUpdate("N: LightGetProxyData", CommandMessageTypeEnum.IncomingCommand);
		
		
		byte index = data[offset++];
		byte datalength = data[offset++];
		
		byte[] rdata = new byte[datalength]; 
		
		switch(index)
		{
			case 0: // test byte
				if (datalength == 1)
					rdata[0] = testByte;
				
				break;
			case 1: // test short
				if (datalength == 2)
					rdata = BitConverter.GetBytes(testShort);
				
				break;
			case 2: // test long
				if (datalength == 4)
					rdata = BitConverter.GetBytes(testInt);
				
				break;
			case 3: // test float
				if (datalength == 4)
					rdata = BitConverter.GetBytes(testFloat);
				
				break;
		}		
		writeData(datalength, rdata);
	}
	
	@Override
	public void onRegisterAddress(byte index, String nerduinoname)
	{
		if (m_verbose)
			fireCommandUpdate("N: RegisterAddress", CommandMessageTypeEnum.IncomingCommand);
		
		// lookup the nerduino
		NerduinoBase nerd = NerduinoManager.Current.getNerduino(nerduinoname);
		
		if (nerd != null)
			m_addresses[index] = nerd;
		else
			// the name could not be resolved, so place the name into the 
			// addess hash table to be reolved later as nerduinos are added 
			m_unresolvedAddresses.put((int) index, nerduinoname);		
	}
	
	@Override
	public String engage()
	{
		setStatus(NerduinoStatusEnum.Uninitialized);
		
		m_engaged = false;
		m_engaging = true;
		
		// clear the locally managed data
		m_localDataPoints.clear();
		
		// TODO unregister from any currently linked points
		
		m_linkDataPoints.clear();
		m_points.clear();
		m_nextPointID = 0;
		m_checkedIn = false;
		
		if (!getActive())
		{
			StatusDisplayer.getDefault().setStatusText("Engaging " + this.getName() + " Failed!");

			String err = "Engage not attempted because the nerduino is marked as Active = FALSE!";
			
			fireEngageStatusUpdate(false, false, 0, err);
			
			m_engaging = false;
			
			return err;
		}

		closeComPort();
		openComPort();
		
		if (!isReadyToSend())
		{
			StatusDisplayer.getDefault().setStatusText("Engaging " + this.getName() + " Failed!");

			String err = "Could not open the com port to engage!";
			
			fireEngageStatusUpdate(false, false, 0, err);
			
			m_engaging = false;
			
			return err;
		}
			
		fireEngageStatusUpdate(true, false, 0, "");
		
		// first attempt to ping the nerduino
		// if it doesn't respond to a ping then rest it and wait for a check in
		
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
		
		if (m_pinged)
		{
			// The board is already running, so request that it re-checkin
			sendCheckin(null);
			
			// wait for checkin response
			wait = 0.0f;

			while (!m_checkedIn && wait < 2.0f)
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
		}
		else
		{
			// force a reset
			resetBoard();
	
			// assert that the serial port was opened
			if (!isReadyToSend())
			{
				StatusDisplayer.getDefault().setStatusText("Engaging " + this.getName() + " Failed!");

				setStatus(NerduinoStatusEnum.Offline);

				String err = "Error encountered while opening the serial port!";

				fireEngageStatusUpdate(false, false, 0, err);

				m_engaging = false;
			
				return err;
			}
			
			// Wait for check in
			fireEngageStatusUpdate(true, false, 20, "");

			sendPing(null, (short) 0);

			wait = 0.0f;

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

			if (m_pinged)
			{
				m_checkedIn = true;
			}
			else
			{
				// resetting the serial port should trigger a reset in the arduino.
				// the setup routine should initiate a checkin message.
				// wait up to 5 seconds for the checkin to occure, if checkin doesn't 
				// occure then the setup routine failed or did not properly call the 
				// nerduino.begin() method.. or could be configured with an inapropriate
				// baud rate
				wait = 0.0f;

				while (!m_checkedIn && wait < 9.0f)
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
					StatusDisplayer.getDefault().setStatusText("Engaging " + this.getName() + " Failed!");

					String err = "This sketch failed to check in on start up, verify that nerduino.begin() was called in the sketch's setup() routine and that it was properly configured!";

					fireEngageStatusUpdate(false, false, 0, err);

					m_engaging = false;
			
					return err;
				}
			}

			// wait for a ping response
			fireEngageStatusUpdate(true, false, 40, "");

			// wait up to 2 seconds for a ping response.  if none is received then 
			// the loop routine is not processing messages.  It may not be calling 
			// process or processIncoming or it may be in a blocking state.

			sendPing(null, (short) 0);

			wait = 0.0f;

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
				StatusDisplayer.getDefault().setStatusText("Engaging " + this.getName() + " Failed!");

				String err = "This sketch failed to respond to a ping message, verify that nerduino.process() or nerduino.processIncoming() was called in the sketch's loop() routine and that this routine is not in a blocked state!";

				fireEngageStatusUpdate(false, false, 0, err);

				m_engaging = false;
			
				return err;
			}
		}
		
		fireEngageStatusUpdate(false, true, 0, "");
		
		m_engaged = true;
		
		setStatus(NerduinoStatusEnum.Online);
		
		StatusDisplayer.getDefault().setStatusText("Engaging " + this.getName() + " Completed!");

		m_engaging = false;
			
		return null;
	}
	
	@Override
	public Node.PropertySet[] getPropertySets()
	{
		final Sheet.Set nerduinoSheet = Sheet.createPropertiesSet();
		final Sheet.Set pointsSheet = Sheet.createPropertiesSet();
		
		nerduinoSheet.setDisplayName("Nerduino Settings");
		//addProperty(nerduinoSheet, short.class, null, "PointCount", "The number of points reported by the nerduino.");
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
						if (point.DataType != null)
						{
							switch(point.DataType)
							{
								case DT_Boolean:
								{
									PropertySupport.Reflection<Boolean> prop = new PropertySupport.Reflection<Boolean>(point, boolean.class, "Boolean");
									prop.setName(point.getName());
									pointsSheet.put(prop);
								}
									break;
								case DT_Byte:
								{
									PropertySupport.Reflection<Byte> prop = new PropertySupport.Reflection<Byte>(point, byte.class, "Byte");
									prop.setName(point.getName());
									pointsSheet.put(prop);
								}
									break;
								case DT_Short:
								{
									PropertySupport.Reflection<Short> prop = new PropertySupport.Reflection<Short>(point, short.class, "Short");
									prop.setName(point.getName());
									pointsSheet.put(prop);
								}
									break;
								case DT_Integer:
								{
									PropertySupport.Reflection<Integer> prop = new PropertySupport.Reflection<Integer>(point, int.class, "Int");
									prop.setName(point.getName());
									pointsSheet.put(prop);
								}
									break;
								case DT_Float:
								{
									PropertySupport.Reflection<Float> prop = new PropertySupport.Reflection<Float>(point, float.class, "Float");
									prop.setName(point.getName());
									pointsSheet.put(prop);
								}
									break;
//								case DT_String:
//								{
//									PropertySupport.Reflection<String> prop = new PropertySupport.Reflection<String>(point, String.class, "String");
//									prop.setName(point.getName());
//									pointsSheet.put(prop);
//								}
//									break;
							}
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
		for(PointBase point : m_localDataPoints)
		{
			if (point.getName().equals(name))
				return point;
		}

		return null;
    }
	
	@Override
	public PointBase getPoint(short id)
    {
		for(PointBase point : m_localDataPoints)
		{
			if (point.Id == id)
				return point;
		}

		for(PointBase point : m_linkDataPoints)
		{
			if (point.Id == id)
				return point;			
		}
		
		return null;
    }
}
