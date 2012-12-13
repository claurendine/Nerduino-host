/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.library;

/**
 *
 * @author chaselaurendine
 */
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