package com.nerduino.library;

import java.util.ArrayList;


public class CommandResponse 
{
	public ResponseStatusEnum Status;
	public DataTypeEnum DataType;
	public short DataLength;
	public ArrayList<Byte> Data;
	
	public CommandResponse()
	{
		Status = ResponseStatusEnum.RS_CommandNotRecognized;
		Data = new ArrayList<Byte>();
		DataType = DataTypeEnum.DT_Byte;
		DataLength = 0;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		for(Byte b : Data)
		{
			sb.append((char) b.byteValue());
		}
		
		return sb.toString();
	}
}
