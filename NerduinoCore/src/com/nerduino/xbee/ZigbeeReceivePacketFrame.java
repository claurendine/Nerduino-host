package com.nerduino.xbee;

import java.nio.ByteBuffer;

public class ZigbeeReceivePacketFrame  extends ZigbeeFrame
{
    // Declarations
	public long SourceAddress;
	public short SourceNetworkAddress;
	public ReceiveOptionEnum Options;
	public byte[] Data;
	public Boolean EscapedMode = false;
	
    public static int Count;
    
    // Constructors
    public ZigbeeReceivePacketFrame(SerialBase parent)
    {
        super(FrameTypeEnum.ZigbeeReceivePacket, parent);
    
        FrameType = FrameTypeEnum.ZigbeeReceivePacket;

        Count++;
    }

    // Methods
	public short getFrameDataLength()
	{
		if (Data == null)
			return 12;
		
		return (short) (Data.length + 12);
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
    public void ReadFrame(byte[] data)
    {
    	int length = data.length;
    	
    	ByteBuffer bb = ByteBuffer.wrap(data);
    	
    	FrameType = FrameTypeEnum.valueOf(bb.get());
    
    	SourceAddress = bb.getLong();
		SourceNetworkAddress = bb.getShort();
		Options = ReceiveOptionEnum.valueOf(bb.get());
        
        if (length > 12)
        {
//          	Data = new byte[length - 12];
 
          	if (EscapedMode)
          	{
          		byte[] edata = new byte[length - 12];

          		for(int i = 0; i < length - 12; i++)
          			edata[i] = bb.get();
          		
          		setEscapedData(edata);
          	}
          	else
          	{
          		Data = new byte[length - 12];

          		for(int i = 0; i < length - 12; i++)
          			Data[i] = bb.get();
          	}
        }
        else
        {
            Data = null;
        }
    }

    public void WriteFrame(ByteBuffer buffer)
    {
    	buffer.put(FrameType.Value());

    	buffer.putLong(SourceAddress);
    	buffer.putShort(SourceNetworkAddress);
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