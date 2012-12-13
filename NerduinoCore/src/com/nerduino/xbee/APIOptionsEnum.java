package com.nerduino.xbee;

public enum APIOptionsEnum 
{
	DefaultReceiveAPIIndicatorsEnabled(0),
	ExplicitRxDataIndicatorAPIFrameEnabled(1),
	ZDOPassthrough(3);

    private final byte value;
    
    APIOptionsEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static APIOptionsEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return DefaultReceiveAPIIndicatorsEnabled;
			case 1:
				return ExplicitRxDataIndicatorAPIFrameEnabled;
			case 2:
				return ZDOPassthrough;
		}	
		
		return null;
	}
}
