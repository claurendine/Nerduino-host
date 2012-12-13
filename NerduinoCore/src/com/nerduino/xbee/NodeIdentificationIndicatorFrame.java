package com.nerduino.xbee;

import java.nio.ByteBuffer;

public class NodeIdentificationIndicatorFrame  extends ZigbeeFrame
{
    // Declarations
    public long SourceAddress;
    public short SourceNetworkAddress;
    public ReceiveOptionEnum Options;
    public short RemoteNetworkAddress;
    public long RemoteAddress;
    public String NodeIdentifier;
    public short RemoteParentNetworkAddress;
    public DeviceTypeEnum DeviceType;
    public SourceEventEnum SourceEvent;
    public short DigiApplicaionProfileID;
    public short DigiManufacturerID;

    public static int Count;
    
    // Constructors
    public NodeIdentificationIndicatorFrame(SerialBase parent)
    {
        super(FrameTypeEnum.NodeIdentificationIndicator, parent);

        FrameType = FrameTypeEnum.NodeIdentificationIndicator;

        Count++;
    }

    // Methods
	public short getFrameDataLength()
	{
		return (short) (31 + NodeIdentifier.length());
	}
	
    // Serialize Methods
    public void ReadFrame(byte[] data)
    {
    	ByteBuffer bb = ByteBuffer.wrap(data);
    	
    	FrameType = FrameTypeEnum.valueOf(bb.get());
        SourceAddress = bb.getLong();
        SourceNetworkAddress = bb.getShort();
		Options = ReceiveOptionEnum.valueOf(bb.get());
		RemoteNetworkAddress = bb.getShort();
        RemoteAddress = bb.getLong();
        
        StringBuilder sb = new StringBuilder();
        
        char c;
        
        do
        {
        	c = bb.getChar();
        	
        	if (c != 0)
        		sb.append(c);
        } while(c != 0);
        
        NodeIdentifier = sb.toString();
        
		RemoteParentNetworkAddress = bb.getShort();
		DeviceType = DeviceTypeEnum.valueOf(bb.get());
		SourceEvent = SourceEventEnum.valueOf(bb.get());
		DigiApplicaionProfileID = bb.getShort();
		DigiManufacturerID = bb.getShort();
    }

    public void WriteFrame(ByteBuffer buffer)
    {
    	buffer.put(FrameType.Value());
    	buffer.putLong(SourceAddress);
    	buffer.putShort(SourceNetworkAddress);
		buffer.put(Options.Value());
		buffer.putShort(RemoteNetworkAddress);
		buffer.putLong(RemoteAddress);
		
		buffer.put(NodeIdentifier.getBytes());
		buffer.put((byte) 0);
		
		buffer.putShort(RemoteParentNetworkAddress);
		buffer.put(DeviceType.Value());
		buffer.put(SourceEvent.Value());
		buffer.putShort(DigiApplicaionProfileID);
		buffer.putShort(DigiManufacturerID);
    }
}
