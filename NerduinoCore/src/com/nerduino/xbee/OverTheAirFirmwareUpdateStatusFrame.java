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

public class OverTheAirFirmwareUpdateStatusFrame  extends ZigbeeFrame
{
    // Declarations
    public long SourceAddress;
    public short DestinationNetworkAddress;
    public ReceiveOptionEnum ReceiveOptions;
    public BootloaderMessageTypeEnum BootloaderMessageType;
    public byte BlockNumber;
    public long TargetAddress;

    public static int Count;
    
    // Constructors
    public OverTheAirFirmwareUpdateStatusFrame()
    {
    	super(FrameTypeEnum.OverTheAirFirmwareUpdateStatus);
    	
        FrameType = FrameTypeEnum.OverTheAirFirmwareUpdateStatus;

        Count++;
    }

    // Methods
	@Override
	public short getFrameDataLength()
	{
		return 22;
	}

    // Serialize Methods
	@Override
    public void ReadFrame(byte[] data)
    {
    	ByteBuffer bb = ByteBuffer.wrap(data);
    	
    	FrameType = FrameTypeEnum.valueOf(bb.get());
        SourceAddress = bb.getLong();
        DestinationNetworkAddress = bb.getShort();
		ReceiveOptions = ReceiveOptionEnum.valueOf(bb.get());
		BootloaderMessageType = BootloaderMessageTypeEnum.valueOf(bb.get());
		BlockNumber = bb.get();
		TargetAddress = bb.getLong();
	}

	@Override
    public void WriteFrame(ByteBuffer buffer)
    {
    	buffer.put(FrameType.Value());
    	buffer.putLong(SourceAddress);
    	buffer.putShort(DestinationNetworkAddress);
    	buffer.put(ReceiveOptions.Value());
    	buffer.put(BootloaderMessageType.Value());
    	buffer.put(BlockNumber);
    	buffer.putLong(TargetAddress);
    }
}
