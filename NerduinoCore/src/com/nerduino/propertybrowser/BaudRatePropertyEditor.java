package com.nerduino.propertybrowser;

public class BaudRatePropertyEditor extends EnumerationPropertyEditor 
{
	@Override
	public Object[] getList()
	{
		Object[] objs = new Object[] { 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200, 230400 };
		
		return objs;
	}
		
	@Override
	public void setAsText(String value)
	{
		int rate = Integer.decode(value);
		
		setValue(rate);
	}	
}
