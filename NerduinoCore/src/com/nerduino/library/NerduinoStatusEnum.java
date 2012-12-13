package com.nerduino.library;




public enum NerduinoStatusEnum 
{
    Uninitialized(16),
    Offline(0),
    Online(1),
    Sleeping(2),
    Distress(3);
    
    private final byte value;
    
    NerduinoStatusEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }
    
	public static NerduinoStatusEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 16:
				return Uninitialized;
			case 0:
				return Offline;
			case 1:
				return Online;
			case 2:
				return Sleeping;
			case 3:
				return Distress;
		}	
		
		return null;
	}

}
