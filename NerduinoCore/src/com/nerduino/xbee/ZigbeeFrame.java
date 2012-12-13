package com.nerduino.xbee;

import java.nio.ByteBuffer;

public class ZigbeeFrame 
{
	// Declarations
    public FrameTypeEnum FrameType;
    public SerialBase Parent;
    public static Boolean EscapedMode;
    
    public byte[] Buffer = new byte[256];
    
    // Constructors
    public ZigbeeFrame(FrameTypeEnum frameType, SerialBase parent)
    {
        FrameType = frameType;

        Parent = parent;
		
		Initialize();
    }
	
	protected void Initialize()
	{
	}

    // Methods
	public Boolean getHasError()
	{
        return false;
	}
	
	public short getFrameDataLength()
	{
        return 0;
	}

	public int Send()
	{
		ByteBuffer buffer = ByteBuffer.wrap(Buffer);
		
    	buffer.rewind();
    	
        // write start delimiter
    	buffer.put((byte) 0x7e);

		// write frame size
        short length = getFrameDataLength();

        buffer.putShort(length);
		
		// write frame data
        int position = buffer.position();

        WriteFrame(buffer);

        // calculate checksum
        byte checksum = -1; //0xff;
        short cs = 0xff;
        
        for(int i = 0; i < length; i++)
        {
            byte b = buffer.get(position + i);
            
            checksum -= b;
        }
        
		// write checksum
        buffer.put(checksum);
        
        return buffer.position();
	}
	
	public int Send(byte frameID)
	{
		return Send();
	}

    /*
    public void Send(ByteBuffer buffer, byte frameID)
    {
    	buffer.rewind();
    	
        // write start delimiter
    	buffer.put((byte) 0x7e);

		// write frame size
        short length = getFrameDataLength();

        buffer.putShort(length);
		
		// write frame data
        int position = buffer.position();

        WriteFrame(buffer, frameID);

        // calculate checksum
        byte checksum = -1; //0xff;
        
        for(int i = 0; i < length; i++)
            checksum -= buffer.get(position + i);

		// write checksum
        buffer.put(checksum);
    }
     */
    
    // Serialize
    public void ReadFrame(byte[] data)
    {
    }

    public void WriteFrame(ByteBuffer buffer)
    {
    }	
}
