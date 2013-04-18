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

package com.nerduino.xbee;

public enum RemoteCommandOptionsEnum 
{
	DisableACK(0x01),
	ApplyChangesOnRemote(0x02),
	UseExtendedTimeout(0x40);
	
    private final byte value;
    
    RemoteCommandOptionsEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static RemoteCommandOptionsEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 1:
				return DisableACK;
			case 2:
				return ApplyChangesOnRemote;
			case 0x40:
				return UseExtendedTimeout;
		}	
		
		return null;
	}

}
