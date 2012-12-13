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
     public ZigbeeIODataSampleRxIndicatorFrame(SerialBase parent)
     {
         super(FrameTypeEnum.ZigbeeIODataSampleRxIndicator, parent);
     
         FrameType = FrameTypeEnum.ZigbeeIODataSampleRxIndicator;

         Count++;
     }
     
     // Methods
	 public short getFrameDataLength()
	 {
		 return 19;
	 }
	 
     // Serialize Methods
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
