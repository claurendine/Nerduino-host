package com.nerduino.library;


public enum LifeSpanTypeEnum 
{
	LST_NeverDies(0),
	LST_Timeout(1);
    
    private final byte value;
    
    LifeSpanTypeEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static LifeSpanTypeEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return LST_NeverDies;
			case 1:
				return LST_Timeout;
		}	
		
		return null;
	}
}
