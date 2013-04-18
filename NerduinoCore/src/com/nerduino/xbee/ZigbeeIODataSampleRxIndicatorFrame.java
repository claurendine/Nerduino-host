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

public class ZigbeeIODataSampleRxIndicatorFrame  extends ZigbeeFrame
{
     // Declarations
	 public long SourceAddress;
	 public short SourceNetworkAddress;
	 public ReceiveOptionEnum Options;
	 public byte NumberOfSamples;
	 public short DigitalChannelMask;
	 public byte AnalogChannelMask;
	 public short DigitalSamples;
	 public short AnalogSample;

     public static int Count;
    
     // Constructors
     public ZigbeeIODataSampleRxIndicatorFrame()
     {
         super(FrameTypeEnum.ZigbeeIODataSampleRxIndicator);
     
         FrameType = FrameTypeEnum.ZigbeeIODataSampleRxIndicator;

         Count++;
     }
     
     // Methods
	@Override
	 public short getFrameDataLength()
	 {
		 return 19;
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
		NumberOfSamples = bb.get();
		DigitalChannelMask = bb.getShort();
		AnalogChannelMask = bb.get();
		DigitalSamples = bb.getShort();
		AnalogSample = bb.getShort();
     }
     
	@Override
     public void WriteFrame(ByteBuffer buffer)
     {
    	 buffer.put(FrameType.Value());
    	 buffer.putLong(SourceAddress);
    	 buffer.putShort(SourceNetworkAddress);
    	 buffer.put(Options.Value());
    	 buffer.put(NumberOfSamples);
    	 buffer.putShort(DigitalChannelMask);
    	 buffer.put(AnalogChannelMask);
    	 buffer.putShort(DigitalSamples);
    	 buffer.putShort(AnalogSample);
     }
}
