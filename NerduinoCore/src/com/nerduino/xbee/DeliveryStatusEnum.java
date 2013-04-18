/*
 Part of the Nerduino IOT project - http://nerduino.com

 Copyright (c) 2013 Chase Laurendine

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

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
