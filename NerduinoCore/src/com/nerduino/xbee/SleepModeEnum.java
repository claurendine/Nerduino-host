package com.nerduino.xbee;

public enum SleepModeEnum 
{
	SleepDisabled(0),
	PinSleepEnabled(1),
	CyclicSleepEnabled(4),
	CyclicSleepPinWake(5);
	
    private final byte value;
    
    SleepModeEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static SleepModeEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return SleepDisabled;
			case 1:
				return PinSleepEnabled;
			case 4:
				return CyclicSleepEnabled;
			case 5:
				return CyclicSleepPinWake;
		}	
		
		return null;
	}
}
