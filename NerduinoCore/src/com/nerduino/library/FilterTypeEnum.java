package com.nerduino.library;


public enum FilterTypeEnum 
{
	FT_NoFilter(0),
	FT_PercentChange(1),
	FT_ValueChange(2);
    
    private final byte value;
    
    FilterTypeEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static FilterTypeEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return FT_NoFilter;
			case 1:
				return FT_PercentChange;
			case 2:
				return FT_ValueChange;
		}	
		
		return null;
	}

}
