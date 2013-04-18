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

import com.nerduino.nodes.TreeNode;
import com.nerduino.xbee.BitConverter;
import java.util.*;
import org.openide.nodes.Children;

public class PointBase extends TreeNode
{
	// Declarations
    public short Id = 0;
    public DataTypeEnum DataType = DataTypeEnum.DT_Byte;
    public byte DataLength = 1;
    public byte Attributes = 0;
    protected Object m_value = (byte) 0;
	NerduinoLight m_nerduino = null;
	
    public byte Status = 0;
    protected List<ValueCallback> m_callbacks = new ArrayList<ValueCallback>();
    
	
    // Constructors
    public PointBase()
    {
        super(new Children.Array(), "Point", "/com/nerduino/resources/PointGreen16.png");
    }
	
    // Properties
		public boolean getBoolean()
	{
		if (DataType == DataTypeEnum.DT_Boolean)
			return (Boolean) m_value;
		
		return false;
	}
	
	public void setBoolean(boolean value)
	{
		if (DataType == DataTypeEnum.DT_Boolean)
			setValue(value);
	}
	
	
	public byte getByte()
	{
		if (DataType == DataTypeEnum.DT_Byte)
			return (Byte) m_value;
		
		return 0;
	}
	
	public void setByte(byte value)
	{
		if (DataType == DataTypeEnum.DT_Byte)
			setValue(value);
	}
	
	
	public short getShort()
	{
		if (DataType == DataTypeEnum.DT_Short)
			return (Short) m_value;
		
		return 0;
	}
	
	public void setShort(short value)
	{
		if (DataType == DataTypeEnum.DT_Short)
			setValue(value);
	}

	
	public int getInt()
	{
		if (DataType == DataTypeEnum.DT_Integer)
			return (Integer) m_value;
		
		return 0;
	}
	
	public void setInt(int value)
	{
		if (DataType == DataTypeEnum.DT_Integer)
			setValue(value);
	}

	
	public float getFloat()
	{
		try
		{
			switch(DataType)
			{
				case DT_Boolean:
					if ((Boolean) m_value == true)
						return 1.0f;
					else
						return 0.0f;
				case DT_Byte:
					return ((Byte) m_value).floatValue();
				case DT_Short:
					return ((Short) m_value).floatValue();
				case DT_Integer:
					return ((Integer) m_value).floatValue();
				case DT_Float:
					return (Float) m_value;
				case DT_String:
					return Float.parseFloat((String) m_value);
			}
		}
		catch(Exception e)
		{
		}
		
		return 0;
	}
	
	public void setFloat(float value)
	{
		if (DataType == DataTypeEnum.DT_Float)
			setValue(value);
	}

/*	
	public String getString()
	{
		if (DataType == DataTypeEnum.DT_String)
			return (String) m_value;
		
		return "";
	}
	
	public void setString(String value)
	{
		if (DataType == DataTypeEnum.DT_String)
			setValue(value);
	}
*/
	
	public byte[] getBytes()
	{
		byte[] data = null;
		
		switch(DataType)
		{
			case DT_Boolean:
				data = BitConverter.GetBytes((Boolean) m_value);
				
				break;
			case DT_Byte:
				data = BitConverter.GetBytes((Byte) m_value);
				
				break;
			case DT_Short:
				data = BitConverter.GetBytes((Short) m_value);
				
				break;
			case DT_Integer:
				data = BitConverter.GetBytes((Integer) m_value);
				
				break;
			case DT_Float:
				data = BitConverter.GetBytes((Float) m_value);
				
				break;
			/*
			case DT_String:
				m_value = value.toString();

				DataLength = (byte) ((String) m_value).length();

				break;
			case DT_Array:
				m_value = value;

				if (value instanceof Byte[])
					DataLength = (byte) ((Byte[]) value).length;

				break;
			*/
		}
		
		return data;
	}
	
    public Object getValue()
    {
        return m_value; 
    }
    
    public void setValue(Object value)
    {
        if (m_value != value)
        {
			try
			{
				switch(DataType)
				{
					case DT_Boolean:
						if (value instanceof Boolean)
							m_value = value;
						else if (value instanceof String)
							m_value = Boolean.parseBoolean((String)value);

						break;
					case DT_Byte:
						if (value instanceof Byte)
							m_value = value;
						else if (value instanceof String)
							m_value = Byte.parseByte((String)value);

						break;
					case DT_Short:
						if (value instanceof Short)
							m_value = value;
						else if (value instanceof String)
							m_value = Short.parseShort((String)value);

						break;
					case DT_Integer:
						if (value instanceof Integer)
							m_value = value;
						else if (value instanceof String)
							m_value = Integer.parseInt((String)value);

						break;
					case DT_Float:
						if (value instanceof Float)
							m_value = value;
						else if (value instanceof String)
							m_value = Float.parseFloat((String)value);

						break;
					/*
					case DT_String:
						m_value = value.toString();

						DataLength = (byte) ((String) m_value).length();

						break;
					case DT_Array:
						m_value = value;

						if (value instanceof Byte[])
							DataLength = (byte) ((Byte[]) value).length;

						break;
					*/
				}
				
				if (m_nerduino != null)
				{
					m_nerduino.sendSetPointValue(Id, DataType, DataLength, value);
				}
				
				// notify all callbacks of the new value
				for(ValueCallback callback : m_callbacks)
				{
					callback.valueUpdated();
				}
			}
			catch(Exception e)
			{

			}
        }
    }

	public void setDataType(DataTypeEnum dataType)
	{
		DataType = dataType;
		DataLength = dataType.getLength();
	}
	
	public boolean isReadOnly()
	{
		if ((Attributes & 0x01) == 0x01)
			return true;
		
		return false;
	}
	
    // Methods
}
