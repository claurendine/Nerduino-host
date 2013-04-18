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

public enum SleepModeEnum 
{
	SleepDisabled(0),
	PinSleepEnabled(1),
	CyclicSleepEnabled(4),
	CyclicSleepPinWake(5);
	
    private final byte value;
    
    SleepModeEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static SleepModeEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return SleepDisabled;
			case 1:
				return PinSleepEnabled;
			case 4:
				return CyclicSleepEnabled;
			case 5:
				return CyclicSleepPinWake;
		}	
		
		return null;
	}
}
