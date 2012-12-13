package com.nerduino.xbee;

import java.nio.ByteBuffer;

public class RouteRecordIndicatorFrame  extends ZigbeeFrame
{
    // Declarations
    public long SourceAddress;
    public short SourceNetworkAddress;
    public ReceiveOptionEnum ReceiveOptions;
    public byte NumberOfAddresses;
	
    public short[] Addresses;

    public static int Count;

    // Constructors
    public RouteRecordIndicatorFrame(SerialBase parent)
    {
        super(FrameTypeEnum.RouteRecordIndicator, parent);

        FrameType = FrameTypeEnum.RouteRecordIndicator;

        Count++;
    }

    // Methods
	public short getFrameDataLength()
	{
		if (Addresses == null)
			return 13;
		
		return (short) (Addresses.length * 2 + 13);
	}

    // Serialize Methods
    public void ReadFrame(byte[] data)
    {
    	ByteBuffer bb = ByteBuffer.wrap(data);
    	
    	FrameType = FrameTypeEnum.valueOf(bb.get());

        SourceAddress = bb.getLong();
	    SourceNetworkAddress = bb.getShort();
	    ReceiveOptions = ReceiveOptionEnum.valueOf(bb.get());
	    NumberOfAddresses = bb.get();
		
        if (NumberOfAddresses > 0)
        {
        	Addresses = new short[NumberOfAddresses];
        	
        	for(int i = 0; i < NumberOfAddresses; i++)
        		Addresses[i] = bb.getShort();
        }
        else
        {
            Addresses = null;
        }
    }

    public void WriteFrame(ByteBuffer buffer)
    {
    	buffer.put(FrameType.Value());
    	buffer.putLong(SourceAddress);
    	buffer.putShort(SourceNetworkAddress);
    	buffer.put(ReceiveOptions.Value());
	    
        if (Addresses == null)
          	NumberOfAddresses = 0;
		else
		    NumberOfAddresses = (byte) Addresses.length;                  
	
        buffer.put(NumberOfAddresses);
		
        for(int i = 0; i < NumberOfAddresses; i++)
        	buffer.putShort(Addresses[i]);
    }
}
