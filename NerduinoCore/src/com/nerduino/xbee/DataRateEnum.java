package com.nerduino.xbee;

public enum DataRateEnum 
{
	b_1200(0),
	b_2400(1),
	b_4800(2),
	b_9600(3),
	b_19200(4),
	b_38400(5),
	b_57600(6),
	b_115200(7);

    private final byte value;
    
    DataRateEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static DataRateEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return b_1200;
			case 1:
				return b_2400;
			case 2:
				return b_4800;
			case 3:
				return b_9600;
			case 4:
				return b_19200;
			case 5:
				return b_38400;
			case 6:
				return b_57600;
			case 7:
				return b_115200;
		}	
		
		return null;
	}
}
