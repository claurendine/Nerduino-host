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

package com.nerduino.xbee;

import java.nio.ByteBuffer;

public class ATCommandResponseFrame  extends ZigbeeFrameWithResponse
{
    // Declarations
	public CommandStatusEnum CommandStatus;
    public String Command;
 	public byte[] Data;

     public static int Count;
     
     // Constructors
     public ATCommandResponseFrame()
     {
    	 super(FrameTypeEnum.ATCommandResponse);
     
         FrameType = FrameTypeEnum.ATCommandResponse;

         Count++;
     }

     // Methods
	@Override
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
 	
	@Override
 	public short getFrameDataLength()
 	{
         if (Data != null)
             return (short) (Data.length + 5);

         return 5;
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
	@Override
     public void ReadFrame(byte[] data)
     {
     	 int length = data.length;
     	
     	 ByteBuffer bb = ByteBuffer.wrap(data);
     	
     	 FrameType = FrameTypeEnum.valueOf(bb.get());
         FrameID = bb.get();
         
         StringBuilder sb = new StringBuilder();
         
         byte cb = bb.get();
         sb.append((char) cb);
         
         cb = bb.get();
         sb.append((char) cb);
         
        
         Command = sb.toString();
         
         CommandStatus = CommandStatusEnum.valueOf(bb.get());
         
         if (length > 5)
         {
         	Data = new byte[length - 5];
  
         	for(int i = 0; i < length - 5; i++)
			 {
				 Data[i] = bb.get();
			 }
         }
         else
         {
             Data = null;
         }
     }

	@Override
     public void WriteFrame(ByteBuffer buffer)
     {
     	 buffer.put(FrameType.Value());
     	 buffer.put(FrameID);
      	 buffer.put(Command.getBytes());
      	 buffer.put(CommandStatus.Value());
         
         if (Data != null)
             buffer.put(Data);
     }
}
