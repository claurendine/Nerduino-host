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

public enum BootloaderMessageTypeEnum 
{
	ACK(0x06),
	NACK(0x15),
	NoMacACK(0x40),
	Query(0x51),
	QueryResponse(0x52);
	
    private final byte value;
    
    BootloaderMessageTypeEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static BootloaderMessageTypeEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 6:
				return ACK;
			case 0x15:
				return NACK;
			case 0x40:
				return NoMacACK;
			case 0x51:
				return Query;
			case 0x52:
				return QueryResponse;
		}	
		
		return null;
	}
}
