package com.nerduino.xbee;

public enum APIEnableEnum 
{
    Disabled(0),
    Enabled(1),
    EnabledWithPPP(2);
     
    private final byte value;
    
    APIEnableEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static APIEnableEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return Disabled;
			case 1:
				return Enabled;
			case 2:
				return EnabledWithPPP;
		}	
		
		return null;
	}
}
