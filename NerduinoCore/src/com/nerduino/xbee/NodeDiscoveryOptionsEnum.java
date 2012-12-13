package com.nerduino.xbee;

public enum NodeDiscoveryOptionsEnum 
{
	AppendDD(0x01),
	LocalDeviceSendsNDResponse(0x02);
	
    private final byte value;
    
    NodeDiscoveryOptionsEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static NodeDiscoveryOptionsEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0x01:
				return AppendDD;
			case 0x02:
				return LocalDeviceSendsNDResponse;
		}	
		
		return null;
	}
}
