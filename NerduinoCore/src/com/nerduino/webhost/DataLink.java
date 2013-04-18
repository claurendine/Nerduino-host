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

package com.nerduino.webhost;

import com.nerduino.library.LocalDataPoint;
import com.nerduino.library.RemoteDataPoint;

public class DataLink
{
	//FT_NoFilter(0),
	//FT_PercentChange(1),
	//FT_ValueChange(2);
	
	//STATUS_Unresolved(1),
	//STATUS_Resolved(2),
	//STATUS_Error(3)
	
	String path;
	int filterType;
	float filterValue;
	float lastValue;
	float currentValue;
	int status;
	int id; // html callback index
	LocalDataPoint localDataPoint;
	RemoteDataPoint remoteDataPoint;
	
	public DataLink()
	{
		path = "";
		filterType = 0;
		filterValue = 1.0f;
		lastValue = -1e10f;
		currentValue = 0.0f;
		status = 0;
		id = -1;
	}
}
