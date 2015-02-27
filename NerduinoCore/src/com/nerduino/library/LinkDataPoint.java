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

import org.openide.util.Exceptions;

public class LinkDataPoint extends PointBase
{
	String m_path;
	PointBase m_dataPoint;
	boolean m_resolved;
	boolean m_readOnly;
	public NerduinoBase Parent;
	
	public LinkDataPoint(NerduinoBase parent, String path, short id)
	{
		Parent = parent;
		m_path = path;
		Id = id;
		
		resolve();
	}
	
	void resolve()
	{
		m_resolved = false;
		
		// if the path includes a '.' delimiter then lookup an associated point
		if (m_path.contains("."))
		{
			int p = m_path.indexOf(".");
			
			String name = m_path.substring(0, p);
			m_name = m_path.substring(p + 1);

			m_nerduino = NerduinoManager.Current.getNerduino(name);
			
			if (m_nerduino != null)
				m_dataPoint = m_nerduino.getPoint(m_name);
		}
		else
			m_dataPoint = PointManager.Current.getPoint(m_path);
		
		if (m_dataPoint != null)
		{
			m_resolved = true;
			
			// register a callback to this point
			if (m_dataPoint instanceof LocalDataPoint)
			{
				LocalDataPoint ldp = (LocalDataPoint) m_dataPoint;
				
				// TODO track apply filter settings
				ldp.onRegisterPointCallback(Parent, Id, FilterTypeEnum.FT_NoFilter.Value(), (byte) 0, null);
			}
			else if (m_nerduino != null)
			{
				byte[] filtervalue = new byte[0];
				
				// TODO track apply filter settings
				m_nerduino.sendRegisterPointCallback(Parent, (byte) 0, Id, m_dataPoint.Id, FilterTypeEnum.FT_NoFilter.Value(), (byte) 0, filtervalue);
				
				try
				{
					// delay for a short while to allow the nerduino to react to the request
					Thread.sleep(50);
				}
				catch(InterruptedException ex)
				{
					Exceptions.printStackTrace(ex);
				}
			}
			
			m_readOnly = m_dataPoint.isReadOnly();
		}
	}
	
	public void onGetPointValueResponse(byte[] data)
    {
    	Status = data[7];
    	DataType = DataTypeEnum.valueOf(data[8]);
		
		if (DataType != null)
		{
			DataLength = DataType.getLength();
			
			m_value = NerduinoHost.parseValue(data, 9, DataType, DataLength);
			
			if (Parent != null && Parent instanceof NerduinoLight)
			{
				((NerduinoLight) Parent).sendLinkSetPointValue(null, Id, DataType, m_value);
			}
		}
		
    	// TODO notify that the value has been updated
    }
	
	@Override
	public void setValue(Object value)
    {
		if (m_setting || m_nerduino == null || m_dataPoint == null)
			return;
		
		m_setting = true;
		
        if (m_value != value)
        {
			try
			{
				initializeValue(value);
				
				m_nerduino.sendSetPointValue(Parent, Id, DataType, value);
				/*
				// notify all callbacks of the new value
				for(ValueCallback callback : m_callbacks)
				{
					callback.valueUpdated();
				}
				*/
			}
			catch(Exception e)
			{
			}
        }
		
		m_setting = false;
    }
}
