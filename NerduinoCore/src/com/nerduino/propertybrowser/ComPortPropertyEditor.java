package com.nerduino.propertybrowser;

import gnu.io.CommPortIdentifier;
import java.util.ArrayList;
import java.util.Enumeration;

public class ComPortPropertyEditor extends EnumerationPropertyEditor
{
	@Override
	public Object[] getList()
	{
		ArrayList<String> comlist = new ArrayList<String>();

		try
		{			
			Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();
			
			while (portList.hasMoreElements()) 
			{
				CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
				
				if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) 
					comlist.add(portId.getName());
			}
		}
		catch(Exception e)
		{
			
		}

		return comlist.toArray();
	}
}
