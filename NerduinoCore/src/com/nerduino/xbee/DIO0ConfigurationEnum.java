package com.nerduino.xbee;

public enum DIO0ConfigurationEnum 
{
	CommissioningButtonEnabled(1),
	AnalogInputSingleEnded(2),
	DigitalInput(3),
	DigitalOutputLow(4),
	DigitalOutputHigh(5);
	
    private final byte value;
    
    DIO0ConfigurationEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static DIO0ConfigurationEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 1:
				return CommissioningButtonEnabled;
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
