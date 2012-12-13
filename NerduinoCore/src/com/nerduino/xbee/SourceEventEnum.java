package com.nerduino.xbee;

public enum SourceEventEnum 
{
	SentByNodeIdentificationPushbutton(1),
	SentAfterJoiningEventOccured(2),
	SentAfterPowerCycleEventOccured(3);

    private final byte value;
    
    SourceEventEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static SourceEventEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 1:
				return SentByNodeIdentificationPushbutton;
			case 2:
				return SentAfterJoiningEventOccured;
			case 3:
				return SentAfterPowerCycleEventOccured;
		}	
		
		return null;
	}
}
