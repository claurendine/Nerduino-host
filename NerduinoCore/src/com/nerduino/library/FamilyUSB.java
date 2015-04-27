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

package com.nerduino.library;

import java.util.ArrayList;
import java.util.Hashtable;
import jssc.SerialPortList;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

public class FamilyUSB extends FamilyBase
{
	@SuppressWarnings("UseOfObsoleteCollectionType")
	Hashtable<String, Boolean> m_ports;
	
	ArrayList<NerduinoUSB> m_engageList = new ArrayList<NerduinoUSB>();
	boolean m_engaging;
	
	
	public FamilyUSB()
	{
		super();
		
		scanPorts();
	}
	
	@Override
	public String getFamilyType()
	{
		return "USB";
	}

	@Override
	public NerduinoBase CreateNerduino()
	{
		return new NerduinoUSB();
	}

	void scanPorts()
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{	
					while(true)
					{
						if (!m_engaging && NerduinoManager.Current != null)
						{
							String[] portNames = SerialPortList.getPortNames();

							if (m_ports == null)
							{
								m_ports = new Hashtable<String, Boolean>();
								
								for(String name : portNames)
								{
									m_ports.put(name, true);
								}
							}
							else
							{
								// mark all ports as not found
								Object[] keys = m_ports.keySet().toArray();

								for(Object key : keys)
								{
									m_ports.put((String) key, false);
								}

								// look for new ports
								for(String name : portNames)
								{
									if (m_ports.containsKey(name))
									{
										m_ports.put(name, true);
									}
									else
									{
										// new port discovered
										onPortDiscovered(name);

										m_ports.put(name, true);
									}
								}

								// look for removed ports
								for(Object key : keys)
								{
									if (!m_ports.get(key))
									{
										onPortRemoved((String) key);

										m_ports.remove(key);
									}
								}
							}
							
							
							if (m_engageList.size() > 0)
							{
								try
								{
									Thread.sleep(1000);
								}
								catch(InterruptedException ex)
								{
									Exceptions.printStackTrace(ex);
								}

								m_engaging = true;

								for(Object nerd : m_engageList.toArray())
								{
									NerduinoUSB nu = (NerduinoUSB) nerd;
									
									nu.setStatus(NerduinoStatusEnum.Uninitialized);

									if (nu.getActive())
										nu.engage();

									m_engageList.remove(nerd);
								}

								m_engaging = false;
							}
						}

						Thread.sleep(5000);
					}
				}
				catch(Exception e)
				{
				}
			}
		}, "Nerduino USB Scan thread");

		thread.start();
	}
	
	void onPortDiscovered(String portname)
	{
		// iterate through Nerduinos to see if any nerduinos are associated with this port
		for(Node node : NerduinoManager.Current.getNodes())
		{
			NerduinoBase nerd = (NerduinoBase) node;
			
			if (nerd instanceof NerduinoUSB)
			{
				NerduinoUSB nu = (NerduinoUSB) nerd;
				
				if (nu.getComPort().equals(portname))
					m_engageList.add(nu);
			}
		}
	}
	
	void onPortRemoved(String portname)
	{
		for(Node node : NerduinoManager.Current.getNodes())
		{
			NerduinoBase nerd = (NerduinoBase) node;
			
			if (nerd instanceof NerduinoUSB)
			{
				NerduinoUSB nu = (NerduinoUSB) nerd;
				
				if (nu.getComPort().equals(portname))
				{
					//nu.setActive(false);

					nu.setStatus(NerduinoStatusEnum.Offline);
				}
			}
		}
	}
}
