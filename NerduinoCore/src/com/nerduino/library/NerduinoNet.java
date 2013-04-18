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


public class NerduinoNet extends NerduinoFull implements FrameReceivedListener
{
	NerduinoSocket m_socket;
	
	SerialBase m_serial;
	CommandResponse commandResponse;
	
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

		sendPing(null, (short) 0);

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

		sendGetMetaData(null, (short) 0);

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
			sendGetPoints(null, (short) 0);

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
}
