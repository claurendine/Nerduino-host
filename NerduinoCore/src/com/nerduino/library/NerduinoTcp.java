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
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.ImageIcon;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class NerduinoTcp extends NerduinoFull //implements FrameReceivedListener
{
	Serial m_serial;
	String m_comPort;
	String m_ipAddress;
	InetAddress m_inetAddress;
	int m_baudRate = 115200;
	
	boolean m_uploaded = false;
	private boolean m_engaging;
		
	public NerduinoTcp()
	{
		super("NET", "/com/nerduino/resources/NerduinoTCPUninitialized16.png");

		m_canDelete = true;
		m_verbose = true;
		m_state = 1;
	}
	
	public InetAddress getAddress()
	{
		return m_inetAddress;
	}
	
	@Override
	public boolean getActive()
	{
		return m_active;
	}
	
	@Override
	public void setActive(Boolean value)
	{
		m_active = value;
	}
	
	public String getComPort()
	{
		return m_comPort;
	}

	public void setComPort(String port)
	{
		m_comPort = port;
		
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
		
		fireUpdate();
		save();
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
	
	public String getIPAddress()
	{
		return m_ipAddress;
	}

	public void setIPAddress(String value) 
	{
		try
		{
			m_ipAddress = value;

			m_inetAddress = InetAddress.getByName(value);
			m_address.IPAddress = m_inetAddress;

			// update the tcp hashtable
			FamilyTcp.Current.removeNerduino(this);
			FamilyTcp.Current.addNerduino(m_inetAddress, this);
			
			fireUpdate();
			save();
		}
		catch(UnknownHostException ex)
		{
			Exceptions.printStackTrace(ex);
		}
	}
	
	@Override
	public Image getIcon(int type)
	{
		java.net.URL imgURL = null;

		switch(getStatus())
		{
			case Uninitialized:
				imgURL = getClass().getResource("/com/nerduino/resources/NerduinoTCPUninitialized16.png");
				break;
			case Online:
				imgURL = getClass().getResource("/com/nerduino/resources/NerduinoTCPOnline16.png");
				break;
			case Offline:
				imgURL = getClass().getResource("/com/nerduino/resources/NerduinoTCPOffline16.png");
				break;
			case Sleeping:
				imgURL = getClass().getResource("/com/nerduino/resources/NerduinoTCPSleeping16.png");
				break;
			case Distress:
				imgURL = getClass().getResource("/com/nerduino/resources/NerduinoTCPDistress16.png");
				break;
		}

		if (imgURL != null)
		{
			return new ImageIcon(imgURL).getImage();
		}
		else
		{
			return null;
		}
	}

	@Override
	public boolean configureNewNerduino()
	{
		if (getParentNode() != null)
		{
			setName(((TreeNode) getParentNode()).getUniqueName(getName()));
		}
		
		// show the configure dialog
		NerduinoTCPConfigDialog dialog = new NerduinoTCPConfigDialog(new javax.swing.JFrame(), true);
		
		dialog.setNerduinoTCP(this);
		dialog.setVisible(true);
		
		return (dialog.m_nerduino != null);
	}

	
	@Override
	public void readXML(Element node)
	{
		super.readXML(node);
		
		m_comPort = node.getAttribute("Port");		
		m_baudRate = Integer.valueOf(node.getAttribute("BaudRate"));
		setIPAddress(node.getAttribute("IPAddress"));
				
		setStatus(NerduinoStatusEnum.Offline);
	}
	
	@Override
	public void writeXML(Document doc, Element element)
	{		
		super.writeXML(doc, element);
		
		element.setAttribute("Port", m_comPort);
		element.setAttribute("BaudRate", Integer.toString(m_baudRate));
		element.setAttribute("USB", "false");
		element.setAttribute("IPAddress", m_ipAddress);
		
		element.setAttribute("Type", "TCP");
	}
	
	@Override
	public PropertySet[] getPropertySets()
	{								
		Sheet.Set tcpSheet = Sheet.createPropertiesSet();
		
		tcpSheet.setDisplayName("Device Settings");
		
		addProperty(tcpSheet, String.class, ComPortPropertyEditor.class, "ComPort", "The serial port used to program the arduino device.");
		addProperty(tcpSheet, int.class, BaudRatePropertyEditor.class, "BaudRate", "The baud rate used to program the arduino device.");
		addProperty(tcpSheet, String.class, DeviceTypePropertyEditor.class, "BoardType", "The arduino board type.");
		addProperty(tcpSheet, String.class, SketchPropertyEditor.class, "Sketch", "The arduino sketch.");
		addProperty(tcpSheet, String.class, null, "IPAddress", "The ip address used by the arduino to communicate to the host.");
		addProperty(tcpSheet, Boolean.class, null, "Active", "");
		
		PropertySet[] basesets = super.getPropertySets();
		PropertySet[] sets = new PropertySet[basesets.length + 1];
		
		System.arraycopy(basesets, 0, sets, 0, basesets.length);
		
		sets[basesets.length] = tcpSheet;
		
		return sets;
	}
	
	@Override
	public void forwardMessage(NerduinoBase originator, byte[] data)
	{
		// identify the originating routing index
		data[0] = (byte) (originator.m_address.RoutingIndex / 0x100);
		data[1] = (byte) (originator.m_address.RoutingIndex & 0xff);
		
		sendMessage(data);
	}
	
	@Override
	public void sendGetAddressResponse(short responseToken, AddressStatusEnum status, Address address, short pointIndex)
	{
		if (m_verbose)
			fireCommandUpdate(null, "GetAddressResponse", CommandMessageTypeEnum.OutgoingCommand);
		
		byte[] data = new byte[14];
		
		short fromindex = 0;
		
		data[0] = (byte) (fromindex / 0x100);
		data[1] = (byte) (fromindex & 0xff);
		data[2] = MessageEnum.MSG_GetAddressResponse.Value();
		data[3] = (byte) (responseToken / 0x100);
		data[4] = (byte) (responseToken & 0xff);
		data[5] = status.Value();
		
		// send the ip address of the source
		// send the routing index
		// send the point index
		data[6] = address.IPAddress.getAddress()[0];
		data[7] = address.IPAddress.getAddress()[1];
		data[8] = address.IPAddress.getAddress()[2];
		data[9] = address.IPAddress.getAddress()[3];
		
		data[10] = (byte) (address.RoutingIndex / 0x100);
		data[11] = (byte) (address.RoutingIndex & 0xff);
		
		data[12] = (byte) (pointIndex / 0x100);
		data[13] = (byte) (pointIndex & 0xff);
		
		sendMessage(data);
	}
	
	@Override
	public void sendMessage(byte[] data)
	{
		try
		{
			m_sending = true;

			m_lastBroadcast = System.currentTimeMillis();
			
			DatagramPacket message = new DatagramPacket(data, data.length, m_inetAddress, 17501);
			DatagramSocket socket = new DatagramSocket();
			
			socket.send(message);

			socket.close();
			
			m_sending = false;
		}
		catch(IOException ex)
		{
//			Exceptions.printStackTrace(ex);
		}
	}

	@Override
	public String engage()
	{
		m_state = 1;
		
		return null;
		/*
		m_engaging = true;
		
		fireEngageStatusUpdate(true, false, 0, "");
		
		// Wait for ping response
		fireEngageStatusUpdate(true, false, 20, "");
		
		// if a sketch was just uploaded then skip the ping step
		// and wait for a checkin
		float wait;
		m_checkedIn = false;

		if (m_uploaded)
		{
			m_uploaded = false;
			
			// wait for checkin response
			wait = 0.0f;
			
			while (!m_checkedIn && wait < 8.0f)
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
			// attempt to ping the nerduino
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
				String err = "This device failed to respond to a ping message, verify that the device is online.";

				fireEngageStatusUpdate(false, false, 0, err);

				m_engaging = false;
		
				return err;
			}

			// if it responds to a ping send a request to checkin

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

		if (!m_checkedIn)
		{
			String err = "This device failed to checkin, verify that the device is online.";
			
			fireEngageStatusUpdate(false, false, 0, err);

			m_engaging = false;

			return err;
		}
		
		// gather point metadata
		fireEngageStatusUpdate(true, false, 50, "");
		
		sendGetPoints(null, (short) 0);

		wait = 0.0f;

		while (!m_receivedGetPoints && wait < 2.0f)
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
			String err = "This device failed to provide point metadata, verify that it is online.";

			fireEngageStatusUpdate(false, false, 0, err);

			m_engaging = false;

			return err;
		}
		
		fireEngageStatusUpdate(false, true, 0, "");
		
		m_engaging = false;
		
		return null;
		*/
	}

	@Override
	public String upload(Sketch sketch)
	{
		boolean monitored = false;
		String message = null;
		m_uploaded = false;
		
		setStatus(NerduinoStatusEnum.Uninitialized);
		
		m_points.clear();
		
		if (m_serial != null)
		{
			monitored = m_serial.getEnabled();
			
			m_serial.setEnabled(false);
		}

		Preferences.set("serial.port", m_comPort);

		try
		{
			fireUploadStatusUpdate(true, false, 0, "");
			
			boolean success = sketch.upload(ArduinoManager.Current.getBuildPath(), sketch.getPrimaryClassName(), false);

			if (!success)
			{
				StatusDisplayer.getDefault().setStatusText("Uploading to " + this.getName() + " Failed!");

				fireUploadStatusUpdate(false, false, 0, "Upload failed for unknown reason!");
				
				message = "Upload failed for unknown reason!";
			}
			else
			{
				StatusDisplayer.getDefault().setStatusText("Uploading to " + this.getName() + " Completed!");

				fireUploadStatusUpdate(false, true, 0, "");
				m_uploaded = true;
			}
		}
		catch(RunnerException ex)
		{
			StatusDisplayer.getDefault().setStatusText("Uploading to " + this.getName() + " Failed!");

			fireUploadStatusUpdate(false, false, 0, "Runner Exception!");

			message = ex.getMessage();
		}
		catch(SerialException ex)
		{
			StatusDisplayer.getDefault().setStatusText("Uploading to " + this.getName() + " Failed!");

			fireUploadStatusUpdate(false, false, 0, "Serial Port Exception!");
			
			message = ex.getMessage();
		}

		if (monitored && m_serial != null)
		{
			m_serial.setEnabled(true);
		}

		return message;
	}
	
	@Override
	public String getHTML()
	{
		String htmlString = "<html>\n"
                          + "<body>\n"
                          + "<h1>Nerduino: " + this.getName() + "  (TCP Connection: " + getIPAddress() + ")</h1>\n"
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
                          + "<a href='test'>Test</a>\n"
						  + "</body>\n";
		
		return htmlString;
	}
	
	public void test()
	{
		InputOutput io = IOProvider.getDefault().getIO("Build", false);
		OutputWriter output = io.getOut();
		
		try
		{
			output.reset();
		}
		catch(IOException ex)
		{
			Exceptions.printStackTrace(ex);
		}
		output.println("Testing:");

		////////////////////////////////////////////////////////////////////////
		// test ping
		if (!testPing())
		{
			output.println("This device failed to respond to a ping message.");
			return;
		}
		
		output.println("Ping Host to Nerduino - OK");
		output.println("PingResponse Nerduino to Host - OK");
		
		////////////////////////////////////////////////////////////////////////
		// test resetRequest
		if (!testResetRequest())
		{
			output.println("This device did not respond to a resetRequest message.");
			return;			
		}
		
		output.println("ResetRequest - N/A");
		
		////////////////////////////////////////////////////////////////////////
		// test GetPoint
		if (!testGetPoints())
		{
			output.println("This device failed to respond to a GetPoint message.");
			return;
		}
		
		output.println("GetPoint Host to Nerduino - OK");
		output.println("GetPointResponse Nerduino to Host - OK");

		
		////////////////////////////////////////////////////////////////////////
		// test Checkin
		if (!testCheckin())
		{
			output.println("This device failed to respond to a checkin message.");
			return;
		}
		
		output.println("Checkin Host to Nerduino - OK");
		output.println("Checkin Nerduino to Host - OK");
		
		////////////////////////////////////////////////////////////////////////
		// test ExecuteCommand
		String err = testExecuteCommand();
		if (err != null)
		{
			output.println(err);
			output.println("This device failed to execute a command.");
			return;
		}

		output.println("ExecuteCommand Host to Nerduino - OK");
		output.println("ExecuteCommandResponse Nerduino to Host - OK");
		
		////////////////////////////////////////////////////////////////////////
		// test SetPointValue
		err = testSetValue();
		
		if (err != null)
		{
			output.println(err);
			output.println("This device failed to get and set point values.");
			return;
		}
		
		output.println("SetPointValue Host to Nerduino - OK");
		output.println("GetPointValue Host to Nerduino - OK");
		output.println("GetPointValueResponse Nerduino to Host - OK");

		
		////////////////////////////////////////////////////////////////////////
		// test RegisterPointCallback
		err = testRegisterPointCallback();

		if (err != null)
		{
			output.println(err);
			output.println("This device failed to register/unregister a point value callback.");
			return;
		}
		
		output.println("registerPointValueCallback Host to Nerduino - OK");
		
		////////////////////////////////////////////////////////////////////////
		// test executeCommand from nerduino

		err = testMethod("TestHostExecute", "Execute commands on Host");
		
		if (err != null)
		{
			output.println(err);
			return;
		}
		
		////////////////////////////////////////////////////////////////////////
		// test ping from nerduino

		err = testMethod("TestPing", "Nerduino to Nerduino Ping");

		
		if (err != null)
		{
			output.println(err);
			return;
		}
		
		////////////////////////////////////////////////////////////////////////
		// test getPointValue from nerduino

		err = testMethod("TestHostGetValue", "Nerduino To Host Get Value");
		
		if (err != null)
		{
			output.println(err);
			return;
		}
		
		////////////////////////////////////////////////////////////////////////
		// test setPointValue from nerduino
		
		err = testMethod("TestHostSetValue", "Nerduino To Host Set Value");
		
		if (err != null)
		{
			output.println(err);
			return;
		}
		
		////////////////////////////////////////////////////////////////////////
		// test RegisterPointCallback from nerduino
		
		////////////////////////////////////////////////////////////////////////
		// test getAddress from nerduino
		
	}
	
	String testMethod(String method, String description)
	{
		CommandResponse response;
		Object value;
		
		/////////////////////////////////////////////////////////////////
		// Test boolean response
		
		response = sendCommand(method, DataTypeEnum.DT_Boolean);
		
		if (response.Status != ResponseStatusEnum.RS_Complete)
		{
			return "Failed to execute method '" + method + "'. Verify that this command is handled in the sketch!";
		}
		
		// verify the response to the test
		value = response.getResponseValue();
		
		if (!((String) value).equals("OK"))
		{
			return "Failed to execute method '" + method + "'!" + (String) value;
		}
		
		return null;
	}
	
	String testRegisterPointCallback()
	{
		// register a point callback
		RemoteDataPoint dp = (RemoteDataPoint) getPoint("TestPoint");
		
		if (dp == null)
			return "The testPoint data point does not exist.";
		
		dp.registerWithNoFilter(dp.Id);
		
		// get the current value for the test point
		// get the local point.. don't get the remote value
		Object  initialvalue = dp.getValue();
		
		if (initialvalue == null)
			return "The tesPoint value is null.";
		
		// wait for 2 seconds
		float wait = 0.0f;
		
		while (wait < 2.0f)
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
		
		// get the test point's current value and verify that it has updated
		Object  nextvalue = dp.getValue();
		
		if (nextvalue == initialvalue)
			return "The testPoint did not succesfully register a callback.";
		
		// unregister a point callback
		dp.unregister();

		// wait for 1/2 seconds to allow the unregister to take affect
		wait = 0.0f;

		while (wait < 0.5f)
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

		// get the current value for the ttest point
		initialvalue = dp.getValue();
		
		// wait for 2 seconds
		wait = 0.0f;

		while (wait < 2.0f)
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

		// get the test point's current value and verify that it has not changed
		nextvalue = dp.getValue();
		
		if (nextvalue != initialvalue)
			return "The testPoint did not succesfully unregister a callback.";
		
		return null;
	}
	
	String testExecuteCommand()
	{
		CommandResponse response;
		Object value;
		
		/////////////////////////////////////////////////////////////////
		// Test boolean response
		
		response = sendCommand("Test0", DataTypeEnum.DT_Boolean);
		
		if (response.Status != ResponseStatusEnum.RS_Complete)
		{
			return "ExecuteCommand failed to execute method 'Test0'. Verify that this command is handled in the sketch!";
		}
		
		// verify the response to the test
		value = response.getResponseValue();
		
		if ((Boolean) value != true)
		{
			return "ExecuteCommand failed to execute method 'Test0'. The command failed to provide a proper response!";	
		}

		/////////////////////////////////////////////////////////////////
		// Test byte response
		
		response = sendCommand("Test1", DataTypeEnum.DT_Byte);
		
		if (response.Status != ResponseStatusEnum.RS_Complete)
		{
			return "ExecuteCommand failed to execute method 'Test1'. Verify that this command is handled in the sketch!";
		}
		
		// verify the response to the test
		value = response.getResponseValue();
		
		if ((Byte) value != (byte) 3)
		{
			return "ExecuteCommand failed to execute method 'Test1'. The command failed to provide a proper response!";
		}


		/////////////////////////////////////////////////////////////////
		// Test short response
		
		response = sendCommand("Test2", DataTypeEnum.DT_Short);
		
		if (response.Status != ResponseStatusEnum.RS_Complete)
		{
			return "ExecuteCommand failed to execute method 'Test2'. Verify that this command is handled in the sketch!";
		}
		
		// verify the response to the test
		value = response.getResponseValue();
		
		if ((Short) value != (short) 33)
		{
			return "ExecuteCommand failed to execute method 'Test2'. The command failed to provide a proper response!";
		}

		
		/////////////////////////////////////////////////////////////////
		// Test int response
		
		response = sendCommand("Test3", DataTypeEnum.DT_Integer);
		
		if (response.Status != ResponseStatusEnum.RS_Complete)
		{
			return "ExecuteCommand failed to execute method 'Test3'. Verify that this command is handled in the sketch!";
		}
		
		// verify the response to the test
		value = response.getResponseValue();
		
		if ((Integer) value != 333)
		{
			return "ExecuteCommand failed to execute method 'Test3'. The command failed to provide a proper response!";
		}


		/////////////////////////////////////////////////////////////////
		// Test float response
		
		response = sendCommand("Test4", DataTypeEnum.DT_Float);
		
		if (response.Status != ResponseStatusEnum.RS_Complete)
		{
			return "ExecuteCommand failed to execute method 'Test4'. Verify that this command is handled in the sketch!";
		}
		
		// verify the response to the test
		value = response.getResponseValue();
		
		if ((Float) value != 3.3f)
		{
			return "ExecuteCommand failed to execute method 'Test4'. The command failed to provide a proper response!";
		}


		/////////////////////////////////////////////////////////////////
		// Test string response
		
		response = sendCommand("Test5", DataTypeEnum.DT_String);
		
		if (response.Status != ResponseStatusEnum.RS_Complete)
		{
			return "ExecuteCommand failed to execute method 'Test5'. Verify that this command is handled in the sketch!";
		}
		
		// verify the response to the test
		value = response.getResponseValue();
		
		if (!((String) value).equals("333"))
		{
			return "ExecuteCommand failed to execute method 'Test5'. The command failed to provide a proper response!";
		}

		/////////////////////////////////////////////////////////////////
		// Test for unrecognized command response
		
		response = sendCommand("???", DataTypeEnum.DT_Boolean);
		
		if (response.Status != ResponseStatusEnum.RS_CommandNotRecognized)
		{
			return "ExecuteCommand failed to report method '???' as unrecognizable!";
		}
		
		return null;
	}
	
	boolean testPing()
	{
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
		
		return m_pinged;
	}
	
	boolean testCheckin()
	{
		sendCheckin(null);
		
		float wait = 0.0f;

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
		
		return m_checkedIn;
	}
	
	boolean testResetRequest()
	{
		return true;
	}
	
	boolean testGetPoints()
	{
		sendGetPoints(null, (short) 0);

		float wait = 0.0f;

		while (!m_receivedGetPoints && wait < 2.0f)
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
		
		return m_receivedGetPoints;
	}
	
	String testSetValue()
	{
		if (!setPoint("TestBool", true))
		{
			return "SetValue failed to set TestBoolean point. Verify that this point is exposed in the sketch!";
		}
		
		setPoint("TestByte", (byte) 0);
		if (!setPoint("TestByte", (byte) 4))
		{
			return "SetValue failed to set TestByte point. Verify that this point is exposed in the sketch!";
		}
		
		setPoint("TestShort", (short) 0);
		if (!setPoint("TestShort", (short) 44))
		{
			return "SetValue failed to set TestShort point. Verify that this point is exposed in the sketch!";
		}
		
		setPoint("TestInt", 0);
		if (!setPoint("TestInt", 444))
		{
			return "SetValue failed to set TestInt point. Verify that this point is exposed in the sketch!";
		}
		
		setPoint("TestFloat", 0f);
		if (!setPoint("TestFloat", 4.4f))
		{
			return "SetValue failed to set TestFloat point. Verify that this point is exposed in the sketch!";
		}
		
		return null;
	}
	
	boolean setPoint(String point, Object value)
	{
		RemoteDataPoint pb = (RemoteDataPoint) getPoint(point);
		
		if (pb == null)
			return false;
		
		pb.setValue(value);
		
		Object  setvalue = pb.getRemoteValue();
		
		if (setvalue == null)
			return false;
		
		return setvalue.equals(value);
	}

}
