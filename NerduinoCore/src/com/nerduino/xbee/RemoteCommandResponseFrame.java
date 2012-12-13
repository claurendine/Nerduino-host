package com.nerduino.xbee;

import java.nio.ByteBuffer;

public class RemoteCommandResponseFrame  extends ZigbeeFrameWithResponse
{
	// Declarations
	public long SourceAddress;
	public short SourceNetworkAddress;
	public CommandStatusEnum CommandStatus;
	public byte[] Data;

	public String Command;

    public static int Count;
    
    // Constructors
    public RemoteCommandResponseFrame(SerialBase parent)
    {
        super(FrameTypeEnum.RemoteCommandResponse, parent);

        FrameType = FrameTypeEnum.RemoteCommandResponse;

        Count++;
    }

    // Methods
    public short getFrameDataLength()
	{
		if (Data != null)
		{
			return (short) (15 + Data.length);
		}
		
		return 15; 
	}
		
    public String getCommand()
	{
		return Command; 
	}
	
	public void setCommand(String value) 
	{
		// make sure that the commands are two characters long and all caps
		if (value.length() < 2)
			value += "  ";
		
		Command = value.substring(0, 2).toUpperCase();
	}	
    
    // Serialize Methods
    public void ReadFrame(byte[] data)
    {
    	int length = data.length;
    	
    	ByteBuffer bb = ByteBuffer.wrap(data);
    	
    	FrameType = FrameTypeEnum.valueOf(bb.get());
        FrameID = bb.get();

		SourceAddress = bb.getLong();
		SourceNetworkAddress = bb.getShort();
        
        StringBuilder sb = new StringBuilder();
        sb.append(bb.getChar());
        sb.append(bb.getChar());
        
        Command = sb.toString();

		CommandStatus = CommandStatusEnum.valueOf(bb.get());
        
        if (length > 15)
        {
        	Data = new byte[length - 15];
 
        	for(int i = 0; i < length - 15; i++)
        		Data[i] = bb.get();
        }
        else
        {
            Data = null;
        }
    }

    public void WriteFrame(ByteBuffer buffer)
    {
    	buffer.put(FrameType.Value());
    	buffer.put(FrameID);
    	buffer.putLong(SourceAddress);
    	buffer.putShort(SourceNetworkAddress);	
        buffer.put(Command.getBytes());
        buffer.put(CommandStatus.Value());
		
        if (Data != null)
            buffer.put(Data);
    }
}
