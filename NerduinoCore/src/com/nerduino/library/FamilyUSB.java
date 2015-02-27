/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.library;

import java.util.ArrayList;
import java.util.Hashtable;
import jssc.SerialPortList;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author chaselaurendine
 */
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
		});

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
