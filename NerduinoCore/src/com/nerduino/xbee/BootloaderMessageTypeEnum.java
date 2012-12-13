package com.nerduino.xbee;

public enum BootloaderMessageTypeEnum 
{
	ACK(0x06),
	NACK(0x15),
	NoMacACK(0x40),
	Query(0x51),
	QueryResponse(0x52);
	
    private final byte value;
    
    BootloaderMessageTypeEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static BootloaderMessageTypeEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 6:
				return ACK;
			case 0x15:
				return NACK;
			case 0x40:
				return NoMacACK;
			case 0x51:
				return Query;
			case 0x52:
				return QueryResponse;
		}	
		
		return null;
	}
}
