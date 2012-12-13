package com.nerduino.xbee;

public enum SleepOptionsEnum 
{
	AlwaysWakeForSTTime(0x02),
	SleepEntireSN_x_SP_Time(0x04);

    private final byte value;
    
    SleepOptionsEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static SleepOptionsEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0x02:
				return AlwaysWakeForSTTime;
			case 0x04:
				return SleepEntireSN_x_SP_Time;
		}	
		
		return null;
	}
}
