package com.nerduino.services;

import org.mozilla.javascript.Context;
import org.w3c.dom.Element;

public class ServiceTrigger
{
	String m_trigger;
	String m_command;
	boolean m_async;
	int m_span;
	boolean m_running;
	long m_startTime;
	
	public ServiceTrigger()
	{
	}

	void loadXML(Element element)
	{
		m_trigger = element.getTextContent();
		m_command = element.getAttribute("command");
		
		String async = element.getAttribute("async");
		
		m_span = 0;
		m_async = false;
		
		if (async != null)
			m_span = Integer.parseInt(async);
		
		if (m_span > 0)
			m_async = true;
	}

	void testTrigger(Context context)
	{
		if (!m_running)
		{
			// test trigger 
			// if triggered, then execute the command
		}
	
	}
	
	void execute(Context context)
	{
		// if asynchronous then create a new thread and execute the command
		// after executing make sure the thread stays asleep till the specified span has ellapsed
		
		
		// not asynchronous, then execute in the current thread using the provided context
	}
}
