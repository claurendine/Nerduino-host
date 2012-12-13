/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.library;

/**
 *
 * @author chaselaurendine
 */
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
