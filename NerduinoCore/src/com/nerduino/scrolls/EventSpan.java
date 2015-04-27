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

package com.nerduino.scrolls;

public class EventSpan extends EventBase
{
	protected double m_span;
	protected int m_loopCount;
	protected double m_startTime;
	protected double m_stopTime;
	protected double m_totalTime;
	
	@Override
	void validate()
	{
		if (m_span < 0.0)
		{
			m_span = 0.0;
		}
		
		if (m_loopCount < 1)
		{
			m_loopCount = 1;
		}
		
		m_totalTime = m_span * m_loopCount;
	}
	
	@Override
	void reset()
	{
		m_startTime = -1.0;
		m_stopTime = m_totalTime;
	}
}
