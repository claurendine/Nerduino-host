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
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NerduinoFull extends NerduinoBase //implements FrameReceivedListener
{
	long m_lastBroadcast = 0;
	boolean m_sending = false;
	long m_broadcastThrottle = 50;
	Address m_incomingAddress = new Address();

	
	public NerduinoFull(String name, String icon)
	{
		super(name, icon);
	}
	
	@Override
	public void readXML(Element node)
	{
		m_name = node.getAttribute("Name");
		m_sketch = node.getAttribute("Sketch");
		m_boardType = node.getAttribute("Board");
		m_active = Boolean.valueOf(node.getAttribute("Active"));		

		setStatus(NerduinoStatusEnum.Offline);
	}

	@Override
	public void writeXML(Document doc, Element element)
	{
		element.setAttribute("Name", m_name);
		element.setAttribute("Sketch", m_sketch);
		element.setAttribute("Board", m_boardType);
		element.setAttribute("Active", Boolean.toString(m_active));
		
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
				if (i < m_points.size())
				{
					RemoteDataPoint point = m_points.get(i);

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
								case DT_String:
								{
									PropertySupport.Reflection<String> prop = new PropertySupport.Reflection<String>(point, String.class, "String");
									prop.setName(point.getName());
									pointsSheet.put(prop);
								}
									break;
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

	/*
    @Override
    public PointBase getPoint(String name)
    {
        byte length = (byte) name.length();
        
    	sendGetPoint(null, (short) 0, (byte) 0, length, name.getBytes()); 
        
        for(int i = 0; i < 3000; i++)
        {
            try 
            {
                if (m_pointResponse != null)
                {
                    return m_pointResponse;
                }
                
                Thread.sleep(1);
            } 
            catch (InterruptedException ex) 
            {
                Logger.getLogger(NerduinoXBee.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
		
        return null;
    }
	*/
	
	@Override
	public void sendCheckin(NerduinoBase requestedBy)
	{
		if (m_verbose)
			fireCommandUpdate(requestedBy, "Checkin", CommandMessageTypeEnum.OutgoingCommand);
		
		m_checkedIn = false;
		
		byte[] data = new byte[3];
		
		short fromindex = 0;
		
		data[0] = (byte) (fromindex / 0x100);
		data[1] = (byte) (fromindex & 0xff);
		data[2] = MessageEnum.MSG_Checkin.Value();
		
		sendMessage(data);
	}

	
	@Override
	public boolean sendPing(NerduinoBase requestedBy, short responseToken)
	{
		if (m_verbose)
			fireCommandUpdate(requestedBy, "Ping", CommandMessageTypeEnum.OutgoingCommand);
		
		m_pinged = false;
		
		byte[] data = new byte[5];
		
		short fromindex = (requestedBy != null) ? requestedBy.m_address.RoutingIndex : 0;
		
		data[0] = (byte) (fromindex / 0x100);
		data[1] = (byte) (fromindex & 0xff);
		data[2] = MessageEnum.MSG_Ping.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		
		sendMessage(data);
		
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

		return m_pinged;
	}
	
	public void sendMessage(byte[] data)
	{
		// this method should be overriden to provide the communication code to the device
		/*
		// zigbee implementation 
		TransmitRequestFrame frame = new TransmitRequestFrame();
		
		frame.DestinationAddress = m_address.SerialNumber;
		frame.DestinationNetworkAddress = m_address.NetworkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;
		
		frame.Data = data;
		
		sendFrame(frame);
		*/
	}
	
	@Override
	public void sendPingResponse(short responseToken, byte status, byte configurationToken)
	{
		if (m_verbose)
			fireCommandUpdate(null, "PingResponse", CommandMessageTypeEnum.OutgoingCommand);
		
		byte[] data = new byte[7];
		
		short fromindex = 0;
		
		data[0] = (byte) (fromindex / 0x100);
		data[1] = (byte) (fromindex & 0xff);
		data[2] = MessageEnum.MSG_PingResponse.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = status;
		data[6] = configurationToken;
		
		sendMessage(data);
	}
	
	@Override
	public void sendGetPoints(NerduinoBase requestedBy, short responseToken)
	{
		if (m_verbose)
			fireCommandUpdate(requestedBy, "GetPoints", CommandMessageTypeEnum.OutgoingCommand);
		
		// mark points as invalid
		for (RemoteDataPoint point : m_points)
		{
			point.Validated = false;
		}
		
		m_receivedGetPoints = false;
		
		byte[] data = new byte[7];
		
		short fromindex = (requestedBy != null) ? requestedBy.m_address.RoutingIndex : 0;
		
		data[0] = (byte) (fromindex / 0x100);
		data[1] = (byte) (fromindex & 0xff);
		data[2] = MessageEnum.MSG_GetPoint.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = PointIdentifierTypeEnum.PIT_All.Value();
		
		sendMessage(data);
	}

	@Override
	public void sendGetPoint(NerduinoBase requestedBy, short responseToken, byte idtype, byte idlength, byte[] id)
	{
		if (m_verbose)
			fireCommandUpdate(requestedBy, "GetPoint", CommandMessageTypeEnum.OutgoingCommand);
		
		byte[] data = new byte[idlength + 7];
		
		short fromindex = (requestedBy != null) ? requestedBy.m_address.RoutingIndex : 0;
		
		data[0] = (byte) (fromindex / 0x100);
		data[1] = (byte) (fromindex & 0xff);
		data[2] = MessageEnum.MSG_GetPoint.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = idtype;
		data[6] = idlength;
		
		System.arraycopy(id, 0, data, 7, idlength);
		
		sendMessage(data);
	}
	
	@Override
	public void sendGetPoint(NerduinoBase requestedBy, short responseToken, String name)
	{
		if (m_verbose)
			fireCommandUpdate(requestedBy, "GetPoint  " + name, CommandMessageTypeEnum.OutgoingCommand);
		
		m_receivedGetPoint = false;
		
		byte nlength = (byte) name.length();		
		byte[] data = new byte[7 + nlength];
		
		short fromindex = (requestedBy != null) ? requestedBy.m_address.RoutingIndex : 0;
		
		data[0] = (byte) (fromindex / 0x100);
		data[1] = (byte) (fromindex & 0xff);
		data[2] = MessageEnum.MSG_GetPoint.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = PointIdentifierTypeEnum.PIT_Name.Value();
		data[6] = nlength;

		byte[] b = name.getBytes();
		
		System.arraycopy(b, 0, data, 7, nlength);
		
		sendMessage(data);
	}

	@Override
	public void sendGetPointValue(NerduinoBase requestedBy, short responseToken, short id)
	{
		if (m_verbose)
		{
			Short sid = id;
			
			fireCommandUpdate(requestedBy, "GetPointValue  " + sid.toString(), CommandMessageTypeEnum.OutgoingCommand);
		}
				
		byte[] data = new byte[8];
		
		short fromindex = (requestedBy != null) ? requestedBy.m_address.RoutingIndex : 0;
		
		data[0] = (byte) (fromindex / 0x100);
		data[1] = (byte) (fromindex & 0xff);
		data[2] = MessageEnum.MSG_GetPointValue.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = (byte) 1; // identifier type 1 - by index
		data[6] = (byte) (id / 0x100);
		data[7] = (byte) (id & 0xff);
		
		sendMessage(data);
	}
	
	@Override
	public void sendGetPointValue(NerduinoBase requestedBy, short responseToken, String name)
	{
		if (m_verbose)
			fireCommandUpdate(requestedBy, "GetPointValue  " + name, CommandMessageTypeEnum.OutgoingCommand);
		
		byte length = (byte) name.length();
		byte[] data = new byte[7 + length];
		
		short fromindex = (requestedBy != null) ? requestedBy.m_address.RoutingIndex : 0;
		
		data[0] = (byte) (fromindex / 0x100);
		data[1] = (byte) (fromindex & 0xff);
		data[2] = MessageEnum.MSG_GetPointValue.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = (byte) 0; // identifier type 0 - by name
		data[6] = length;
		
		byte[] bytes = name.getBytes();
		
		System.arraycopy(bytes, 0, data, 7, length);
		
		sendMessage(data);
	}
	
	@Override
	public void sendGetPointValueResponse(short responseToken, short id, byte status, DataTypeEnum dataType, byte[] value)
	{
		if (m_verbose)
			fireCommandUpdate(null, "GetPointValueResponse", CommandMessageTypeEnum.OutgoingCommand);
		
		int dataLength = dataType.getLength();
		
		byte[] data = new byte[9 + dataLength];
		
		short fromindex = 0;
		
		data[0] = (byte) (fromindex / 0x100);
		data[1] = (byte) (fromindex & 0xff);
		data[2] = MessageEnum.MSG_GetPointValueResponse.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = (byte) (id / 0x100);
		data[6] = (byte) (id & 0xff);
		data[7] = status;
		data[8] = dataType.Value();
		
		System.arraycopy(value, 0, data, 9, dataLength);

		sendMessage(data);
	}

	@Override
	public void sendSetPointValue(NerduinoBase requestedBy, short index, DataTypeEnum dataType, Object value)
	{
		if (m_verbose)
		{
			Short sid = index;
			
			fireCommandUpdate(requestedBy, "SetPointValue  " + sid.toString() + " = " + value.toString(), CommandMessageTypeEnum.OutgoingCommand);
		}
		
		byte dataLength = dataType.getLength();		
		byte[] data = new byte[8 + dataLength];
		
		short fromindex = (requestedBy != null) ? requestedBy.m_address.RoutingIndex : 0;
		
		data[0] = (byte) (fromindex / 0x100);
		data[1] = (byte) (fromindex & 0xff);
		data[2] = MessageEnum.MSG_SetPointValue.Value();
		
		data[3] = (byte) 1; // identifier type
		data[4] = (byte) (index / 0x100);
		data[5] = (byte) (index & 0xff);
		data[6] = dataType.Value();
		data[7] = dataLength;
		
		System.arraycopy(NerduinoHost.toBytes(value), 0, data, 8, dataLength);
		
		sendMessage(data);
	}
	
	@Override
	public void sendSetPointValue(NerduinoBase requestedBy, String pointName, DataTypeEnum dataType, Object value)
	{
		if (m_verbose)
			fireCommandUpdate(requestedBy, "SetPointValue  " + pointName + " = " + value.toString(), CommandMessageTypeEnum.OutgoingCommand);
		
		byte dataLength = dataType.getLength();
		byte nameLength = (byte) pointName.length();		
		byte[] data = new byte[7 + dataLength + nameLength];
		
		short fromindex = (requestedBy != null) ? requestedBy.m_address.RoutingIndex : 0;
		
		data[0] = (byte) (fromindex / 0x100);
		data[1] = (byte) (fromindex & 0xff);
		data[2] = MessageEnum.MSG_SetPointValue.Value();
		
		data[3] = (byte) 0; // identifier type
		data[4] = nameLength;
		
		int offset = 5 + nameLength;
		
		System.arraycopy(pointName.getBytes(), 0, data, 5, dataLength);
		
		data[offset++] = dataType.Value();
		data[offset++] = dataLength;
		
		System.arraycopy(NerduinoHost.toBytes(value), 0, data, offset, dataLength);
		
		sendMessage(data);
	}

	
	@Override
	public void sendRegisterPointCallback(NerduinoBase requestedBy, byte addRemove, short responseToken, short index, byte filterType, byte filterLength, byte[] filterValue)
	{
		if (m_verbose)
		{
			Short sid = index;
			
			fireCommandUpdate(requestedBy, "RegisterPointCallback  " + sid.toString(), CommandMessageTypeEnum.OutgoingCommand);
		}
		
		byte[] data = new byte[11 + filterLength];
		
		short fromindex = (requestedBy != null) ? requestedBy.m_address.RoutingIndex : 0;
		
		data[0] = (byte) (fromindex / 0x100);
		data[1] = (byte) (fromindex & 0xff);
		data[2] = MessageEnum.MSG_RegisterPointCallback.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = (byte) addRemove;
		data[6] = (byte) 1;
		data[7] = (byte) (index / 0x100);
		data[8] = (byte) (index & 0xff);
		data[9] = filterType;
		data[10] = filterLength;
		
		if (filterLength > 0)
			System.arraycopy(filterValue, 0, data, 11, filterLength);
		
		sendMessage(data);
	}

	@Override
	public void sendGetPointResponse(short responseToken, LocalDataPoint point)
	{
		if (m_verbose)
			fireCommandUpdate(null, "GetPointResponse", CommandMessageTypeEnum.OutgoingCommand);

		byte[] data;
		short fromindex = 0;

		if (point == null)
		{
			data = new byte[7];
			short id = -1;
			
			data[0] = (byte) (fromindex / 0x100);
			data[1] = (byte) (fromindex & 0xff);
			data[2] = MessageEnum.MSG_GetPointResponse.Value();
			data[3] = (byte) (responseToken / 0x100);
			data[4] = (byte) (responseToken & 0xff);

			data[5] = (byte) (id / 0x100);
			data[6] = (byte) (id & 0xff);
		}
		else
		{
			byte nlength = (byte) point.getName().length();

			data = new byte[12 + nlength + point.DataLength];

			data[0] = (byte) (fromindex / 0x100);
			data[1] = (byte) (fromindex & 0xff);
			data[2] = MessageEnum.MSG_GetPointResponse.Value();
			data[3] = (byte) (responseToken / 0x100);
			data[4] = (byte) (responseToken & 0xff);

			data[5] = (byte) (point.Id / 0x100);
			data[6] = (byte) (point.Id & 0xff);
			data[7] = point.Attributes;
			data[8] = point.Status;

			char[] name = point.getName().toCharArray();

			int offset = 9;

			data[offset++] = nlength;

			for (int i = 0; i < nlength; i++)
			{
				data[offset++] = (byte) name[i];
			}

			data[offset++] = point.DataType.Value();
			data[offset++] = point.DataLength;

			switch(point.DataType)
			{
				case DT_Boolean:
				{
					Boolean v = (Boolean) point.m_value;

					if (v)
						data[offset++] = 1;
					else
						data[offset++] = 0;

					break;
				}
				case DT_Byte:
				{
					Byte v = (Byte) point.m_value;

					data[offset++] = v;

					break;
				}
				case DT_Short:
				{
					Short v = (Short) point.m_value;

					byte[] b = BitConverter.GetBytes(v);

					data[offset++] = b[0];
					data[offset++] = b[1];

					break;
				}
				case DT_Integer:
				{
					Integer v = (Integer) point.m_value;

					byte[] b = BitConverter.GetBytes(v);

					System.arraycopy(b, 0, data, offset, 4);

					break;
				}
				case DT_Float:
				{
					Float v = (Float) point.m_value;

					byte[] b = BitConverter.GetBytes(v);

					System.arraycopy(b, 0, data, offset, 4);
					
					break;
				}
				case DT_String:
				{
					String v = (String) point.m_value;

					byte[] b = v.getBytes();
					
					System.arraycopy(b, 0, data, offset, point.DataLength);
					
					break;
				}
			}
		}

		sendMessage(data);
	}

	@Override
	public void sendGetAddressResponse(short responseToken, AddressStatusEnum status, Address address, short pointIndex)
	{
		if (m_verbose)
			fireCommandUpdate(null, "GetAddressResponse", CommandMessageTypeEnum.OutgoingCommand);
		
		byte[] data = new byte[20];
		
		short fromindex = 0;
		
		data[0] = (byte) (fromindex / 0x100);
		data[1] = (byte) (fromindex & 0xff);
		data[2] = MessageEnum.MSG_GetAddressResponse.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = status.Value();
		
		byte[] sn = BitConverter.GetBytes(address.SerialNumber);
		
		data[6] = sn[0];
		data[7] = sn[1];
		data[8] = sn[2];
		data[9] = sn[3];
		data[10] = sn[4];
		data[11] = sn[5];
		data[12] = sn[6];
		data[13] = sn[7];
		
		data[14] = (byte) (address.NetworkAddress / 0x100);
		data[15] = (byte) (address.NetworkAddress & 0xff);
		
		data[16] = (byte) (address.RoutingIndex / 0x100);
		data[17] = (byte) (address.RoutingIndex & 0xff);
		
		data[18] = (byte) (pointIndex / 0x100);
		data[19] = (byte) (pointIndex & 0xff);
		
		sendMessage(data);
	}

	@Override
	public CommandResponse sendExecuteCommand(NerduinoBase requestedBy, short responseToken, byte responseDataType, byte length, byte[] command)
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < length; i++)
		{
			sb.append((char) command[i]);
		}
		
		if (m_verbose)
			fireCommandUpdate(requestedBy, "Execute  " + sb.toString(), CommandMessageTypeEnum.OutgoingCommand);
		else
			fireCommandUpdate(sb.toString(), CommandMessageTypeEnum.OutgoingCommand);
		
		byte[] data = new byte[7 + length];

//		short fromindex = (requestedBy != null) ? requestedBy.m_address.RoutingIndex : 0;
		
		short fromindex = m_address.RoutingIndex;
		
		data[0] = (byte) (fromindex / 0x100);
		data[1] = (byte) (fromindex & 0xff);
		data[2] = MessageEnum.MSG_ExecuteCommand.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = responseDataType;
		data[6] = length;

		System.arraycopy(command, 0, data, 7, length);
		
		sendMessage(data);

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
	
	@Override
	public void sendExecuteCommandResponse(short responseToken, byte status, byte dataType, byte length, byte[] response)
	{
		if (m_verbose)
			fireCommandUpdate(null, "ExecuteCommandResponse", CommandMessageTypeEnum.OutgoingCommand);
		//else
		//	fireCommandUpdate("ExecuteCommandResponse", CommandMessageTypeEnum.OutgoingCommand);
		
		byte[] data = new byte[7 + length];
		
		short fromindex = 0;
		
		data[0] = (byte) (fromindex / 0x100);
		data[1] = (byte) (fromindex & 0xff);
		data[2] = MessageEnum.MSG_ExecuteCommandResponse.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = status;
		data[6] = dataType;
		
		if (response != null)
			System.arraycopy(response, 0, data, 7, length);
		
		sendMessage(data);
	}
	
	/*
	@Override
	public void frameReceived(ZigbeeFrame f)
	{
		switch(f.FrameType)
		{
			case ATCommand:
				break;
			case ATCommandQueue:
				break;
			case ATCommandResponse:
				break;
			case CreateSourceRoute:
				break;
			case ExplicitAddressingZigbeeCommand:
				break;
			case ManyToOneRouteRequestIndicator:
				break;
			case ModemStatus:
				break;
			case NodeIdentificationIndicator:
				break;
			case OverTheAirFirmwareUpdateStatus:
				break;
			case ReceivePacket16BitAddress:
				break;
			case ReceivePacket64BitAddress:
				break;
			case RemoteATCommandRequest:
				break;
			case RemoteCommandResponse:
				break;
			case RouteRecordIndicator:
				break;
			case TransmitRequest:
			{
				TransmitRequestFrame frame = (TransmitRequestFrame) f;
				
				m_incomingAddress.SerialNumber = frame.DestinationAddress;
				m_incomingAddress.NetworkAddress = frame.DestinationNetworkAddress;
				
				processMessage(frame.Data);

				break;
			}
			case XBeeSensorReadIndicator:
				break;
			case ZigbeeExplicitRxIndicator:
				break;
			case ZigbeeIODataSampleRxIndicator:
				break;
			case ZigbeeReceivePacket:
				break;
			case ZigbeeTransmitStatus:
				break;
		}
	}
	*/
	
	@Override
	public void processMessage(NerduinoBase originator, byte[] data)
	{
		if (data.length > 0)
		{
			short routingindex = (short) (data[0] * 0x100 + data[1]);

			//m_incomingAddress.RoutingIndex = (short) (data[0] * 0x100 + data[1]);
			byte messageType = data[2];
			byte offset = 3;
			
			if (routingindex != 0 && routingindex != m_address.RoutingIndex)
			{
				// lookup the nerduino from the routing index
				int index = routingindex - 1;
				
				if (index >= 0 && index < s_nerduinos.size())
				{
					NerduinoBase nerduino = s_nerduinos.get(index);
					
					// replace the routingIndex before forwarding
					//data[0] = (byte) 0;
					//data[1] = (byte) 0;

					nerduino.forwardMessage(this, data);
				}
			}
			else
			{
				byte length = (byte) (data.length - 3);

				switch(messageType)
				{
					case 0x05: // MSG_ResetRequest(0x05)
						onResetRequest();

						break;
					case 0x06: //MSG_Ping(0x06),
						onPing(originator, data, 3);
						
						break;
					case 0x07: //MSG_PingResponse(0x07),
						onPingResponse(data, 3);
						
						break;
					case 0x08: //MSG_Checkin(0x08),
						onCheckin(data, 3);
						
						break;
					case 0x10: //MSG_ExecuteCommand(0x10),
						if (routingindex == m_address.RoutingIndex)
							onExecuteCommand(originator, data, 3);
						else
							onHostExecuteCommand(data, 3);

						break;
					case 0x11: //MSG_ExecuteCommandResponse(0x11),
						onExecuteCommandResponse(data, 3);
						
						break;
					case 0x20: //MSG_GetPoint(0x20), n/a
						if (routingindex == m_address.RoutingIndex)
							onGetPoint(originator, data, 3);
						else
							onHostGetPoint(originator, data, 3);
							
							
						break;
					case 0x21: //MSG_GetPointResponse(0x21),
						onGetPointResponse(data, 3);
						
						break;
					case 0x22: //MSG_GetPointValue(0x22)
						if (routingindex == m_address.RoutingIndex)
							onGetPointValue(originator, data, 3);
						else
							onHostGetPointValue(originator, data, 3);
							
						break;
					case 0x23: //MSG_GetPointValueResponse(0x23),
						onGetPointValueResponse(data, 3);
						
						break;
					case 0x24: //MSG_RegisterPointCallback(0x24), n/a
						if (routingindex == m_address.RoutingIndex)
							onRegisterPointCallback(originator, data, 3);
						else
							onHostRegisterPointCallback(data, 3);
						
						break;
					case 0x26: //MSG_SetPointValue(0x26), n/a
						if (routingindex == m_address.RoutingIndex)
							onSetPointValue(data, 3);
						else
							onHostSetPointValue(data, 3);
						
						break;
					case 0x30: //MSG_GetAddress(0x30),
						onGetAddress(data, 3);

						break;
					case 0x51: // LightMessage_DeclarePoint 0x51
						onLightDeclarePoint(data, 3);
						
						break;
					case 0x52: // LightMessage_RegisterPoint 0x52
						onLightRegisterPoint(data, 3);
						
						break;
					case 0x53: // LightMessage_SetProxyData 0x53
						onLightSetProxyData(data, 3);
						
						break;
					case 0x54: // LightMessage_GetProxyData 0x54
						onLightGetProxyData(data, 3);
						
						break;
					case 0x55: // LightMessage_SetPointValue 0x55
						onLightSetPointValue(data, 3);
						
						break;
					case 0x56: // LightMessage_RegisterAddress 0x56
						onLightRegisterAddress(data, 3);
						
						break;
				}
			}
		}
	}
	
	@Override
	public void onGetPointValue(NerduinoBase originator, byte[] data, int offset)
	{
		short responseToken = (short) (data[offset++] * 0x100 + data[offset++]);
		byte identifierType = data[offset++];
		
		RemoteDataPoint point;
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

			point = (RemoteDataPoint) getPoint(pname);
		}
		else // search by index
		{
			Short index = (short) (data[offset++] * 0x100 + data[offset++]);
			pid = index.toString();
			
			point = (RemoteDataPoint) getPoint(index);
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
	public void onSetPointValue(byte[] data, int offset)
	{
		byte identifierType = data[offset++];
		
		RemoteDataPoint point;
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

			point = (RemoteDataPoint) getPoint(pname);
		}
		else // search by index
		{
			Short index = (short) (data[offset++] * 0x100 + data[offset++]);
			pid = index.toString();
			
			point = (RemoteDataPoint) getPoint(index);
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
	public void onResetRequest()
	{
		if (m_verbose)
			fireCommandUpdate("N: ResetRequest", CommandMessageTypeEnum.IncomingCommand);
		
		reset();
	}
	
	public void onDeclarePoint(byte datatype, byte readonly, byte publish, String pointname)
	{
		if (m_verbose)
			fireCommandUpdate("N: DeclarePoint", CommandMessageTypeEnum.IncomingCommand);
		
		
		// TODO create a new point and add to the light client local data 
		// point collection
		
		// TODO if the point is marked for publish then publish the point 
		// with the host
	}

	public void onRegisterPoint(byte datatype, String pointpath)
	{
		if (m_verbose)
			fireCommandUpdate("N: RegisterPoint", CommandMessageTypeEnum.IncomingCommand);
		
		
		// TODO create a new remote point and add to the light client remote data 
		// point collection		
	}

	
	public void onRegisterAddress(byte index, String nerduinoname)
	{
		if (m_verbose)
			fireCommandUpdate("N: RegisterAddress", CommandMessageTypeEnum.IncomingCommand);
		
		
		// TODO make sure that this address is not already registered and known
		
		// create an uninitialize address object and place it in the 
		// list of registered addresses
		// lookup the address of the nerduino
		// if the name is not recognized then revisit the address as nerduinos
		// are added to the host and resolve at a later time
	}
	
	@Override
	public String engage()
	{
		setStatus(NerduinoStatusEnum.Uninitialized);
		
		m_engaged = false;
		m_engaging = true;
			
		fireEngageStatusUpdate(true, false, 0, "");
		
		resetBoard();
		
		// assert that the serial port was opened
//		if (!getActive())
//		{
//			String err = "Error encountered while opening the serial port!";
//			
//			fireEngageStatusUpdate(false, false, 0, err);
//			
//			m_engaging = false;
//			
//			return err;
//		}
		
		// Wait for check in
		fireEngageStatusUpdate(true, false, 20, "");
		
		// resetting the serial port should trigger a reset in the arduino.
		// the setup routine should initiate a checkin message.
		// wait up to 5 seconds for the checkin to occure, if checkin doesn't 
		// occure then the setup routine failed or did not properly call the 
		// nerduino.begin() method.. or could be configured with an inapropriate
		// baud rate
		m_checkedIn = false;
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
			String err = "This sketch failed to check in on start up, verify that nerduino.begin() was called in the sketch's setup() routine and that it was properly configured!";
			
			fireEngageStatusUpdate(false, false, 0, err);
			
			m_engaging = false;
			
			return err;
		}
		
		// wait for a ping response
		fireEngageStatusUpdate(true, false, 40, "");
		
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
			String err = "This sketch failed to respond to a ping message, verify that nerduino.process() or nerduino.processIncoming() was called in the sketch's loop() routine and that this routine is not in a blocked state!";
			
			fireEngageStatusUpdate(false, false, 0, err);
			
			m_engaging = false;
			
			return err;
		}
		
		// request metadata
		fireEngageStatusUpdate(true, false, 60, "");
		
		// gather point metadata
		fireEngageStatusUpdate(true, false, 80, "");
		
		sendGetPoints(null, (short) 0);

		wait = 0.0f;

		while (!m_receivedGetPoints && wait < 5.0f)
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

		if (!m_receivedGetPoints)
		{
			String err = "This sketch failed to provide point metadata, verify that nerduino.process() or nerduino.processIncoming() was called in the sketch's loop() routine and that this routine is not in a blocked state!";

			fireEngageStatusUpdate(false, false, 0, err);

			m_engaging = false;
			
			return err;
		}

		fireEngageStatusUpdate(false, true, 100, "");
		
		m_engaged = true;
		m_engaging = false;
			
		return null;
	}
}
