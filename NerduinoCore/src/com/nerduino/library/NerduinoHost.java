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
import com.nerduino.nodes.TreeNode;
import processing.app.ArduinoManager;
import com.nerduino.webhost.WebHost;
import com.nerduino.xbee.BitConverter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

public class NerduinoHost extends TreeNode 
{
	// Declarations
	Children m_hostPoints;
	
	ArrayList<FamilyBase> m_families = new ArrayList<FamilyBase>();
	
	protected NerduinoManager m_manager;
	byte m_configurationToken = 0;
	byte m_status = 0;
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

		addFamily(new FamilyTcp());
		//addFamily(new FamilyXBee());
		addFamily(new FamilyUSB());
		addFamily(new FamilyBluetooth());
		addFamily(new FamilyUPNP());

		
		Thread sleepThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				boolean running = true;

				while (running)
				{
					try
					{
						m_manager.scan();

						Thread.sleep(m_sleepScanRate * 1000);
					}
					catch(InterruptedException ie)
					{
						if (running == false)
							break;
					}
				}
			}
		}, "Nerduino manager thread");

		sleepThread.setPriority(Thread.MIN_PRIORITY);
		sleepThread.start();
	}

	void addFamily(FamilyBase family)
	{
		m_families.add(family);
	}
	
	public int getHostPointCount()
	{
		return m_hostPoints.getNodesCount();
	}
	
	public LocalDataPoint getHostPoint(int index)
	{
		return (LocalDataPoint) m_hostPoints.getNodeAt(index);
	}
	
	@Override
	public PropertySet[] getPropertySets()
	{
		final Sheet.Set webSheet = Sheet.createPropertiesSet();
		//final Sheet.Set xbeeSheet = Sheet.createPropertiesSet();
		final Sheet.Set arduinoSheet = Sheet.createPropertiesSet();
		final Sheet.Set upnpSheet = Sheet.createPropertiesSet();
		
		try
		{
			/*
			xbeeSheet.setDisplayName("Zigbee Device Settings");
			
			Property<Boolean> enabledProp = new PropertySupport.Reflection<Boolean>(m_xbeeManager, Boolean.class, "Enabled");
			enabledProp.setName("Enabled");
			enabledProp.setShortDescription("Enable/Disable the communications through the zigbee device. See the output window for diagnostics.");
			xbeeSheet.put(enabledProp);
			
			PropertySupport.Reflection<String> portProp = new PropertySupport.Reflection<String>(m_xbeeManager, String.class, "ComPort");
			portProp.setName("Com Port");
			portProp.setShortDescription("The serial port used to communicate to the zigbee device.");
			portProp.setPropertyEditorClass(ComPortPropertyEditor.class);
			xbeeSheet.put(portProp);
			
			PropertySupport.Reflection<Integer> baudProp = new PropertySupport.Reflection<Integer>(m_xbeeManager, int.class, "BaudRate");
			baudProp.setName("Baud Rate");
			baudProp.setShortDescription("The baud rate used to communicate to the zigbee device.");
			baudProp.setPropertyEditorClass(BaudRatePropertyEditor.class);
			xbeeSheet.put(baudProp);

			Property<Integer> discoveryProp = new PropertySupport.Reflection<Integer>(m_xbeeManager, int.class, "DiscoverRate");
			discoveryProp.setName("Discovery Rate");
			discoveryProp.setShortDescription("The interval in seconds that the zigbee radio poles for nearby zigbee devices.");
			xbeeSheet.put(discoveryProp);
			*/
			
			webSheet.setDisplayName("Local Web Server Settings");
			
			Property<Boolean> webEnabledProp = new PropertySupport.Reflection<Boolean>(WebHost.Current, Boolean.class, "Enabled");
			webEnabledProp.setName("Enable Web Server");
			webEnabledProp.setShortDescription("Enable/Disable the Web Server.");
			webSheet.put(webEnabledProp);
			
			Property<Integer> webPortProp = new PropertySupport.Reflection<Integer>(WebHost.Current, int.class, "Port");
			webPortProp.setName("Web Server Port");
			webPortProp.setShortDescription("The network port used to listen for incoming web requests.");
			webSheet.put(webPortProp);
			
			Property<String> webRootProp = new PropertySupport.Reflection<String>(WebHost.Current, String.class, "WebRoot");
			webRootProp.setName("Root Path");
			webRootProp.setShortDescription("The local file path that the Web Server uses to serve content from.");
			webSheet.put(webRootProp);
			
			Property<String> httpAddressProp = new PropertySupport.Reflection<String>(WebHost.Current, String.class, "HttpAddress");
			httpAddressProp.setName("Http Address");
			httpAddressProp.setShortDescription("The http path to engage this web server.");
			webSheet.put(httpAddressProp);
			
			arduinoSheet.setDisplayName("Path Settings");
			
			Property<String> arduinoSourcePathProp = new PropertySupport.Reflection<String>(ArduinoManager.Current, String.class, "ArduinoPath");
			arduinoSourcePathProp.setName("Arduino Path");
			arduinoSourcePathProp.setShortDescription("Installed Arduino Data Path");
			arduinoSheet.put(arduinoSourcePathProp);
			
			Property<String> nerduinoSourcePathProp = new PropertySupport.Reflection<String>(AppManager.Current, String.class, "DataPath");
			nerduinoSourcePathProp.setName("Nerduino Data Path");
			nerduinoSourcePathProp.setShortDescription("Installed Nerduino Data Path");
			arduinoSheet.put(nerduinoSourcePathProp);
			
			upnpSheet.setDisplayName("uPnP Discovery Settings");
			
			Property<String> upnpServicesProp = new PropertySupport.Reflection<String>(FamilyUPNP.Current, String.class, "Map");
			upnpServicesProp.setName("Upnp-Nerduino Map");
			upnpServicesProp.setShortDescription("XML map of upnp actions/state variables to nerduino points and methods.");
			upnpSheet.put(upnpServicesProp);
		}
		catch(NoSuchMethodException ex)
		{
		}
		
		return new PropertySet[]
				{
					arduinoSheet, webSheet, upnpSheet
				};
	}
	
	public static Object parseValue(byte[] data, int offset, DataTypeEnum dtype, byte dlength)
	{
		Object value = null;
		
		if (dlength == 0)
			return null;
		
		switch(dtype)
		{
			case DT_Boolean:
				value = (data[offset] != 0);
				break;
			case DT_Byte:
				value = data[offset];
				break;
			case DT_Float:
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
			return 0.0F;
		
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
	
	public static byte[] toBytes(Object object)
	{
		if (object instanceof Boolean)
		{
			byte[] data = new byte[1];
			
			if ((Boolean) object == true)
				data[0] = (byte) 1;
			else
				data[0] = (byte) 0;
			
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
			short val = (short) ((Short) object);
			byte[] data = BitConverter.GetBytes(val);
			
			return data;
		}
		
		if (object instanceof Integer)
		{
			int val = (int) ((Integer) object);
			byte[] data = BitConverter.GetBytes(val);
			
			return data;
		}
		
		if (object instanceof Float)
		{
			float f = (float) ((Float) object);
			byte[] data = BitConverter.GetBytes(f);
			
			return data;
		}
		
		if (object instanceof String)
		{
			String s = (String) object;
			
			return s.getBytes();
		}
		
		if (object instanceof byte[])
			return (byte[]) object;
		
		return null;
	}
}
