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

import processing.app.ArduinoManager;
import processing.app.Preferences;
import processing.app.SerialException;
import processing.app.Sketch;
import processing.app.debug.RunnerException;
import com.nerduino.propertybrowser.BaudRatePropertyEditor;
import com.nerduino.propertybrowser.ComPortPropertyEditor;
import com.nerduino.propertybrowser.DeviceTypePropertyEditor;
import com.nerduino.propertybrowser.SketchPropertyEditor;
import com.nerduino.xbee.Serial;
import java.awt.Image;
import javax.swing.ImageIcon;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Sheet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class NerduinoUSB extends NerduinoLight
{
	Serial m_serial;
	String m_comPort;
	int m_baudRate = 115200;
	NerduinoUSB m_nerduino;
	
	byte[] outBuffer = new byte[128];
	byte[] endBuffer = new byte[3];
	
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
		
		m_serial.Protocol = 1;
	}

	@Override
	public void openComPort()
	{
		m_serial.setEnabled(true);
	}
	
	@Override
	public void closeComPort()
	{
		m_serial.setEnabled(false);
	}

	@Override
	public boolean isReadyToSend()
	{
		return m_serial.getEnabled();
	}

	@Override
	public boolean configureNewNerduino()
	{
		setName(getUniqueName(getName()));
		
		// show the configure dialog
		NerduinoUSBConfigDialog dialog = new NerduinoUSBConfigDialog(new javax.swing.JFrame(), true);
		
		dialog.setNerduinoUSB(this);
		dialog.setVisible(true);
		
		return (dialog.m_nerduino != null);
	}

	void processMessage(byte targethandler, byte addressIndex, byte message, byte length, byte[] data)
	{
		if (targethandler == 0) // host message
		{
			// lookup the nerduino associated with the addressIndex
			
			NerduinoBase nerduino = null;
			
			if (addressIndex > 0)
				nerduino = m_addresses[addressIndex];
			
			switch(message)
			{
				case 0x06: // MSG_Ping;
					if (nerduino != null)
					{
						if (m_verbose)
							fireCommandUpdate(this, "Ping", CommandMessageTypeEnum.OutgoingCommand);

						short responseToken = (short) ((int) data[0] * 0x100 + (int) data[1]);
						
						nerduino.sendPing(this, responseToken);
					}
					break;				
				case 0x10: // MSG_ExecuteCommand;
				{
					short responseToken = (short) ((int) data[0] * 0x100 + (int) data[1]);

					byte returndatatype = data[2];
					byte commandlength = data[3];

					byte[] bytes = new byte[commandlength];

					System.arraycopy(data, 4, bytes, 0, commandlength);


					StringBuilder sb = new StringBuilder();

					for (int i = 0; i < commandlength; i++)
					{
						sb.append((char) data[4 + i]);
					}

					if (m_verbose)
						fireCommandUpdate(this, "Execute  " + sb.toString(), CommandMessageTypeEnum.OutgoingCommand);
					else
						fireCommandUpdate(sb.toString(), CommandMessageTypeEnum.OutgoingCommand);

					
					if (nerduino != null)
					{
						CommandResponse response = nerduino.sendExecuteCommand(this, responseToken, returndatatype, commandlength, bytes);

						// TODO report the response to the client
						byte[] rdata = new byte[response.DataLength];

						if (response.Status == ResponseStatusEnum.RS_Complete)
						{
							for(int i = 0; i < response.DataLength; i++)
							{
								rdata[i] = response.Data.get(i);
							}
						}
						
						sendExecuteCommandResponse(responseToken, response.Status.Value(), response.DataType.Value(), (byte) response.DataLength, rdata);
					}
					
					//TODO  add code to execute a script on the host
//						onExecuteCommand(nerduino, data, 0);
//					else
//						onHostExecuteCommand(data, 0);
				}
					break;
				case 0x11: // MSG_ExecuteCommandResponse;
					if (m_verbose)
						fireCommandUpdate(this, "ExecuteCommandResponse", CommandMessageTypeEnum.OutgoingCommand);

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
				
					break;	
				case 0x22: // MSG_GetPointValue;
					if (m_verbose)
						fireCommandUpdate(this, "GetPointValue", CommandMessageTypeEnum.OutgoingCommand);

					if (nerduino != null)
					{
						short responseToken = (short) ((int) data[0] * 0x100 + (int) data[1]);
						byte lookuptype = data[2];
						
						if (lookuptype == 0) // by name
						{
							byte namelength = data[3];
							
							StringBuilder sb = new StringBuilder();
							byte offset = 4;
							
							for(int i = 0; i < namelength; i++)
							{
								sb.append((char) data[offset++]);
							}
							
							String pointname = sb.toString();
							
							nerduino.sendGetPointValue(this, responseToken, pointname);						
						}
						else if (lookuptype == 1) // by index
						{
							short pointid = (short) ((int) data[3] * 0x100 + (int) data[4]);
							
							nerduino.sendGetPointValue(this, responseToken, pointid);
						}
					}
					else
						onHostGetPointValue(this, data, 0);
				
					break;
				case 0x26: // MSG_SetPointValue;
					if (m_verbose)
						fireCommandUpdate(this, "SetPointValue", CommandMessageTypeEnum.OutgoingCommand);

					if (nerduino != null)
					{
						byte lookuptype = data[0];
						
						if (lookuptype == 0) // by name
						{
							byte namelength = data[1];
							
							StringBuilder sb = new StringBuilder();
							byte offset = 2;
							
							for(int i = 0; i < namelength; i++)
							{
								sb.append((char) data[offset++]);
							}
							
							String pointname = sb.toString();
							
							DataTypeEnum dtype = DataTypeEnum.valueOf(data[offset++]);
							byte datalength = dtype.getLength();
							byte[] bytes = new byte[datalength];
							
							System.arraycopy(data, offset, bytes, 0, datalength);
							
							nerduino.sendSetPointValue(null, pointname, dtype, bytes);
						}
						else if (lookuptype == 1) // by index
						{
							byte offset = 1;
							
							short pointid = (short) ((int) data[offset++] * 0x100 + (int) data[offset++]);
							DataTypeEnum dtype = DataTypeEnum.valueOf(data[offset++]);
							byte datalength = dtype.getLength();
							byte[] bytes = new byte[datalength];
							
							System.arraycopy(data, offset, bytes, 0, datalength);
							
							nerduino.sendSetPointValue(null, pointid, dtype, bytes);
						}
					}
					else
						onHostSetPointValue(data, 0);
					
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
		
		if (m_verbose)
			fireCommandUpdate("N: LightDeclarePoint  " + pointname, CommandMessageTypeEnum.IncomingCommand);

		onDeclarePoint(datatype, readonly, publish, pointname, value);
	}
	
	@Override
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
		
		if (m_verbose)
			fireCommandUpdate("N: LightRegisterAddress  " + nerduinoname, CommandMessageTypeEnum.IncomingCommand);
		
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
		
		if (m_verbose)
			fireCommandUpdate("N: RegisterPoint  " + pointpath, CommandMessageTypeEnum.IncomingCommand);

		onRegisterPoint(datatype, pointpath);
	}	
	
	@Override
	public void sendMessage(byte message, byte length, byte[] data)
	{
		if (isReadyToSend())
		{
			outBuffer[0] = 0x7e;
			outBuffer[1] = 0x0;
			outBuffer[2] = (byte) (length + 1);
			outBuffer[3] = message;
			
			if (length > 0)
				System.arraycopy(data, 0, outBuffer, 4, length);
		
			m_serial.writeData(outBuffer, 4 + length);
		}
	}
	
	@Override
	public void writeData(byte length, byte[] data)
	{
		if (isReadyToSend())
			m_serial.writeData(data, length);
	}
	
	public String getComPort()
	{
		return m_comPort;
	}

	public void setComPort(String port)
	{
		m_comPort = port;
		
		closeComPort();
		
		m_serial.setComPort(port);
		
		fireUpdate();
		save();
	}

	public int getBaudRate()
	{
		return m_baudRate;
	}

	public void setBaudRate(int value)
	{
		m_baudRate = value;
		closeComPort();
		
		m_serial.setBaudRate(value);

		fireUpdate();
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
	public Serial getSerialMonitor()
	{
		if (m_serial == null && m_comPort != null && m_comPort!="")
		{
			m_serial = new Serial();
			
			m_serial.setComPort(m_comPort);
		}
	
		return m_serial;
	}

	@Override
	public void resetBoard()
	{
		//m_serial.resetPort();
	}

	
	@Override
	public String upload(Sketch sketch)
	{
		setStatus(NerduinoStatusEnum.Uninitialized);

		m_points.clear();
		
		closeComPort();

		Preferences.set("serial.port", m_comPort);

		try
		{
			fireUploadStatusUpdate(true, false, 0, "");
			
			boolean success = sketch.upload(ArduinoManager.Current.getBuildPath(), sketch.getPrimaryClassName(), false);

			if (!success)
			{
				StatusDisplayer.getDefault().setStatusText("Uploading to " + this.getName() + " Failed!");
				
				fireUploadStatusUpdate(false, false, 0, "Upload failed for unknown reason!");
				
				return "Upload failed for unknown reason!";
			}
			else
			{
				StatusDisplayer.getDefault().setStatusText("Uploading to " + this.getName() + " Completed!");

				fireUploadStatusUpdate(false, true, 0, "");
				
				//openComPort();

				return null;
			}
		}
		catch(RunnerException ex)
		{
			StatusDisplayer.getDefault().setStatusText("Uploading to " + this.getName() + " Failed!");

			fireUploadStatusUpdate(false, false, 0, "Runner Exception!");

			return ex.getMessage();
		}
		catch(SerialException ex)
		{
			StatusDisplayer.getDefault().setStatusText("Uploading to " + this.getName() + " Failed!");
			
			fireUploadStatusUpdate(false, false, 0, "Serial Port Exception!");
			
			return ex.getMessage();
		}
	}
	
	@Override
	public String getHTML()
	{
		String htmlString = "<html>\n"
                          + "<body>\n"
                          + "<h1>Nerduino: " + this.getName() + "  (USB Connection: " + getComPort() + ")</h1>\n"
                          + "<h2>" 
						  + "Status: " + this.getStatus().toString() + "<br>"
                          + "Board: " + this.getBoardType() + "<br>"
                          + "Sketch: " + this.getSketch() + "<br>"
						  + "</h2>\n"
                          + "<a href='ping'>Ping</a>\n"
                          + "<a href='reset'>Reset</a>\n"
                          + "<a href='verify'>Verify</a>\n"
                          + "<a href='upload'>Upload</a>\n"
                          + "<a href='engage'>Engage</a>\n"
						  + "</body>\n";
		
		return htmlString;
	}

}
