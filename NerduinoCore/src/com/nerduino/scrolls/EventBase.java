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

import org.mozilla.javascript.Context;

public class EventBase
{
	protected double m_time;
	
	void validate()
	{
		if (m_time < 0.0)
		{
			m_time = 0.0;
		}
	}
	
	void reset()
	{
	}
	
	int play(Context context, double lastTime, double currentTime)
	{
		return 0;
	}
}
