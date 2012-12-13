package com.nerduino.library;


public enum DeviceTypeEnum 
{
	DT_Host(0),
	DT_Router(1),
	DT_EndPoint(2),
	DT_USB(3);
	
    
    private final byte value;
    
    DeviceTypeEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static DeviceTypeEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return DT_Host;
			case 1:
				return DT_Router;
			case 2:
				return DT_EndPoint;
			case 3:
				return DT_USB;
		}	
		
		return null;
	}

}
