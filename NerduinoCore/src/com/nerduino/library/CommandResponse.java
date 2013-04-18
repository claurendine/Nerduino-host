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

import com.nerduino.xbee.BitConverter;
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
	
	public Object getResponseValue()
	{
		Object value = null;

		if (DataLength == 0)
		{
			return null;
		}
		
		switch(DataType)
		{
			case DT_Boolean:
				value = (Data.get(0) != 0);
				break;
			case DT_Byte:
				value = Data.get(0);
				break;
			case DT_Float:
			{
				byte[] data = new byte[4];
				data[0] = Data.get(0);
				data[1] = Data.get(1);
				data[2] = Data.get(2);
				data[3] = Data.get(3);
				
				value = BitConverter.GetFloat(data, 0);
			}	
				break;
			case DT_Short:
			{
				byte[] data = new byte[4];
				data[0] = Data.get(0);
				data[1] = Data.get(1);
				
				value = BitConverter.GetShort(data, 0);
			}
				break;
			case DT_Integer:
			{
				byte[] data = new byte[4];
				data[0] = Data.get(0);
				data[1] = Data.get(1);
				data[2] = Data.get(2);
				data[3] = Data.get(3);
				
				value = BitConverter.GetInt(data, 0);
			}
				break;
			case DT_String:
			{
				StringBuilder sb = new StringBuilder();

				for (int i = 0; i < DataLength; i++)
				{
					sb.append((char) (byte) Data.get(i));
				}

				value = sb.toString();
			}

			break;
		}

		return value;
	}
}
