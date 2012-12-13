package com.nerduino.xbee;

public enum DeviceTypeEnum 
{
    Coordinator(0),
	Router(1),
	EndDevice(2);

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
				return Coordinator;
			case 1:
				return Router;
			case 2:
				return EndDevice;
		}	
		
		return null;
	}
}
