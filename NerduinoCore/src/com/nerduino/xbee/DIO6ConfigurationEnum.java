package com.nerduino.xbee;

public enum DIO6ConfigurationEnum 
{
	Disabled(0),
	RTSFlowControl(1),
	DigitalInput(3),
	DigitalOutputLow(4),
	DigitalOutputHigh(5);
	
    private final byte value;
    
    DIO6ConfigurationEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static DIO6ConfigurationEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return Disabled;
			case 1:
				return RTSFlowControl;
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
