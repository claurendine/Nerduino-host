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

import com.nerduino.processing.app.ArduinoManager;
import com.nerduino.processing.app.Preferences;
import com.nerduino.processing.app.SerialException;
import com.nerduino.processing.app.Sketch;
import com.nerduino.processing.app.debug.RunnerException;
import com.nerduino.propertybrowser.BaudRatePropertyEditor;
import com.nerduino.propertybrowser.ComPortPropertyEditor;
import com.nerduino.propertybrowser.DeviceTypePropertyEditor;
import com.nerduino.propertybrowser.SketchPropertyEditor;
import com.nerduino.xbee.FrameReceivedListener;
import com.nerduino.xbee.Serial;
import java.awt.Image;
import javax.swing.ImageIcon;
import org.openide.nodes.Sheet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class NerduinoUSB extends NerduinoLight implements FrameReceivedListener
{
	Serial m_serial;
	String m_comPort;
	int m_baudRate = 115200;
	NerduinoBase[] m_targets = new NerduinoBase[255];
	NerduinoUSB m_nerduino;
	
	public NerduinoUSB()
	{
		super("USB", "/com/nerduino/resources/NerduinoUSBUninitialized16.png");

		m_canDelete = true;
		m_nerduino = this;
		
		m_serial = new Serial()
		{
			@Override
			public void processNerduinoMessage(byte target, byte addressIndex, byte message, byte length, byte[] m_data)
			{
				m_nerduino.processMessage(target, addressIndex, message, length, m_data);
			}
		};
		
		m_serialBase = m_serial;
		m_serial.Protocol = 1;
		
		m_serial.addFrameReceivedListener(this);
	}

	void processMessage(byte targethandler, byte addressIndex, byte message, byte length, byte[] data)
	{
		if (targethandler == 0) // host message
		{
			// lookup the nerduino associated with the addressIndex
			
			NerduinoBase nerduino = null;
			
			if (addressIndex > 0)
				nerduino = m_targets[addressIndex];
			
			switch(message)
			{
				case 0x06: // MSG_Ping;
				{
					if (nerduino != null)
						nerduino.sendPing(this, addressIndex);
				}
					break;
				
				case 0x10: // MSG_ExecuteCommand;
				{
					// TODO validate the payload format 
					if (nerduino != null)
					{
						byte returndatatype = data[1];
						byte commandlength = data[2];
						
						byte[] bytes = new byte[commandlength];
						
						System.arraycopy(data, 3, bytes, 0, commandlength);
						
						CommandResponse response = nerduino.sendExecuteCommand(this, (short) 0, returndatatype, commandlength, bytes);
						
						// TODO report the response to the client
					}
					else
						onExecuteCommand(data, 0);
				}
					break;
				case 0x11: // MSG_ExecuteCommandResponse;
				{
					// TODO validate the payload format 
					if (nerduino != null)
					{
						short responseToken = (short) ((int) data[1] * 0x100 + (int) data[2]);
						byte status = data[3];
						byte datatype = data[4];
						byte datalength = data[5];
						byte[] bytes = new byte[datalength];
						
						System.arraycopy(data, 6, bytes, 0, datalength);
						
						nerduino.sendExecuteCommandResponse(responseToken, status, datatype, datalength, bytes);
					}
					else
						onExecuteCommandResponse(data, 0);
				}
					break;
					
				case 0x22: // MSG_GetPointValue;
				{
					// TODO validate the payload format 
					if (nerduino != null)
					{
						byte namelength = data[1];
						
						StringBuilder sb = new StringBuilder();
						byte offset = 2;
						
						for(int i = 0; i < namelength; i++)
						{
							sb.append((char) data[offset++]);
						}
						
						String pointname = sb.toString();
						
						nerduino.sendGetPointValue(this, (short) 0, pointname);
					}
					else
						onGetPointValue(data, 1);
				}
					break;
				case 0x26: // MSG_SetPointValue;
				{
					// TODO validate the payload format 					
					if (nerduino != null)
					{
						short pointindex = (short) ((int) data[1] * 0x100 + (int) data[2]);
						
						DataTypeEnum datatype = DataTypeEnum.valueOf(data[3]);
						byte datalength = data[4];
						
						byte[] bytes = new byte[datalength];
						
						System.arraycopy(data, 5, bytes, 0, datalength);
						
						nerduino.sendSetPointValue(pointindex, datatype, datalength, bytes);
					}
					else
						onSetPointValue(data, 1);
				}
					break;

				case 0x40: // MSG_GetDeviceStatus;
					// TODO validate the payload format 
					if (nerduino != null)
					{
						short responseToken = (short) ((int) data[1] * 0x100 + (int) data[2]);
						
						// TODO  make sure that this message is intended to 
						// originate from a nerduino, if so make sure that other 
						// protocols also react to this message
						//nerduino.sendGetDeviceStatus(responseToken);
					}
					else
						onGetDeviceStatus(data, 1);
					break;
			}
		}
		else // proxy message
		{
			switch(message)
			{
				case 0x05: // MSG_ResetRequest;
					onResetRequest();
					break;
				case 0x07: // MSG_PingResponse
					onPingResponse(data, 0);					
					break;
				case 0x08: // MSG_Checkin
					onCheckin(data, 0);					
					break;
				case 0x30: // MSG_GetAddress;
					onGetAddress(data, 0);
					break;
				case 0x51: // LMSG_DeclarePoint
					onLightDeclarePoint(data, 0);
					break;
				case 0x52: // LMSG_RegisterPoint
					onLightRegisterPoint(data, 0);
					break;
				case 0x53: // LMSG_SetProxyData
					onLightSetProxyData(data, 0);
					break;
				case 0x54: // LMSG_GetProxyData
					onLightGetProxyData(data, 0);
					break;
				case 0x55: // LMSG_SetPointValue
					onLightSetPointValue(data, 0);
					break;
				case 0x56: // LMSG_RegisterAddress
					onLightRegisterAddress(data, 0);
					break;
			}
		}
	}
	
	public void onLightSetProxyData(byte[] data, short offset)
	{
		// TODO validate the payload format
		byte dataid = data[offset++];
		byte datalength = data[offset++];

		byte[] bytes = new byte[datalength];

		System.arraycopy(data, offset, bytes, 0, datalength);
	}
	
	@Override
	public void onLightGetProxyData(byte[] data, int offset)
	{
		// TODO validate the payload format
		byte dataid = data[offset++];

		// TODO add this handler
		//onGetTransceiverData(dataid);
	}
	
	@Override
	public void onLightDeclarePoint(byte[] data, int offset)
	{
		byte publish = data[offset++];
		byte datatype = data[offset++];
		byte readonly = data[offset++];
		byte namelength = data[offset++];
		
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < namelength; i++)
		{
			sb.append((char) data[offset++]);
		}
		
		String pointname = sb.toString();
		
		DataTypeEnum dtype = DataTypeEnum.valueOf(datatype);
		byte datalength = dtype.getLength();
		
		Object value = NerduinoHost.parseValue(data, offset, dtype, datalength);
		
		onDeclarePoint(datatype, readonly, publish, pointname, value);
	}
	
	public void	onLightRegisterAddress(byte[] data, int offset)
	{
		byte index = data[offset++];
		byte namelength = data[offset++];

		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < namelength; i++)
		{
			sb.append((char) data[offset++]);
		}

		String nerduinoname = sb.toString();

		onRegisterAddress(index, nerduinoname);
	}
	
	public void onRegisterPoint(byte[] data, int offset)
	{
		byte datatype = data[offset++];
		byte pathlength = data[offset++];
		
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < pathlength; i++)
		{
			sb.append((char) data[offset++]);
		}
		
		String pointpath = sb.toString();
		
		onRegisterPoint(datatype, pointpath);
	}	
	
	byte[] outBuffer = new byte[128];
	byte[] endBuffer = new byte[3];
	
	@Override
	public void sendMessage(byte message, byte length, byte[] data)
	{
		if (m_serial.getEnabled())
		{
			outBuffer[0] = 0x7e;
			outBuffer[1] = 0x0;
			outBuffer[2] = (byte) (length + 1);
			outBuffer[3] = message;
			
			m_serial.writeData(outBuffer, 4);
			m_serial.writeData(data, length);
			//byte offset = 4;
			
			//for(int i = 0; i < length; i++)
			//	outBuffer[offset++] = data[i];
			
			//m_serial.writeData(outBuffer, length + 4);
		}
	}

	
		
	public String getComPort()
	{
		return m_comPort;
	}

	public void setComPort(String port)
	{
		m_comPort = port;

		setActive(false);

		m_serial.setComPort(port);

		save();
	}

	public int getBaudRate()
	{
		return m_baudRate;
	}

	public void setBaudRate(int value)
	{
		m_baudRate = value;
		setActive(false);

		m_serial.setBaudRate(value);

		save();
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
		super.readXML(node);

		m_comPort = node.getAttribute("Port");
		
		m_baudRate = Integer.valueOf(node.getAttribute("BaudRate"));
		
		m_serial.setBaudRate(m_baudRate);
		m_serial.setComPort(m_comPort);
	}

	@Override
	public void writeXML(Document doc, Element element)
	{
		super.writeXML(doc, element);
		
		element.setAttribute("Port", m_comPort);
		element.setAttribute("BaudRate", Integer.toString(m_baudRate));
		element.setAttribute("USB", "true");
		element.setAttribute("Type", "USB");
	}

	@Override
	public PropertySet[] getPropertySets()
	{
		final Sheet.Set usbSheet = Sheet.createPropertiesSet();
		
		usbSheet.setDisplayName("Device Settings");
		
		addProperty(usbSheet, String.class, ComPortPropertyEditor.class, "ComPort", "The serial port used to communicate to the arduino device.");
		addProperty(usbSheet, int.class, BaudRatePropertyEditor.class, "BaudRate", "The baud rate used to communicate to the arduino device.");
		addProperty(usbSheet, String.class, DeviceTypePropertyEditor.class, "BoardType", "The arduino board type.");
		addProperty(usbSheet, String.class, SketchPropertyEditor.class, "Sketch", "The arduino sketch.");
		addProperty(usbSheet, Boolean.class, null, "Active", "");
		
		PropertySet[] basesets = super.getPropertySets();
		PropertySet[] sets = new PropertySet[basesets.length + 1];
		
		System.arraycopy(basesets, 0, sets, 0, basesets.length);
		
		sets[basesets.length] = usbSheet;
		
		return sets;
	}

	@Override
	public String upload(Sketch sketch)
	{
		m_points.clear();
		
		m_serial.setEnabled(false);

		Preferences.set("serial.port", m_comPort);

		try
		{
			String classname;

			classname = sketch.upload(ArduinoManager.Current.getBuildPath(), sketch.getPrimaryClassName(), false);

			if (classname == null)
			{
				return "Upload failed for unknown reason!";
			}
			else
			{
				return null;
			}
		}
		catch(RunnerException ex)
		{
			return ex.getMessage();
		}
		catch(SerialException ex)
		{
			return ex.getMessage();
		}
	}
}
