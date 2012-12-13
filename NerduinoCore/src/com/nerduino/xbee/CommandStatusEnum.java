package com.nerduino.xbee;

public enum CommandStatusEnum 
{
	OK(0x00),
	ERROR(0x01),
	InvalidCommand(0x02),
	InvalidParameter(0x03),
	TxFailure(0x04);
     
    private final byte value;
    
    CommandStatusEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static CommandStatusEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return OK;
			case 1:
				return ERROR;
			case 2:
				return InvalidCommand;
			case 3:
				return InvalidParameter;
			case 4:
				return TxFailure;
		}	
		
		return null;
	}
}
