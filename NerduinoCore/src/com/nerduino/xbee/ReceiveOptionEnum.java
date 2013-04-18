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

package com.nerduino.xbee;

public enum ReceiveOptionEnum 
{
    PacketAcknowledged(0x01),
    PacketWasBroadcast(0x02),
    PacketEncryptedWithAPS(0x20),
	PacketSentFromEndDevice(0x40);

    private final byte value;
    
    ReceiveOptionEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static ReceiveOptionEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 1:
				return PacketAcknowledged;
			case 2:
				return PacketWasBroadcast;
			case 0x20:
				return PacketEncryptedWithAPS;
			case 0x40:
				return PacketSentFromEndDevice;
		}	
		
		return null;
	}
}
