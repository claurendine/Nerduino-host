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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtil
{
	public static String GetChildElementText(Element element, String name)
	{
		if (element != null && name != null && !name.isEmpty())
		{
			NodeList nodes = element.getChildNodes();
			
			for(int i = 0; i < nodes.getLength(); i++)
			{
				Node node = nodes.item(i);
				
				if (node instanceof Element && name.equals(node.getNodeName()))
				{
					return node.getTextContent();
				}
			}
		}
		return null;
	}
	
	public static String GetChildElementText(Element element, String name, String defaultValue)
	{
		if (element != null && name != null && !name.isEmpty())
		{
			NodeList nodes = element.getChildNodes();
			
			for(int i = 0; i < nodes.getLength(); i++)
			{
				Node node = nodes.item(i);
				
				if (node instanceof Element && name.equals(node.getNodeName()))
				{
					return node.getTextContent();
				}
			}
		}
		return defaultValue;
	}
	
	public static String SearchForElementText(Element element, String name)
	{
		if (element != null && name != null && !name.isEmpty())
		{
			NodeList nodes = element.getElementsByTagName(name);

			if (nodes != null && nodes.getLength() > 0)
			{
				return nodes.item(0).getTextContent();
			}
		}
		
		return null;
	}
	
	public static String SearchForElementText(Element element, String name, String defaultValue)
	{
		if (element != null && name != null && !name.isEmpty())
		{
			NodeList nodes = element.getElementsByTagName(name);

			if (nodes != null && nodes.getLength() > 0)
			{
				return nodes.item(0).getTextContent();
			}
		}
		
		return defaultValue;
	}

	public static Element GetChildElement(Element element, String name)
	{
		if (element != null && name != null && !name.isEmpty())
		{
			NodeList nodes = element.getChildNodes();
			
			for(int i = 0; i < nodes.getLength(); i++)
			{
				Node node = nodes.item(i);
				
				if (node instanceof Element && name.equals(node.getNodeName()))
				{
					return (Element) node;
				}
			}
		}
		
		return null;
	}
	

}
