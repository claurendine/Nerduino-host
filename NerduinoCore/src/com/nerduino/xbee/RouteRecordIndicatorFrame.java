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

public class RouteRecordIndicatorFrame  extends ZigbeeFrame
{
    // Declarations
    public long SourceAddress;
    public short SourceNetworkAddress;
    public ReceiveOptionEnum ReceiveOptions;
    public byte NumberOfAddresses;
	
    public short[] Addresses;

    public static int Count;

    // Constructors
    public RouteRecordIndicatorFrame()
    {
        super(FrameTypeEnum.RouteRecordIndicator);

        FrameType = FrameTypeEnum.RouteRecordIndicator;

        Count++;
    }

    // Methods
	@Override
	public short getFrameDataLength()
	{
		if (Addresses == null)
			return 13;
		
		return (short) (Addresses.length * 2 + 13);
	}

    // Serialize Methods
	@Override
    public void ReadFrame(byte[] data)
    {
    	ByteBuffer bb = ByteBuffer.wrap(data);
    	
    	FrameType = FrameTypeEnum.valueOf(bb.get());

        SourceAddress = bb.getLong();
	    SourceNetworkAddress = bb.getShort();
	    ReceiveOptions = ReceiveOptionEnum.valueOf(bb.get());
	    NumberOfAddresses = bb.get();
		
        if (NumberOfAddresses > 0)
        {
        	Addresses = new short[NumberOfAddresses];
        	
        	for(int i = 0; i < NumberOfAddresses; i++)
			{
				Addresses[i] = bb.getShort();
			}
        }
        else
        {
            Addresses = null;
        }
    }

	@Override
    public void WriteFrame(ByteBuffer buffer)
    {
    	buffer.put(FrameType.Value());
    	buffer.putLong(SourceAddress);
    	buffer.putShort(SourceNetworkAddress);
    	buffer.put(ReceiveOptions.Value());
	    
        if (Addresses == null)
          	NumberOfAddresses = 0;
		else
		    NumberOfAddresses = (byte) Addresses.length;                  
	
        buffer.put(NumberOfAddresses);
		
        for(int i = 0; i < NumberOfAddresses; i++)
		{
			buffer.putShort(Addresses[i]);
		}
    }
}
