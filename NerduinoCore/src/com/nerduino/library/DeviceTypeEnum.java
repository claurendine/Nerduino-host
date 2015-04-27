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


public enum DeviceTypeEnum 
{
	DT_Host(0),
	DT_ZigbeeRouter(1),
	DT_ZigbeeEndPoint(2),
	DT_USB(3),
	DT_TCP(4),
	DT_UPNP(5),
	DT_Plugin(6);
	
    
    private final byte value;
    
    DeviceTypeEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static DeviceTypeEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return DT_Host;
			case 1:
				return DT_ZigbeeRouter;
			case 2:
				return DT_ZigbeeEndPoint;
			case 3:
				return DT_USB;
			case 4:
				return DT_TCP;
			case 5:
				return DT_UPNP;
			case 6:
				return DT_Plugin;
		}
		
		return null;
	}
}
