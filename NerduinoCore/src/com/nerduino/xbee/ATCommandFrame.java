package com.nerduino.xbee;

import java.nio.ByteBuffer;

public class ATCommandFrame extends ZigbeeFrameWithResponse
{
	// Declarations
    public String Command;
    public byte[] Data;
    
    public static int Count;
    
    // Constructors
    public ATCommandFrame(SerialBase parent)
    {   
    	super(FrameTypeEnum.ATCommand, parent);
    }

    public ATCommandFrame(String command, SerialBase parent)
    {
    	super(FrameTypeEnum.ATCommand, parent);
    	
    	Command = command;
    }

    public ATCommandFrame(String command, Boolean data, SerialBase parent)
    {
    	super(FrameTypeEnum.ATCommand, parent);

    	Command = command;
		
    	Data = BitConverter.GetBytes(data);
    }

    public ATCommandFrame(String command, byte data, SerialBase parent)
    {
    	super(FrameTypeEnum.ATCommand, parent);

    	Command = command;

    	Data = BitConverter.GetBytes(data);
    }

    public ATCommandFrame(String command, short data, SerialBase parent)
    {
    	super(FrameTypeEnum.ATCommand, parent);

    	Command = command;
    	
    	Data = BitConverter.GetBytes(data);
    }

    public ATCommandFrame(String command, int data, SerialBase parent)
    {
    	super(FrameTypeEnum.ATCommand, parent);

    	Command = command;

    	Data = BitConverter.GetBytes(data);
    }


    public ATCommandFrame(String command, float data, SerialBase parent)
    {
    	super(FrameTypeEnum.ATCommand, parent);

    	Command = command;

    	Data = BitConverter.GetBytes(data);
    }

    public ATCommandFrame(String command, double data, SerialBase parent)
    {
    	super(FrameTypeEnum.ATCommand, parent);

    	Command = command;

    	Data = BitConverter.GetBytes(data);
    }

    public ATCommandFrame(String command, String data, SerialBase parent)
    {
    	super(FrameTypeEnum.ATCommand, parent);

    	Command = command;

    	Data = data.getBytes();
    }


	public void Initialize()
	{
		Count++;

        AutoGenerateFrameID = true;
	}

    // Methods
    public Boolean getHasError()
	{
		if (Command.length() != 2)
			return true;
		
		char c0 = Command.charAt(0);
		char c1 = Command.charAt(1);
		
		if (c0 < 'A' || c0 > 'Z' ||
				c1 < 'A' || c1 > 'Z')
			return true;
			
		return false;
	}
	
	public short getFrameDataLength()
	{
        if (Data != null)
            return (short) (Data.length + 4);

        return 4;
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
        
        StringBuilder sb = new StringBuilder();
        sb.append(bb.getChar());
        sb.append(bb.getChar());
        
        Command = sb.toString();
        
        if (length > 4)
        {
        	Data = new byte[length - 4];
 
        	for(int i = 0; i < length - 4; i++)
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
    	buffer.put(Command.getBytes());
        
        if (Data != null)
            buffer.put(Data);
    }
}
