package com.nerduino.xbee;

public enum FrameTypeEnum 
{
	ATCommand(0x08),
	ATCommandQueue(0x09),
    TransmitRequest(0x10),
    ExplicitAddressingZigbeeCommand(0x11),
    RemoteATCommandRequest(0x17),
    CreateSourceRoute(0x21),
	ATCommandResponse(-120), //0x88
	ModemStatus(-118), // 0x8A: // modem status
	ZigbeeTransmitStatus(-117), // 0x8B: // zigbee transmit status
	ZigbeeReceivePacket(-112), // 0x90: // zigbee receive packet A0 = 0
	ZigbeeExplicitRxIndicator(-111), // 0x91: // zigbee explicit rx indicator A0 = 1
	ZigbeeIODataSampleRxIndicator(-110), // 0x92: // zigbee IO data sample Rx indicator
	XBeeSensorReadIndicator(-108), // 0x94: // xbee sensor read indicator A0 = 0
	NodeIdentificationIndicator(-107), // 0x95: // node identification indicator A0 = 0
	RemoteCommandResponse(-105), // 0x97: // remote command response
	ReceivePacket64BitAddress(-128), // 0x80: // RX (Receive) Packet: 64-bit Address
	ReceivePacket16BitAddress(-127), // 0x81: // RX (Receive) Packet: 16-bit Address
	OverTheAirFirmwareUpdateStatus(-96), // 0xA0: // over the air firmware update status
	RouteRecordIndicator(-95), // 0xA1: // route record indicator
	ManyToOneRouteRequestIndicator(-94); // 0xA3: // many to one route request indicator
	     
    private final byte value;
    
	FrameTypeEnum(int val)
    {
    	this.value = (byte)val;
    }
    
    public byte Value()
    {
    	return this.value;
    }

	public static FrameTypeEnum valueOf(byte b) 
	{
		switch(b)
		{
            case 0x08: // AT command
            	return ATCommand;
            case 0x09: // AT command Queue
            	return ATCommandQueue;
            case 0x10: // zigbee transmit request
            	return TransmitRequest;
            case 0x11: // explicit addressing zigbee command frame
            	return ExplicitAddressingZigbeeCommand;
            case 0x17: // remote command request
                return RemoteATCommandRequest;
            case 0x21: // remote command request
                return CreateSourceRoute;
            case -120: // 0x88: // AT command response
            	return ATCommandResponse;
            case -118: // 0x8A: // modem status
            	return ModemStatus;
            case -117: // 0x8B: // zigbee transmit status
            	return ZigbeeTransmitStatus;
            case -112: // 0x90: // zigbee receive packet A0 = 0
            	return ZigbeeReceivePacket;
            case -111: // 0x91: // zigbee explicit rx indicator A0 = 1
                return ZigbeeExplicitRxIndicator;
            case -110: // 0x92: // zigbee IO data sample Rx indicator
                return ZigbeeIODataSampleRxIndicator;
            case -108: // 0x94: // xbee sensor read indicator A0 = 0
            	return XBeeSensorReadIndicator;
            case -107: // 0x95: // node identification indicator A0 = 0
                return NodeIdentificationIndicator;
            case -105: // 0x97: // remote command response
                return RemoteCommandResponse;
            case -128: // 0x80: // RX (Receive) Packet: 64-bit Address
                return ReceivePacket64BitAddress;
            case -127: // 0x81: // RX (Receive) Packet: 16-bit Address
                return ReceivePacket16BitAddress;
            case -96: // 0xA0: // over the air firmware update status
                return OverTheAirFirmwareUpdateStatus;
            case -95: // 0xA1: // route record indicator
                return RouteRecordIndicator;
            case -94: // 0xA3: // many to one route request indicator
                return ManyToOneRouteRequestIndicator;
		}	
		
		return null;
	}
}
