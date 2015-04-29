/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.services;

import com.nerduino.library.LocalDataPoint;
import com.nerduino.library.NerduinoBase;
import com.nerduino.library.NerduinoManager;
import com.nerduino.library.PointManager;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 *
 * @author chaselaurendine
 */
public class ServiceManagerScope extends ScriptableObject
{

	@Override
	public String getClassName()
	{
		return "Host";
	}
	
	@Override
    public Object getDefaultValue(Class<?> typeHint) 
	{
        return toString();
    }
	
	
	
	@Override
    public Object get(String name, Scriptable start) 
	{
		Object ret = super.get(name, start);

		if (ret != NOT_FOUND)
		{
			return ret;
		}
		
		// look through all local points for a matching name
		if (PointManager.Current != null)
		{
			LocalDataPoint point = PointManager.Current.getPoint(name);

			if (point != null) 
			{
				return point.getValue();
			}
		}
		
		// look through all nerduinos for a matching name
		
		if (NerduinoManager.Current != null)
		{
			NerduinoBase nerd = NerduinoManager.Current.getNerduino(name);
			
			if (nerd != null)
			{
				return nerd.getScope();
			}
		}
				
		return NOT_FOUND;
    }
	
	@Override
	public void put(String name, Scriptable start, Object value)
	{
		// look through all local points for a matching name
		if (PointManager.Current != null)
		{
			LocalDataPoint point = PointManager.Current.getPoint(name);

			if (point != null) 
			{
				point.setValue(value);
				return;
			}
		}
		
		super.put(name, start, value);
	}
}
