package com.nerduino.library;


public enum PointValueStatusEnum 
{
    PVS_OK(0),
    PVS_DataError(1),
    PVS_InvalidIndex(2),
    PVS_InvalidFilter(3);
    
    private final byte value;
    
    PointValueStatusEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static PointValueStatusEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return PVS_OK;
			case 1:
				return PVS_DataError;
			case 2:
				return PVS_InvalidIndex;
			case 3:
				return PVS_InvalidFilter;
		}	
		
		return null;
	}

}
