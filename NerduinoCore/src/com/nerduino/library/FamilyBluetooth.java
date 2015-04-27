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
import java.util.Vector;
import java.util.regex.Pattern;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import jssc.SerialNativeInterface;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

import jssc.SerialPortList;


public class FamilyBluetooth extends FamilyBase implements DiscoveryListener
{
	public static String MAC_OS_BT_PATTERN = "tty.(FireFly).*";
	
	private static Object lock=new Object();
    private static Vector vecDevices=new Vector();
	
	@SuppressWarnings("UseOfObsoleteCollectionType")
	Hashtable<String, Boolean> m_ports;
	
	ArrayList<NerduinoBT> m_engageList = new ArrayList<NerduinoBT>();
	boolean m_engaging;
	int m_osType;
	
	static FamilyBluetooth Current; 
	
	
	public FamilyBluetooth()
	{
		super();
		
		Current = this;
		
		SerialNativeInterface serialInterface = new SerialNativeInterface();
        
		m_osType = SerialNativeInterface.getOsType();
		
		scanPorts();
		
	}

	@Override
	public String getFamilyType()
	{
		return "Bluetooth";
	}

	@Override
	public NerduinoBase CreateNerduino()
	{
		//return new NerduinoBT();
		
		return null;
	}

	@Override
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) 
    {
		//System.out.println("Device discovered: "+btDevice.getBluetoothAddress());
		
		//add the device to the vector
        if (!vecDevices.contains(btDevice))
		{
            vecDevices.addElement(btDevice);
        }
	}
	
	@Override
	public void servicesDiscovered(int i, ServiceRecord[] srs)
	{
	}

	@Override
	public void serviceSearchCompleted(int i, int i1)
	{
	}

	@Override
	public void inquiryCompleted(int discType) 
	{
        synchronized(lock)
		{	
            lock.notify();
        }
       
        switch (discType) 
		{
            case DiscoveryListener.INQUIRY_COMPLETED :
                //System.out.println("INQUIRY_COMPLETED");
				
				// evaluate the completed list
				if (m_ports == null)
				{
					m_ports = new Hashtable<String, Boolean>();

					for(Object obj : vecDevices)
					{
						RemoteDevice dev = (RemoteDevice) obj;
						String address = dev.getBluetoothAddress();
						m_ports.put(address, true);
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
					for(Object obj : vecDevices)
					{
						RemoteDevice dev = (RemoteDevice) obj;
						String address = dev.getBluetoothAddress();
					
						if (m_ports.containsKey(address))
						{
							m_ports.put(address, true);
						}
						else
						{
							// new port discovered
							onPortDiscovered(address);

							m_ports.put(address, true);
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
					m_engaging = true;
					
					for(Object nerd : m_engageList.toArray())
					{
						NerduinoBT nu = (NerduinoBT) nerd;

						nu.setStatus(NerduinoStatusEnum.Uninitialized);
						nu.engage();

						//if (nu.m_serial.getEnabled())
						{
							m_engageList.remove(nerd);
						}
					}

					m_engaging = false;
				}
				
                break;
               
            case DiscoveryListener.INQUIRY_TERMINATED :
                System.out.println("INQUIRY_TERMINATED");
                break;
               
            case DiscoveryListener.INQUIRY_ERROR :
                System.out.println("INQUIRY_ERROR");
                break;
 
            default :
                System.out.println("Unknown Response Code");
                break;
        }
    }//end method

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
							if (m_osType == SerialNativeInterface.OS_MAC_OS_X)
							{
								// jsr82 library does not work on 64but mac, so fall back to the jssc library 
								// with an alternate device querry pattern
								
								Pattern pattern = Pattern.compile(MAC_OS_BT_PATTERN);
								
								String[] portNames = SerialPortList.getPortNames(pattern);
								
								if (m_ports == null)
								{
									m_ports = new Hashtable<String, Boolean>();
									
									if (portNames != null)
									{
										for(String name : portNames)
										{	
											m_ports.put(name, true);
										}
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
										nu.engage();

										//if (nu.m_serial.getEnabled())
										{
											m_engageList.remove(nerd);
										}
									}

									m_engaging = false;
								}
							}
							else
							{
								// if not on the mac, use the jsr82 library
								try
								{
									vecDevices.clear();

									LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, Current);
								}
								catch(BluetoothStateException ex)
								{
									Exceptions.printStackTrace(ex);
								}
							}
							
						}

						Thread.sleep(5000);
					}
				}
				catch(Exception e)
				{
				}
			}
		}, "Nerduino Bluetooth scanning thread");

		thread.start();
	}
	
	void onPortDiscovered(String portname)
	{		
		// iterate through Nerduinos to see if any nerduinos are associated with this port
		for(Node node : NerduinoManager.Current.getNodes())
		{
			NerduinoBase nerd = (NerduinoBase) node;
			
			if (nerd instanceof NerduinoBT)
			{
				NerduinoBT nu = (NerduinoBT) nerd;
				
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
			
			if (nerd instanceof NerduinoBT)
			{
				NerduinoBT nu = (NerduinoBT) nerd;
				
				if (nu.getComPort().equals(portname))
					nu.setStatus(NerduinoStatusEnum.Offline);
			}
		}
	}
}

