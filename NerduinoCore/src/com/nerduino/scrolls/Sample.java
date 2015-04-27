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

package com.nerduino.scrolls;

import java.util.List;
import org.w3c.dom.Element;

public class Sample extends EventBase
{
	double m_values[];

	void loadXML(Element node, List<String> names)
	{
		m_time = Double.parseDouble(node.getAttribute("time"));
		
		int count = names.size();
		
		m_values = new double[count];
		
		for(int i = 0; i < count; i++)
		{
			m_values[i] = Double.parseDouble(node.getAttribute(names.get(i)));
		}
	}
}
