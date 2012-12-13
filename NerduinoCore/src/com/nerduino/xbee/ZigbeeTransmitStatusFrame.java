package com.nerduino.xbee;

import java.nio.ByteBuffer;

public class ZigbeeTransmitStatusFrame  extends ZigbeeFrameWithResponse
{
    // Declarations
	public short DestinationNetworkAddress;
	public byte TransmitRetryCount;
	public DeliveryStatusEnum DeliveryStatus;
	public DiscoveryStatusEnum DiscoveryStatus;

    public static int Count;
    
    // Constructors
    public ZigbeeTransmitStatusFrame(SerialBase parent)
    {
    	super(FrameTypeEnum.ZigbeeTransmitStatus, parent);

        FrameType = FrameTypeEnum.ZigbeeTransmitStatus;

        Count++;
    }

    // Methods
	public short getFrameDataLength()
	{
		return 7;
	}
	
    // Serialize Methods
    public void ReadFrame(byte[] data)
    {
    	ByteBuffer bb = ByteBuffer.wrap(data);
    	
    	FrameType = FrameTypeEnum.valueOf(bb.get());
        FrameID = bb.get();
    
        DestinationNetworkAddress = bb.getShort();
	    TransmitRetryCount = bb.get();
	    DeliveryStatus = DeliveryStatusEnum.valueOf(bb.get());
	    DiscoveryStatus = DiscoveryStatusEnum.valueOf(bb.get());
    }

    public void WriteFrame(ByteBuffer buffer)
    {
     	buffer.put(FrameType.Value());
    	buffer.put(FrameID);
    	buffer.putShort(DestinationNetworkAddress);
    	buffer.put(TransmitRetryCount);
    	buffer.put(DeliveryStatus.Value());
	    buffer.put(DiscoveryStatus.Value());
    }
}
