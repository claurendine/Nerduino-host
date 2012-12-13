package com.nerduino.library;


public enum MessageEnum {
    MSG_Initialize(0x00),
    
    MSG_GetMetaData(0x01),
    MSG_GetNamedMetaData(0x02),
    MSG_GetMetaDataResponse(0x03),
    MSG_SetMetaData(0x04),
    MSG_ResetRequest(0x05),
    
    MSG_Ping(0x06),
    MSG_PingResponse(0x07),
    MSG_Checkin(0x08),

    MSG_ExecuteCommand(0x10),
    MSG_ExecuteCommandResponse(0x11),
    
    MSG_GetPoint(0x20),
    MSG_GetPointResponse(0x21),
    MSG_GetPointValue(0x22),
    MSG_GetPointValueResponse(0x23),
    MSG_RegisterPointCallback(0x24),
    MSG_UnregisterPointCallback(0x25),
    MSG_SetPointValue(0x26),

	MSG_GetAddress(0x30),
	MSG_GetAddressResponse(0x31),
	
    MSG_GetDeviceStatus(0x40),
    MSG_GetDeviceStatusResponse(0x41);
    
    
    
    private final byte value;
    
    MessageEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static MessageEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
                return MSG_Initialize;
            case 0x01:
                return MSG_GetMetaData;
            case 0x02:
                return MSG_GetNamedMetaData;
            case 0x03:
                return MSG_GetMetaDataResponse;
            case 0x04:
                return MSG_SetMetaData;
            case 0x05:
                return MSG_ResetRequest;
            case 0x06:
                return MSG_Ping;
            case 0x07:
                return MSG_PingResponse;
            case 0x08:
                return MSG_Checkin;
            
			case 0x10:
                return MSG_ExecuteCommand;
            case 0x11:
                return MSG_ExecuteCommandResponse;
            
			case 0x20:
                return MSG_GetPoint;
            case 0x21:
                return MSG_GetPointResponse;
            case 0x22:
                return MSG_GetPointValue;
            case 0x23:
                return MSG_GetPointValueResponse;
            case 0x24:
                return MSG_RegisterPointCallback;
            case 0x25:
                return MSG_UnregisterPointCallback;
            case 0x26:
                return MSG_SetPointValue;
			
			case 0x30:
				return MSG_GetAddress;
			case 0x31:
				return MSG_GetAddressResponse;

            case 0x40:
                return MSG_GetDeviceStatus;
            case 0x41:
            	return MSG_GetDeviceStatusResponse;
		}
		
		return null;
	}
}