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

public class ZigbeeExplicitRxIndicatorFrame  extends ZigbeeFrame
{
    // Declarations 
	public long SourceAddress;
	public short SourceNetworkAddress;
	public byte SourceEndpoint;
	public byte DestinationEndpoint;
	public short ClusterID;
	public short ProfileID;
	public ReceiveOptionEnum Options;
	public byte[] Data;
	
	//public Boolean EscapedMode;
	
    public static int Count;
    
    // Constructors
    public ZigbeeExplicitRxIndicatorFrame()
    {
    	super(FrameTypeEnum.ZigbeeExplicitRxIndicator);

        FrameType = FrameTypeEnum.ZigbeeExplicitRxIndicator;

        Count++;
    }

    // Method
	@Override
	public short getFrameDataLength()
	{
		if (Data == null)
			return 17;
		
		return (short) (Data.length + 17);
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
        
		SourceAddress = bb.getLong();
        SourceNetworkAddress = bb.getShort();
		SourceEndpoint = bb.get();
		DestinationEndpoint = bb.get();
		ClusterID = bb.getShort();
		ProfileID = bb.getShort();
		Options = ReceiveOptionEnum.valueOf(bb.get());
        
        if (length > 17)
        {
        	if (EscapedMode)
        	{
        		byte[] edata = new byte[length - 17];
        		 
	        	for(int i = 0; i < length - 17; i++)
				{
					edata[i] = bb.get();
				}
	        	
	        	setEscapedData(edata);
        	}
        	else
        	{
	          	Data = new byte[length - 17];
	 
	        	for(int i = 0; i < length - 17; i++)
				{
					Data[i] = bb.get();
				}
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
       	buffer.put(FrameType.Value());

    	buffer.putLong(SourceAddress);
    	buffer.putShort(SourceNetworkAddress);
  		buffer.put(SourceEndpoint);
    	buffer.put(DestinationEndpoint);
        
    	buffer.putShort(ClusterID);
    	buffer.putShort(ProfileID);
    	buffer.put(Options.Value());
    	
        if (Data != null)
        {
            if (EscapedMode)
            	buffer.put(getEscapedData());
			else
				buffer.put(Data);    
		}
	}
}

