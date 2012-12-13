package com.nerduino.library;


public enum PointIdentifierTypeEnum 
{
    PIT_Name(0),
    PIT_All(1);
    
    private final byte value;
    
    PointIdentifierTypeEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static PointIdentifierTypeEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return PIT_Name;
			case 1:
				return PIT_All;
		}	
		
		return null;
	}

}
