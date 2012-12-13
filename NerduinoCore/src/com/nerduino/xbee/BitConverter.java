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
        
        return bb.getShort();
    }
    
    public static int GetInt(byte[] bytes)
    {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        
        return bb.getInt();
    }
    
    public static long GetLong(byte[] bytes)
    {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        
        return bb.getLong();
    }

    public static short GetShort(byte[] bytes, int offset)
    {
        //ByteBuffer bb = ByteBuffer.wrap(bytes);
        
        ByteBuffer bb = ByteBuffer.wrap(bytes, offset, 2);
        
        return bb.getShort();
    }
    
    public static int GetInt(byte[] bytes, int offset)
    {
        ByteBuffer bb = ByteBuffer.wrap(bytes, offset, 4);
        
        return bb.getInt();
    }
    
    public static float GetFloat(byte[] bytes, int offset)
    {
        ByteBuffer bb = ByteBuffer.wrap(bytes, offset, 4);
        
        return bb.getFloat();
    }
    
    public static long GetLong(byte[] bytes, int offset)
    {
        //ByteBuffer bb = ByteBuffer.wrap(bytes);
        
        ByteBuffer bb = ByteBuffer.wrap(bytes, offset, 8);
        
        return bb.getLong();
    }
}
