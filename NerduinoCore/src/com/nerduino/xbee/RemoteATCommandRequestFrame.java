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

public class RemoteATCommandRequestFrame  extends ZigbeeFrameWithResponse
{
    // Declarations
	public long DestinationAddress;
	public short DestinationNetworkAddress;
	public RemoteCommandOptionsEnum Options;
	public byte[] Data;
	
	public Boolean DisableACK;
	public Boolean ApplyChanges;
	public Boolean UseExtendedTimeout;

	public String Command;

    public static int Count;

    // Constructors
    public RemoteATCommandRequestFrame()
    {
        super(FrameTypeEnum.RemoteATCommandRequest);

        FrameType = FrameTypeEnum.RemoteATCommandRequest;

        AutoGenerateFrameID = true;

        Count++;
    }

    // Methods
	@Override
    public Boolean getHasError()
	{
		if (Command.length() != 2)
			return true;
		
		char c0 = Command.charAt(0);
		char c1 = Command.charAt(1);
		
		if (c0 < 'A' || c0 > 'Z' ||
				c1 < 'A' || c1 > 'Z')
			return true;
			
		return false;
	}
	
	@Override
	public short getFrameDataLength()
	{
        if (Data != null)
            return (short) (Data.length + 15);

        return 15;
	}
	
	public String getCommand()
	{
		return Command; 
	}
	
	public void setCommand(String value) 
	{
		// make sure that the commands are two characters long and all caps
		if (value.length() < 2)
			value += "  ";
		
		Command = value.substring(0, 2).toUpperCase();
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
		Options = RemoteCommandOptionsEnum.valueOf(bb.get());
        
        StringBuilder sb = new StringBuilder();
        sb.append(bb.getChar());
        sb.append(bb.getChar());
        
        Command = sb.toString();
        
        if (length > 15)
        {
        	Data = new byte[length - 15];
 
        	for(int i = 0; i < length - 15; i++)
			{
				Data[i] = bb.get();
			}
        }
        else
        {
            Data = null;
        }
    }

	@Override
    public void WriteFrame(ByteBuffer buffer)
    {
		byte options = 0;
		
		if (DisableACK)
			options |= RemoteCommandOptionsEnum.DisableACK.Value();
		
		if (ApplyChanges)
			options |= RemoteCommandOptionsEnum.ApplyChangesOnRemote.Value();
		
		if (UseExtendedTimeout)
			options |= RemoteCommandOptionsEnum.UseExtendedTimeout.Value();
		
		Options = RemoteCommandOptionsEnum.valueOf(options);
		
    	buffer.put(FrameType.Value());
    	buffer.put(FrameID);
    	buffer.putLong(DestinationAddress);
    	buffer.putShort(DestinationNetworkAddress);
    	buffer.put(options);
    	buffer.put(Command.getBytes());
        
        if (Data != null)
            buffer.put(Data);
    }
}
