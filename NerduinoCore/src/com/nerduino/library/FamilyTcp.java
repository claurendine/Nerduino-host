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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Hashtable;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FamilyTcp extends FamilyBase
{
	public static FamilyTcp Current;
	ServerSocket m_servSock;
	Thread m_connectionThread;
	int m_port = 17401;
	Boolean m_enabled;
	
	int m_udpDiscoveryPort = 17451;
	DatagramSocket m_udpDiscoverySocket; 
	DatagramPacket m_udpDiscoveryPacket; 

	int m_udpMessagePort = 17501;
	DatagramSocket m_udpMessageSocket; 
	DatagramPacket m_udpMessagePacket; 
	
	@SuppressWarnings("UseOfObsoleteCollectionType")
	Hashtable<InetAddress, NerduinoTcp> m_addresses = new Hashtable<InetAddress, NerduinoTcp>();
	
	
	@SuppressWarnings({"LeakingThisInConstructor", "CallToThreadStartDuringObjectConstruction"})
	public FamilyTcp()
	{
		super();
		
		Current = this;
		
		// respond to udp broadcast requests
		try
		{
			m_udpDiscoverySocket = new DatagramSocket(m_udpDiscoveryPort);
			m_udpDiscoveryPacket = new DatagramPacket(new byte[128], 128);
			m_udpMessageSocket = new DatagramSocket(m_udpMessagePort);
			m_udpMessagePacket = new DatagramPacket(new byte[128], 128);
			
			// create a thread to process incoming discovery requests
			Thread discoveryThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					for (;;) 
					{  	try
						{
							// Run forever, receiving and echoing datagrams
							 m_udpDiscoverySocket.receive(m_udpDiscoveryPacket);
							 m_udpDiscoverySocket.send(m_udpDiscoveryPacket);  // Send the same packet back to client
							 m_udpDiscoveryPacket.setLength(128); // Reset length to avoid shrinking buffer
						}
						catch(IOException ex)
						{
							//Exceptions.printStackTrace(ex);
						}
				  }
				}
			});
			
			// create a thread to process incoming discovery requests
			Thread messageThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					for (;;) 
					{  	try
						{
							// Run forever, receiving and echoing datagrams
							 m_udpMessageSocket.receive(m_udpMessagePacket);
							 
							 InetAddress source = m_udpMessagePacket.getAddress();
							 
							 // lookup the associated nerduino
							 NerduinoTcp nerd = getNerduino(source);
							 
							 if (nerd != null)
							 {
								 nerd.processMessage(nerd, m_udpMessagePacket.getData());
							 }
							 
							 m_udpMessagePacket.setLength(128); // Reset length to avoid shrinking buffer
						}
						catch(IOException ex)
						{
							//Exceptions.printStackTrace(ex);
						}
				  }
				}
			});
			
			discoveryThread.start();
			messageThread.start();
		}
		catch(IOException ex)
		{
			Exceptions.printStackTrace(ex);
		}
	}
	
	@Override
	public String getFamilyType()
	{
		return "TCP";
	}

	@Override
	public NerduinoBase CreateNerduino()
	{
		return new NerduinoTcp();
	}
		
	NerduinoTcp getNerduino(InetAddress address)
	{
		if (m_addresses.containsKey(address))
			return m_addresses.get(address);
		
		return null;
	}
	
	void removeNerduino(NerduinoTcp nerd)
	{
		if (m_addresses.contains(nerd))
			m_addresses.remove(nerd.getAddress());
	}
	
	boolean addNerduino(InetAddress address, NerduinoTcp nerd)
	{
		if (m_addresses.containsKey(address))
			return false;
		
		m_addresses.put(address, nerd);
		
		return true;
	}
	
	public int getPort()
	{
		return m_port;
	}
	
	public void setPort(int port)
	{
		if (m_port != port)
		{
			m_port = port;
			
			if (m_enabled)
			{
				setEnabled(false);
				
				try
				{
					Thread.sleep(100);
				}
				catch(InterruptedException ex)
				{
					Exceptions.printStackTrace(ex);
				}
				
				setEnabled(true);
			}
		}
	}
	
	public Boolean getEnabled()
	{
		return m_enabled;
	}

	public void setEnabled(Boolean value)
	{
		m_enabled = value;			
	}
	
	private void onOutput(String data)
	{
		// TODO Auto-generated method stub
	}

	public void readXML(Element element)
	{
		if (element != null)
		{
			String portStr = element.getAttribute("Port");
			String enabledStr = element.getAttribute("Enabled");

			try
			{
				int port = Integer.decode(portStr);
				
				setPort(port);
			}
			catch(Exception e)
			{
			}
			
			setEnabled(true);
		}
	}

	public void writeXML(Document doc, Element element)
	{
		element.setAttribute("Port", ((Integer) getPort()).toString());
	}
}
