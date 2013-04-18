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

import java.nio.ByteBuffer;

public class ZigbeeTransmitStatusFrame  extends ZigbeeFrameWithResponse
{
    // Declarations
	public short DestinationNetworkAddress;
	public byte TransmitRetryCount;
	public DeliveryStatusEnum DeliveryStatus;
	public DiscoveryStatusEnum DiscoveryStatus;

    public static int Count;
    
    // Constructors
    public ZigbeeTransmitStatusFrame()
    {
    	super(FrameTypeEnum.ZigbeeTransmitStatus);

        FrameType = FrameTypeEnum.ZigbeeTransmitStatus;

        Count++;
    }

    // Methods
	@Override
	public short getFrameDataLength()
	{
		return 7;
	}
	
    // Serialize Methods
	@Override
    public void ReadFrame(byte[] data)
    {
    	ByteBuffer bb = ByteBuffer.wrap(data);
    	
    	FrameType = FrameTypeEnum.valueOf(bb.get());
        FrameID = bb.get();
    
        DestinationNetworkAddress = bb.getShort();
	    TransmitRetryCount = bb.get();
	    DeliveryStatus = DeliveryStatusEnum.valueOf(bb.get());
	    DiscoveryStatus = DiscoveryStatusEnum.valueOf(bb.get());
    }

	@Override
    public void WriteFrame(ByteBuffer buffer)
    {
     	buffer.put(FrameType.Value());
    	buffer.put(FrameID);
    	buffer.putShort(DestinationNetworkAddress);
    	buffer.put(TransmitRetryCount);
    	buffer.put(DeliveryStatus.Value());
	    buffer.put(DiscoveryStatus.Value());
    }
}
