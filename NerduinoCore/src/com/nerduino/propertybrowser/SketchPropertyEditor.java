package com.nerduino.propertybrowser;

import com.nerduino.processing.app.SketchManager;

public class SketchPropertyEditor extends EnumerationPropertyEditor
{
	@Override
	public Object[] getList()
	{
		return SketchManager.Current.getSketchList();
	}	
}
