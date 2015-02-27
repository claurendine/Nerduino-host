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

public class ValueCallback 
{
	public NerduinoBase Nerduino;
	LocalDataPoint DataPoint;
	public short ResponseToken;
	public FilterTypeEnum FilterType;
	public DataTypeEnum FilterDataType;
	public byte FilterLength;
	public Object FilterValue;
	public Object LastBroadcastValue;
	
	public void valueUpdated()
	{
		Object value = DataPoint.getValue();
		
		// determine if the value has changed enough to trigger notification
		boolean changed = !LastBroadcastValue.equals(value);

		if (changed)
		{
			switch(FilterType)
			{
				case FT_PercentChange:
				{
					float percent = 0.0f;
					float lastValue = 0.0f;
					float currentValue = 0.0f;
					
					switch(DataPoint.DataType)
					{
						case DT_Byte:
						{
							lastValue = (float) ((Byte) LastBroadcastValue);
							currentValue = (float) ((Byte) value);
							
							break;
						}
						case DT_Short:
						{
							lastValue = (float) ((Short) LastBroadcastValue);
							currentValue = (float) ((Short) value);
							
							break;
						}
						case DT_Integer:
						{
							lastValue = (float) ((Integer) LastBroadcastValue);
							currentValue = (float) ((Integer) value);
							
							break;
						}
						case DT_Float:
						{
							lastValue = (Float) LastBroadcastValue;
							currentValue = (Float) value;
							
							break;
						}
						/*
						case DT_Array:
						case DT_String:
						{
							changed = !LastBroadcastValue.equals(value);
							
							if (changed)
								lastValue = 0.0f;
							else
								lastValue = currentValue = 1.0f;
							
							break;
						}
						*/
					}
					
					if (lastValue != 0.0f)
						percent = (lastValue - currentValue) / lastValue;
					else 
						percent = 1.0f;

					changed = (Math.abs(percent) > (Float) FilterValue);
					
					break;
				}
				case FT_ValueChange:
				{
					switch(DataPoint.DataType)
					{
						case DT_Byte:
						{
							long lastValue = (long) ((Byte) LastBroadcastValue);
							long currentValue = (long) ((Byte) value);
							long deltaValue = (long) ((Byte) FilterValue);
							
							changed = Math.abs(lastValue - currentValue) >= deltaValue;
							
							break;
						}
						case DT_Short:
						{
							long lastValue = (long) ((Short) LastBroadcastValue);
							long currentValue = (long) ((Short) value);
							long deltaValue = (long) ((Short) FilterValue);
							
							changed = Math.abs(lastValue - currentValue) >= deltaValue;

							break;
						}
						case DT_Integer:
						{
							long lastValue = (long) ((Integer) LastBroadcastValue);
							long currentValue = (long) ((Integer) value);
							long deltaValue = (long) ((Integer) FilterValue);
							
							changed = Math.abs(lastValue - currentValue) >= deltaValue;
							
							break;
						}
						case DT_Float:
						{
							float lastValue = (Float) LastBroadcastValue;
							float currentValue = (Float) value;
							float deltaValue = (Float) FilterValue;

							changed = Math.abs(lastValue - currentValue) >= deltaValue;
							
							break;
						}
					}

					break;
				}
			}
		}
		
		if (changed)
			sendUpdate(value);
	}
	
	public void sendUpdate(Object value)
	{
		LastBroadcastValue = value;
		
		DataPoint.sendGetPointValueResponse(Nerduino, ResponseToken);	
	}
}
