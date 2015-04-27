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

package com.nerduino.uPnP;

import com.nerduino.library.Address;
import com.nerduino.library.AddressStatusEnum;
import com.nerduino.library.DeviceTypeEnum;
import com.nerduino.library.NerduinoBase;
import com.nerduino.library.NerduinoLight;		
import static com.nerduino.library.NerduinoStatusEnum.Online;
import com.nerduino.library.RemoteDataPoint;
import java.awt.Image;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.ActionList;
import org.cybergarage.upnp.Argument;
import org.cybergarage.upnp.ArgumentList;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.Service;
import org.cybergarage.upnp.ServiceList;
import org.openide.nodes.Sheet;
import processing.app.Sketch;

public class NerduinoUPNP extends NerduinoLight
{
	public Device m_upnpDevice = null;
	
	ArrayList<UpnpMethod> m_methods;

	public NerduinoUPNP()
	{
		super("uPnP", "/com/nerduino/resources/NerduinoUPNPOnline16.png");
		
		m_canDelete = false;
		m_canCopy = false;
		m_canDrag = false;
		m_canRename = false;
		
		m_methods = new ArrayList<UpnpMethod>();
		
		setDeviceType(DeviceTypeEnum.DT_UPNP);
	}
	
	public boolean getTest()
	{
		return false;
	}
	
//	boolean on = false;
	
	public void setTest(boolean val)
	{
//		Action action = m_upnpDevice.getAction("SetBinaryState");
//  /      action.setArgumentValue("BinaryState", on ? 1 : 0);

//		on = !on;
		
//		action.postControlAction();
		
	}

	@Override
	public void setName(String value)
	{
		super.setName(value);
		
		m_upnpDevice.setFriendlyName(value);		
	}
	
	@Override
	public Image getIcon(int type)
	{
		java.net.URL imgURL;

		if (getStatus() == Online)
		{
			imgURL = getClass().getResource("/com/nerduino/resources/NerduinoUPNPOnline16.png");
		}
		else
		{
			imgURL = getClass().getResource("/com/nerduino/resources/NerduinoUPNPOffline16.png");
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
	public PropertySet[] getPropertySets()
	{								
		Sheet.Set deviceSheet = Sheet.createPropertiesSet();
		
		deviceSheet.setDisplayName("Device Information");
		
		addProperty(deviceSheet, m_upnpDevice, String.class, null, "DeviceType", "The reported uPnP device type.");
		addProperty(deviceSheet, m_upnpDevice, String.class, null, "Manufacture", "The reported uPnP device manufacturer.");
		addProperty(deviceSheet, m_upnpDevice, String.class, null, "ModelDescription", "The reported uPnP device manufacturer.");
		addProperty(deviceSheet, m_upnpDevice, String.class, null, "ModelName", "The reported uPnP device manufacturer.");
		addProperty(deviceSheet, m_upnpDevice, String.class, null, "ModelNumber", "The reported uPnP device manufacturer.");
		addProperty(deviceSheet, m_upnpDevice, String.class, null, "SerialNumber", "The reported uPnP device manufacturer.");
		
		PropertySet[] basesets = super.getPropertySets();
		PropertySet[] sets = new PropertySet[basesets.length + 1];
		
		System.arraycopy(basesets, 0, sets, 0, basesets.length);
		
		sets[basesets.length] = deviceSheet;
		
		return sets;
	}
	
	@Override
	public void forwardMessage(NerduinoBase originator, byte[] data)
	{
	}
	
	@Override
	public void sendGetAddressResponse(short responseToken, AddressStatusEnum status, Address address, short pointIndex)
	{

	}
	
	@Override
	public String engage()
	{
		return null;
	}

	@Override
	public String upload(Sketch sketch)
	{
		return null;
	}
	
	@Override
	public String getHTML()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("<html>\n"
                  + "<body>\n"
                  + "<h1>Nerduino: " + getName() + "  (uPnP Device: " + m_upnpDevice.getFriendlyName() + ")</h1>\n"
                  + "<h2>"
				  + "DeviceType: " + m_upnpDevice.getDeviceType() + "<br><br><pre>"
				);
		
		ServiceList sl = m_upnpDevice.getServiceList();
		
		for(int i = 0; i < sl.size(); i++)
		{
			Service service = sl.getService(i);
			
			sb.append("Service: " + service.getServiceID() + " - " + service.getServiceType() +  "<br>");			
			
			ActionList al = service.getActionList();
			
			for(int j = 0; j < sl.size(); j++)
			{
				try
				{
					Action action = al.getAction(j);
					
					sb.append("    Action: " + action.getName() + "<br>");			

					ArgumentList args = action.getArgumentList();

					try
					{
						for(int k = 0; k < args.size(); k++)
						{
							Argument arg = args.getArgument(k);

							sb.append("        " + arg.getDirection() + " " + arg.getName() + " : " + arg.getRelatedStateVariableName() + "<br>");

						}
					}
					catch(Exception e)
					{
					}
				}
				catch(Exception e)
				{
				}
			}			
		}
		
		sb.append("</pre></h2>\n"
				  + "</body>\n");
		
		return sb.toString();
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

	public void setUpnpDevice(Device device)
	{
		m_upnpDevice = device;
		
		String name = device.getFriendlyName();
		
		setName(name);
		setDisplayName(name);
	}

	public void setUpnpDeviceType(UpnpDeviceType dt)
	{
		// declare local points and methods from the device type definition
		
		short index = 0;
		
		for(UpnpPoint point : dt.getPoints())
		{
			UpnpPoint upnpPoint = new UpnpPoint(this, point);
			
			upnpPoint.Id = index++;
			//upnpPoint.setValue("");
			
			m_localDataPoints.add(upnpPoint);
		}
		
		m_pointCount = index;
		
		for(UpnpMethod method : dt.getMethods())
		{
			UpnpMethod localMethod = new UpnpMethod(this, method);
			
			m_methods.add(localMethod);
		}
	}
}
