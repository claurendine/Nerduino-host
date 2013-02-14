package com.nerduino.library;

import com.nerduino.core.AppManager;
import com.nerduino.nodes.TreeNode;
import com.nerduino.processing.app.ArduinoManager;
import com.nerduino.propertybrowser.BaudRatePropertyEditor;
import com.nerduino.propertybrowser.ComPortPropertyEditor;
import com.nerduino.webhost.WebHost;
import com.nerduino.xbee.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.logging.*;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class NerduinoHost extends TreeNode implements FrameReceivedListener
{
	// Declarations
	Children m_hostPoints;
	protected XBee m_xbee = new XBee();
	protected NerduinoManager m_manager;
	byte m_configurationToken = 0;
	byte m_status = 0;
	int m_discoverRate = 60;
	int m_sleepScanRate = 5;
	public static NerduinoHost Current;

	// Constructors
	public NerduinoHost()
	{
		super(new Children.Array(), "Host", "/com/nerduino/resources/Host16.png");

		m_hostPoints = this.getChildren();
		m_hasEditor = false;

		Current = this;

		m_manager = new NerduinoManager();

		Thread discoverThread = new Thread(new Runnable()
		{
			public void run()
			{
				boolean running = true;

				while (running)
				{
					try
					{
						m_xbee.sendNodeDiscover();

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

		Thread sleepThread = new Thread(new Runnable()
		{
			public void run()
			{
				boolean running = true;

				while (running)
				{
					try
					{
						//m_xbee.sendNodeDiscover();
						m_manager.scan();

						Thread.sleep(m_sleepScanRate * 1000);
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
		}, "Sleep thread");

		sleepThread.setPriority(Thread.MIN_PRIORITY);
		sleepThread.start();
	}

	public int getDiscoverRate()
	{
		return m_discoverRate;
	}

	public void setDiscoverRate(int rate)
	{
		m_discoverRate = rate;
	}
	
	public String getDataPath()
	{
		return "/Users/chaselaurendine/Documents/Nerduino";
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
		System.out.println("Connect!");


		m_xbee.setComPort(port);
		m_xbee.setBaudRate(baud);

		m_xbee.setEnabled(true);


		m_xbee.addFrameReceivedListener(this);

	}

	public void connect()
	{
		System.out.println("Connect!");

		m_xbee.setEnabled(true);

		m_xbee.addFrameReceivedListener(this);
	}

	public void disconnect()
	{
		System.out.println("Disonnect!");

		m_xbee.removeFrameReceivedListener(this);

		m_xbee.setEnabled(false);
	}

	public void sendInitialize(long address, short networkAddress)
	{
		System.out.println("SendInitialize!");

		TransmitRequestFrame frame = new TransmitRequestFrame(m_xbee);

		frame.DestinationAddress = address;
		frame.DestinationNetworkAddress = networkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;

		byte[] data = new byte[1];

		data[0] = MessageEnum.MSG_Initialize.Value();

		frame.Data = data;

		m_xbee.sendFrame(frame, (byte) 0);
	}

	public void sendGetMetaData(long address, short networkaddress, short responseToken)
	{
		System.out.println("SendGetMetaData!");

		TransmitRequestFrame frame = new TransmitRequestFrame(m_xbee);

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

	public void sendGetMetaDataResponse(long address, short networkAddress, short responseToken, byte length, byte[] metadata)
	{
		System.out.println("SendGetMetaDataResponse!");

		TransmitRequestFrame frame = new TransmitRequestFrame(m_xbee);

		frame.DestinationAddress = address;
		frame.DestinationNetworkAddress = networkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;

		byte[] data = new byte[4 + length];

		data[0] = MessageEnum.MSG_GetMetaDataResponse.Value();
		data[1] = (byte) (responseToken / 0x100);
		data[2] = (byte) (responseToken & 0xff);
		data[3] = length;

		System.arraycopy(metadata, 0, data, 4, length);

		frame.Data = data;

		m_xbee.sendFrame(frame);
	}

	public void sendPing(long address, short networkAddress, short responseToken)
	{
		System.out.println("SendPing!");

		TransmitRequestFrame frame = new TransmitRequestFrame(m_xbee);

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

	public void sendPingResponse(long address, short networkAddress, short responseToken, byte status, byte configurationToken)
	{
		System.out.println("SendPingResponse!");

		TransmitRequestFrame frame = new TransmitRequestFrame(m_xbee);

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

	public void sendExecuteCommand(long address, short networkaddress, short responseToken, byte length, byte[] command)
	{
		System.out.println("SendExecuteCommand!");

		TransmitRequestFrame frame = new TransmitRequestFrame(m_xbee);

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

	public void sendExecuteCommandResponse(long address, short networkaddress, short responseToken, byte status, byte length, byte[] response)
	{
		System.out.println("SendExecuteCommandResponse!");

		TransmitRequestFrame frame = new TransmitRequestFrame(m_xbee);

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

	public void sendGetPoint(long address, short networkAddress, short responseToken, byte idtype, byte idlength, byte[] id)
	{
		System.out.println("SendGetPoint!");

		TransmitRequestFrame frame = new TransmitRequestFrame(m_xbee);

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

			value = toBytes(ldp.getValue());
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
		System.out.println("SendGetPointResponse!");

		TransmitRequestFrame frame = new TransmitRequestFrame(m_xbee);

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

	public void sendGetPointValue(long address, short networkaddress, short responseToken, short pointIndex)
	{
		System.out.println("SendGetPointValue!");

		TransmitRequestFrame frame = new TransmitRequestFrame(m_xbee);

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

	public void sendGetPointValueResponse(long address, short networkaddress, short responseToken, short id, byte status, byte dataType, byte dataLength, byte[] value)
	{
		System.out.println("SendGetPointValueResponse!");

		TransmitRequestFrame frame = new TransmitRequestFrame(m_xbee);

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

	public void sendRegisterPointCallback(long address, short networkaddress, short responseToken, short pointIndex, byte filterType, byte filterDataType, byte filterLength, byte[] filterValue)
	{
		System.out.println("SendRegisterPointCallback!");

		TransmitRequestFrame frame = new TransmitRequestFrame(m_xbee);

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

		TransmitRequestFrame frame = new TransmitRequestFrame(m_xbee);

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

		TransmitRequestFrame frame = new TransmitRequestFrame(m_xbee);

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
			case DT_Array:
			{
				byte[] ba = (byte[]) value;

				System.arraycopy(ba, 0, data, 5, dlength);
			}
			break;
			case DT_String:
			{
				vdata = toBytes(value);

				for (int i = 0; i < dlength; i++)
				{
					data[5 + i] = vdata[ 7 + i];
				}
			}
			break;
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

		TransmitRequestFrame frame = new TransmitRequestFrame(m_xbee);

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

	public static byte[] toBytes(Object object)
	{
		if (object instanceof Boolean)
		{
			byte[] data = new byte[1];

			data[0] = (byte) ((Byte) object);

			return data;
		}

		if (object instanceof Byte)
		{
			byte[] data = new byte[1];

			data[0] = (byte) ((Byte) object);

			return data;
		}

		if (object instanceof Short)
		{
			byte[] data = new byte[2];

			short val = (short) ((Short) object);

			data[0] = (byte) ((val >> 8) & 0xff);
			data[1] = (byte) (val & 0xff);

			return data;
		}

		if (object instanceof Integer)
		{
			byte[] data = new byte[4];

			int val = (int) ((Integer) object);

			data[0] = (byte) ((val >> 24) & 0xff);
			data[1] = (byte) ((val >> 16) & 0xff);
			data[2] = (byte) ((val >> 8) & 0xff);
			data[3] = (byte) (val & 0xff);

			return data;
		}


		if (object instanceof Long)
		{
			byte[] data = new byte[8];

			long val = (long) ((Long) object);

			data[0] = (byte) ((val >> 56) & 0xff);
			data[1] = (byte) ((val >> 48) & 0xff);
			data[2] = (byte) ((val >> 40) & 0xff);
			data[3] = (byte) ((val >> 32) & 0xff);
			data[4] = (byte) ((val >> 24) & 0xff);
			data[5] = (byte) ((val >> 16) & 0xff);
			data[6] = (byte) ((val >> 8) & 0xff);
			data[7] = (byte) (val & 0xff);

			return data;
		}

		if (object instanceof Float)
		{
			byte[] data = new byte[4];

			float f = (float) ((Float) object);
			int fi = Float.floatToRawIntBits(f);

			data[0] = (byte) ((fi >> 24) & 0xff);
			data[1] = (byte) ((fi >> 16) & 0xff);
			data[2] = (byte) ((fi >> 8) & 0xff);
			data[3] = (byte) (fi & 0xff);

			return data;
		}

		if (object instanceof String)
		{
			String s = (String) object;

			return s.getBytes();
		}

		if (object instanceof byte[])
		{
			return (byte[]) object;
		}

		return null;
	}

	public static Object parseValue(byte[] data, int offset, DataTypeEnum dtype, byte dlength)
	{
		Object value = null;

		if (dlength == 0)
		{
			return null;
		}

		switch(dtype)
		{
			case DT_Boolean:
				value = (data[offset] != 0);
				break;
			case DT_Byte:
				value = data[offset];
				break;
			case DT_Array:
			{
				byte[] vdata = new byte[dlength];

				for (int i = 0; i < dlength; i++)
				{
					vdata[i] = data[offset + i];
				}

				value = vdata;
			}
			break;
			case DT_Float:
				//value = convertByteArrayToFloat(data, offset);
				value = BitConverter.GetFloat(data, offset);

				break;
			case DT_Short:
				value = BitConverter.GetShort(data, offset);

				break;
			case DT_Integer:
				value = BitConverter.GetInt(data, offset);

				break;
			case DT_String:
			{
				StringBuilder sb = new StringBuilder();

				for (int i = 0; i < dlength; i++)
				{
					sb.append((char) data[offset + i]);
				}

				value = sb.toString();
			}

			break;
		}

		return value;
	}

	public static float convertByteArrayToFloat(byte[] b, int offset)
	{
		if (b.length < offset + 4)
		{
			return 0.0F;
		}

		DataInputStream dis = null;

		float f = 0.0F;

		try
		{
			dis = new DataInputStream(new ByteArrayInputStream(b));

			dis.skipBytes(offset);

			// Effectively performs:
			// * readInt call
			// * Float.intBitsToFloat call
			f = dis.readFloat();
		}
		catch(EOFException e)
		{
			// Handle ...
		}
		catch(IOException e)
		{
			// Handle ...
		}
		finally
		{
			if (dis != null)
			{
				try
				{
					dis.close();
				}
				catch(IOException e)
				{
				}
			}
		}

		return f;
	}

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
				case MSG_Initialize:
					onInitialize(zrf);

					break;
				case MSG_GetMetaData:
					onGetMetaData(zrf);

					break;
				case MSG_GetMetaDataResponse:
					onGetMetaDataResponse(zrf);

					break;
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
				case MSG_UnregisterPointCallback:
					onUnregisterPointCallback(zrf);

					break;
				case MSG_SetPointValue:
					onSetPointValue(zrf);

					break;
				case MSG_GetDeviceStatus:
					onGetDeviceStatus(zrf);

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
				NerduinoZigbee nerd = m_manager.getNerduino(serialNumber);

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
					String newname = m_manager.getUniqueName(name);

					// if the name is not unique then remotely set the unique name
					if (!newname.equals(name))
					{
						m_xbee.sendRemoteCommand(networkAddress, serialNumber, "NI", "a" + newname);
					}

					NerduinoZigbee newnerd = new NerduinoZigbee();

					newnerd.setName(newname);
					newnerd.m_serialNumber = serialNumber;
					newnerd.m_networkAddress = networkAddress;
					newnerd.m_parentAddress = parentAddress;

					m_manager.addChild(newnerd);
				}
			}
		}
	}

	private void onInitialize(ZigbeeReceivePacketFrame zrf)
	{
		// Nothing to do:  this message should be ignored by the host.  
		// It should only be implemented by Nerduinos

		System.out.println("OnInitialize!");

	}

	private void onGetMetaData(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnGetMetaData!");

		short responseToken = BitConverter.GetShort(zrf.Data, 2);

		short count = (short) m_hostPoints.getNodesCount();

		byte length = 15;
		byte[] data =
		{
			4, 'H', 'o', 's', 't', 4, 'H', 'o', 's', 't',
			m_configurationToken,
			(byte) ((count >> 8) & 0xff),
			(byte) (count & 0xff),
			DeviceTypeEnum.DT_Host.Value(), m_status
		};

		sendGetMetaDataResponse(zrf.SourceAddress, zrf.SourceNetworkAddress, responseToken, length, data);
	}

	private void onGetMetaDataResponse(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnGetMetaDataResponse!");

		// look up the nerduino and process the response
		NerduinoZigbee nerd = m_manager.getNerduino(zrf.SourceAddress, zrf.SourceNetworkAddress);

		if (nerd != null)
		{
			nerd.onGetMetaDataResponse(zrf);
		}
	}

	private void onPing(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnPing!");

		short responseToken = BitConverter.GetShort(zrf.Data, 2);

		sendPingResponse(zrf.SourceAddress, zrf.SourceNetworkAddress, responseToken, m_status, m_configurationToken);
	}

	private void onPingResponse(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnPingResponse!");

		// look up the nerduino and process the response
		NerduinoZigbee nerd = m_manager.getNerduino(zrf.SourceAddress, zrf.SourceNetworkAddress);

		if (nerd != null)
		{
			nerd.onPingResponse(zrf);
		}
	}

	private void onCheckin(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnCheckin!");

		// look up the nerduino and process the response
		NerduinoZigbee nerd = m_manager.getNerduino(zrf.SourceAddress, zrf.SourceNetworkAddress);

		if (nerd != null)
		{
			nerd.onCheckin(zrf);
		}
		else
		{
			nerd = new NerduinoZigbee();

			nerd.m_serialNumber = zrf.SourceAddress;

			m_manager.addChild(nerd);

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
		NerduinoZigbee nerd = m_manager.getNerduino(zrf.SourceAddress, zrf.SourceNetworkAddress);

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

				if (index < m_hostPoints.getNodesCount())
				{
					LocalDataPoint ldp = (LocalDataPoint) m_hostPoints.getNodeAt(index);

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

				for (short index = 0; index < m_hostPoints.getNodesCount(); index++)
				{
					LocalDataPoint ldp = (LocalDataPoint) m_hostPoints.getNodeAt(index);

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
				for (short index = 0; index < m_hostPoints.getNodesCount(); index++)
				{
					LocalDataPoint ldp = (LocalDataPoint) m_hostPoints.getNodeAt(index);

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
		NerduinoZigbee nerd = m_manager.getNerduino(zrf.SourceAddress, zrf.SourceNetworkAddress);

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

		if (pointIndex < 0 || pointIndex >= m_hostPoints.getNodesCount())
		{
			// return error as status in the response
			byte[] data = new byte[0];
			sendGetPointValueResponse(zrf.SourceAddress, zrf.SourceNetworkAddress, responseToken, (short) 0, PointValueStatusEnum.PVS_InvalidIndex.Value(), (byte) 0, (byte) 0, data);
		}
		else
		{
			LocalDataPoint ldp = (LocalDataPoint) m_hostPoints.getNodeAt(pointIndex);

			byte status = ldp.Status;
			
			/*
			if (zrf.SourceAddress == ldp.OwnerAddress)
			{
				status = 100; // flag to indicate that this value should update a local data point
			}
			*/
			
			sendGetPointValueResponse(zrf.SourceAddress, zrf.SourceNetworkAddress, responseToken, ldp.Id, status, ldp.DataType.Value(), ldp.DataLength, toBytes(ldp.getValue()));
		}
	}

	private void onGetPointValueResponse(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnGetPointValueResponse!");

		// look up the nerduino and process the response 
		NerduinoZigbee nerd = m_manager.getNerduino(zrf.SourceAddress, zrf.SourceNetworkAddress);

		if (nerd != null)
		{
			nerd.onGetPointValueResponse(zrf);
		}
	}

	private void onRegisterPointCallback(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnRegisterPointCallback!");

		//short responseToken = BitConverter.GetShort(zrf.Data, 1);
		short pointIndex = BitConverter.GetShort(zrf.Data, 4);

		if (pointIndex >= 0 && pointIndex < m_hostPoints.getNodesCount())
		{
			LocalDataPoint ldp = (LocalDataPoint) m_hostPoints.getNodeAt(pointIndex);

			ldp.onRegisterPointCallback(zrf);
		}
	}

	private void onUnregisterPointCallback(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnUnregisterPointCallback!");

		short pointIndex = BitConverter.GetShort(zrf.Data, 2);

		if (pointIndex >= 0 && pointIndex < m_hostPoints.getNodesCount())
		{
			LocalDataPoint ldp = (LocalDataPoint) m_hostPoints.getNodeAt(pointIndex);

			ldp.onUnregisterPointCallback(zrf);
		}
	}

	private void onSetPointValue(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnSetPointValue!");

		short pointIndex = BitConverter.GetShort(zrf.Data, 2);

		if (pointIndex >= 0 && pointIndex < m_hostPoints.getNodesCount())
		{
			LocalDataPoint ldp = (LocalDataPoint) m_hostPoints.getNodeAt(pointIndex);

			ldp.onSetPointValue(zrf);
		}
	}

	private void onGetDeviceStatus(ZigbeeReceivePacketFrame zrf)
	{
		System.out.println("OnGetDeviceStatus!");

		short responseToken = BitConverter.GetShort(zrf.Data, 1);
		IdentifierTypeEnum itype = IdentifierTypeEnum.valueOf(zrf.Data[3]);
		byte length = zrf.Data[4];

		NerduinoZigbee nerduino = null;

		switch(itype)
		{
			case IT_NetworkAddress:
			{
				short address = BitConverter.GetShort(zrf.Data, 5);

				nerduino = m_manager.getNerduino(address);

				break;
			}
			case IT_HardwareAddress:
			{
				long address = BitConverter.GetLong(zrf.Data, 5);

				nerduino = m_manager.getNerduino(address, (short) 0);

				break;
			}
			case IT_Name:
			{
				StringBuilder sb = new StringBuilder();

				for (int i = 0; i < length; i++)
				{
					sb.append((char) zrf.Data[i + 5]);
				}

				NerduinoBase nb = m_manager.getNerduino(sb.toString());

				if (nb instanceof NerduinoZigbee)
				{
					nerduino = (NerduinoZigbee) nb;
				}

				break;
			}
		}

		if (nerduino != null)
		{
			nerduino.sendGetDeviceStatusResponse(zrf.SourceAddress, zrf.SourceNetworkAddress, responseToken);
		}
	}

	private void onOutput(String data)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void readXML(Element node)
	{
		if (node != null)
		{
			NodeList nl = node.getElementsByTagName("NerduinoHost");

			if (nl != null && nl.getLength() > 0)
			{
				Element config = (Element) nl.item(0);

				String commPort = config.getAttribute("CommPort");
				String baudRate = config.getAttribute("BaudRate");

				try
				{
					int rate = Integer.decode(baudRate);

					setComPort(commPort);
					setBaudRate(rate);
				}
				catch(Exception e)
				{
				}
			}

			setEnabled(true);
		}
	}

	@Override
	public void writeXML(Document doc, Element node)
	{
		Element element = doc.createElement("NerduinoHost");

		element.setAttribute("CommPort", getComPort());
		element.setAttribute("BaudRate", ((Integer) getBaudRate()).toString());

		node.appendChild(element);
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

	@Override
	public PropertySet[] getPropertySets()
	{
		final Sheet.Set webSheet = Sheet.createPropertiesSet();
		final Sheet.Set xbeeSheet = Sheet.createPropertiesSet();
		final Sheet.Set arduinoSheet = Sheet.createPropertiesSet();

		try
		{
			xbeeSheet.setDisplayName("Zigbee Device Settings");

			Property enabledProp = new PropertySupport.Reflection(this, Boolean.class, "Enabled");
			enabledProp.setName("Enabled");
			enabledProp.setShortDescription("Enable/Disable the communications through the zigbee device. See the output window for diagnostics.");
			xbeeSheet.put(enabledProp);

			PropertySupport.Reflection portProp = new PropertySupport.Reflection(this, String.class, "ComPort");
			portProp.setName("Com Port");
			portProp.setShortDescription("The serial port used to communicate to the zigbee device.");
			portProp.setPropertyEditorClass(ComPortPropertyEditor.class);
			xbeeSheet.put(portProp);

			PropertySupport.Reflection baudProp = new PropertySupport.Reflection(this, int.class, "BaudRate");
			baudProp.setName("Baud Rate");
			baudProp.setShortDescription("The baud rate used to communicate to the zigbee device.");
			baudProp.setPropertyEditorClass(BaudRatePropertyEditor.class);
			xbeeSheet.put(baudProp);

			Property discoveryProp = new PropertySupport.Reflection(this, int.class, "DiscoverRate");
			discoveryProp.setName("Discovery Rate");
			discoveryProp.setShortDescription("The interval in seconds that the zigbee radio poles for nearby zigbee devices.");
			xbeeSheet.put(discoveryProp);


			webSheet.setDisplayName("Local Web Server Settings");

			Property webEnabledProp = new PropertySupport.Reflection(WebHost.Current, Boolean.class, "Enabled");
			webEnabledProp.setName("Enable/Disable the Web Server.");
			webEnabledProp.setShortDescription("Enable/Disable");
			webSheet.put(webEnabledProp);

			Property webPortProp = new PropertySupport.Reflection(WebHost.Current, int.class, "Port");
			webPortProp.setName("Port");
			webPortProp.setShortDescription("The network port used to listen for incoming web requests.");
			webSheet.put(webPortProp);

			Property webRootProp = new PropertySupport.Reflection(WebHost.Current, String.class, "WebRoot");
			webRootProp.setName("Root Path");
			webRootProp.setShortDescription("The local file path that the Web Server uses to serve content from.");
			webSheet.put(webRootProp);

			Property httpAddressProp = new PropertySupport.Reflection(WebHost.Current, String.class, "HttpAddress");
			httpAddressProp.setName("Http Address");
			httpAddressProp.setShortDescription("The http path to engage this web server.");
			webSheet.put(httpAddressProp);

			arduinoSheet.setDisplayName("Arduino Settings");

			Property arduinoSourcePathProp = new PropertySupport.Reflection(ArduinoManager.Current, String.class, "ArduinoPath");
			arduinoSourcePathProp.setName("Arduino Path");
			arduinoSourcePathProp.setShortDescription("Installed Arduino Data Path");
			arduinoSheet.put(arduinoSourcePathProp);
		}
		catch(NoSuchMethodException ex)
		{
			int i = 0;
		}

		return new PropertySet[]
				{
					arduinoSheet, xbeeSheet, webSheet
				};
	}
}
