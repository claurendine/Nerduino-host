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

public enum ModemStatusEnum 
{
	HardwareReset(0),
	WatchdogTimerReset(1),
	JoinedNetwork(2),
	Disassociated(3),
	CoordinatorStarted(6),
	NetorkSecurityKeyUpdated(7),
	VoltageSupplyLimitExceeded(0x0d),
	ModemConfigChangedWhileJoinInProgress(0x11),
	StackError(-128); //  (0x80);
	
    private final byte value;
    
    ModemStatusEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static ModemStatusEnum valueOf(byte b) 
	{
		switch(b)
		{
			case 0:
				return HardwareReset;
			case 1:
				return WatchdogTimerReset;
			case 2:
				return JoinedNetwork;
			case 3:
				return Disassociated;
			case 6:
				return CoordinatorStarted;
			case 7:
				return NetorkSecurityKeyUpdated;
			case 0x0d:
				return VoltageSupplyLimitExceeded;
			case 0x11:
				return ModemConfigChangedWhileJoinInProgress;
			case -128: //0x80:
				return StackError;
		}
		
		return null;
	}
}
