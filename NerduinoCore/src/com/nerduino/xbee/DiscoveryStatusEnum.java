package com.nerduino.xbee;

public enum DiscoveryStatusEnum 
{
	NoDiscoveryOverhead(0),
	AddressDicovery(0x01),
	RouteDiscovery(0x02),
	AddressAndRoute(0x03),
	ExtendedTimeoutDiscovery(0x40);
	
	private final byte value;
    
	DiscoveryStatusEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static DiscoveryStatusEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return NoDiscoveryOverhead;
			case 1:
				return AddressDicovery;
			case 2:
				return RouteDiscovery;
			case 3:
				return AddressAndRoute;
			case 0x40:
				return ExtendedTimeoutDiscovery;
		}	
		
		return null;
	}

}
