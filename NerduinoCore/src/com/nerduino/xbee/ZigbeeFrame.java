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

public class ZigbeeFrame 
{
	// Declarations
    public FrameTypeEnum FrameType;
    public static Boolean EscapedMode;
    
    public byte[] Buffer = new byte[256];
    
    // Constructors
    public ZigbeeFrame(FrameTypeEnum frameType)
    {
        FrameType = frameType;
		
		Initialize();
    }
	
	protected void Initialize()
	{
	}

    // Methods
	public Boolean getHasError()
	{
        return false;
	}
	
	public short getFrameDataLength()
	{
        return 0;
	}

	public int Send()
	{
		ByteBuffer buffer = ByteBuffer.wrap(Buffer);
		
    	buffer.rewind();
    	
        // write start delimiter
    	buffer.put((byte) 0x7e);

		// write frame size
        short length = getFrameDataLength();

        buffer.putShort(length);
		
		// write frame data
        int position = buffer.position();

        WriteFrame(buffer);

        // calculate checksum
        byte checksum = -1; //0xff;
        short cs = 0xff;
        
        for(int i = 0; i < length; i++)
        {
            byte b = buffer.get(position + i);
            
            checksum -= b;
        }
        
		// write checksum
        buffer.put(checksum);
        
        return buffer.position();
	}
	
	public int Send(byte frameID)
	{
		return Send();
	}

    /*
    public void Send(ByteBuffer buffer, byte frameID)
    {
    	buffer.rewind();
    	
        // write start delimiter
    	buffer.put((byte) 0x7e);

		// write frame size
        short length = getFrameDataLength();

        buffer.putShort(length);
		
		// write frame data
        int position = buffer.position();

        WriteFrame(buffer, frameID);

        // calculate checksum
        byte checksum = -1; //0xff;
        
        for(int i = 0; i < length; i++)
            checksum -= buffer.get(position + i);

		// write checksum
        buffer.put(checksum);
    }
     */
    
    // Serialize
    public void ReadFrame(byte[] data)
    {
    }

    public void WriteFrame(ByteBuffer buffer)
    {
    }	
}
