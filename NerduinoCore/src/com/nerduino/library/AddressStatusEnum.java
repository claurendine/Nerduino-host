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

public enum AddressStatusEnum
{
    AddressFound(1),
    PointFound(2),
    FormatError(3),
    AddressUnknown(4),
    PointUnknown(5);
    
    private final byte value;
    
    AddressStatusEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }
    
	public static AddressStatusEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 1:
				return AddressFound;
			case 2:
				return PointFound;
			case 3:
				return FormatError;
			case 4:
				return AddressUnknown;
			case 5:
				return PointUnknown;
		}	
		
		return null;
	}

}