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
