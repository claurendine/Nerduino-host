package com.nerduino.xbee;

import java.nio.ByteBuffer;

public class OverTheAirFirmwareUpdateStatusFrame  extends ZigbeeFrame
{
    // Declarations
    public long SourceAddress;
    public short DestinationNetworkAddress;
    public ReceiveOptionEnum ReceiveOptions;
    public BootloaderMessageTypeEnum BootloaderMessageType;
    public byte BlockNumber;
    public long TargetAddress;

    public static int Count;
    
    // Constructors
    public OverTheAirFirmwareUpdateStatusFrame(SerialBase parent)
    {
    	super(FrameTypeEnum.OverTheAirFirmwareUpdateStatus, parent);
    	
        FrameType = FrameTypeEnum.OverTheAirFirmwareUpdateStatus;

        Count++;
    }

    // Methods
	public short getFrameDataLength()
	{
		return 22;
	}

    // Serialize Methods
    public void ReadFrame(byte[] data)
    {
    	ByteBuffer bb = ByteBuffer.wrap(data);
    	
    	FrameType = FrameTypeEnum.valueOf(bb.get());
        SourceAddress = bb.getLong();
        DestinationNetworkAddress = bb.getShort();
		ReceiveOptions = ReceiveOptionEnum.valueOf(bb.get());
		BootloaderMessageType = BootloaderMessageTypeEnum.valueOf(bb.get());
		BlockNumber = bb.get();
		TargetAddress = bb.getLong();
	}

    public void WriteFrame(ByteBuffer buffer)
    {
    	buffer.put(FrameType.Value());
    	buffer.putLong(SourceAddress);
    	buffer.putShort(DestinationNetworkAddress);
    	buffer.put(ReceiveOptions.Value());
    	buffer.put(BootloaderMessageType.Value());
    	buffer.put(BlockNumber);
    	buffer.putLong(TargetAddress);
    }
}
