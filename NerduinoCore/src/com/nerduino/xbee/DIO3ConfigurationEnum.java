package com.nerduino.xbee;

public enum DIO3ConfigurationEnum 
{
	Disabled(0),
	AnalogInputSingleEnded(2),
	DigitalInput(3),
	DigitalOutputLow(4),
	DigitalOutputHigh(5);
	
    private final byte value;
    
    DIO3ConfigurationEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static DIO3ConfigurationEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return Disabled;
			case 2:
				return AnalogInputSingleEnded;
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
