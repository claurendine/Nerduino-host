package com.nerduino.xbee;

import java.nio.ByteBuffer;

public class ModemStatusFrame  extends ZigbeeFrame
{
    // Declarations
    public ModemStatusEnum Status;

    public static int Count;

    // Constructors
    public ModemStatusFrame(SerialBase parent)
    {
        super(FrameTypeEnum.ModemStatus, parent);

        FrameType = FrameTypeEnum.ModemStatus;

        Count++;
    }

    // Methods
	public short getFrameDataLength()
	{
		return 2;
	}
	
    // Serialize Methods
    public void ReadFrame(byte[] data)
    {
    	ByteBuffer bb = ByteBuffer.wrap(data);
    	
    	FrameType = FrameTypeEnum.valueOf(bb.get());
        Status = ModemStatusEnum.valueOf(bb.get());
    }

    public void WriteFrame(ByteBuffer buffer)
    {
    	buffer.put(FrameType.Value());
    	buffer.put(Status.Value());
    }
}
