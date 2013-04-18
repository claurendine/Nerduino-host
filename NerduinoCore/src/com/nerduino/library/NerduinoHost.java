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
import com.nerduino.processing.app.ArduinoManager;
import com.nerduino.propertybrowser.BaudRatePropertyEditor;
import com.nerduino.propertybrowser.ComPortPropertyEditor;
import com.nerduino.webhost.WebHost;
import com.nerduino.xbee.BitConverter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class NerduinoHost extends TreeNode 
{
	// Declarations
	Children m_hostPoints;
	XBeeManager m_xbeeManager;
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
		m_xbeeManager = new XBeeManager();

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

	public int getHostPointCount()
	{
		return m_hostPoints.getNodesCount();
	}
	
	public LocalDataPoint getHostPoint(int index)
	{
		return (LocalDataPoint) m_hostPoints.getNodeAt(index);
	}
	
	public String getDataPath()
	{
		return "/Users/chaselaurendine/Documents/Nerduino";
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

				m_xbeeManager.readXML(config);
			}
		}
	}

	@Override
	public void writeXML(Document doc, Element node)
	{
		Element element = doc.createElement("NerduinoHost");
		
		m_xbeeManager.writeXML(doc, element);
		
		node.appendChild(element);
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
			/*
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
			*/
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

}
