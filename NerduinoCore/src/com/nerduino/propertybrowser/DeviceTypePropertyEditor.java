package com.nerduino.propertybrowser;

import com.nerduino.processing.app.BoardManager;

public class DeviceTypePropertyEditor extends EnumerationPropertyEditor
{
	static Object[] s_deviceList;
	
	@Override
	public Object[] getList()
	{
		if (s_deviceList == null)
			s_deviceList = BoardManager.Current.getDeviceList();
		
		return s_deviceList;
	}	
}
