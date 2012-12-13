package com.nerduino.xbee;

public enum DIO7ConfigurationEnum 
{
	Disabled(0),
	CTSFlowControl(1),
	DigitalInput(3),
	DigitalOutputLow(4),
	DigitalOutputHigh(5),
	RS485TransmitEnableLow(6),
	RS485TransmitEnableHigh(7);

	
	
    private final byte value;
    
    DIO7ConfigurationEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static DIO7ConfigurationEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return Disabled;
			case 1:
				return CTSFlowControl;
			case 3:
				return DigitalInput;
			case 4:
				return DigitalOutputLow;
			case 5:
				return DigitalOutputHigh;
			case 6:
				return RS485TransmitEnableLow;
			case 7:
				return RS485TransmitEnableHigh;
		}	
		
		return null;
	}
}
