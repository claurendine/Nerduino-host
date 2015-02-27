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

package com.nerduino.nodes;

import java.awt.Component;
import java.awt.Dimension;

public class EmptyCommand extends Component
{
	public static int m_height = 22;
	
	public EmptyCommand()
	{
		setMaximumSize(new Dimension(120, m_height));
		setMinimumSize(new Dimension(4, 1));
		setPreferredSize(new Dimension(60, m_height));
	}
}
