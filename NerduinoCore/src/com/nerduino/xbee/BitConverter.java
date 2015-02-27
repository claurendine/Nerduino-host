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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BitConverter 
{
	public static byte[] GetBytes(Boolean value)
	{
		byte[] ret = new byte[1];
	
		ret[0] = (byte) (value?1:0);
				
		return ret;
	}
	
	public static byte[] GetBytes(byte value)
	{
		byte[] ret = new byte[1];
		
		ret[0] = value;
		
		return ret;
	}
	
	public static byte[] GetBytes(short value)
	{
		ByteBuffer bb = ByteBuffer.allocate(2);
		
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putShort(value);

		return bb.array();
	}
	
	public static byte[] GetBytes(int value)
	{
		ByteBuffer bb = ByteBuffer.allocate(4);
		
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(value);

		return bb.array();
	}
	
	public static byte[] GetBytes(long value)
	{
		ByteBuffer bb = ByteBuffer.allocate(8);
		
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putLong(value);

		return bb.array();
	}
	
	public static byte[] GetBytes(float value)
	{
		ByteBuffer bb = ByteBuffer.allocate(4);
		
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putFloat(value);

		return bb.array();
	}
	
	public static byte[] GetBytes(double value)
	{
		ByteBuffer bb = ByteBuffer.allocate(8);
		
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putDouble(value);

		return bb.array();
	}
    
    public static short GetShort(byte[] bytes)
    {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
		
        return bb.getShort();
    }
    
    public static int GetInt(byte[] bytes)
    {
		//return (int)bytes[0] * 0x1000000 + (int)bytes[1] * 0x10000 + (int)bytes[2] * 0x100 + (int)bytes[3];
		
		ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);
		
        return bb.getInt();

    }
    
    public static long GetLong(byte[] bytes)
    {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        return bb.getLong();
    }

    public static short GetShort(byte[] bytes, int offset)
    {
        ByteBuffer bb = ByteBuffer.wrap(bytes, offset, 2);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        return bb.getShort();
    }
    
    public static int GetInt(byte[] bytes, int offset)
    {
		ByteBuffer bb = ByteBuffer.wrap(bytes, offset, 4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
		
        return bb.getInt();
    }
    
    public static float GetFloat(byte[] bytes, int offset)
    {
        ByteBuffer bb = ByteBuffer.wrap(bytes, offset, 4);
		bb.order(ByteOrder.LITTLE_ENDIAN);

        return bb.getFloat();
    }
    
    public static float GetFloat(byte[] bytes)
    {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.order(ByteOrder.LITTLE_ENDIAN);

        return bb.getFloat();
    }
    
    public static long GetLong(byte[] bytes, int offset)
    {
        ByteBuffer bb = ByteBuffer.wrap(bytes, offset, 8);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        return bb.getLong();
    }
}
