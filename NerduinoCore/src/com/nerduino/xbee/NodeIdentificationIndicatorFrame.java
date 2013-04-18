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

public class NodeIdentificationIndicatorFrame  extends ZigbeeFrame
{
    // Declarations
    public long SourceAddress;
    public short SourceNetworkAddress;
    public ReceiveOptionEnum Options;
    public short RemoteNetworkAddress;
    public long RemoteAddress;
    public String NodeIdentifier;
    public short RemoteParentNetworkAddress;
    public DeviceTypeEnum DeviceType;
    public SourceEventEnum SourceEvent;
    public short DigiApplicaionProfileID;
    public short DigiManufacturerID;

    public static int Count;
    
    // Constructors
    public NodeIdentificationIndicatorFrame()
    {
        super(FrameTypeEnum.NodeIdentificationIndicator);

        FrameType = FrameTypeEnum.NodeIdentificationIndicator;

        Count++;
    }

    // Methods
	@Override
	public short getFrameDataLength()
	{
		return (short) (31 + NodeIdentifier.length());
	}
	
    // Serialize Methods
	@Override
    public void ReadFrame(byte[] data)
    {
    	ByteBuffer bb = ByteBuffer.wrap(data);
    	
    	FrameType = FrameTypeEnum.valueOf(bb.get());
        SourceAddress = bb.getLong();
        SourceNetworkAddress = bb.getShort();
		Options = ReceiveOptionEnum.valueOf(bb.get());
		RemoteNetworkAddress = bb.getShort();
        RemoteAddress = bb.getLong();
        
        StringBuilder sb = new StringBuilder();
        
        char c;
        
        do
        {
        	c = bb.getChar();
        	
        	if (c != 0)
        		sb.append(c);
        } while(c != 0);
        
        NodeIdentifier = sb.toString();
        
		RemoteParentNetworkAddress = bb.getShort();
		DeviceType = DeviceTypeEnum.valueOf(bb.get());
		SourceEvent = SourceEventEnum.valueOf(bb.get());
		DigiApplicaionProfileID = bb.getShort();
		DigiManufacturerID = bb.getShort();
    }

	@Override
    public void WriteFrame(ByteBuffer buffer)
    {
    	buffer.put(FrameType.Value());
    	buffer.putLong(SourceAddress);
    	buffer.putShort(SourceNetworkAddress);
		buffer.put(Options.Value());
		buffer.putShort(RemoteNetworkAddress);
		buffer.putLong(RemoteAddress);
		
		buffer.put(NodeIdentifier.getBytes());
		buffer.put((byte) 0);
		
		buffer.putShort(RemoteParentNetworkAddress);
		buffer.put(DeviceType.Value());
		buffer.put(SourceEvent.Value());
		buffer.putShort(DigiApplicaionProfileID);
		buffer.putShort(DigiManufacturerID);
    }
}
