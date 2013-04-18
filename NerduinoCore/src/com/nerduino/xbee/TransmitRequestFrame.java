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


public class TransmitRequestFrame extends ZigbeeFrameWithResponse
{
	// Declarations
	public long DestinationAddress;
	public short DestinationNetworkAddress;
	public TransmitRequestOptionEnum Options;
	public byte[] Data;
    
	public Boolean DisableACK = true;
	public Boolean EnableAPSEncryption = false;
	public Boolean UseExtendedTimout = false;
	public Boolean ToCoordinator = false;
	public Boolean Broadcast = false;
	public Boolean NetworkAddressUnknown = false;
	public Boolean EscapedMode = false;
	
    public static int Count;
    
    // Constructors
    public TransmitRequestFrame()
    {
    	super(FrameTypeEnum.TransmitRequest);
    	
		FrameType = FrameTypeEnum.TransmitRequest;
		
        Count++;
	}

    // Methods
	@Override
	public short getFrameDataLength()
	{
		if (Data == null)
			return 14;
		
		return (short) (Data.length + 14);
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
	
	public byte[] getEscapedData()
    {
        if (Data == null)
            return null;

        // insert escape codes where necessary
        int count = 0;

        for (byte b : Data)
        {
            if (b == 0x7e || b == 0x7d || b == 0x11 || b == 0x13)
                count++;
        }

        if (count == 0)
        {
            return Data;
        }
        else
        {
            byte[] edata = new byte[Data.length + count];

            int i = 0;

            for (byte b : Data)
            {
                if (b == 0x7e || b == 0x7d || b == 0x11 || b == 0x13)
                {
                    edata[i++] = 0x7d;
                }

                edata[i++] = b;
            }

            return edata;
        }
    }

    public void setEscapedData(byte[] value)
    {
        if (value == null)
        {
            Data = value;
        }
        else
        {
            // insert escape codes where necessary
            int count = 0;

            for (int i = 0; i < value.length; i++)
            {
                byte b = value[i];

                if (b == 0x7d)
                {
                    count++;
                    i++; // skip the next character
                }
            }

            if (count == 0)
            {
                Data = value;
            }
            else
            {
                Data = new byte[value.length - count];

                int j = 0;

                for (int i = 0; i < value.length; i++)
                {
                    byte b = value[i];

                    if (b == 0x7d)
                    {
                        i++;
                        b = value[i];
                    }

                    Data[j++] = b;
                }
            }
        }
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
        Broadcast = (bb.get() == 1);
        Options = TransmitRequestOptionEnum.valueOf(bb.get());
        
        if (length > 14)
        {
          	Data = new byte[length - 14];
 
        	for(int i = 0; i < length - 14; i++)
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
			options |= TransmitRequestOptionEnum.DisableACK.Value();
		
		if (EnableAPSEncryption)
			options |= TransmitRequestOptionEnum.EnableAPSEncryption.Value();
		
		if (UseExtendedTimout)
			options |= TransmitRequestOptionEnum.UseExtendedTimout.Value();
		
		Options = TransmitRequestOptionEnum.valueOf(options);
		
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
    	
    	if (Broadcast)
    		buffer.put((byte) 1);
    	else
    		buffer.put((byte) 0);
    	
    	buffer.put(options);
        
        if (Data != null)
        {
            if (EscapedMode)
            	buffer.put(getEscapedData());
			else
				buffer.put(Data);
            
		}
    }	
}
