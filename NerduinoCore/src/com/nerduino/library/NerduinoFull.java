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
import com.nerduino.xbee.FrameReceivedListener;
import com.nerduino.xbee.SerialBase;
import com.nerduino.xbee.TransmitRequestFrame;
import com.nerduino.xbee.ZigbeeFrame;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NerduinoFull extends NerduinoBase implements FrameReceivedListener
{
	SerialBase m_serialBase;
	
	boolean m_active = false;
	long m_lastBroadcast = 0;
	boolean m_sending = false;
	long m_broadcastThrottle = 50;
	Address m_incomingAddress = new Address();
	RemoteDataPoint m_pointResponse = null;

	
	public NerduinoFull(String name, String icon)
	{
		super(name, icon);
	}
	
	public boolean getActive()
	{
		return m_serialBase.getEnabled();
	}

	public void setActive(Boolean value)
	{
		m_serialBase.setEnabled(value);
	}	

	
	@Override
	public void readXML(Element node)
	{
		m_name = node.getAttribute("Name");
		m_sketch = node.getAttribute("Sketch");
		m_boardType = node.getAttribute("Board");

		m_interactive = Boolean.valueOf(node.getAttribute("Interactive"));

		setStatus(NerduinoStatusEnum.Offline);
	}

	@Override
	public void writeXML(Document doc, Element element)
	{
		element.setAttribute("Name", m_name);
		element.setAttribute("Sketch", m_sketch);
		element.setAttribute("Board", m_boardType);
		element.setAttribute("Interactive", Boolean.toString(m_interactive));
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
								case DT_String:
									prop = new PropertySupport.Reflection(point, String.class, "String");
									break;
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
        byte length = (byte) name.length();
        
    	sendGetPoint(this, (short) 0, (byte) 0, (byte) length, name.getBytes()); 
        
        m_pointResponse = null;
        
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
	
	@Override
    public void setConfigurationToken(byte configurationToken)
    {
    	if (m_configurationToken != configurationToken)
    	{
    		m_configurationToken = configurationToken;
    		
            getMetaData();
            
    		// get an updated property list
    		getPoints();
    		
    		// TODO notify callback that the configuration has changed
    	}
    }
    
	
	@Override
	public void sendPing(NerduinoBase requestedBy, short responseToken)
	{
		//System.out.println("SendPing!");
		
		m_pinged = false;
		
		TransmitRequestFrame frame = new TransmitRequestFrame();

		frame.DestinationAddress = m_address.SerialNumber;
		frame.DestinationNetworkAddress = m_address.NetworkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;

		byte[] data = new byte[5];

		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
		data[2] = MessageEnum.MSG_Ping.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);

		frame.Data = data;

		sendFrame(frame);
	}
	
	@Override
	public void sendPingResponse(short responseToken, byte status, byte configurationToken)
	{
		//System.out.println("SendPingResponse!");
		
		TransmitRequestFrame frame = new TransmitRequestFrame();

		frame.DestinationAddress = m_address.SerialNumber;
		frame.DestinationNetworkAddress = m_address.NetworkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;

		byte[] data = new byte[7];

		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
		data[2] = MessageEnum.MSG_PingResponse.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = status;
		data[6] = configurationToken;

		frame.Data = data;

		sendFrame(frame);
	}
	
	@Override
	public void sendGetMetaData(NerduinoBase requestedBy, short responseToken)
	{
		//System.out.println("SendGetMetaData!");

		m_receivedMetaData = false;

		TransmitRequestFrame frame = new TransmitRequestFrame();

		frame.DestinationAddress = m_address.SerialNumber;
		frame.DestinationNetworkAddress = m_address.NetworkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;

		byte[] data = new byte[5];

		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
		data[2] = MessageEnum.MSG_GetMetaData.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);

		frame.Data = data;

		sendFrame(frame);
	}
	

	@Override
	public void sendSetName()
	{
		//System.out.println("SendSetMetaData!");
		
		TransmitRequestFrame frame = new TransmitRequestFrame();

		frame.DestinationAddress = m_address.SerialNumber;
		frame.DestinationNetworkAddress = m_address.NetworkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;

		String name = getName();
		int slen = name.length();

		byte[] data = new byte[5 + slen];

		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
		data[2] = MessageEnum.MSG_SetName.Value();
		data[3] = 1; // name only		
		data[4] = (byte) slen;

		for (int i = 0; i < slen; i++)
		{
			data[5 + i] = (byte) name.charAt(i);
		}

		frame.Data = data;

		sendFrame(frame);
	}

	@Override
	public void sendGetPoints(NerduinoBase requestedBy, short responseToken)
	{
		// mark points as invalid
		for (RemoteDataPoint point : m_points)
		{
			point.Validated = false;
		}

		m_receivedGetPoints = false;

		TransmitRequestFrame frame = new TransmitRequestFrame();

		frame.DestinationAddress = m_address.SerialNumber;
		frame.DestinationNetworkAddress = m_address.NetworkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;

		byte[] data = new byte[7];

		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
		data[2] = MessageEnum.MSG_GetPoint.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = PointIdentifierTypeEnum.PIT_All.Value();

		frame.Data = data;

		sendFrame(frame);
	}

	@Override
	public void sendGetPoint(NerduinoBase requestedBy, short responseToken, byte idtype, byte idlength, byte[] id)
	{
		//System.out.println("SendGetPoint!");
		
		TransmitRequestFrame frame = new TransmitRequestFrame();
		
		frame.DestinationAddress = m_address.SerialNumber;
		frame.DestinationNetworkAddress = m_address.NetworkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;
		
		byte[] data = new byte[idlength + 7];
		
		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
		data[2] = MessageEnum.MSG_GetPoint.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = idtype;
		data[6] = idlength;
		
		System.arraycopy(id, 0, data, 7, idlength);
		
		frame.Data = data;
		
		sendFrame(frame);
	}
	
	@Override
	public void sendGetPoint(NerduinoBase requestedBy, short responseToken, String name)
	{
		m_receivedGetPoint = false;

		TransmitRequestFrame frame = new TransmitRequestFrame();

		frame.DestinationAddress = m_address.SerialNumber;
		frame.DestinationNetworkAddress = m_address.NetworkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;

		byte nlength = (byte) name.length();

		byte[] data = new byte[7 + nlength];

		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
		data[2] = MessageEnum.MSG_GetPoint.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = PointIdentifierTypeEnum.PIT_Name.Value();
		data[6] = nlength;

		byte[] b = name.getBytes();
		
		System.arraycopy(b, 0, data, 7, nlength);
		
		frame.Data = data;

		sendFrame(frame);
	}

	@Override
	public void sendGetPointValue(NerduinoBase requestedBy, short responseToken, short id)
	{
		TransmitRequestFrame frame = new TransmitRequestFrame();

		frame.DestinationAddress = m_address.SerialNumber;
		frame.DestinationNetworkAddress = m_address.NetworkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;

		byte[] data = new byte[8];

		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
		data[2] = MessageEnum.MSG_GetPointValue.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = (byte) 1; // identifier type 1 - by index
		data[6] = (byte) (id / 0x100);
		data[7] = (byte) (id & 0xff);

		frame.Data = data;

		sendFrame(frame);
	}
	
	@Override
	public void sendGetPointValue(NerduinoBase requestedBy, short responseToken, String name)
	{
		TransmitRequestFrame frame = new TransmitRequestFrame();

		frame.DestinationAddress = m_address.SerialNumber;
		frame.DestinationNetworkAddress = m_address.NetworkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;

		byte length = (byte) name.length();

		byte[] data = new byte[7 + length];

		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
		data[2] = MessageEnum.MSG_GetPointValue.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = (byte) 0; // identifier type 0 - by name
		data[6] = length;
		
		byte[] bytes = name.getBytes();
		
		System.arraycopy(bytes, 0, data, 7, length);

		frame.Data = data;

		sendFrame(frame);
	}
	
	@Override
	public void sendGetPointValueResponse(short responseToken, short id, byte status, DataTypeEnum dataType, byte dataLength, byte[] value)
	{
		//System.out.println("SendGetPointValueResponse!");

		long address = 0;
		short networkAddress = 0;

		TransmitRequestFrame frame = new TransmitRequestFrame();

		frame.DestinationAddress = address;
		frame.DestinationNetworkAddress = networkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;

		byte[] data = new byte[10 + dataLength];

		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
		data[2] = MessageEnum.MSG_GetPointValueResponse.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = (byte) (id / 0x100);
		data[6] = (byte) (id & 0xff);
		data[7] = status;
		data[8] = dataType.Value();
		data[9] = dataLength;
		
		System.arraycopy(value, 0, data, 10, dataLength);

		frame.Data = data;

		sendFrame(frame);
	}

	@Override
	public void sendSetPointValue(short index, DataTypeEnum dataType, byte dataLength, Object value)
	{
		//System.out.println("SendSetPointValue!");

		TransmitRequestFrame frame = new TransmitRequestFrame();

		frame.DestinationAddress = m_address.SerialNumber;
		frame.DestinationNetworkAddress = m_address.NetworkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;

		byte[] data = new byte[8 + dataLength];

		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
		data[2] = MessageEnum.MSG_SetPointValue.Value();

		data[3] = (byte) 1; // identifier type
		data[4] = (byte) (index / 0x100);
		data[5] = (byte) (index & 0xff);
		data[6] = dataType.Value();
		data[7] = dataLength;
		
		byte[] vdata = NerduinoHost.toBytes(value);
		
		System.arraycopy(vdata, 0, data, 8, dataLength);
		
		frame.Data = data;

		sendFrame(frame);
	}

	@Override
	public void sendRegisterPointCallback(NerduinoBase requestedBy, short responseToken, short index, byte filterType, byte filterLength, byte[] filterValue)
	{
		//System.out.println("SendRegisterPointCallback!");

		TransmitRequestFrame frame = new TransmitRequestFrame();

		frame.DestinationAddress = m_address.SerialNumber;
		frame.DestinationNetworkAddress = m_address.NetworkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;

		byte[] data = new byte[10 + filterLength];

		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
		data[2] = MessageEnum.MSG_RegisterPointCallback.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);

		data[5] = (byte) 1;
		data[6] = (byte) (index / 0x100);
		data[7] = (byte) (index & 0xff);
		data[8] = filterType;
		data[9] = filterLength;

		if (filterLength > 0)
			System.arraycopy(filterValue, 0, data, 10, filterLength);

		frame.Data = data;

		sendFrame(frame);
	}

	void sendGetMetaDataResponse(long address, short networkAddress, short responseToken, NerduinoBase nerduino)
	{
		//System.out.println("SendGetMetaDataResponse!");
		
		TransmitRequestFrame frame = new TransmitRequestFrame();

		frame.DestinationAddress = address;
		frame.DestinationNetworkAddress = networkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;

		byte[] name = nerduino.getName().getBytes();

		byte nlength = (byte) name.length;

		byte[] data = new byte[11 + nlength];

		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
		data[2] = MessageEnum.MSG_GetMetaDataResponse.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);

		int offset = 5;
		data[offset++] = nlength;

		System.arraycopy(name, 0, data, offset, nlength);
		offset += nlength;
		
		data[offset++] = nerduino.m_status.Value();
		data[offset++] = nerduino.m_configurationToken;

		short count = nerduino.m_pointCount;

		data[offset++] = (byte) (count / 0x100);
		data[offset++] = (byte) (count & 0xff);

		if (nerduino.m_deviceType == null)
			nerduino.m_deviceType = DeviceTypeEnum.DT_USB;

		data[offset++] = nerduino.m_deviceType.Value();

		frame.Data = data;

		sendFrame(frame);
	}
	
	@Override
	public void sendGetPointResponse(short responseToken, LocalDataPoint point)
	{
		//System.out.println("SendGetPointResponse!");

		long address = 0;
		short networkAddress = 0;

		TransmitRequestFrame frame = new TransmitRequestFrame();

		frame.DestinationAddress = address;
		frame.DestinationNetworkAddress = networkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;

		byte[] data;

		if (point == null)
		{
			data = new byte[7];
			short id = -1;

			data[0] = (byte) (m_address.RoutingIndex / 0x100);
			data[1] = (byte) (m_address.RoutingIndex & 0xff);
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

			data[0] = (byte) (m_address.RoutingIndex / 0x100);
			data[1] = (byte) (m_address.RoutingIndex & 0xff);
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
					{
						data[offset++] = 1;
					}
					else
					{
						data[offset++] = 0;
					}

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

					data[offset++] = b[0];
					data[offset++] = b[1];
					data[offset++] = b[2];
					data[offset++] = b[3];

					break;
				}
				case DT_Float:
				{
					Float v = (Float) point.m_value;

					byte[] b = BitConverter.GetBytes(v);

					data[offset++] = b[0];
					data[offset++] = b[1];
					data[offset++] = b[2];
					data[offset++] = b[3];

					break;
				}
				case DT_String:
				{
					String v = (String) point.m_value;

					byte[] b = v.getBytes();

					for (int i = 0; i < point.DataLength; i++)
					{
						data[offset++] = (byte) b[i];
					}

					break;
				}
			}
		}

		frame.Data = data;

		sendFrame(frame);
	}

	@Override
	public void sendUnregisterPointCallback(NerduinoBase requestedBy, short index)
	{
		//System.out.println("SendUnregisterPointCallback!");

		TransmitRequestFrame frame = new TransmitRequestFrame();

		frame.DestinationAddress = m_address.SerialNumber;
		frame.DestinationNetworkAddress = m_address.NetworkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;

		byte[] data = new byte[6];

		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
		data[2] = MessageEnum.MSG_UnregisterPointCallback.Value();
		data[3] = (byte) 1; // identifier type
		data[4] = (byte) (index / 0x100);
		data[5] = (byte) (index & 0xff);

		frame.Data = data;

		sendFrame(frame);
	}
	
	@Override
	public void sendGetAddressResponse(short responseToken, AddressStatusEnum status, Address address, short pointIndex)
	{
		System.out.println("SendUnregisterPointCallback!");

		TransmitRequestFrame frame = new TransmitRequestFrame();

		frame.DestinationAddress = m_address.SerialNumber;
		frame.DestinationNetworkAddress = m_address.NetworkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;

		byte[] data = new byte[20];

		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
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


		frame.Data = data;

		sendFrame(frame);
	}

	@Override
	public void sendGetDeviceStatusResponse(long serialNumber, short networkAddress, short responseToken)
	{
		TransmitRequestFrame frame = new TransmitRequestFrame();
		
		frame.DestinationAddress = serialNumber;
		frame.DestinationNetworkAddress = networkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;
		
		byte[] data = new byte[9];
		
		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
		data[2] = MessageEnum.MSG_GetDeviceStatusResponse.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = m_status.Value();
		data[6] = m_configurationToken;
		
		short seconds = (short) getTimeSinceLastResponse();
		
		data[7] = (byte) (seconds / 0x100);
		data[8] = (byte) (seconds & 0xff);

		frame.Data = data;

		sendFrame(frame);
	}
	
	@Override
	public CommandResponse sendExecuteCommand(NerduinoBase requestedBy, short responseToken, byte responseDataType, byte length, byte[] command)
	{
		TransmitRequestFrame frame = new TransmitRequestFrame();

		frame.DestinationAddress = m_address.SerialNumber;
		frame.DestinationNetworkAddress = m_address.NetworkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;

		byte[] data = new byte[6 + length];

		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
		data[2] = MessageEnum.MSG_ExecuteCommand.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = length;

		System.arraycopy(command, 0, data, 6, length);

		frame.Data = data;

		m_serialBase.sendFrame(frame);

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
	
	@Override
	public void sendExecuteCommandResponse(short responseToken, byte status, byte dataType, byte length, byte[] response)
	{
		//System.out.println("SendExecuteCommandResponse!");

		TransmitRequestFrame frame = new TransmitRequestFrame();

		frame.DestinationAddress = m_address.SerialNumber;
		frame.DestinationNetworkAddress = m_address.NetworkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;

		byte[] data = new byte[8 + length];

		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
		data[2] = MessageEnum.MSG_ExecuteCommandResponse.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = status;
		data[6] = dataType;
		data[7] = length;
		
		System.arraycopy(response, 0, data, 8, length);

		frame.Data = data;

		sendFrame(frame);
	}


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
	
	void forwardMessage(byte[] data)
	{
		// lookup the proxy target..
		int index = m_incomingAddress.RoutingIndex - 1;
		
		if (index >= 0 && index < s_nerduinos.size())
		{
			NerduinoBase nerduino = (NerduinoBase) s_nerduinos.get(index);
			
			// replace the routingIndex of this packet with this nerduino's index
			data[0] = (byte) (m_address.RoutingIndex / 0x100);
			data[1] = (byte) (m_address.RoutingIndex & 0xff);
			
			// repackage the message data
			TransmitRequestFrame frame = new TransmitRequestFrame();
			
			frame.DestinationAddress = nerduino.m_address.SerialNumber;
			frame.DestinationNetworkAddress = nerduino.m_address.NetworkAddress;
			frame.Broadcast = false;
			frame.DisableACK = true;
			
			frame.Data = data;
			
			// send the message			
			nerduino.sendFrame(frame);			
		}
	}

	@Override
	public void sendFrame(ZigbeeFrame frame)
	{
		m_sending = true;
		
		// throttle broadcasts to prevent overloading the nerduino
		while(System.currentTimeMillis() - m_lastBroadcast < m_broadcastThrottle)
		{
			try
			{
				Thread.sleep(10);
			}
			catch(Exception e)
			{
			}
		}
		
		m_lastBroadcast = System.currentTimeMillis();
		
		m_serialBase.sendFrame(frame);
		
		m_sending = false;
	}
	
	void processMessage(byte[] data)
	{
		if (data.length > 0)
		{
			m_incomingAddress.RoutingIndex = BitConverter.GetShort(data, 0);
			byte messageType = data[2];
			byte offset = 3;
			
			if (m_incomingAddress.RoutingIndex != 0 && m_incomingAddress.RoutingIndex != m_address.RoutingIndex)
			{
				forwardMessage(data);
			}
			else
			{
				byte length = (byte) (data.length - 3);

				switch(messageType)
				{
					case 0x03: //MSG_GetMetaDataResponse(0x03),
						onGetMetaDataResponse(data, 3);
						
						break;
					case 0x05: // MSG_ResetRequest(0x05)
						onResetRequest();

						break;
					case 0x07: //MSG_PingResponse(0x07),
						onPingResponse(data, 3);
						
						break;
					case 0x08: //MSG_Checkin(0x08),
						onCheckin(data, 3);
						
						break;
					case 0x10: //MSG_ExecuteCommand(0x10),
						onExecuteCommand(data, 3);

						break;
					case 0x11: //MSG_ExecuteCommandResponse(0x11),
						onExecuteCommandResponse(data, 3);
						
						break;
					case 0x20: //MSG_GetPoint(0x20), n/a
						onGetPoint(data, 3);
						
						break;
					case 0x21: //MSG_GetPointResponse(0x21),
						onGetPointResponse(data, 3);
						
						break;
					case 0x22: //MSG_GetPointValue(0x22)
						onGetPointValue(data, 3);
						
						break;
					case 0x23: //MSG_GetPointValueResponse(0x23),
						onGetPointValueResponse(data, 3);
						
						break;
					case 0x24: //MSG_RegisterPointCallback(0x24), n/a
						onRegisterPointCallback(data, 3);
						
						break;
					case 0x25: //MSG_UnregisterPointCallback(0x25), n/a
						onUnregisterPointCallback(data, 3);
						
						break;
					case 0x26: //MSG_SetPointValue(0x26), n/a
						onSetPointValue(data, 3);
						
						break;
					case 0x30: //MSG_GetAddress(0x30),
						onGetAddress(data, 3);

						break;
					case 0x40: //MSG_GetDeviceStatus(0x40), n/a
						onGetDeviceStatus(data, 3);
						
						break;
					case 0x51: // LightMessage_DeclarePoint 0x51
						onLightDeclarePoint(data, 3);
						
						break;
					case 0x52: // LightMessage_RegisterPoint 0x52
						onLightDeclarePoint(data, 3);
						
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
	public void onResetRequest()
	{
		setActive(false);
		setActive(true);
	}

	public void onDeclarePoint(byte datatype, byte readonly, byte publish, String pointname)
	{
		// TODO create a new point and add to the light client local data 
		// point collection
		
		// TODO if the point is marked for publish then publish the point 
		// with the host
	}

	public void onRegisterPoint(byte datatype, String pointpath)
	{
		// TODO create a new remote point and add to the light client remote data 
		// point collection		
	}

	
	
	public void onSetPointValue(byte[] data)
	{
		short pointindex = (short) ((int) data[0] * 0x100 + (int) data[1]);
		byte datalength = data[2];
		
		// TODO validate the index
		// make sure that the point is not a remote data point marked as readonly
		// get the datatype / length
		// parse the data value
		// set the point's current value
		// make sure that local points trigger callbacks
		// make sure that remote points push the updated value (if it has changed)
	}
	
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

		// request metadata
		if (task != null)
		{
			task.setProgress(60);
		}

		sendGetMetaData(null, (short) 0);

		wait = 0.0f;

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

		if (!m_receivedMetaData)
		{
			if (task != null)
			{
				task.setSuccess(false);
			}
			
			if (compileButton != null)
			{
				compileButton.setEngaged(false);
			}

			return "This sketch failed to provide metadata, verify that nerduino.process() or nerduino.processIncoming() was called in the sketch's loop() routine and that this routine is not in a blocked state!";
		}

		// gather point metadata
		if (task != null)
		{
			task.setProgress(80);
		}

		if (getPointCount() > 0)
		{
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
				if (task != null)
				{
					task.setSuccess(false);
				}

				if (compileButton != null)
				{
					compileButton.setEngaged(false);
				}

				
				return "This sketch failed to provide point metadata, verify that nerduino.process() or nerduino.processIncoming() was called in the sketch's loop() routine and that this routine is not in a blocked state!";
			}
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
}
