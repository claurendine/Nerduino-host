package com.nerduino.services;

import org.mozilla.javascript.Context;
import org.openide.util.Exceptions;
import org.w3c.dom.Element;

public class ServiceTrigger
{
	String m_condition;
	String m_command;
	boolean m_async;
	float m_span;
	boolean m_running;
	long m_startTime;
	NerduinoService m_service;
	
	public ServiceTrigger(NerduinoService service)
	{
		m_service = service;
	}

	void loadXML(Element element)
	{
		m_condition = element.getAttribute("condition");
		m_command = element.getAttribute("command");
		
		String async = element.getAttribute("async");
		
		m_span = 0.0f;
		m_async = false;
		
		if (async != null)
			m_span = Float.parseFloat(async);
		
		if (m_span > 0)
			m_async = true;
	}

	void testTrigger(Context context)
	{
		if (!m_running && m_condition != null)
		{
			Object ret = execute(context, m_condition);
			
			if (ret instanceof Boolean && ((Boolean) ret))
			{
				if (m_async)
					executeCommandAsync();
				else
					execute(context, m_command);
			}
		}
	}
	
	private Object execute(Context context, String script)
	{
		try
		{
			return ServiceManager.Current.execute(context, script);
		}
		catch(Exception e)
		{
		}
				
		return true;
	}

	private void executeCommandAsync()
	{
		m_running = true;
		
		// create thread and call execute from this thread
		Thread pthread = new Thread(new Runnable() 
			{
				@Override
				public void run()
				{
					try
					{
						long startTime = System.currentTimeMillis();

						// create a new context for this thread
						Context context = Context.enter();
						
						execute(context, m_command);
						
						
						long measuredTime = System.currentTimeMillis() - startTime;
						
						try
						{
							long sleepTime = (int) (m_span * 1000.0f) - measuredTime;

							if (sleepTime > 0)
								Thread.sleep(sleepTime);
						}
						catch(InterruptedException ex)
						{
							Exceptions.printStackTrace(ex);
						}
						
						m_running = false;
					}
					catch(Exception e)
					{
					}
				}
			});
		
		pthread.start();
		
	}
}
