package com.nerduino.xbee;

public enum TransmitRequestOptionEnum 
{
	DisableACK(0x01),
    EnableAPSEncryption(0x20),
    UseExtendedTimout(0x40);
	
    private final byte value;
    
    TransmitRequestOptionEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static TransmitRequestOptionEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 1:
				return DisableACK;
			case 0x20:
				return EnableAPSEncryption;
			case 0x40:
				return UseExtendedTimout;
		}	
		
		return null;
	}
}
