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

import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class UpnpDeviceType
{
	String m_pattern;
	ArrayList<UpnpPoint> m_points;
	ArrayList<UpnpMethod> m_methods;
	
	public UpnpDeviceType()
	{
		m_points = new ArrayList<UpnpPoint>();
		m_methods = new ArrayList<UpnpMethod>();
	}

	public String getPattern()
	{
		return m_pattern;
	}
	
	public ArrayList<UpnpPoint> getPoints()
	{
		return m_points;
	}

	public ArrayList<UpnpMethod> getMethods()
	{
		return m_methods;
	}

	public void parseXML(Element element)
	{
		m_pattern = element.getAttribute("pattern");
		
		NodeList nodes = element.getElementsByTagName("Point");
		
		for(int i = 0; i < nodes.getLength(); i++)
		{
			Element epoint = (Element) nodes.item(i);
			
			UpnpPoint point = new UpnpPoint();
			
			point.parseXML(epoint);
			
			m_points.add(point);
		}
		
		
		nodes = element.getElementsByTagName("Method");
		
		for(int i = 0; i < nodes.getLength(); i++)
		{
			Element emethod = (Element) nodes.item(i);
			
			UpnpMethod method = new UpnpMethod();
			
			method.parseXML(emethod);
			
			m_methods.add(method);
		}
	}
}
