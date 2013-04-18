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

public class CreateSourceRouteFrame  extends ZigbeeFrameWithResponse
{
	// Declarations
    public long DestinationAddress;
    public short DestinationNetworkAddress;
    public byte RouteCommandOptions;
    public byte NumberOfAddresses;
    public short[] Addresses;

    public Boolean NetworkAddressUnknown;
    public Boolean Broadcast;
    public Boolean ToCoordinator;

    public static int Count;

    // Constructors
    public CreateSourceRouteFrame()
    {
    	super(FrameTypeEnum.CreateSourceRoute);
    
        FrameType = FrameTypeEnum.CreateSourceRoute;

        Count++;
    }

    // Methods
	@Override
	public short getFrameDataLength()
	{
		if (Addresses == null)
			return 14;
		
		return (short) (Addresses.length * 2 + 14);
	}
		
	public void setBroadcast(Boolean value)
	{
		Broadcast = value;
		
		if (Broadcast)
			ToCoordinator = false;
	}
	
	public void setToCoordinator(Boolean value)
	{
		ToCoordinator = value;
		
		if (ToCoordinator)
			Broadcast = false;
	}
	
    // Serialize Methods
	@Override
    public void ReadFrame(byte[] data)
    {
    	int length = data.length;
    	
    	ByteBuffer bb = ByteBuffer.wrap(data);
    	
    	FrameType = FrameTypeEnum.valueOf(bb.get());
        FrameID = bb.get();
        
        DestinationAddress = bb.getLong();
        DestinationNetworkAddress = bb.getShort();
		RouteCommandOptions = bb.get();
       	NumberOfAddresses = bb.get();
		
		if (length > 14)
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
    	if (Broadcast)
        {
            DestinationAddress = 0xffff;
		}
		
		if (ToCoordinator)
		{
			DestinationAddress = 0;
			DestinationNetworkAddress = -2; // 0xfffe;
		}
		
		if (NetworkAddressUnknown)
		{
			DestinationNetworkAddress = -2; // 0xfffe;
		}
    	
    	buffer.put(FrameType.Value());
    	buffer.put(FrameID);
    	buffer.putLong(DestinationAddress);
    	buffer.putShort(DestinationNetworkAddress);
    	buffer.put(RouteCommandOptions);
    	buffer.putShort(NumberOfAddresses);
		
		if (Addresses != null)
            for(int i = 0; i < Addresses.length; i++)
            {
				buffer.putShort(Addresses[i]);
            }
    }
}
