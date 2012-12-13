package com.nerduino.xbee;

public enum P1ConfigurationEnum 
{
	UnmonitoredDigitalInput(0),
	DigitalInput(3),
	DigitalOutputLow(4),
	DigitalOutputHigh(5);
	
    private final byte value;
    
    P1ConfigurationEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static P1ConfigurationEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return UnmonitoredDigitalInput;
			case 3:
				return DigitalInput;
			case 4:
				return DigitalOutputLow;
			case 5:
				return DigitalOutputHigh;
		}	
		
		return null;
	}
}
