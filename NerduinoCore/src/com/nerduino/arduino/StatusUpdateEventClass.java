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


package com.nerduino.arduino;

import java.util.EventObject;

public class StatusUpdateEventClass extends EventObject
{
	public static int COMPILE = 0;
	public static int UPLOAD = 1;
	public static int ENGAGE = 2;
	public static int DIRTY = 3;
	
	public int statusType;
	public boolean pending;
	public int percentComplete;
	public boolean succeeded;
	public String error;
	
	public StatusUpdateEventClass(Object source)
	{
		super(source);
	}
}
