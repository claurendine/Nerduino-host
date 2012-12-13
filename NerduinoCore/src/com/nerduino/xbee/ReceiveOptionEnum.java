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
