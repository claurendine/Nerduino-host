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

package com.nerduino.actions;

import com.nerduino.library.CommandResponse;
import com.nerduino.library.NerduinoBase;
import com.nerduino.library.PointBase;
import com.nerduino.library.ResponseStatusEnum;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.OutputWriter;

@ActionID(
		category = "View",
		id = "com.nerduino.actions.TestAction")
@ActionRegistration(
		iconBase = "com/nerduino/resources/Properties16.png",
		displayName = "#CTL_TestAction")
@ActionReferences({})
@Messages("CTL_TestAction=Test")
public final class TestAction implements ActionListener
{
	OutputWriter output;
	NerduinoBase nerduino;

	@Override
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			DatagramSocket socket = new DatagramSocket();
			
			byte[] buf = new byte[16];
			
			buf[0] = 'h';
			buf[1] = 'e';
			buf[2] = 'l';
			buf[3] = 'l';
			buf[4] = 'o';
			buf[5] = 0;
			
			InetAddress address = InetAddress.getByName("255.255.255.255");
			
			DatagramPacket packet = new DatagramPacket(buf, buf.length, 
								address, 17451);

			socket.send(packet);
			socket.receive(packet);
			
			InetAddress serverAddress = packet.getAddress();
			
			String saddress = serverAddress.getHostAddress();
			String received = new String(packet.getData(), 0, packet.getLength());	
			
			
			Socket tcp = new Socket(saddress, 17401);
			
			if (tcp.isConnected())
			{
				tcp.close();
			}
		}
		catch(IOException ex)
		{
			Exceptions.printStackTrace(ex);
		}
		
		
		
		return;
/*		
		InputOutput io = IOProvider.getDefault().getIO("Test", false);
		io.select();
		output = io.getOut();
		try
		{
			output.reset();
		}
		catch(IOException ex)
		{
			Exceptions.printStackTrace(ex);
		}
		
		output.println("Testing!");
		
		// get the test nerduino
		nerduino = null;
		TreeNode selectednode = ExplorerTopComponent.Current.getSelectedNode();
		
		if (selectednode != null && selectednode instanceof NerduinoBase)
		{
			nerduino = (NerduinoBase) selectednode;
			
			for(int i = 0; i < testCount; i++)
			{
				if (!test(i))
				{
					output.println("");
					output.println("Failed!");
					
					return;
				}
			}
			
			output.println("");
			output.println("Done!");
		}
		else
		{
			output.println("Please select the Nerduino to Test");
		}
		*/
		
		
		
		/*
		io.getOut().println("Send Hello!");

		CommandResponse response = nerduino.executeCommand("Hello");
		
		switch(response.Status)
		{
			case RS_Complete:
				io.getOut().println(response.toString());

				break;
			case RS_Timeout:
				io.getOut().println("Response Timeout!");

				break;
			case RS_CommandNotRecognized:
				io.getOut().println("Unrecognized Command!");

				break;
		}
		
		// get the first remotedatapoint
		RemoteDataPoint point = nerduino.getPoint("Count");
		
		if (point != null)
		{
			if (point.getRegistered())
				point.unregister();
			else
//				point.registerWithNoFilter((short) 0);
				point.registerWithChangeFilter((short) 0, (byte) 4);
		}
*/
	}
	
	int testCount = 14;
	
	boolean test(int index)
	{
		switch(index)
		{
			case 0:
				return validateNerduino();
			case 2:
				return testPing();
			case 3:
				return testSetValue();
			case 4:
				return testExecute();
			case 5:
				return testMethod("TestHostExecute", "Execute commands on Host");
			case 6:
				return testMethod("TestPing", "Nerduino to Nerduino Ping");
			case 7:
				return testMethod("TestRemoteGetValue", "Nerduino to Nerduino Get Value");
			case 8:
				return testMethod("TestRemoteSetValue", "Nerduino to Nerduino Set Value");
			case 9:
				return testMethod("TestHostGetValue", "Nerduino To Host Get Value");
			case 10:
				return testMethod("TestHostSetValue", "Nerduino To Host Set Value");
			case 11:
				return testMethod("TestGetStatus", "Nerduino Get Status Value");
			case 12:
				return testMethod("TestRemoteExecute", "Nerduino to Nerduino Execute");
			//case 13:
			case 1:
				return testMethod("TestProxyGetSet", "Nerduino Proxy Data IO");
		}
		
		return false;
	}
	
	boolean validateNerduino()
	{
		if (nerduino != null)
		{
			output.println("Nerduino Found");
			return true;
		}
		
		return false;
	}
	
	boolean testPing()
	{
		if (nerduino.ping())
		{
			output.println("Ping message OK!");
			return true;
		}
		else
		{
			output.println("Did not receive a response to the Ping message!");
			return false;
		}
	}
	
	boolean testSetValue()
	{
		if (!setPoint("TestBoolean", true))
		{
			output.println("SetValue failed to set TestBoolean point. Verify that this point is exposed in the sketch!");
			return false;
		}
		
		setPoint("TestByte", (byte) 0);
		if (!setPoint("TestByte", (byte) 4))
		{
			output.println("SetValue failed to set TestByte point. Verify that this point is exposed in the sketch!");
			return false;
		}
		
		setPoint("TestShort", (short) 0);
		if (!setPoint("TestShort", (short) 44))
		{
			output.println("SetValue failed to set TestShort point. Verify that this point is exposed in the sketch!");
			return false;
		}
		
		setPoint("TestInt", 0);
		if (!setPoint("TestInt", 444))
		{
			output.println("SetValue failed to set TestInt point. Verify that this point is exposed in the sketch!");
			return false;
		}
		
		setPoint("TestFloat", 0f);
		if (!setPoint("TestFloat", 4.4f))
		{
			output.println("SetValue failed to set TestFloat point. Verify that this point is exposed in the sketch!");
			return false;
		}
		
		output.println("SetValue message OK!");
		return true;
	}
	
	boolean testExecute()
	{
		CommandResponse response;
		Object value;
		
		/////////////////////////////////////////////////////////////////
		// Test boolean response
		
		response = sendCommand("Test0", (byte) 0);
		
		if (response.Status != ResponseStatusEnum.RS_Complete)
		{
			output.println("ExecuteCommand failed to execute method 'Test0'. Verify that this command is handled in the sketch!");
			return false;
		}
		
		// verify the response to the test
		value = response.getResponseValue();
		
		if ((Boolean) value != true)
		{
			output.println("ExecuteCommand failed to execute method 'Test0'. The command failed to provide a proper response!");
			return false;			
		}

		/////////////////////////////////////////////////////////////////
		// Test byte response
		
		response = sendCommand("Test1", (byte) 0);
		
		if (response.Status != ResponseStatusEnum.RS_Complete)
		{
			output.println("ExecuteCommand failed to execute method 'Test1'. Verify that this command is handled in the sketch!");
			return false;
		}
		
		// verify the response to the test
		value = response.getResponseValue();
		
		if ((Byte) value != (byte) 3)
		{
			output.println("ExecuteCommand failed to execute method 'Test1'. The command failed to provide a proper response!");
			return false;			
		}


		/////////////////////////////////////////////////////////////////
		// Test short response
		
		response = sendCommand("Test2", (byte) 0);
		
		if (response.Status != ResponseStatusEnum.RS_Complete)
		{
			output.println("ExecuteCommand failed to execute method 'Test2'. Verify that this command is handled in the sketch!");
			return false;
		}
		
		// verify the response to the test
		value = response.getResponseValue();
		
		if ((Short) value != (short) 33)
		{
			output.println("ExecuteCommand failed to execute method 'Test2'. The command failed to provide a proper response!");
			return false;			
		}

		
		/////////////////////////////////////////////////////////////////
		// Test int response
		
		response = sendCommand("Test3", (byte) 0);
		
		if (response.Status != ResponseStatusEnum.RS_Complete)
		{
			output.println("ExecuteCommand failed to execute method 'Test3'. Verify that this command is handled in the sketch!");
			return false;
		}
		
		// verify the response to the test
		value = response.getResponseValue();
		
		if ((Integer) value != 333)
		{
			output.println("ExecuteCommand failed to execute method 'Test3'. The command failed to provide a proper response!");
			return false;			
		}


		/////////////////////////////////////////////////////////////////
		// Test float response
		
		response = sendCommand("Test4", (byte) 0);
		
		if (response.Status != ResponseStatusEnum.RS_Complete)
		{
			output.println("ExecuteCommand failed to execute method 'Test4'. Verify that this command is handled in the sketch!");
			return false;
		}
		
		// verify the response to the test
		value = response.getResponseValue();
		
		if ((Float) value != 3.3f)
		{
			output.println("ExecuteCommand failed to execute method 'Test4'. The command failed to provide a proper response!");
			return false;			
		}


		/////////////////////////////////////////////////////////////////
		// Test string response
		
		response = sendCommand("Test5", (byte) 0);
		
		if (response.Status != ResponseStatusEnum.RS_Complete)
		{
			output.println("ExecuteCommand failed to execute method 'Test5'. Verify that this command is handled in the sketch!");
			return false;
		}
		
		// verify the response to the test
		value = response.getResponseValue();
		
		if (!((String) value).equals("333"))
		{
			output.println("ExecuteCommand failed to execute method 'Test5'. The command failed to provide a proper response!");
			return false;			
		}


		
		/////////////////////////////////////////////////////////////////
		// Test byte array response
		
		response = sendCommand("Test6", (byte) 0);
		
		if (response.Status != ResponseStatusEnum.RS_Complete)
		{
			output.println("ExecuteCommand failed to execute method 'Test6'. Verify that this command is handled in the sketch!");
			return false;
		}
		
		// verify the response to the test
		if (response.DataLength != 3)
		{
			output.println("ExecuteCommand failed to execute method 'Test6'. The command failed to provide a proper response!");
			return false;			
		}
		
		for(int i = 0; i < 3; i++)
		{
			if (response.Data.get(i) != (byte) 3)
			{
				output.println("ExecuteCommand failed to execute method 'Test6'. The command failed to provide a proper response!");
				return false;				
			}
		}

		
		
		/////////////////////////////////////////////////////////////////
		// Test for unrecognized command response
		
		response = sendCommand("???", (byte) 0);
		
		if (response.Status != ResponseStatusEnum.RS_CommandNotRecognized)
		{
			output.println("ExecuteCommand failed to execute method '???'. Expeceted a command not recognized response!");
			return false;
		}
		
		
		output.println("ExecuteCommand message OK!");		
		return true;
	}
	
	CommandResponse sendCommand(String command, byte datatype)
	{
		return nerduino.sendExecuteCommand(null, (short) 1, datatype, (byte) command.length(), command.getBytes());
	}
	
	boolean setPoint(String point, Object value)
	{
		PointBase pb = nerduino.getPoint(point);
		
		if (pb == null)
			return false;
		
		pb.setValue(value);
		
		Object  setvalue = pb.getValue();
		
		return setvalue.equals(value);
	}
	
	Object getPoint(String point)
	{
		PointBase rdp = nerduino.getPoint(point);
		
		if (rdp == null)
			return null;
		
		return rdp.getValue();
	}
		
	boolean testMethod(String method, String description)
	{
		CommandResponse response;
		Object value;
		
		/////////////////////////////////////////////////////////////////
		// Test boolean response
		
		response = sendCommand(method, (byte) 0);
		
		if (response.Status != ResponseStatusEnum.RS_Complete)
		{
			output.println("Failed to execute method '" + method + "'. Verify that this command is handled in the sketch!");
			return false;
		}
		
		// verify the response to the test
		value = response.getResponseValue();
		
		if (!((String) value).equals("OK"))
		{
			output.println("Failed to execute method '" + method + "'!");
			output.println((String) value);
			
			return false;
		}

		output.println(description + " OK!");
		return true;
	}
}


		
		
