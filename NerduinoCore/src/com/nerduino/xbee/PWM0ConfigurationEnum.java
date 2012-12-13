package com.nerduino.xbee;

public enum PWM0ConfigurationEnum 
{
	Disabled(0),
	RSSI_PWM(1),
	DigitalInput(3),
	DigitalOutputLow(4),
	DigitalOutputHigh(5);
	
    private final byte value;
    
    PWM0ConfigurationEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static PWM0ConfigurationEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return Disabled;
			case 1:
				return RSSI_PWM;
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
