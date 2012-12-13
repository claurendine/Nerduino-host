package com.nerduino.library;


public enum IdentifierTypeEnum 
{
	IT_NetworkAddress(0),
	IT_HardwareAddress(1),
	IT_Name(2);
    
    private final byte value;
    
    IdentifierTypeEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static IdentifierTypeEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return IT_NetworkAddress;
			case 1:
				return IT_HardwareAddress;
			case 2:
				return IT_Name;
		}	
		
		return null;
	}
}
