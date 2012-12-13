package com.nerduino.xbee;

public enum DeliveryStatusEnum 
{
	Success(0),
	MacAckFailure(0x01),
	CcaFailure(0x02),
	InvalidDestinationEndpoint(0x15),
	NetworkAckFailure(0x21),
	NotJoinedToNetwork(0x22),
	SelfAddressed(0x23),
	AddressNotFound(0x24),
	RouteNotFound(0x25),
	BroadcastFailedToHearNeighborRelay(0x26),
	InvalidBindingTableIndex(0x2b),
	ResourceError(0x2c),
	AttemptedBroadcastWithAPSTransmission(0x2d),
	AttemptedUnicastWithAPSTransmission(0x2e),
	ResouceError2(0x32),
	DataPayloadTooLarge(0x74),
	IndirectMessageUnrequested(0x75);
	
    private final byte value;
    
    DeliveryStatusEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static DeliveryStatusEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return Success;
			case 0x01:
				return MacAckFailure;
			case 0x02:
				return CcaFailure;
			case 0x15:
				return InvalidDestinationEndpoint;
			case 0x21:
				return NetworkAckFailure;
			case 0x22:
				return NotJoinedToNetwork;
			case 0x23:
				return SelfAddressed;
			case 0x24:
				return AddressNotFound;
			case 0x25:
				return RouteNotFound;
			case 0x26:
				return BroadcastFailedToHearNeighborRelay;
			case 0x2b:
				return InvalidBindingTableIndex;
			case 0x2c:
				return ResourceError;
			case 0x2d:
				return AttemptedBroadcastWithAPSTransmission;
			case 0x2e:
				return AttemptedUnicastWithAPSTransmission;
			case 0x32:
				return ResouceError2;
			case 0x74:
				return DataPayloadTooLarge;
			case 0x75:
				return IndirectMessageUnrequested;
		}	
		
		return null;
	}


}
