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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import org.openide.util.Exceptions;

public class Address
{
	public InetAddress IPAddress;
	public long SerialNumber;
	public short NetworkAddress;
	public short RoutingIndex;
	
	public Address()
	{
		SerialNumber = 0L;
		NetworkAddress = 0;
		RoutingIndex = 0;

		try
		{
			IPAddress = getHostAddress();			
		}
		catch(UnknownHostException ex)
		{
		//	Exceptions.printStackTrace(ex);
		}
	}

	public Address(long serialNumber, short networkAddress, short routingIndex)
	{
		SerialNumber = serialNumber;
		NetworkAddress = networkAddress;
		RoutingIndex = routingIndex;
		
		try
		{
			IPAddress = getHostAddress();
		}
		catch(UnknownHostException ex)
		{
		//	Exceptions.printStackTrace(ex);
		}
	}
	
	public Address(InetAddress address, short routingIndex)
	{
		SerialNumber = 0;
		NetworkAddress = 0;
		RoutingIndex = routingIndex;
		IPAddress = address;
	}
	
	private InetAddress getHostAddress() throws UnknownHostException
	{
		InetAddress ret = null;
		
		try
		{
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements())
			{
				NetworkInterface current = interfaces.nextElement();
				//System.out.println(current);
				
				if (!current.isUp() || current.isLoopback() || current.isVirtual())
				{
					continue;
				}
				Enumeration<InetAddress> addresses = current.getInetAddresses();
				while (addresses.hasMoreElements())
				{
					InetAddress current_addr = addresses.nextElement();
					if (current_addr.isLoopbackAddress())
					{
						continue;
					}
					if (current_addr instanceof Inet4Address)
						ret = current_addr;
				}
			}
		}
		catch(SocketException ex)
		{
//			Exceptions.printStackTrace(ex);
		}
		
		if (ret != null)
			return ret;
		
		return InetAddress.getLocalHost();
	}

}
