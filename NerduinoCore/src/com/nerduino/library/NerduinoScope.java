/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.library;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import static org.mozilla.javascript.Scriptable.NOT_FOUND;
import org.mozilla.javascript.ScriptableObject;

/**
 *
 * @author chaselaurendine
 */
public class NerduinoScope extends ScriptableObject
{
	NerduinoBase m_nerd;
	
	NerduinoScope(NerduinoBase nerd)
	{
		m_nerd = nerd;
	}

	@Override
	public String getClassName()
	{
		return m_nerd.getName();
	}
	
	@Override
    public Object getDefaultValue(Class<?> typeHint) 
	{
        return toString();
    }
	
	@Override
    public Object get(String name, Scriptable start) 
	{
		PointBase point = m_nerd.getPoint(name);
		
		// look through all local points for a matching name
		if (point != null) 
		{
			return point.getValue();
		}
		
		// try to execute a remote method?
		
		return NOT_FOUND;
    }
	
	
	@Override
	public void put(String name, Scriptable start, Object value)
	{
		PointBase point = m_nerd.getPoint(name);
		
		// look through all local points for a matching name
		if (point != null) 
		{
			point.setValue(value);
		}
	}
}
