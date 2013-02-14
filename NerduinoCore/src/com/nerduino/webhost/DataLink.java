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
