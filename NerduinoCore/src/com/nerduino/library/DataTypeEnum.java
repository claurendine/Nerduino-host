package com.nerduino.library;


public enum DataTypeEnum 
{
	    DT_Boolean(0),
	    DT_Byte(1),
	    DT_Short(2),
	    DT_Integer(3),
		DT_Float(4),
        DT_String(5),
	    DT_Array(16);
	    
	    private final byte value;
	    
	    DataTypeEnum(int val)
	    {
	    	this.value = (byte)val;
	    }
	    
	    public byte Value()
	    {
	    	return this.value;
	    }

		public static DataTypeEnum valueOf(byte b) 
		{
			switch(b)
			{
				case 0:
					return DT_Boolean;
				case 1:
					return DT_Byte;
				case 2:
					return DT_Short;
				case 3:
					return DT_Integer;
				case 4:
					return DT_Float;
				case 5:
					return DT_String;
				case 16:
					return DT_Array;
			}	
			
			return null;
		}
}
