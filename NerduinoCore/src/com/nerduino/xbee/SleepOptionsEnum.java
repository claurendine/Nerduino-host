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

public enum SleepOptionsEnum 
{
	AlwaysWakeForSTTime(0x02),
	SleepEntireSN_x_SP_Time(0x04);

    private final byte value;
    
    SleepOptionsEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static SleepOptionsEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0x02:
				return AlwaysWakeForSTTime;
			case 0x04:
				return SleepEntireSN_x_SP_Time;
		}	
		
		return null;
	}
}
