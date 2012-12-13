package com.nerduino.xbee;

import java.nio.ByteBuffer;

public class CreateSourceRouteFrame  extends ZigbeeFrameWithResponse
{
	// Declarations
    public long DestinationAddress;
    public short DestinationNetworkAddress;
    public byte RouteCommandOptions;
    public byte NumberOfAddresses;
    public short[] Addresses;

    public Boolean NetworkAddressUnknown;
    public Boolean Broadcast;
    public Boolean ToCoordinator;

    public static int Count;

    // Constructors
    public CreateSourceRouteFrame(SerialBase parent)
    {
    	super(FrameTypeEnum.CreateSourceRoute, parent);
    
        FrameType = FrameTypeEnum.CreateSourceRoute;

        Count++;
    }

    // Methods
	public short getFrameDataLength()
	{
		if (Addresses == null)
			return 14;
		
		return (short) (Addresses.length * 2 + 14);
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
	
    // Serialize Methods
    public void ReadFrame(byte[] data)
    {
    	int length = data.length;
    	
    	ByteBuffer bb = ByteBuffer.wrap(data);
    	
    	FrameType = FrameTypeEnum.valueOf(bb.get());
        FrameID = bb.get();
        
        DestinationAddress = bb.getLong();
        DestinationNetworkAddress = bb.getShort();
		RouteCommandOptions = bb.get();
       	NumberOfAddresses = bb.get();
		
		if (length > 14)
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
    	buffer.put(RouteCommandOptions);
    	buffer.putShort(NumberOfAddresses);
		
		if (Addresses != null)
            for(int i = 0; i < Addresses.length; i++)
            {
				buffer.putShort(Addresses[i]);
            }
    }
}
