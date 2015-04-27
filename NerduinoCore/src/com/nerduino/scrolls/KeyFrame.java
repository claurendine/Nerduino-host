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

import com.nerduino.services.ServiceManager;
import static java.lang.Math.max;
import java.util.ArrayList;
import org.mozilla.javascript.Context;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class KeyFrame extends EventSpan
{
	ArrayList m_samples;
	ArrayList m_names;
	int m_variableCount;
	int m_sampleCount;
	String m_prototype;
	String m_interpolate;

	@Override
	void validate()
	{
		double maxsampletime = 0.0;
		
		for(int i = 0; i < m_sampleCount; i++)
		{
			Sample s = (Sample) m_samples.get(i);
			
			// don't allow negative sample times
			if (s.m_time < 0.0)
				s.m_time = 0.0;
			
			maxsampletime = max(maxsampletime, s.m_time);
		}
		
		// sort samples by time
		
		for(int i = 0; i < m_sampleCount - 1; i++)
		{
			Sample s1 = (Sample) m_samples.get(i);

			for(int j = i + 1; j < m_sampleCount; j++)
			{
				Sample s2 = (Sample) m_samples.get(j);
				
				if (s1.m_time == s2.m_time)
				{
					// remove samples with duplicate time
					m_samples.remove(j);
					m_sampleCount--;
					j = j - 1;
				}
				else  if (s2.m_time < s1.m_time)
				{
					// swap samples in the array
					m_samples.remove(j);
					m_samples.remove(i);
					
					m_samples.add(i, s2);
					m_samples.add(j, s1);
					
					Sample temp = s1;
					s1 = s2;
					s2 = temp;
				}
			}
		}
		
		// validate span value
		if (maxsampletime > m_span)
		{
			m_span = maxsampletime;
		}
	}

	
	void loadXML(Element node)
	{
		m_samples = new ArrayList();
		m_names = new ArrayList();

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
		
		m_interpolate = node.getAttribute("interpolate");
		m_prototype = node.getAttribute("prototype");
		
		parsePrototype(m_prototype);
		
		NodeList samples = node.getElementsByTagName("sample");
		
		for(int i = 0; i < samples.getLength(); i++)
		{
			Element sampleNode = (Element) samples.item(i);
			
			Sample sample = new Sample();
			
			sample.loadXML(sampleNode, m_names);
			
			m_samples.add(sample);
		}
		
		// sort and validate samples.. do not allow samples that have too few variables
			
		m_sampleCount = m_samples.size();

	}
	
	void parsePrototype(String prototype)
	{
		// look for variables of the form %x%
		// replace the variables with %i 
		
		String tp = prototype.trim();
		String parts[] = tp.split("%");
		m_names.clear();
		
		boolean variable = tp.startsWith("%");
		
		m_prototype = "";
		
		for(int i = 0; i < parts.length; i++)
		{
			String part = parts[i];
			
			if (variable)
			{
				int index = m_names.indexOf(part);
				
				if (index < 0)
				{
					index = m_names.size();
					m_names.add(part);
				}
				
				index++;
				
				m_prototype += "%" + index;
			}
			else
			{
				m_prototype += part;
			}
			
			variable = !variable;
		}
		
		m_variableCount = m_names.size();
	}
	
	@Override
	int play(Context context, double lastTime, double currentTime)
	{
		if (currentTime >= m_time)
		{
			if (m_startTime < 0.0)
			{
				m_startTime = currentTime;
				m_stopTime = m_startTime + m_totalTime;
			}

			if (currentTime <= m_stopTime)
			{
				double time = (currentTime - m_startTime) % m_span;

				String cmd = m_prototype;
				
				for(int i = 0; i < m_variableCount; i++)
				{
					// calculate the interpolated keyframe values
					double value = getSampleValue(time, i);
					
					// apply the interpolated values to the prototype
					cmd = cmd.replaceAll("%" + (i + 1), Double.toString(value));
				}
				
				// execute the prototype script
				ServiceManager.Current.execute(context, cmd);
				
				return 1;
			}
		}
		
		return 0;
	}

	private double getSampleValue(double time, int index)
	{
		if (m_sampleCount > 0)
		{
			Sample s0 = (Sample) m_samples.get(0);
			
			double starttime = s0.m_time;
			double startvalue = s0.m_values[index];
			double stoptime = starttime;
			double stopvalue = startvalue;
			
			// look up the nearest sample without passing the time stamp
			for (int i = 0; i < m_sampleCount; i++)
			{
				Sample si = (Sample) m_samples.get(i);

				stoptime = si.m_time;
				stopvalue = si.m_values[index];

				if (si.m_time >= time)
				{
					break;
				}
				
				starttime = stoptime;
				startvalue = stopvalue;
			}
			
			// interpolate results
			
			if (starttime == stoptime)
				return startvalue;
			
			double value = startvalue + (time - starttime) / (stoptime - starttime) * (stopvalue - startvalue);
			
			return value;
		}
		
		return 0.0;
	}
}

