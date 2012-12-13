package com.nerduino.xbee;

import java.nio.ByteBuffer;

public class ManyToOneRouteRequestIndicatorFrame  extends ZigbeeFrame
{
	// Declarations
    public long SourceAddress;
    public short SourceNetworkAddress;
    byte m_reserved;
    
    public static int Count;
    
    // Constructors
    public ManyToOneRouteRequestIndicatorFrame(SerialBase parent)
    {
    	super(FrameTypeEnum.ManyToOneRouteRequestIndicator, parent);
    
    	FrameType = FrameTypeEnum.ManyToOneRouteRequestIndicator;

        Count++;
    }

    // Methods
	public short getFrameDataLength()
	{
		return 12;
	}
	
    // Serialize Methods
    public void ReadFrame(byte[] data)
    {
    	ByteBuffer bb = ByteBuffer.wrap(data);
    	
    	FrameType = FrameTypeEnum.valueOf(bb.get());
        SourceAddress = bb.getLong();
        SourceNetworkAddress = bb.getShort();
		m_reserved = bb.get();
    }

    public void WriteFrame(ByteBuffer buffer)
    {
    	buffer.put(FrameType.Value());
    	buffer.putLong(SourceAddress);
    	buffer.putShort(SourceNetworkAddress);
    	buffer.put(m_reserved);
    }
}
