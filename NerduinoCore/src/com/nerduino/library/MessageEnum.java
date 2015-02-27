/*
 Part of the Nerduino IOT project - http://nerduino.com

 Copyright (c) 2013 Chase Laurendine

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.nerduino.library;


public enum MessageEnum 
{
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
    MSG_SetPointValue(0x26),

	MSG_GetAddress(0x30),
	MSG_GetAddressResponse(0x31),
	
	LMSG_DeclarePoint(0x51),
	LMSG_RegisterPoint(0x52),
	LMSG_SetTransceiverData(0x53),
	LMSG_GetTransceiverData(0x54),
	LMSG_SetPointValue(0x55),
	LMSG_RegisterAddress(0x56);
	
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
            case 0x26:
                return MSG_SetPointValue;
			
			case 0x30:
				return MSG_GetAddress;
			case 0x31:
				return MSG_GetAddressResponse;
				
			case 0x51:
            	return LMSG_DeclarePoint;
			case 0x52:
            	return LMSG_RegisterPoint;
			case 0x53:
            	return LMSG_SetTransceiverData;
			case 0x54:
            	return LMSG_GetTransceiverData;
			case 0x55:
            	return LMSG_SetPointValue;
			case 0x56:
            	return LMSG_RegisterAddress;
		}
		
		return null;
	}
}
