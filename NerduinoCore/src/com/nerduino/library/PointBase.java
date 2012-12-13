package com.nerduino.library;

import com.nerduino.nodes.TreeNode;
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
		if (DataType == DataTypeEnum.DT_Float)
			return (Float) m_value;
		
		return 0;
	}
	
	public void setFloat(float value)
	{
		if (DataType == DataTypeEnum.DT_Float)
			setValue(value);
	}

	
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
					case DT_String:
						m_value = value.toString();

						DataLength = (byte) ((String) m_value).length();

						break;
					case DT_Array:
						m_value = value;

						if (value instanceof Byte[])
							DataLength = (byte) ((Byte[]) value).length;

						break;
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
		
		switch(dataType)
		{
			case DT_Byte:
				DataLength = 1;
				break;
			case DT_Short:
				DataLength = 2;
				break;
			case DT_Integer:
				DataLength = 4;
				break;
			case DT_Float:
				DataLength = 4;
				break;				
		}
	}
	
	public boolean isReadOnly()
	{
		if ((Attributes & 0x01) == 0x01)
			return true;
		
		return false;
	}
	
    // Methods
}
