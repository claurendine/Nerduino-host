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

public class UpnpMethod
{
	NerduinoUPNP m_nerduino;
	
	String m_name;
	String m_type;
	
	ArrayList<UpnpAction> m_actions;
	
	public UpnpMethod()
	{
		m_actions = new ArrayList<UpnpAction>();
	}

	public UpnpMethod(NerduinoUPNP nerduino, UpnpMethod method)
	{
		m_nerduino = nerduino;
		m_actions = method.m_actions;
	}

	
	public void parseXML(Element element)
	{
		m_name = element.getAttribute("name");
		m_type = element.getAttribute("type");
		
		NodeList nodes = element.getElementsByTagName("Action");

		for(int i = 0; i < nodes.getLength(); i++)
		{
			Element eaction = (Element) nodes.item(i);

			UpnpAction action = new UpnpAction();

			action.parseXML(eaction);

			m_actions.add(action);
		}
	}	
}
