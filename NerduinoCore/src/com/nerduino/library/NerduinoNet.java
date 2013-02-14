package com.nerduino.library;

import com.nerduino.processing.app.IBuildTask;
import com.nerduino.services.ServiceManager;
import com.nerduino.webhost.NerduinoSocket;
import com.nerduino.xbee.BitConverter;
import com.nerduino.xbee.FrameReceivedListener;
import com.nerduino.xbee.SerialBase;
import com.nerduino.xbee.TransmitRequestFrame;
import com.nerduino.xbee.ZigbeeFrame;
import java.awt.Image;
import javax.swing.ImageIcon;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Undefined;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class NerduinoNet extends NerduinoBase implements FrameReceivedListener
{
	NerduinoSocket m_socket;
	
	SerialBase m_serial;
	public boolean m_configured;
	boolean m_checkedIn = false;
	boolean m_pinged = false;
	boolean m_receivedMetaData = false;
	boolean m_receivedGetPoint = false;
	boolean m_receivedGetPoints = false;
	boolean m_active = false;
	long m_lastBroadcast = 0;
	long m_broadcastThrottle = 50;
	boolean m_sending = false;
	CommandResponse commandResponse;
	Address m_incomingAddress = new Address();
	
	public NerduinoNet()
	{
		super("NET", "/com/nerduino/resources/NerduinoUSBUninitialized16.png");

		m_canDelete = true;

		commandResponse = new CommandResponse();
	}
	
	public void setSocket(NerduinoSocket socket)
	{
		m_socket = socket;
	}

	@Override
	public Image getIcon(int type)
	{
		java.net.URL imgURL = null;

		switch(getStatus())
		{
			case Uninitialized:
				imgURL = getClass().getResource("/com/nerduino/resources/NerduinoUSBUninitialized16.png");
				break;
			case Online:
				imgURL = getClass().getResource("/com/nerduino/resources/NerduinoUSBOnline16.png");
				break;
			case Offline:
				imgURL = getClass().getResource("/com/nerduino/resources/NerduinoUSBOffline16.png");
				break;
			case Sleeping:
				imgURL = getClass().getResource("/com/nerduino/resources/NerduinoUSBSleeping16.png");
				break;
			case Distress:
				imgURL = getClass().getResource("/com/nerduino/resources/NerduinoUSBDistress16.png");
				break;
		}

		if (imgURL != null)
		{
			return new ImageIcon(imgURL).getImage();
		}
		else
		{
//            System.err.println("Couldn't find file: " + imgURL.toString());
			return null;
		}
	}
	
	@Override
	public void readXML(Element node)
	{
		m_name = node.getAttribute("Name");
		m_boardType = node.getAttribute("Board");

		setStatus(NerduinoStatusEnum.Offline);
	}

	@Override
	public void writeXML(Document doc, Element element)
	{
		element.setAttribute("Name", m_name);
		element.setAttribute("USB", "false");
		element.setAttribute("Board", m_boardType);
		element.setAttribute("Type", "NET");
	}

	@Override
	public PropertySet[] getPropertySets()
	{
		final Sheet.Set nerduinoSheet = Sheet.createPropertiesSet();
		final Sheet.Set pointsSheet = Sheet.createPropertiesSet();

		nerduinoSheet.setDisplayName("Nerduino Settings");
		addProperty(nerduinoSheet, String.class, null, "Description", "The description reported by the nerduino.");
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

		return new PropertySet[]
			{
				pointsSheet, nerduinoSheet
			};
	}


	@Override
	public String engage(IBuildTask task)
	{
		if (task != null)
		{
			task.setProgress(0);
		}

		
		// Wait for check in
		if (task != null)
		{
			task.setProgress(20);
		}

		// wait for a ping response
		if (task != null)
		{
			task.setProgress(40);
		}

		// wait up to 5 seconds for a ping response.  if none is received then 
		// the loop routine is not processing messages.  It may not be calling 
		// process or processIncoming or it may be in a blocking state.

		sendPing();

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

		if (!m_pinged)
		{
			if (task != null)
			{
				task.setSuccess(false);
			}

			return "This device failed to respond to a ping message, verify that the device is online.";
		}

		// request metadata
		if (task != null)
		{
			task.setProgress(60);
		}

		sendGetMetaData();

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

			return "This device failed to provide metadata, verify that the device is online.";
		}

		// gather point metadata
		if (task != null)
		{
			task.setProgress(80);
		}

		if (getPointCount() > 0)
		{
			sendGetPoints();

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

				return "This device failed to provide point metadata, verify that it is online.";
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

		return null;
	}

	@Override
	public void sendPing()
	{
		System.out.println("SendPing!");
		
		m_pinged = false;
		
		short responseToken = 0;
		
		TransmitRequestFrame frame = new TransmitRequestFrame(m_serial);
		
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
	public void sendInitialize()
	{
		System.out.println("SendInitialize!");
		
		TransmitRequestFrame frame = new TransmitRequestFrame(m_serial);
		
		frame.DestinationAddress = m_address.SerialNumber;
		frame.DestinationNetworkAddress = m_address.NetworkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;
		
		byte[] data = new byte[3];
		
		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
		data[2] = MessageEnum.MSG_Initialize.Value();
		
		frame.Data = data;
		
		sendFrame(frame);
	}
	
	@Override
	public void sendGetMetaData()
	{
		m_receivedMetaData = false;

		System.out.println("SendGetMetaData!");

		short responseToken = 0;

		TransmitRequestFrame frame = new TransmitRequestFrame(m_serial);

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
	public void sendSetMetaData()
	{
		System.out.println("SendSetMetaData!");
		
		TransmitRequestFrame frame = new TransmitRequestFrame(m_serial);
		
		frame.DestinationAddress = m_address.SerialNumber;
		frame.DestinationNetworkAddress = m_address.NetworkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;
		
		String name = getName();
		int slen = name.length();
		
		byte[] data = new byte[5 + slen];
		
		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
		data[2] = MessageEnum.MSG_SetMetaData.Value();
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
	public CommandResponse executeCommand(String command)
	{
		System.out.println("ExecuteCommand!");

		short responseToken = 0;

		TransmitRequestFrame frame = new TransmitRequestFrame(m_serial);

		frame.DestinationAddress = m_address.SerialNumber;
		frame.DestinationNetworkAddress = m_address.NetworkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;

		byte clength = (byte) command.length();
		byte[] data = new byte[6 + clength];

		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
		data[2] = MessageEnum.MSG_ExecuteCommand.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = clength;

		byte[] b = command.getBytes();

		for (int i = 0; i < clength; i++)
		{
			data[6 + i] = b[i];
		}

		frame.Data = data;

		m_serial.sendFrame(frame);

		commandResponse.Status = ResponseStatusEnum.RS_PartialResult;
		commandResponse.Data.clear();

		float wait = 0.0f;

		while (commandResponse.Status == ResponseStatusEnum.RS_PartialResult
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

		if (commandResponse.Status == ResponseStatusEnum.RS_PartialResult)
		{
			commandResponse.Status = ResponseStatusEnum.RS_Timeout;
		}


		return commandResponse;
	}

	@Override
	public void sendGetPoints()
	{
		// mark points as invalid
		for (RemoteDataPoint point : m_points)
		{
			point.Validated = false;
		}

		m_receivedGetPoints = false;

		System.out.println("SendGetPoints(All)!");

		short responseToken = 0;

		TransmitRequestFrame frame = new TransmitRequestFrame(m_serial);

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
	public void sendGetPoint(String name)
	{
		m_receivedGetPoint = false;

		System.out.println("SendGetPoint(Name)!");

		short responseToken = 0;

		TransmitRequestFrame frame = new TransmitRequestFrame(m_serial);

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
		
		char[] b = name.toCharArray();
		
		for (int i = 0; i < nlength; i++)
		{
			data[7 + i] = (byte) b[i];
		}
		
		frame.Data = data;
		
		sendFrame(frame);
	}

	@Override
	public void sendGetPointValue(short id)
	{
		System.out.println("SendGetPointValue!");
		
		short responseToken = 0;
		
		TransmitRequestFrame frame = new TransmitRequestFrame(m_serial);
		
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
	public void sendGetPointValue(String name)
	{
		System.out.println("SendGetPointValue!");
		
		short responseToken = 0;
		
		TransmitRequestFrame frame = new TransmitRequestFrame(m_serial);
		
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
		
		for(int i = 0; i < length; i++)
		{
			data[7 + i] = (byte) name.charAt(i);
		}
		
		frame.Data = data;
		
		sendFrame(frame);
	}
	
	@Override
	public void sendGetPointValueResponse(short responseToken, short id, byte status, DataTypeEnum dataType, byte dataLength, byte[] value)
	{
		System.out.println("SendGetPointValueResponse!");

		long address = 0;
		short networkAddress = 0;

		TransmitRequestFrame frame = new TransmitRequestFrame(m_serial);

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
		
		for(int i = 0; i < dataLength; i++)
		{
			data[10 + i] = value[i];
		}
		
		frame.Data = data;

		sendFrame(frame);
	}

	@Override
	public void sendSetPointValue(short index, DataTypeEnum dataType, byte dataLength, Object value)
	{
		System.out.println("SendSetPointValue!");

		TransmitRequestFrame frame = new TransmitRequestFrame(m_serial);

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
		
		switch(dataType)
		{
			case DT_Boolean:
			{
				Boolean v = (Boolean) value;
				
				data[8] = v ? (byte) 1 : (byte) 0;
				
				break;
			}
			case DT_Byte:
			{
				Byte v = (Byte) value;
				
				data[8] = v;
				
				break;
			}
			case DT_Short:
			{
				Short v = (Short) value;
				
				byte[] b = BitConverter.GetBytes(v);
				
				data[8] = b[0];
				data[9] = b[1];
				
				break;
			}
			case DT_Integer:
			{
				Integer v = (Integer) value;
				
				byte[] b = BitConverter.GetBytes(v);
				
				data[8] = b[0];
				data[9] = b[1];
				data[10] = b[2];
				data[11] = b[3];
				
				break;
			}
			case DT_Float:
			{
				Float v = (Float) value;
				
				byte[] b = BitConverter.GetBytes(v);
				
				data[8] = b[0];
				data[9] = b[1];
				data[10] = b[2];
				data[11] = b[3];
				
				break;
			}
			case DT_String:
			{
				String v = (String) value;
				
				char[] b = v.toCharArray();
				
				for (int i = 0; i < dataLength; i++)
				{
					data[8 + i] = (byte) b[i];
				}
				
				break;
			}
		}
		
		frame.Data = data;
		
		sendFrame(frame);
	}

	@Override
	public void sendRegisterPointCallback(short responseToken, short index, byte filterType, byte filterLength, byte[] filterValue)
	{
		System.out.println("SendRegisterPointCallback!");

		TransmitRequestFrame frame = new TransmitRequestFrame(m_serial);
		
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
		System.out.println("SendGetMetaDataResponse!");
		
		TransmitRequestFrame frame = new TransmitRequestFrame(m_serial);
		
		frame.DestinationAddress = address;
		frame.DestinationNetworkAddress = networkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;
		
		byte[] name = nerduino.getName().getBytes();
		
		byte nlength = (byte) name.length;
		
		byte[] data = new byte[10 + nlength];
		
		data[0] = (byte) (m_address.RoutingIndex / 0x100);
		data[1] = (byte) (m_address.RoutingIndex & 0xff);
		data[2] = MessageEnum.MSG_GetMetaDataResponse.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		
		int offset = 5;
		data[offset++] = nlength;
		
		for (int i = 0; i < nlength; i++)
		{
			data[offset++] = name[i];
		}
		
		data[offset++] = nerduino.m_status.Value();
		data[offset++] = nerduino.m_configurationToken;
		
		short count = nerduino.m_pointCount;
		
		data[offset++] = (byte) (count / 0x100);
		data[offset++] = (byte) (count & 0xff);
		data[offset++] = nerduino.m_deviceType.Value();
		
		frame.Data = data;
		
		sendFrame(frame);
	}
	
	@Override
	public void sendGetPointResponse(short responseToken, LocalDataPoint point)
	{
		System.out.println("SendGetPointResponse!");

		long address = 0;
		short networkAddress = 0;

		TransmitRequestFrame frame = new TransmitRequestFrame(m_serial);

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
	public void sendUnregisterPointCallback(short index)
	{
		System.out.println("SendUnregisterPointCallback!");
		
		TransmitRequestFrame frame = new TransmitRequestFrame(m_serial);
		
		frame.DestinationAddress = m_address.SerialNumber;
		frame.DestinationNetworkAddress = m_address.NetworkAddress;
		frame.Broadcast = false;
		frame.DisableACK = true;
		
		byte[] data = new byte[5];
		
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
		
		TransmitRequestFrame frame = new TransmitRequestFrame(m_serial);
		
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
		//NerduinoHost.Current.sendGetDeviceStatusResponse(serialNumber, networkAddress, responseToken, m_signalStrength, m_configurationToken, (short) getTimeSinceLastResponse());
	}
	
	public void sendExecuteCommandResponse(short responseToken, byte status, byte dataType, byte length, byte[] response)

	{
		System.out.println("SendExecuteCommandResponse!");

		TransmitRequestFrame frame = new TransmitRequestFrame(m_serial);

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
		
		for(int i = 0; i < length; i++)
		{
			data[8+i] = response[i];
		}
				
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
				
				processResponse(frame.Data);

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
			TransmitRequestFrame frame = new TransmitRequestFrame(m_serial);
			
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
		
		m_serial.sendFrame(frame);
		
		m_sending = false;
	}
	
	void processResponse(byte[] data)
	{
		if (data.length > 0)
		{
			m_incomingAddress.RoutingIndex = BitConverter.GetShort(data, 0);
			byte messageType = data[2];
			byte offset = 3;
			
			// check for proxy flag (7th bit if the message type)
			if (m_incomingAddress.RoutingIndex != 0 && m_incomingAddress.RoutingIndex != m_address.RoutingIndex)
			{
				forwardMessage(data);
			}
			else
			{
				byte length = (byte) (data.length - 3);
//				byte length = data[offset++];

				switch(messageType)
				{
					case 0x02: //MSG_GetNamedMetaData(0x02),
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
							sendGetMetaDataResponse(0L, (short) 0, responseToken, nerd);
						}

						break;
					}
					case 0x03: //MSG_GetMetaDataResponse(0x03),
					{
						offset += 2; // skip response token
						
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
						
						break;
					}
					case 0x05: // MSG_ResetRequest(0x05)
						// since the arduino cannot soft reset, this message is sent
						// by the arduino to ask the host to reset it remotely.
						// reset can be accomplished by closing/reopening the serial 
						// port.

						//setActive(false);
						//setActive(true);

						break;

					case 0x07: //MSG_PingResponse(0x07),
						offset += 2; // skip response token

						setStatus(NerduinoStatusEnum.valueOf(data[offset++]));
						setConfigurationToken(data[offset++]);

						m_pinged = true;

						break;
					case 0x08: //MSG_Checkin(0x08),
						setStatus(NerduinoStatusEnum.valueOf(data[offset++]));
						setConfigurationToken(data[offset++]);

						m_checkedIn = true;

						// reset the context, in case the serial port has reset and 
						// is now receiving on a new thread
						m_context = null;

						break;

					case 0x10: //MSG_ExecuteCommand(0x10),
						onExecuteCommand(data);

						break;
					case 0x11: //MSG_ExecuteCommandResponse(0x11),
					{
						short responseToken = BitConverter.GetShort(data, offset);
						offset += 2;

						ResponseStatusEnum status = ResponseStatusEnum.valueOf(data[offset++]);
						commandResponse.DataType = DataTypeEnum.valueOf(data[offset++]);
						commandResponse.DataLength = data[offset++];

						for (int j = 0; j < commandResponse.DataLength; j++)
						{
							commandResponse.Data.add(data[offset++]);
						}

						commandResponse.Status = status;

						break;
					}
					case 0x20: //MSG_GetPoint(0x20), n/a
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

						break;
					}
					case 0x21: //MSG_GetPointResponse(0x21),
					{
						try
						{
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
						RemoteDataPoint point = getPoint(index);

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
						}

						point.publish();
						}
						catch(Exception e)
						{
						}

						break;
					}
					case 0x22: //MSG_GetPointValue(0x22)
					{
						short responseToken = BitConverter.GetShort(data, offset);
						offset += 2;
						
						byte identifierType = data[offset++];
						
						LocalDataPoint point = null;
						
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
						
						break;
					}
					case 0x23: //MSG_GetPointValueResponse(0x23),
					{
						short responseToken = BitConverter.GetShort(data, offset);
						offset += 2;


						short index = BitConverter.GetShort(data, offset);
						offset += 2;

						// lookup this index, to see if this point is already known
						RemoteDataPoint point = getPoint(index);

						if (point != null)
						{
							point.onGetPointValueResponse(data);
						}

						break;
					}
					case 0x24: //MSG_RegisterPointCallback(0x24), n/a
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


						break;
					}
					case 0x25: //MSG_UnregisterPointCallback(0x25), n/a
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
						
						break;
					}
					case 0x26: //MSG_SetPointValue(0x26), n/a
					{						
						break;
					}
					case 0x30: //MSG_GetAddress(0x30),
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
						String name = null;
						String pointName = null;
						AddressStatusEnum status = AddressStatusEnum.FormatError;
						NerduinoBase nerduino = null;
						short pointIndex = -1;
						Address address = new Address();
						
						// if the path includes a '.' delimiter then lookup an associated point
						if (path.contains("."))
						{
							String[] parts = path.split(".");
							
							if (parts.length == 2)
							{
								name = parts[0];
								pointName = parts[1];
							}
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
									RemoteDataPoint point = nerduino.getPoint(pointName);
									
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
						
						break;
					}
					
					case 0x40: //MSG_GetDeviceStatus(0x40), n/a
						break;				
				}
			}
		}
	}

	void onExecuteCommand(byte[] data)
	{
		int offset = 3;

		short responseToken = BitConverter.GetShort(data, offset);
		offset += 2;

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
						case 4: // long
						{
							Long b = Long.parseLong(v);
							
							responseLength = 8;
							response = BitConverter.GetBytes(b);
							
							break;
						}
						case 5: // float
						{
							Float b = Float.parseFloat(v);
							
							responseLength = 4;
							response = BitConverter.GetBytes(b);
							
							break;
						}
						case 6: // string
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
					case 4: // long
					{
						Long b = fval.longValue();
						
						responseLength = 8;
						response = BitConverter.GetBytes(b);
						
						break;
					}
					case 5: // float
					{
						responseLength = 4;
						response = BitConverter.GetBytes(fval);

						break;
					}
					case 6: // string
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
					case 4: // long
					{
						Long b = l.longValue();
						
						responseLength = 8;
						response = BitConverter.GetBytes(b);
						
						break;
					}
					case 5: // float
					{
						Float b = l.floatValue();
						
						responseLength = 4;
						response = BitConverter.GetBytes(b);
						
						break;
					}
					case 6: // string
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
}
