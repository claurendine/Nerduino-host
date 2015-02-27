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

package com.nerduino.propertybrowser;

import com.nerduino.library.FamilyBluetooth;
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
import jssc.SerialPortList;
import org.openide.util.Exceptions;

public class BluetoothPortPropertyEditor extends EnumerationPropertyEditor implements DiscoveryListener
{
	private static Object lock=new Object();
    private static Vector vecDevices=new Vector();
	boolean m_engaging = false;

	int m_osType;
	
	static BluetoothPortPropertyEditor Current; 

	public BluetoothPortPropertyEditor()
	{
		super();
		
		Current = this;
		
		m_osType = SerialNativeInterface.getOsType();
	}	
	
	
	@Override
	public Object[] getList()
	{
		if (m_osType == SerialNativeInterface.OS_MAC_OS_X)
		{
			// jsr82 library does not work on 64but mac, so fall back to the jssc library 
			// with an alternate device querry pattern
			
			Pattern pattern = Pattern.compile(FamilyBluetooth.MAC_OS_BT_PATTERN);
			
			return SerialPortList.getPortNames(pattern);			
		}
		else
		{
			// if not on the mac, use the jsr82 library
			try
			{
				m_engaging = true;
				
				vecDevices.clear();
				
				LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, Current);
				
				// wait till the list is completed
				while(!m_engaging)
				{
					try
					{
						Thread.sleep(100);
					}
					catch(InterruptedException ex)
					{
						Exceptions.printStackTrace(ex);
					}
				}
				
				// return the list of addresses
				Vector addresses = new Vector();
				
								// evaluate the completed list
				for(Object obj : vecDevices)
				{
					RemoteDevice dev = (RemoteDevice) obj;
					String address = dev.getBluetoothAddress();
					
					addresses.add(address);
				}
				
				return addresses.toArray();
			}
			catch(BluetoothStateException ex)
			{
				Exceptions.printStackTrace(ex);
			}
		}
		
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
				m_engaging = false;				
                break;
               
            case DiscoveryListener.INQUIRY_TERMINATED :
                System.out.println("INQUIRY_TERMINATED");
				m_engaging = false;
                break;
               
            case DiscoveryListener.INQUIRY_ERROR :
                System.out.println("INQUIRY_ERROR");
				m_engaging = false;
                break;
 
            default :
                System.out.println("Unknown Response Code");
				m_engaging = false;
                break;
        }
    }//end method
}
