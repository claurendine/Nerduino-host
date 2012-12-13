package com.nerduino.xbee;

public enum RemoteCommandOptionsEnum 
{
	DisableACK(0x01),
	ApplyChangesOnRemote(0x02),
	UseExtendedTimeout(0x40);
	
    private final byte value;
    
    RemoteCommandOptionsEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static RemoteCommandOptionsEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 1:
				return DisableACK;
			case 2:
				return ApplyChangesOnRemote;
			case 0x40:
				return UseExtendedTimeout;
		}	
		
		return null;
	}

}
