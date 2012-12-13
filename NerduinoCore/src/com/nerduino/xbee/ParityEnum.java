package com.nerduino.xbee;

public enum ParityEnum 
{
	None(0),
	Even(1),
	Odd(2),
	Mark(3);
	
    private final byte value;
    
    ParityEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static ParityEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return None;
			case 1:
				return Even;
			case 2:
				return Odd;
			case 3:
				return Mark;
		}	
		
		return null;
	}
}
