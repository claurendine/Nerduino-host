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

import com.nerduino.library.DataTypeEnum;
import com.nerduino.library.FamilyUPNP;
import com.nerduino.library.LocalDataPoint;
import java.util.ArrayList;
import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.Argument;
import org.cybergarage.upnp.Service;
import org.cybergarage.upnp.ServiceList;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class UpnpPoint extends LocalDataPoint
{
	String m_type;
	NerduinoUPNP m_nerduino;
	
	ArrayList<UpnpAction> m_getActions;
	ArrayList<UpnpAction> m_setActions;
	
	String m_eventService = "";
	public String m_eventStateVariable = "";
	
	public UpnpPoint()
	{
		m_getActions = new ArrayList<UpnpAction>();
		m_setActions = new ArrayList<UpnpAction>();
	}
	
	public UpnpPoint(NerduinoUPNP nerduino, UpnpPoint point)
	{
		m_nerduino = nerduino;
		
		m_getActions = point.m_getActions;
		m_setActions = point.m_setActions;
		
		m_eventService = point.m_eventService;
		m_eventStateVariable = point.m_eventStateVariable;
		
		setName(point.getName());
		
		String dtype = point.m_type.toLowerCase();
		
		if ("float".equals((dtype)))
			setDataType(DataTypeEnum.DT_Float);
		else if ("string".equals((dtype)))
			setDataType(DataTypeEnum.DT_String);
		else if ("bool".equals((dtype)))
			setDataType(DataTypeEnum.DT_Boolean);
		else if ("short".equals((dtype)))
			setDataType(DataTypeEnum.DT_Short);
		else if ("byte".equals((dtype)))
			setDataType(DataTypeEnum.DT_Byte);
		else 
			setDataType(DataTypeEnum.DT_Integer);
		
		if (m_eventService != null && m_eventService.length() > 0
			&& m_eventStateVariable != null && m_eventStateVariable.length() > 0)
		{
			// loop through services looking for a matching service
			// subscribe to this service if it is not already subscribed			
			ServiceList services = nerduino.m_upnpDevice.getServiceList();
			for (int m = 0; m < services.size(); m++) 
			{
				Service svc = services.getService(m);
				if (!svc.hasSID() && svc.getServiceType().contains(m_eventService)) 
				{
					// subscribe to events
					FamilyUPNP.Current.m_controlPoint.subscribe(svc);
					break;
				}
			}
			
		}
	}
	
	@Override
	public Object getValue()
	{
		for(UpnpAction uaction : m_getActions)
		{
			Action action = m_nerduino.m_upnpDevice.getAction(uaction.m_action);
			
			if (action != null)
			{
				action.postControlAction();

				for(UpnpArgument uarg : uaction.m_arguments)
				{
					if (uarg.m_return)
					{
						Argument arg = action.getOutputArgumentList().getArgument(uarg.m_stateVariable);

						String value = arg.getValue();
						
						return value;
					}
				}

			}
		}
		
		return super.getValue();
	}
	
	public void remoteSetValue(Object val)
	{
		super.setValue(val);
	}
	
	@Override
	public void setValue(Object val)
	{
		super.setValue(val);
		
		String svalue;
		
		if (DataType == DataTypeEnum.DT_Boolean)
		{
			boolean b = getBoolean();
			
			svalue = b ? "1" : "0";
		}
		else
		{
			svalue = val.toString();
		}
		
		for(UpnpAction uaction : m_setActions)
		{
			Action action = m_nerduino.m_upnpDevice.getAction(uaction.m_action);
			
			if (action != null)
			{
				for(UpnpArgument uarg : uaction.m_arguments)
				{
					if ("param".equals(uarg.m_value))
					{
						action.setArgumentValue(uarg.m_stateVariable, svalue);				
					}
					else
					{
						// provided constant value
						action.setArgumentValue(uarg.m_stateVariable, uarg.m_value);					
					}
				}

				action.postControlAction();
			}
		}
	}
	
	public void parseXML(Element element)
	{
		m_name = element.getAttribute("name");
		m_type = element.getAttribute("type");
		m_eventService = element.getAttribute("service");
		m_eventStateVariable = element.getAttribute("stateVariable");
	
		Element eget = XmlUtil.GetChildElement(element, "Get");
		
		if (eget != null)
		{
			NodeList nodes = eget.getElementsByTagName("Action");

			for(int i = 0; i < nodes.getLength(); i++)
			{
				Element eaction = (Element) nodes.item(i);

				UpnpAction action = new UpnpAction();

				action.parseXML(eaction);

				m_getActions.add(action);
			}
		}
		
		Element eset = XmlUtil.GetChildElement(element, "Set");
		
		if (eset != null)
		{
			NodeList nodes = eset.getElementsByTagName("Action");

			for(int i = 0; i < nodes.getLength(); i++)
			{
				Element eaction = (Element) nodes.item(i);

				UpnpAction action = new UpnpAction();

				action.parseXML(eaction);

				m_setActions.add(action);
			}
		}
	}
}
