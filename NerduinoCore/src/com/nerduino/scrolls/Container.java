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

import static java.lang.Math.max;
import java.util.ArrayList;
import org.mozilla.javascript.Context;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Container extends EventSpan
{
	ArrayList m_events;
	ArrayList m_activeEvents;
	
	void loadXML(Element node)
	{
		// load container attributes
		try
		{
			m_time = Double.parseDouble(node.getAttribute("time"));
		}
		catch(Exception e)
		{
			m_time = 0.0;
		}
		
		try
		{
			m_span = Double.parseDouble(node.getAttribute("span"));
		}
		catch(Exception e)
		{
			m_span = 1.0;
		}
		
		try
		{
			m_loopCount = Integer.parseInt(node.getAttribute("loopCount"));
		}
		catch(Exception e)
		{
			m_loopCount = 1;
		}
		
		m_events = new ArrayList();
		m_activeEvents = new ArrayList();
		
		
		// attempt to load children
		//NodeList nodes = node.getElementsByTagName("script");
		NodeList nodes = node.getChildNodes();

		for(int i = 0; i < nodes.getLength(); i++)
		{
			Object inode = nodes.item(i);
			
			if (inode instanceof Element)
			{
				Element element = (Element) inode;

				String etype = element.getNodeName();

				if (etype == "script")
				{
					Script script = new Script();

					script.loadXML(element);

					m_events.add(script);
				}
				else if (etype == "keyFrame")
				{
					KeyFrame keyFrame = new KeyFrame();

					keyFrame.loadXML(element);

					m_events.add(keyFrame);			
				}
				else if (etype == "container")
				{
					Container container = new Container();

					container.loadXML(element);

					m_events.add(container);
				}
			}
		}
		
		// sort the events by time
	}

	@Override
	void validate()
	{
		super.validate();

		for (Object m_event : m_events)
		{
			((EventBase) m_event).validate();
		}
		
		double maxtime = 0.0;
		int nodeCount = m_events.size();
		
		for(int i = 0; i < nodeCount; i++)
		{
			Object e = m_events.get(i);
			
			if (e instanceof EventSpan)
			{
				EventSpan es = (EventSpan) e;
				
				maxtime = max(maxtime, es.m_time + es.m_span * es.m_loopCount);
			}
			else
			{
				EventBase eb = (EventBase) e;
				
				maxtime = max(maxtime, eb.m_time);
			}
		}
		
		// sort samples by time
		
		for(int i = 0; i < nodeCount - 1; i++)
		{
			EventBase eb1 = (EventBase) m_events.get(i);

			for(int j = i + 1; j < nodeCount; j++)
			{
				EventBase eb2 = (EventBase) m_events.get(j);
				
				if (eb2.m_time < eb1.m_time)
				{
					// swap samples in the array
					m_events.remove(j);
					m_events.remove(i);
					
					m_events.add(i, eb2);
					m_events.add(j, eb1);
					
					eb1 = eb2;
				}
			}
		}
		
		// validate span value
		if (maxtime > m_span)
		{
			m_span = maxtime;
		}
	}

	@Override
	void reset()
	{
		super.reset();

		for (Object m_event : m_events)
		{
			((EventBase) m_event).reset();
		}
		
		m_activeEvents.clear();
	}

	@Override
	int play(Context context, double lastTime, double currentTime)
	{
		int count = 0;

		if (currentTime >= m_time)
		{
			if (m_startTime < 0.0)
			{
				m_startTime = currentTime;
				m_stopTime = m_startTime + m_totalTime;
			}
		
			for (Object m_event : m_events)
			{
				double last = (lastTime - m_startTime) % m_span;
				double time = (currentTime - m_startTime) % m_span;
				
				((EventBase) m_event).play(context, last, time);
			}

			//if (currentTime < m_stopTime)
			if (currentTime < m_span + m_startTime)
			{
				count++;
			}
		}
		
		return count;
	}
	
}
