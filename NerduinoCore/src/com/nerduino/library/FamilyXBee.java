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

import com.nerduino.core.AppManager;
import com.nerduino.xbee.APIEnableEnum;
import com.nerduino.xbee.ATCommandResponseFrame;
import com.nerduino.xbee.BitConverter;
import com.nerduino.xbee.FrameReceivedListener;
import com.nerduino.xbee.TransmitRequestFrame;
import com.nerduino.xbee.XBee;
import com.nerduino.xbee.ZigbeeFrame;
import com.nerduino.xbee.ZigbeeFrameWithResponse;
import com.nerduino.xbee.ZigbeeReceivePacketFrame;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FamilyXBee extends FamilyBase implements FrameReceivedListener
{
	public static FamilyXBee Current;
	static XBee m_xbee = new XBee();
	
	int m_discoverRate = 60;

	
	public FamilyXBee()
	{
		super();
		
		Current = this;
		
		Thread discoverThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				boolean running = true;

				while (running)
				{
					try
					{
						discover();
						
						Thread.sleep(m_discoverRate * 1000);
					}
					catch(InterruptedException ie)
					{
						if (running == false)
						{
							break;
						}
					}
				}
			}
		}, "Discovery thread");

		discoverThread.setPriority(Thread.MIN_PRIORITY);
		discoverThread.start();
	}
	
	@Override
	public String getFamilyType()
	{
		return "XBee";
	}

	@Override
	public NerduinoBase CreateNerduino()
	{
		return new NerduinoXBee();
	}
	
	public int getDiscoverRate()
	{
		return m_discoverRate;
	}

	public void setDiscoverRate(int rate)
	{
		m_discoverRate = rate;
	}

	public Boolean getEnabled()
	{
		return m_xbee.getEnabled();
	}

	public void setEnabled(Boolean value)
	{
		if (value)
		{
			AppManager.log("Connecting to XBee");
			AppManager.log("CommPort: " + getComPort());
			AppManager.log("Baud Rate: " + ((Integer) getBaudRate()).toString());

			try
			{
				connect();

				if (getEnabled())
				{
					AppManager.log("Validating XBee Configuration");

					if (!validateConnection())
					{
						AppManager.log("Error Validating the XBee!  Check the Xbee configuration, insure that it is configured for API mode.");

						// attempt to connect at other baud rates
						// if another rate is validated then send api commands to change to the specified rate
						// reconnect

						// attemp to communicate using AT mode at different baud rates
						// if AT mode is detected then attempt to change the baud rate to the
						// specified rate and then change to AT Mode
						// reconnect
					}
					else
					{
						AppManager.log("Connected to XBee");
					}
				}
				else
				{
					AppManager.log("Error Connecting to XBee!  Check port configuration.");
				}
			}
			catch(Exception e)
			{
				AppManager.log("Error Connecting to XBee!  Check port configuration.");
			}
		}
		else
		{
			AppManager.log("Disconnected from XBee");

			disconnect();
		}

		if (getEnabled())
		{
			AppManager.Current.setRibbonComponentImage("Home/Host Settings/Zigbee", "com/nerduino/resources/ZigbeeEnabled.png");
		}
		else
		{
			AppManager.Current.setRibbonComponentImage("Home/Host Settings/Zigbee", "com/nerduino/resources/ZigbeeDisabled.png");
		}

		AppManager.log("");
	}

	public int getBaudRate()
	{
		return m_xbee.getBaudRate();
	}

	public void setBaudRate(int value)
	{
		m_xbee.setBaudRate(value);
	}

	public String getComPort()
	{
		return m_xbee.getComPort();
	}

	public void setComPort(String value)
	{
		m_xbee.setComPort(value);
	}

	// Methods
	public void connect(String port, int baud)
	{
		//System.out.println("Connect!");
		
		m_xbee.setComPort(port);
		m_xbee.setBaudRate(baud);
		m_xbee.setEnabled(true);
		m_xbee.addFrameReceivedListener(this);
	}

	public void connect()
	{
		//System.out.println("Connect!");
		
		m_xbee.setEnabled(true);
		
		m_xbee.addFrameReceivedListener(this);
	}
	
	public void disconnect()
	{
		//System.out.println("Disonnect!");
		
		m_xbee.removeFrameReceivedListener(this);
		
		m_xbee.setEnabled(false);
	}
	
	void discover()
	{
		m_xbee.sendNodeDiscover();
	}					
	
	public static void sendFrame(TransmitRequestFrame frame)
	{
		m_xbee.sendFrame(frame);
	}

	/*
	public void sendGetMetaData(long address, short networkaddress, short responseToken)
	{
		System.out.println("SendGetMetaData!");
		
		TransmitRequestFrame frame = new TransmitRequestFrame();
		
		frame.DestinationAddress = address;
		frame.DestinationNetworkAddress = networkaddress;
		frame.Broadcast = false;
		frame.DisableACK = true;
		
		byte[] data = new byte[3];
		
		data[0] = MessageEnum.MSG_GetMetaData.Value();
		data[1] = (byte) (responseToken / 0x100);
		data[2] = (byte) (responseToken & 0xff);
		
		frame.Data = data;
		
		m_xbee.sendFrame(frame);
	}
	
	public void sendPing(long address, short networkAddress, short responseToken)
	{
		System.out.println("SendPing!");
		
		TransmitRequestFrame frame = new TransmitRequestFrame();
		
		frame.DestinationAddress = address;
		frame.DestinationNetworkAddress = networkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;
		
		byte[] data = new byte[3];
		
		data[0] = MessageEnum.MSG_Ping.Value();
		data[1] = (byte) (responseToken / 0x100);
		data[2] = (byte) (responseToken & 0xff);
		
		frame.Data = data;
		
		m_xbee.sendFrame(frame);
	}
	*/
	
	public void sendPingResponse(long address, short networkAddress, short responseToken, byte status, byte configurationToken)
	{
		System.out.println("SendPingResponse!");
		
		TransmitRequestFrame frame = new TransmitRequestFrame();
		
		frame.DestinationAddress = address;
		frame.DestinationNetworkAddress = networkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;
		
		byte[] data = new byte[5];
		
		data[0] = MessageEnum.MSG_PingResponse.Value();
		data[1] = (byte) (responseToken / 0x100);
		data[2] = (byte) (responseToken & 0xff);
		data[3] = status;
		data[4] = configurationToken;
		
		frame.Data = data;
		
		m_xbee.sendFrame(frame);
	}
	
	/*
	public void sendExecuteCommand(long address, short networkaddress, short responseToken, byte length, byte[] command)
	{
		System.out.println("SendExecuteCommand!");
		
		TransmitRequestFrame frame = new TransmitRequestFrame();
		
		frame.DestinationAddress = address;
		frame.DestinationNetworkAddress = networkaddress;
		frame.Broadcast = false;
		frame.DisableACK = true;
		
		byte[] data = new byte[length + 4];
		
		data[0] = MessageEnum.MSG_ExecuteCommand.Value();
		data[1] = (byte) (responseToken / 0x100);
		data[2] = (byte) (responseToken & 0xff);
		data[3] = length;
		
		System.arraycopy(command, 0, data, 4, length);
		
		frame.Data = data;
		
		m_xbee.sendFrame(frame);
	}
	*/
	
	public void sendExecuteCommandResponse(long address, short networkaddress, short responseToken, byte status, byte length, byte[] response)
	{
		System.out.println("SendExecuteCommandResponse!");
		
		TransmitRequestFrame frame = new TransmitRequestFrame();
		
		frame.DestinationAddress = address;
		frame.DestinationNetworkAddress = networkaddress;
		frame.Broadcast = false;
		frame.DisableACK = true;
		
		byte[] data = new byte[length + 5];
		
		data[0] = MessageEnum.MSG_ExecuteCommandResponse.Value();
		data[1] = (byte) (responseToken / 0x100);
		data[2] = (byte) (responseToken & 0xff);
		data[3] = status;
		data[4] = length;
		
		System.arraycopy(response, 0, data, 5, length);
		
		frame.Data = data;
		
		m_xbee.sendFrame(frame);
	}
	
	/*
	public void sendGetPoint(long address, short networkAddress, short responseToken, byte idtype, byte idlength, byte[] id)
	{
		System.out.println("SendGetPoint!");
		
		TransmitRequestFrame frame = new TransmitRequestFrame();
		
		frame.DestinationAddress = address;
		frame.DestinationNetworkAddress = networkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;
		
		byte[] data = new byte[idlength + 5];
		
		data[0] = MessageEnum.MSG_GetPoint.Value();
		data[1] = (byte) (responseToken / 0x100);
		data[2] = (byte) (responseToken & 0xff);
		data[3] = idtype;
		data[4] = idlength;
		
		System.arraycopy(id, 0, data, 5, idlength);
		
		frame.Data = data;
		
		m_xbee.sendFrame(frame);
	}
	*/
	
	public void sendGetPointResponse(long address, short networkAddress, short responseToken, short pointIndex, LocalDataPoint ldp)
	{
		String name = "";
		byte attributes = 0;
		byte dataType = 0;
		byte dataLength = 0;
		byte status = 0;
		byte[] value;
		
		if (ldp != null)
		{
			name = ldp.getName();
			attributes = ldp.Attributes;
			
			dataType = ldp.DataType.Value();
			dataLength = ldp.DataLength;
			//Object val = ldp.getValue();
			
			value = NerduinoHost.toBytes(ldp.getValue());
		}
		else
		{
			value = new byte[0];
			status = 101;
		}
		
		sendGetPointResponse(address, networkAddress, responseToken, pointIndex, name, attributes, dataType, dataLength, status, value);
	}
	
	public void sendGetPointResponse(long address, short networkAddress, short responseToken, short pointIndex, String name, byte attributes, byte dataType, byte dataLength, byte status, byte[] value)
	{
		//System.out.println("SendGetPointResponse!");
		
		TransmitRequestFrame frame = new TransmitRequestFrame();
		
		frame.DestinationAddress = address;
		frame.DestinationNetworkAddress = networkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;
		
		byte nlength = (byte) name.length();
		
		byte[] data = new byte[dataLength + nlength + 10];
		
		data[0] = MessageEnum.MSG_GetPointResponse.Value();
		data[1] = (byte) (responseToken / 0x100);
		data[2] = (byte) (responseToken & 0xff);
		data[3] = (byte) (pointIndex / 0x100);
		data[4] = (byte) (pointIndex & 0xff);
		data[5] = attributes;
		data[6] = status;
		data[7] = nlength;
		
		byte[] nameBytes = name.getBytes();
		int i = 8;
		
		for (int j = 0; j < nlength; j++)
		{
			data[i++] = nameBytes[j];
		}
		
		data[i++] = dataType;
		data[i++] = dataLength;
		
		for (int j = 0; j < dataLength; j++)
		{
			data[i++] = value[j];
		}
		
		frame.Data = data;
		
		m_xbee.sendFrame(frame);
	}
	
	/*
	public void sendGetPointValue(long address, short networkaddress, short responseToken, short pointIndex)
	{
		System.out.println("SendGetPointValue!");
		
		TransmitRequestFrame frame = new TransmitRequestFrame();
		
		frame.DestinationAddress = address;
		frame.DestinationNetworkAddress = networkaddress;
		frame.Broadcast = false;
		frame.DisableACK = true;
		
		byte[] data = new byte[5];
		
		data[0] = MessageEnum.MSG_GetPointValue.Value();
		data[1] = (byte) (responseToken / 0x100);
		data[2] = (byte) (responseToken & 0xff);
		data[3] = (byte) (pointIndex / 0x100);
		data[4] = (byte) (pointIndex & 0xff);
		
		frame.Data = data;
		
		m_xbee.sendFrame(frame);
	}
	*/
	
	public void sendGetPointValueResponse(long address, short networkaddress, short responseToken, short id, byte status, byte dataType, byte dataLength, byte[] value)
	{
		System.out.println("SendGetPointValueResponse!");
		
		TransmitRequestFrame frame = new TransmitRequestFrame();
		
		frame.DestinationAddress = address;
		frame.DestinationNetworkAddress = networkaddress;
		frame.Broadcast = false;
		frame.DisableACK = true;
		
		byte[] data = new byte[8 + dataLength];
		
		data[0] = MessageEnum.MSG_GetPointValueResponse.Value();
		data[1] = (byte) (responseToken / 0x100);
		data[2] = (byte) (responseToken & 0xff);
		data[3] = (byte) (id / 0x100);
		data[4] = (byte) (id & 0xff);
		data[5] = status;
		data[6] = dataType;
		data[7] = dataLength;
		
		System.arraycopy(value, 0, data, 8, dataLength);
		
		frame.Data = data;
		
		m_xbee.sendFrame(frame);
	}
	
	/*
	public void sendRegisterPointCallback(long address, short networkaddress, short responseToken, short pointIndex, byte filterType, byte filterDataType, byte filterLength, byte[] filterValue)
	{
		System.out.println("SendRegisterPointCallback!");
		
		TransmitRequestFrame frame = new TransmitRequestFrame();
		
		frame.DestinationAddress = address;
		frame.DestinationNetworkAddress = networkaddress;
		frame.Broadcast = false;
		frame.DisableACK = true;
		
		byte[] data = new byte[8 + filterLength];
		
		data[0] = MessageEnum.MSG_RegisterPointCallback.Value();
		data[1] = (byte) (responseToken / 0x100);
		data[2] = (byte) (responseToken & 0xff);
		data[3] = (byte) (pointIndex / 0x100);
		data[4] = (byte) (pointIndex & 0xff);
		data[5] = filterType;
		data[6] = filterDataType;
		data[7] = filterLength;
		
		System.arraycopy(filterValue, 0, data, 8, filterLength);
		
		frame.Data = data;
		
		m_xbee.sendFrame(frame);
	}
	
	public void sendUnregisterPointCallback(long address, short networkaddress, short pointIndex)
	{
		System.out.println("SendUnregisterPointCallback!");
		
		TransmitRequestFrame frame = new TransmitRequestFrame();
		
		frame.DestinationAddress = address;
		frame.DestinationNetworkAddress = networkaddress;
		frame.Broadcast = false;
		frame.DisableACK = true;
		
		byte[] data = new byte[3];
		
		data[0] = MessageEnum.MSG_UnregisterPointCallback.Value();
		data[1] = (byte) (pointIndex / 0x100);
		data[2] = (byte) (pointIndex & 0xff);
		
		frame.Data = data;
		
		m_xbee.sendFrame(frame);
	}
	
	public void sendSetPointValue(long address, short networkaddress, short pointIndex, DataTypeEnum dtype, byte dlength, Object value)
	{
		System.out.println("SendSetPointValue!");
		
		TransmitRequestFrame frame = new TransmitRequestFrame();
		
		frame.DestinationAddress = address;
		frame.DestinationNetworkAddress = networkaddress;
		frame.Broadcast = false;
		frame.DisableACK = true;
		
		byte[] data = new byte[5 + dlength];
		byte[] vdata;
		
		data[0] = MessageEnum.MSG_SetPointValue.Value();
		data[1] = (byte) (pointIndex / 0x100);
		data[2] = (byte) (pointIndex & 0xff);
		data[3] = dtype.Value();
		data[4] = dlength;
		
		switch(dtype)
		{
			case DT_Boolean:
				data[5] = (byte) ((Byte) value);
				
				break;
			case DT_Byte:
				data[5] = (byte) ((Byte) value);
				
				break;
//			case DT_Array:
//			{
//				byte[] ba = (byte[]) value;
//				* 
//				System.arraycopy(ba, 0, data, 5, dlength);
//			}
//				break;
//			case DT_String:
//			{
//				vdata = NerduinoHost.toBytes(value);
//				
//				for (int i = 0; i < dlength; i++)
//				{
//					data[5 + i] = vdata[ 7 + i];
//				}
//			}
//				break;
			case DT_Short:
			{
				short fi = (Short) value;
				
				data[5] = (byte) ((fi >> 8) & 0xff);
				data[6] = (byte) (fi & 0xff);
			}
				break;
			case DT_Float:
			{
				float f = (float) ((Float) value);
				int fi = Float.floatToRawIntBits(f);
				
				data[5] = (byte) ((fi >> 24) & 0xff);
				data[6] = (byte) ((fi >> 16) & 0xff);
				data[7] = (byte) ((fi >> 8) & 0xff);
				data[8] = (byte) (fi & 0xff);
			}
				break;
			case DT_Integer:
			{
				int fi = (Integer) value;
				
				data[5] = (byte) ((fi >> 24) & 0xff);
				data[6] = (byte) ((fi >> 16) & 0xff);
				data[7] = (byte) ((fi >> 8) & 0xff);
				data[8] = (byte) (fi & 0xff);
			}
				break;
		}
		
		frame.Data = data;
		
		m_xbee.sendFrame(frame);
	}
	
	public void sendGetDeviceStatusResponse(long address, short networkaddress, short responseToken, byte status, byte configurationToken, short seconds)
	{
		System.out.println("SendGetDeviceStatusResponse!");

		TransmitRequestFrame frame = new TransmitRequestFrame();

		frame.DestinationAddress = address;
		frame.DestinationNetworkAddress = networkaddress;
		frame.Broadcast = false;
		frame.DisableACK = true;

		byte[] data = new byte[7];

		data[0] = MessageEnum.MSG_GetDeviceStatusResponse.Value();
		data[1] = (byte) (responseToken / 0x100);
		data[2] = (byte) (responseToken & 0xff);
		data[3] = status;
		data[4] = configurationToken;
		data[5] = (byte) (seconds / 0x100);
		data[6] = (byte) (seconds & 0xff);

		frame.Data = data;

		m_xbee.sendFrame(frame);
	}
	*/
	

	@Override
	public void frameReceived(ZigbeeFrame frame)
	{
//        System.out.println("Frame Received!");

		if (frame instanceof ZigbeeReceivePacketFrame)
		{
			ZigbeeReceivePacketFrame zrf = (ZigbeeReceivePacketFrame) frame;

			MessageEnum command = MessageEnum.valueOf(zrf.Data[0]);

			switch(command)
			{
				case MSG_Ping:
					onPing(zrf);

					break;
				case MSG_PingResponse:
					onPingResponse(zrf);

					break;
				case MSG_Checkin:
					onCheckin(zrf);

					break;
				case MSG_ExecuteCommand:
					onExecuteCommand(zrf);

					break;
				case MSG_ExecuteCommandResponse:
					onExecuteCommandResponse(zrf);

					break;
				case MSG_GetPoint:
					onGetPoint(zrf);

					break;
				case MSG_GetPointResponse:
					onGetPointResponse(zrf);

					break;
				case MSG_GetPointValue:
					onGetPointValue(zrf);

					break;
				case MSG_GetPointValueResponse:
					onGetPointValueResponse(zrf);

					break;
				case MSG_RegisterPointCallback:
					onRegisterPointCallback(zrf);

					break;
				case MSG_SetPointValue:
					onSetPointValue(zrf);

					break;
				default:
				{
					String data = "Command(" + command.toString() + ")";

					//OnOutput(data);

					break;
				}
			}
		}
		else if (frame instanceof ATCommandResponseFrame)
		{
			ATCommandResponseFrame rf = (ATCommandResponseFrame) frame;

			if (rf.Command.equals("ND"))
			{
				ByteBuffer bb = ByteBuffer.wrap(rf.Data);

				// parse response
				// skip first byte
				//bb.get();

				// 2 byte network address
				short networkAddress = bb.getShort();

				// 4 byte high address
				// 4 byte low address
				long serialNumber = bb.getLong();

				// look up the nerduino by address
				NerduinoXBee nerd = NerduinoManager.Current.getNerduino(serialNumber);

				if (nerd == null)
				{
					// parse the remaining data

					// name
					StringBuilder sb = new StringBuilder();

					// skip character
					bb.get();

					byte b = bb.get();

					while (b != 0)
					{
						sb.append((char) b);

						b = bb.get();
					}

					String name = sb.toString();

					// 2 byte parent address
					short parentAddress = bb.getShort();

					// device type (1 byte)
					byte deviceType = bb.get();

					// status (1 byte)
					byte status = bb.get();

					// profile (2 bytes)
					short profile = bb.getShort();

					// manufacturer (2 bytes)
					short manufacturer = bb.getShort();

					// make sure the name is unique.
					String newname = NerduinoManager.Current.getUniqueName(name);

					// if the name is not unique then remotely set the unique name
					if (!newname.equals(name))
					{
						m_xbee.sendRemoteCommand(networkAddress, serialNumber, "NI", "a" + newname);
					}

					NerduinoXBee newnerd = new NerduinoXBee();

					newnerd.setName(newname);
					newnerd.m_address.SerialNumber = serialNumber;
					newnerd.m_address.NetworkAddress = networkAddress;
					newnerd.m_parentAddress = parentAddress;

					NerduinoManager.Current.addChild(newnerd);
				}
			}
		}
	}

	private void onPing(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnPing!");

		short responseToken = BitConverter.GetShort(zrf.Data, 2);

		sendPingResponse(zrf.SourceAddress, zrf.SourceNetworkAddress, responseToken, NerduinoHost.Current.m_status, NerduinoHost.Current.m_configurationToken);
	}

	private void onPingResponse(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnPingResponse!");

		// look up the nerduino and process the response
		NerduinoXBee nerd = NerduinoManager.Current.getNerduino(zrf.SourceAddress, zrf.SourceNetworkAddress);

		if (nerd != null)
		{
			nerd.onPingResponse(zrf);
		}
	}

	private void onCheckin(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnCheckin!");

		// look up the nerduino and process the response
		NerduinoXBee nerd = NerduinoManager.Current.getNerduino(zrf.SourceAddress, zrf.SourceNetworkAddress);

		if (nerd != null)
		{
			nerd.onCheckin(zrf);
		}
		else
		{
			nerd = new NerduinoXBee();

			nerd.m_address.SerialNumber = zrf.SourceAddress;

			NerduinoManager.Current.addChild(nerd);

			nerd.onCheckin(zrf);
		}
	}

	private void onExecuteCommand(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnExecuteCommand!");

		short responseToken = BitConverter.GetShort(zrf.Data, 2);

		byte length = zrf.Data[4];

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < length; i++)
		{
			sb.append((char) zrf.Data[i + 5]);
		}

		String commandString = sb.toString();


		String response = "Say What?";
		length = (byte) response.length();

		sendExecuteCommandResponse(zrf.SourceAddress, zrf.SourceNetworkAddress, responseToken, (byte) 0, length, response.getBytes());
		// TODO call callback to resolve and respond to the requested command

		//OnExecuteCommandFromNerduino(zrf.SourceAddress, zrf.SourceNetworkAddress, commandString);
	}

	private void onExecuteCommandResponse(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnExecuteCommandResponse!");

		// look up the nerduino and process the response
		NerduinoXBee nerd = NerduinoManager.Current.getNerduino(zrf.SourceAddress, zrf.SourceNetworkAddress);

		if (nerd != null)
		{
			nerd.onExecuteCommandResponse(zrf);
		}
	}

	private void onGetPoint(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnGetPoint!");

		short responseToken = BitConverter.GetShort(zrf.Data, 2);
		byte idType = zrf.Data[4];
		byte idLength = zrf.Data[5];

		switch(idType)
		{
			case 0: // short
			{
				short index = BitConverter.GetShort(zrf.Data, 6);

				if (index < NerduinoHost.Current.getHostPointCount())
				{
					LocalDataPoint ldp = NerduinoHost.Current.getHostPoint(index);

					if (ldp != null)
					{
						sendGetPointResponse(zrf.SourceAddress, zrf.SourceNetworkAddress, responseToken, index, ldp);

						return;
					}
				}

				sendGetPointResponse(zrf.SourceAddress, zrf.SourceNetworkAddress, responseToken, (short) -1, null);

				break;
			}
			case 1: // string  - name
			{
				StringBuilder sb = new StringBuilder();

				for (int i = 0; i < idLength; i++)
				{
					sb.append((char) zrf.Data[6 + i]);
				}

				String name = sb.toString();

				for (short index = 0; index < NerduinoHost.Current.getHostPointCount(); index++)
				{
					LocalDataPoint ldp = NerduinoHost.Current.getHostPoint(index);

					if (ldp != null && ldp.getName().equals(name))
					{
						sendGetPointResponse(zrf.SourceAddress, zrf.SourceNetworkAddress, responseToken, index, ldp);

						return;
					}
				}

				sendGetPointResponse(zrf.SourceAddress, zrf.SourceNetworkAddress, responseToken, (short) -1, null);

				break;
			}
			case 2: // all points
			{
				for (short index = 0; index < NerduinoHost.Current.getHostPointCount(); index++)
				{
					LocalDataPoint ldp = NerduinoHost.Current.getHostPoint(index);

					if (ldp != null)
					{
						if (ldp != null)
						{
							sendGetPointResponse(zrf.SourceAddress, zrf.SourceNetworkAddress, responseToken, ldp.Id, ldp);

							try
							{
								Thread.sleep(50);
							}
							catch(InterruptedException ex)
							{
								Logger.getLogger(NerduinoHost.class.getName()).log(Level.SEVERE, null, ex);
							}
						}
					}
				}

				sendGetPointResponse(zrf.SourceAddress, zrf.SourceNetworkAddress, responseToken, (short) 0, null);

				break;
			}
		}
	}

	private void onGetPointResponse(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnGetPointResponse!");

		// look up the nerduino and process the response 
		NerduinoXBee nerd = NerduinoManager.Current.getNerduino(zrf.SourceAddress, zrf.SourceNetworkAddress);

		if (nerd != null)
		{
			nerd.onGetPointResponse(zrf);
		}
	}

	private void onGetPointValue(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnGetPointValue!");

		short responseToken = BitConverter.GetShort(zrf.Data, 1);
		short pointIndex = BitConverter.GetShort(zrf.Data, 3);

		if (pointIndex < 0 || pointIndex >= NerduinoHost.Current.getHostPointCount())
		{
			// return error as status in the response
			byte[] data = new byte[0];
			sendGetPointValueResponse(zrf.SourceAddress, zrf.SourceNetworkAddress, responseToken, (short) 0, PointValueStatusEnum.PVS_InvalidIndex.Value(), (byte) 0, (byte) 0, data);
		}
		else
		{
			LocalDataPoint ldp = NerduinoHost.Current.getHostPoint(pointIndex);

			byte status = ldp.Status;
			
			/*
			if (zrf.SourceAddress == ldp.OwnerAddress)
			{
				status = 100; // flag to indicate that this value should update a local data point
			}
			*/
			
			sendGetPointValueResponse(zrf.SourceAddress, zrf.SourceNetworkAddress, responseToken, ldp.Id, status, ldp.DataType.Value(), ldp.DataLength, NerduinoHost.toBytes(ldp.getValue()));
		}
	}

	private void onGetPointValueResponse(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnGetPointValueResponse!");

		// look up the nerduino and process the response 
		NerduinoXBee nerd = NerduinoManager.Current.getNerduino(zrf.SourceAddress, zrf.SourceNetworkAddress);

		if (nerd != null)
		{
			nerd.onGetPointValueResponse(zrf);
		}
	}

	private void onRegisterPointCallback(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnRegisterPointCallback!");

		//short responseToken = BitConverter.GetShort(zrf.Data, 1);
		byte addRemove = zrf.Data[3];
		short pointIndex = BitConverter.GetShort(zrf.Data, 5);
		
		if (pointIndex >= 0 && pointIndex < NerduinoHost.Current.getHostPointCount())
		{
			LocalDataPoint ldp = NerduinoHost.Current.getHostPoint(pointIndex);

			// TODO supply the unregister
			if (addRemove == 0)
				ldp.onRegisterPointCallback(zrf);
			else
				ldp.onUnregisterPointCallback(zrf);
		}
	}

	private void onSetPointValue(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnSetPointValue!");

		short pointIndex = BitConverter.GetShort(zrf.Data, 2);

		if (pointIndex >= 0 && pointIndex < NerduinoHost.Current.getHostPointCount())
		{
			LocalDataPoint ldp = NerduinoHost.Current.getHostPoint(pointIndex);
			
			ldp.onSetPointValue(zrf);
		}
	}

	private void onGetDeviceStatus(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnGetDeviceStatus!");

		short responseToken = BitConverter.GetShort(zrf.Data, 1);
		IdentifierTypeEnum itype = IdentifierTypeEnum.valueOf(zrf.Data[3]);
		byte length = zrf.Data[4];

		NerduinoXBee nerduino = null;

		switch(itype)
		{
			case IT_NetworkAddress:
			{
				short address = BitConverter.GetShort(zrf.Data, 5);

				nerduino = NerduinoManager.Current.getNerduino(address);

				break;
			}
			case IT_HardwareAddress:
			{
				long address = BitConverter.GetLong(zrf.Data, 5);

				nerduino = NerduinoManager.Current.getNerduino(address, (short) 0);

				break;
			}
			case IT_Name:
			{
				StringBuilder sb = new StringBuilder();

				for (int i = 0; i < length; i++)
				{
					sb.append((char) zrf.Data[i + 5]);
				}

				NerduinoBase nb = NerduinoManager.Current.getNerduino(sb.toString());

				if (nb instanceof NerduinoXBee)
				{
					nerduino = (NerduinoXBee) nb;
				}

				break;
			}
		}
	}

	private void onOutput(String data)
	{
		// TODO Auto-generated method stub
	}

	public void readXML(Element element)
	{
		if (element != null)
		{
			String commPort = element.getAttribute("CommPort");
			String baudRate = element.getAttribute("BaudRate");

			try
			{
				int rate = Integer.decode(baudRate);

				setComPort(commPort);
				setBaudRate(rate);
			}
			catch(Exception e)
			{
			}
			
			setEnabled(true);
		}
	}

	public void writeXML(Document doc, Element element)
	{
		element.setAttribute("CommPort", getComPort());
		element.setAttribute("BaudRate", ((Integer) getBaudRate()).toString());
	}

	public boolean validateConnection()
	{
		if (m_xbee != null && m_xbee.getEnabled())
		{
			// send API command for the zigbee mode
			int dtype = m_xbee.getDeviceType();

			if (dtype == 0)
			{
				return false;
			}


			APIEnableEnum ae = m_xbee.getAPIEnabled();
			String str = m_xbee.getNodeIdentifier();

			if (ae == APIEnableEnum.Disabled)
			{
				return false;
			}


			// if no response is received then fail validation
			// if yes then verify the response
			// verify that the xbee is configured for coordinator

			return true;
		}

		return false;
	}
	
	public static byte getNextFrameID()
	{
		return m_xbee.getNextFrameID();
	}
	
	public static void reserveFrameID(ZigbeeFrameWithResponse frame, byte frameID)
	{
		m_xbee.reserveFrameID(frame, frameID);
	}
	
	public static void releaseFrameID(byte frameID)
	{
		m_xbee.releaseFrameID(frameID);
	}
}
