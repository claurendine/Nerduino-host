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

public class Address
{
	public long SerialNumber;
	public short NetworkAddress;
	public short RoutingIndex;
	
	public Address()
	{
		SerialNumber = 0L;
		NetworkAddress = 0;
		RoutingIndex = 0;
	}

	public Address(long serialNumber, short networkAddress, short routingIndex)
	{
		SerialNumber = serialNumber;
		NetworkAddress = networkAddress;
		RoutingIndex = routingIndex;
	}
}
