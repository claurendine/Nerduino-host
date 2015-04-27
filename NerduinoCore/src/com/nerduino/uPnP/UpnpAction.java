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

public class UpnpAction
{
	String m_action;
	
	ArrayList<UpnpArgument> m_arguments;
	
	public UpnpAction()
	{
		m_arguments = new ArrayList<UpnpArgument>();
	}
	
	public void parseXML(Element element)
	{
		m_action = element.getAttribute("action");

		NodeList nodes = element.getElementsByTagName("Argument");

		for(int i = 0; i < nodes.getLength(); i++)
		{
			Element earg = (Element) nodes.item(i);

			UpnpArgument arg = new UpnpArgument();

			arg.parseXML(earg);

			m_arguments.add(arg);
		}
	}	
}
