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

public enum DataTypeEnum 
{
	DT_Boolean(0),
	DT_Byte(1),
	DT_Short(2),
	DT_Integer(3),
	DT_Float(4),
	DT_String(5),
	DT_None(16);
	
	private final byte value;

	DataTypeEnum(int val)
	{
		this.value = (byte)val;
	}

	public byte Value()
	{
		return this.value;
	}

	public byte getLength()
	{
		switch(value)
		{
			case 0:
				return (byte) 1;
			case 1:
				return (byte) 1;
			case 2:
				return (byte) 2;
			case 3:
				return (byte) 4;
			case 4:
				return (byte) 4;
		}	

		return 0;
	}

	public static DataTypeEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return DT_Boolean;
			case 1:
				return DT_Byte;
			case 2:
				return DT_Short;
			case 3:
				return DT_Integer;
			case 4:
				return DT_Float;
			case 5:
				return DT_String;
			case 16:
				return DT_None;
		}	

		return null;
	}
}
