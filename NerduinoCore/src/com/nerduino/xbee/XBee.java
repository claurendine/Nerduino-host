package com.nerduino.xbee;

import java.io.IOException;
import javax.sql.rowset.serial.SerialException;


public class XBee extends Serial
{
    // Definitions
    public static int XSTICK = 0x30009;
	
	
    // Declarations
    APIEnableEnum m_apiEnable = APIEnableEnum.Enabled;
    
    // Constructors
    public XBee ()
    {
		super();
    }

    // Properties    

	public long getDestinationAddress()
	{
		long address = 0;
		
		try
		{
			long il = (long) sendCommandInt("DL");
			long ih = (long) sendCommandInt("DH");
			
			address = (long) (ih * 0x100000000L + il);
		} 
		catch (Exception e) 
		{
		}
		
		return address;
	}
	
	public short getNetworkAddress()
	{
		short ret = 0;
		
		try
		{
			ret = sendCommandShort("MY");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}

	public short getParentNetworkAddress() throws Exception
	{
        if (m_deviceType == XSTICK)
            throw new Exception("Not supported on the XStick");

		return sendCommandShort("MP");	
   	}

	public short getNumberOfRemainingChildren()
	{
		short ret = 0;
		
		try
		{
			ret = sendCommandShort("NC");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}
	
	public long getSerialNumber()
	{
        if (m_initialized)
            return m_serialNumber;
        
        long address = 0;
        
        try
        {
			int low = sendCommandInt("SL");
			int high = sendCommandInt("SH");
			
			long il = (long) low;
			long ih = (long) high;
			
			address = (long) (ih * 0x100000000L + il);
        }
		catch (Exception e) 
		{
		}
		
		return address;
	}
	
	public String getNodeIdentifier()
	{
		return sendCommandString("NI");
    }
    
	public void setNodeIdentifier(String value) 
	{ 
		sendCommand("NI", value);
	}
	
	public short getSourceEndpoint()
	{
		short ret = 0;
		
		try
		{
			ret = sendCommandShort("SE");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}

	public void setSourceEndpoint(short value) 
	{ 
		sendCommand("SE", value);
	}

	public short getDestinationEndpoint()
	{
		short ret = 0;
		
		try
		{
			ret = sendCommandShort("DE");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}

	public void setDestinationEndpoint(short value) 
	{ 
		sendCommand("DE", value); 
	}

	public short getClusterID()
	{
		short ret = 0;
		
		try
		{
			ret = sendCommandShort("CI");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}
	
	public void setClusterID(short value)
	{ 
		sendCommand("CI", value);
	}

	public short getMaximumPayload()
	{
		short ret = 0;
		
		try
		{
			ret = sendCommandShort("NP");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}
	
	public int getDeviceType()
	{
		int ret = 0;
		
		try
		{
			ret = sendCommandInt("DD");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
    }
	
	public byte getChannel()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("CH");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}
	
	public long getExtendedPanID()
	{
		long ret = 0;
		
		try
		{
			ret = sendCommandLong("ID");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}
	
	public void	setExtendedPanID(long value) 
	{ 
		sendCommand("ID", value);
	}
	
	public long getOperatingExtendedPanID()
	{
		long ret = 0;
		
		try
		{
			ret = sendCommandLong("OP");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}
	
	public byte getMaximumUnicastHops()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("NH");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}
	
	public void setMaximumUnicastHops(byte value)
	{ 
		sendCommand("NH", value);	
	}

	public byte getMaximumBroadcastHops()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("BH");
		} 
		catch (Exception e) 
		{
		}
		
		return ret;
	}
	
	public void setMaximumBroadcastHops(byte value) 
	{ 
		sendCommand("BH", value);
	}

	public short getOperatingPanID()
	{
		short ret = 0;
		
		try
		{
			ret = sendCommandShort("OI");
		} 
		catch (Exception e) 
		{
		}
		
		return ret;
	}
	
	public void setOperatingPanID(short value)
	{ 
		sendCommand("OI", value);
	}
	
	public int getNodeDiscoveryTimeout()
    {
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("NT");
		}
		catch (Exception e) 
		{
		}
		
		return (int)(ret * 100);
    }

	public void setNodeDiscoveryTimeout(int value)
    {
        byte val = (byte)(value / 100);

        sendCommand("NT", val);
    }
	
	public NodeDiscoveryOptionsEnum getNodeDiscoveryOptions()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("NO");
		}
		catch (Exception e) 
		{
		}
		
		return NodeDiscoveryOptionsEnum.valueOf(ret);
	}
	
	public void setNodeDiscoveryOptions(NodeDiscoveryOptionsEnum value) 
	{ 
		sendCommand("NO", value.Value());
	}
	
	public void sendNodeDiscover()
	{
		try 
		{
			sendCommand("ND");
		}
		catch (Exception e) 
		{
		}

	}
	
	public short getScanChannels()
	{
		short ret = 0;
		
		try
		{
			ret = sendCommandShort("SC");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}
	
	public void setScanChannels(short value) 
	{ 
		sendCommand("SC", value);
	}
	
	public byte getScanDuration()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("SD");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}
	
	public void setScanDuration(byte value) 
	{ 
		sendCommand("SD", value);
	}
	
	public byte getZigbeeStackProfile()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("ZS");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}

	public void setZigbeeStackProfile(byte value)
	{ 
		sendCommand("ZS", value);
	}
	
	public byte getNodeJoinTime()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("NJ");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}

	public void setNodeJoinTime(byte value)
	{ 
		sendCommand("NJ", value);
	}

	public Boolean getChannelVerificationEnabled() throws Exception
	{
        if (m_deviceType == XSTICK)
            throw new Exception("Not supported on the XStick");

		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("JV");
		}
		catch (Exception e) 
		{
		}
		
		return (ret == 1);
	}

	public void	setChannelVerificationEnabled(Boolean value) throws Exception 
	{
        if (m_deviceType == XSTICK)
            throw new Exception("Not supported on the XStick");

		byte val = 0;
		
		if (value) val = 1;
		
		sendCommand("JV", val); 
	}
	
	public short getNetworkWatchdogTimeout() throws Exception
	{
        if (m_deviceType == XSTICK)
            throw new Exception("Not supported on the XStick");

		short ret = 0;
		
		try
		{
			ret = sendCommandShort("NW");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}
		
    public void setNetworkWatchdogTimeout(short value) throws Exception 
    {
        if (m_deviceType == XSTICK)
            throw new Exception("Not supported on the XStick");

        sendCommand("NW", value); 
    }
	
	public Boolean getJoinNotificationEnabled() throws Exception
	{
        if (m_deviceType == XSTICK)
            throw new Exception("Not supported on the XStick");

		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("JN");
		}
		catch (Exception e) 
		{
		}
		
		return (ret == 1);
	}

	public void setJointNotificatoinEnabled(Boolean value) throws Exception 
	{
        if (m_deviceType == XSTICK)
            throw new Exception("Not supported on the XStick");

		byte val = 0;
		
		if (value) val = 1;
		
		sendCommand("JN", val); 
	}
	
	public byte getAggregateRoutingNotification()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("AR");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
    }
	
	public void setAggregateRoutingNotification(byte value)
	{ 
		sendCommand("AR", value);
	}

	public Boolean getEncryptionEnabled()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("EE");
		}
		catch (Exception e) 
		{
		}
		
		return (ret == 1);
	}
	
	public void setEncryptionEnabled(Boolean value) 
	{ 
		byte val = 0;
		
		if (value) val = 1;
		
		sendCommand("EE", val); 
	}

	public byte getEncryptionOptions()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("EO");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}
	
	public void setEncryptionOptions(byte value) 
	{ 
		sendCommand("EO", value); 
	}

	public short getNetworkEncryptionKey()
	{
		short ret = 0;
		
		try
		{
			ret = sendCommandShort("NK");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}

	public void setNetworkEncryptionKey(short value) 
	{
		sendCommand("NK", value);
	}

	public short getLinkKey()
	{
		short ret = 0;
		
		try
		{
			ret = sendCommandShort("KY");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
    }
	
	public void setLinkKey(short value) 
	{ 
		sendCommand("KY", value);
	}

	public byte getPowerLevel()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("PL");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}

	public void setPowerLevel(byte value) 
	{ 
		sendCommand("PL", value);
	}
	
	public Boolean getBoostMode()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("PM");
		}
		catch (Exception e) 
		{
		}
		
		return (ret == 1);
	}
	
	public void setBoostMode(Boolean value) 
	{ 
		byte val = 0;
		
		if (value) val = 1;
		
		sendCommand("PM", val); 
	}
	
	public byte getRSSI()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("DB");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
    }
	
	public byte getPeakPower()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("PP");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}

	public APIEnableEnum getAPIEnabled()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("AP");
		}
		catch (Exception e) 
		{
		}
		
		return APIEnableEnum.valueOf(ret);
	}
	
	public void setAPIEnabled(APIEnableEnum value) 
    { 
        sendCommand("AP", value.Value());
        
        m_apiEnable = value;
	}

	public APIOptionsEnum getAPIOptions()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("AO");
		}
		catch (Exception e) 
		{
		}
		
		return APIOptionsEnum.valueOf(ret);
	}

	public void setAPIOptions(APIOptionsEnum value) 
	{ 
		sendCommand("AO", value.Value());
	}

	public DataRateEnum getDataRate()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("BD");
		}
		catch (Exception e) 
		{
		}
		
		return DataRateEnum.valueOf(ret);
	}
	
	public void setDataRate(DataRateEnum value) 
	{ 
		sendCommand("BD", value.Value());
	}
	
	public byte getPacketizingTimeout()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("RO");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}

	public void setPacketizingTimeout(byte value) 
	{ 
		sendCommand("RO", value);
	}

	public DIO7ConfigurationEnum getDIO7Configuration()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("D7");
		}
		catch (Exception e) 
		{
		}
		
		return DIO7ConfigurationEnum.valueOf(ret);
	}

	public void setDIO7Configuration(DIO7ConfigurationEnum value) 
	{ 
		sendCommand("D7", value.Value());
	}

	public DIO6ConfigurationEnum getDIO6Configuration()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("D6");
		}
		catch (Exception e) 
		{
		}
		
		return DIO6ConfigurationEnum.valueOf(ret);
	}
	
	public void setDIO6Configuration(DIO6ConfigurationEnum value) 
	{ 
		sendCommand("D6", value.Value());
	}

	public DIO5ConfigurationEnum getDIO5Configuration()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("D5");
		}
		catch (Exception e) 
		{
		}
		
		return DIO5ConfigurationEnum.valueOf(ret);
	}

	public void setDIO5Configuration(DIO5ConfigurationEnum value) 
	{ 
		sendCommand("D5", value.Value());
	}

	public DIO4ConfigurationEnum getDIO4Configuration()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("D4");
		}
		catch (Exception e) 
		{
		}
		
		return DIO4ConfigurationEnum.valueOf(ret);
	}

	public void setDIO4Configuration(DIO4ConfigurationEnum value) 
	{ 
		sendCommand("D4", value.Value());
	}

	public DIO3ConfigurationEnum getDIO3Configuration()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("D3");
		}
		catch (Exception e) 
		{
		}
		
		return DIO3ConfigurationEnum.valueOf(ret);
	}

	public void setDIO3Configuration(DIO3ConfigurationEnum value) 
	{ 
		sendCommand("D3", value.Value());
	}

	public DIO2ConfigurationEnum getDIO2Configuration()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("D2");
		}
		catch (Exception e) 
		{
		}
		
		return DIO2ConfigurationEnum.valueOf(ret);
	}

	public void setDIO2Configuration(DIO2ConfigurationEnum value) 
	{ 
		sendCommand("D2", value.Value());
	}

	public DIO1ConfigurationEnum getDIO1Configuration()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("D1");
		}
		catch (Exception e) 
		{
		}
		
		return DIO1ConfigurationEnum.valueOf(ret);
	}

	public void setDIO1Configuration(DIO1ConfigurationEnum value) 
	{ 
		sendCommand("D1", value.Value());
	}

	public DIO0ConfigurationEnum getDIO0Configuration()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("D0");
		}
		catch (Exception e) 
		{
		}
		
		return DIO0ConfigurationEnum.valueOf(ret);
	}

	public void setDIO0Configuration(DIO0ConfigurationEnum value) 
	{ 
		sendCommand("D0", value.Value());
	}

	public short getSampleRate()
	{
		short ret = 0;
		
		try
		{
			ret = sendCommandShort("IR");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}

	public void setSampleRate(short value) 
	{ 
		sendCommand("IR", value);
	}
	
	public short getDIOChangeDetection()
	{	
		short ret = 0;
		
		try
		{
			ret = sendCommandShort("IC");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
    }

	public void setDIOChangeDetection(short value)
	{ 
		sendCommand("IC", value);
	}
	
	public PWM0ConfigurationEnum getPWM0Configuration()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("P0");
		}
		catch (Exception e) 
		{
		}
		
		return PWM0ConfigurationEnum.valueOf(ret);
	}

	public void setPWM0Configuration(PWM0ConfigurationEnum value) 
	{ 
		sendCommand("P0", value.Value());
	}
	
	public P1ConfigurationEnum getP1Configuration()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("P1");
		}
		catch (Exception e) 
		{
		}
		
		return P1ConfigurationEnum.valueOf(ret);
	}

	public void setP1Configuration(P1ConfigurationEnum value) 
	{ 
		sendCommand("P1", value.Value());
	}

	public P2ConfigurationEnum getP2Configuration()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("P2");
		}
		catch (Exception e) 
		{
		}
		
		return P2ConfigurationEnum.valueOf(ret);
	}

	public void setP2Configuration(P2ConfigurationEnum value) 
	{ 
		sendCommand("P2", value.Value());
	}

	public P3ConfigurationEnum getP3Configuration()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("P3");
		}
		catch (Exception e) 
		{
		}
		
		return P3ConfigurationEnum.valueOf(ret);
	}

	public void setP3Configuration(P3ConfigurationEnum value) 
	{ 
		sendCommand("P3", value.Value());
	}
			
	public byte getAssocLEDBlinkTime()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("LT");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}

	public void setAssocLEDBlinkTime(byte value) 
	{ 
		sendCommand("LT", value);
	}
			
	public short getPullUpResistor()
	{
		short ret = 0;
		
		try
		{
			ret = sendCommandShort("PR");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}
	
	public void setPullUpResistor(short value) 
	{ 
		sendCommand("PR", value);
	}
			
	public byte getRssiPwmTimer()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("RP");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}
		
	public void setRssiPwmTimer(byte value) 
	{ 
		sendCommand("RP", value);
	}
			
	public short getSupplyVoltage()
	{
		short ret = 0;
		
		try
		{
			ret = sendCommandShort("%V");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}
			
	public short getVoltageSupplyMonitoring()
	{
		short ret = 0;
		
		try
		{
			ret = sendCommandShort("V+");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}
	
	public void setVoltageSupplyMonitoring(short value)
	{ 
		sendCommand("V+", value);
	}
			
	public short getModuleTemperature()
	{
		short ret = 0;
		
		try
		{
			ret = sendCommandShort("TP");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}
			
	public short getFirmwareVersion()
	{
        if (m_initialized)
            return m_firmwareVersion;

		short ret = 0;
		
		try
		{
			ret = sendCommandShort("VR");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}
			
	public short getHardwareVersion()
	{
        if (m_initialized)
            return m_hardwareVersion;

//		String ret = "";
		short ret = 0;
		
		try
		{
            //ret = SendCommandString("HV");
            
			ret = sendCommandShort("HV");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}
			
	public byte getAssociationIdentification()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("AI");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}
			
	public short getCommandModeTimeout()
	{
		short ret = 0;
		
		try
		{
			ret = sendCommandShort("CT");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}

	public void setCommandModeTimeout(short value) 
	{ 
		sendCommand("CT", value);
	}
			
	public void exitCommandMode()
	{
		sendCommand("CN");
	}
			
	public short getGuardTimes()
	{
		short ret = 0;
		
		try
		{
			ret = sendCommandShort("GT");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}

	public void setGuardTimes(short value) 
	{ 
		sendCommand("GT", value);
	}
			
	public byte getCommandSequenceCharacter()
	{
		byte ret = 0;
		
		try
		{
			ret = sendCommandByte("CC");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}
	
	public void setCommandSequenceCharacter(byte value) 
	{ 
		sendCommand("CC", value);
	}
			
	public SleepModeEnum getSleepMode() throws Exception
	{
        if (m_deviceType == XSTICK)
            throw new Exception("Not supported on the XStick");

		byte val = sendCommandByte("SM");

		return SleepModeEnum.valueOf(val);
	}

	public void setSleepMode(SleepModeEnum value) throws Exception
    {
        if (m_deviceType == XSTICK)
            throw new Exception("Not supported on the XStick");

        sendCommand("SM", value.Value()); 
    }
			
	public short getNumberOfSleepPeriods()
	{
		short ret = 0;
		
		try
		{
			ret = sendCommandShort("SN");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}

	public void setNumberOfSleepPeriods(short value) 
	{ 
		sendCommand("SN", value);
	}
			
	public short getSleepPeriod()
	{
		short ret = 0;
		
		try
		{
			ret = sendCommandShort("SP");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}

	public void setSleepPeriod(short value) 
	{ 
		sendCommand("SP", value);
	}
			
	public short getTimeBeforeSleep() throws Exception
	{
        if (m_deviceType == XSTICK)
            throw new Exception("Not supported on the XStick");

		short ret = 0;
		
		try
		{
			ret = sendCommandShort("ST");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}

	public void setTimeBeforeSleep(short value)  throws Exception
    {
        if (m_deviceType == XSTICK)
            throw new Exception("Not supported on the XStick");

        sendCommand("ST", value); 
    }
			
	public SleepOptionsEnum getSleepOptions() throws Exception
	{
        if (m_deviceType == XSTICK)
            throw new Exception("Not supported on the XStick");

		byte val = sendCommandByte("SO");

		return SleepOptionsEnum.valueOf(val);
	}
		
	public void setSleepOptiones(SleepOptionsEnum value) throws Exception 
	{
        if (m_deviceType == XSTICK)
            throw new Exception("Not supported on the XStick");

        sendCommand("SO", value.Value()); 
    }
			
	public short getWakeHost() throws Exception
	{
        if (m_deviceType == XSTICK)
            throw new Exception("Not supported on the XStick");

		short ret = 0;
		
		try
		{
			ret = sendCommandShort("WH");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}

	public void setWakeHost(short value)  throws Exception
    {
        if (m_deviceType == XSTICK)
            throw new Exception("Not supported on the XStick");

        sendCommand("WH", value); 
	}
					
	public short getPollingRate() throws Exception
	{
        if (m_deviceType == XSTICK)
            throw new Exception("Not supported on the XStick");

		short ret = 0;
		
		try
		{
			ret = sendCommandShort("PO");
		}
		catch (Exception e) 
		{
		}
		
		return ret;
	}

	public void setPollingRate(short value) throws Exception
    {
        if (m_deviceType == XSTICK)
            throw new Exception("Not supported on the XStick");

        sendCommand("PO", value); 
    }

	public void applyChanges()
	{
		sendCommand("AC");
	}
			
	public void write()
	{
		sendCommand("WR");
	}
			
	public void restoreDefaults()
	{
		sendCommand("RE");
	}
			
	public void softwareReset()
	{
		sendCommand("FR");
	}
			
	public void networkReset(Boolean resetAll)
	{
		if (resetAll)
			sendCommand("NR", (byte) 1);
		else
			sendCommand("NR", (byte) 0);
	}
	
	public void sleepImmediately() throws Exception
	{
        if (m_deviceType == XSTICK)
            throw new Exception("Not supported on the XStick");

		sendCommand("SI");
	}
			
	public void commissioningPushbutton()
	{
		sendCommand("CB");
	}

    public void destinationNode(String identifier)
	{
		sendCommand("DN", identifier);
	}
			
	public void forceSample()
	{
		sendCommand("IS");
	}
			
	public void XBeeSensorSample() throws Exception
	{
        if (m_deviceType == XSTICK)
            throw new Exception("Not supported on the XStick");

		sendCommand("1S");
	}
	
	// Methods
    @Override
	void connect() throws SerialException
    {
		super.connect();
		
        // gather values which are invariant
        m_serialNumber = getSerialNumber();
        m_firmwareVersion = getFirmwareVersion();
        m_hardwareVersion = getHardwareVersion();
        m_deviceType = getDeviceType();        
    }

    public void getAttention()
    {
        if (m_active)
        {
        	try
        	{
	            m_bufferLength = 0;
	            //m_buffer = "";
	            
	            byte[] plus = new byte[1];
	            plus[0] = '+'; 
	
	            try 
	            {
	    			m_outputStream.write(plus);
	    			
	                Thread.sleep(50);
	            	
	                m_outputStream.write(plus);
	    			
		            Thread.sleep(50);
		            
		            m_outputStream.write(plus);
	    		} 
	            catch (IOException e) 
	            {
	    		}

	            
	            while(m_buffer[0] != 'O' && m_buffer[1] != 'K' && m_buffer[2] != '\r')
	            {
	                //System.Windows.Forms.Application.DoEvents();
	                Thread.sleep(100);
	            }
        	} 
        	catch (InterruptedException e) 
        	{
			}
        }
    }

	@Override
    public byte[] sendCommand(String command, byte data)
    {
        if (!m_active)
            return null;

        if (m_apiEnable == APIEnableEnum.Disabled)
        {
            String str = Integer.toHexString((int) data);
            
            return sendCommand(command + str);
        }
        else
        {
			ATCommandFrame acf = new ATCommandFrame(command, data, this);
			
			return sendFrame(acf);
        }
    }

	@Override
    public byte sendCommandByte(String command) throws Exception
    {
        byte data = 0;

        if (m_active)
        {
	        if (m_apiEnable == APIEnableEnum.Disabled)
	        {
	            byte[] response = sendCommand(command);
	
	            String ret = "0x" + bytesToString(response);
	            
	            data = Byte.decode(ret);
	        }
	        else
	        {
				ATCommandFrame acf = new ATCommandFrame(command, this);
				
				sendFrame(acf);
				
				// wait for response
	            byte[] response = waitForResponseFrame(acf.FrameID);
	            
	            // if a response was not received then throw an exception
	            if (response != null && response.length != 0)
	                data = response[response.length - 1];
	            else
	                throw new Exception("Empty Response!");
	        }
        }

        return data;
    }
    
	@Override
    public short sendCommandShort(String command) throws Exception
    {
        short data = 0;

        if (m_active)
        {
	        if (m_apiEnable == APIEnableEnum.Disabled)
	        {
	            byte[] response = sendCommand(command);
	
	            String ret = "0x" + bytesToString(response);
	
	            data = Short.decode(ret);
	        }
	        else
	        {
				ATCommandFrame acf = new ATCommandFrame(command, this);
				
				sendFrame(acf);
				
				// wait for response
	            byte[] response = waitForResponseFrame(acf.FrameID);
	
	            // if a response was not received then throw an exception
	            if (response != null && response.length == 2)
                    data = BitConverter.GetShort(response);
                else
	                throw new Exception("Empty response!");
	        }
        }
        
        return data;
    }

	@Override
    public int sendCommandInt(String command) throws Exception
    {
        int data = 0;

        if (m_active)
        {
	        if (m_apiEnable == APIEnableEnum.Disabled)
	        {
	            byte[] response = sendCommand(command);
	
	            String val = "0x" + bytesToString(response);
	
	            data = Integer.decode(val);
	        }
	        else
	        {
				ATCommandFrame acf = new ATCommandFrame(command, this);
				
				sendFrame(acf);
				
				// wait for response
	            byte[] response = waitForResponseFrame(acf.FrameID);
	
	            // if a response was not received then throw an exception
	            if (response != null && response.length == 4)
	                data = BitConverter.GetInt(response);
	            else
	                throw new Exception("Empty Response!");
	        }
        }
        
        return data;
    }

	@Override
    public long sendCommandLong(String command) throws Exception
    {
        long data = 0;

        if (m_active)
        {
	        if (m_apiEnable == APIEnableEnum.Disabled)
	        {
	            byte[] response = sendCommand(command);
	
	            String ret = "0x" + bytesToString(response);
	
	            data = Long.decode(ret);
	        }
	        else
	        {
				ATCommandFrame acf = new ATCommandFrame(command, this);
				
				sendFrame(acf);
				
				// wait for response
	            byte[] response = waitForResponseFrame(acf.FrameID);
	
	            // if a response was not received then throw an exception
	            if (response != null && response.length == 8)
	                data = BitConverter.GetLong(response);
	            else
	                throw new Exception("Empty Response!");
	        }
        }
        
        return data;
    }

	
	@Override
    public String sendCommandString(String command)
    {
        String data = null;

        if (m_active)
        {
	        if (m_apiEnable == APIEnableEnum.Disabled)
	        {
	            byte[] response = sendCommand(command);
	
	            data = bytesToString(response);
	        }
	        else
	        {
				ATCommandFrame acf = new ATCommandFrame(command, this);
				
				sendFrame(acf);
				
				// wait for response
	            byte[] response = waitForResponseFrame(acf.FrameID);
	
	            // if a response was not received then throw an exception
	            if (response != null)
	                data = bytesToString(response);
	            else
	                data = "";
	        }
        }
        
        return data;
    }


	@Override
    public byte[] sendCommand(String command, short data)
    {
        if (!m_active)
            return null;

        if (m_apiEnable == APIEnableEnum.Disabled)
        {
        	String str = Integer.toHexString((int) data); 

            return sendCommand(command + str);
        }
        else
        {
			ATCommandFrame acf = new ATCommandFrame(command, data, this);
			
			return sendFrame(acf);
        }
    }

	@Override
    public byte[] sendCommand(String command, int data)
    {
        if (!m_active)
            return null;

        if (m_apiEnable == APIEnableEnum.Disabled)
        {
        	String str = Integer.toHexString(data); 

            return sendCommand(command + str);
        }
        else
        {
			ATCommandFrame acf = new ATCommandFrame(command, data, this);
			
			return sendFrame(acf);
        }
    }

	@Override
	public byte[] sendCommand(String command, long data)
    {
        if (!m_active)
            return null;

        if (m_apiEnable == APIEnableEnum.Disabled)
        {
        	String str = Long.toHexString(data);

            return sendCommand(command + str);
        }
        else
        {
			ATCommandFrame acf = new ATCommandFrame(command, data, this);
			
			return sendFrame(acf);
        }
    }

	
	@Override
    public byte[] sendCommand(String command, String data)
    {
        if (!m_active)
            return null;

        if (m_apiEnable == APIEnableEnum.Disabled)
        {
            return sendCommand(command + data);
        }
        else
        {
			ATCommandFrame acf = new ATCommandFrame(command, data, this);
			
			return sendFrame(acf);
        }
	}

	@Override
    public byte[] sendCommand(String command)
    {
    	try
    	{
	        if (m_active)
	        {
	            if (m_apiEnable == APIEnableEnum.Disabled)
		        {
		            m_bufferLength = 0;
		            //m_buffer = "";
		
		            String s = "AT" + command + "\r";
		            
		            m_outputStream.write(s.getBytes());
	    			
		            // wait for a response
		            Thread.sleep(100);
		
		            if (m_bufferLength == 0)
		            {
		                // try to enter AT mode
		                Thread.sleep(2000);
		
		                getAttention();
		
		                m_bufferLength = 0;
		                //m_buffer = "";
		
		                m_outputStream.write(s.getBytes());
		    			
		                // wait for a response
		                Thread.sleep(100);
		            }
		        }
		        else
		        {
		            if (command.length() < 2)
		            {
		                return new byte[0];
		            }
		
					ATCommandFrame acf = new ATCommandFrame(command, this);
				
					return sendFrame(acf);
		        }
	        }
    	} 
    	catch (InterruptedException e) 
    	{
		} 
    	catch (IOException e) 
    	{
		}
    	
    	return m_buffer;
    }

	
	@Override
	public byte[] sendRemoteCommand(short destinationNetworkAddress, long destinationSerialNumber, String command, String data)
	{
	    if (!m_active)
            return null;

        if (m_apiEnable == APIEnableEnum.Disabled)
        {
			// TODO
            //return sendRemoteCommand(command + data);
			return null;
        }
        else
        {
			RemoteATCommandRequestFrame racrf = new RemoteATCommandRequestFrame(this);
			
			racrf.AutoGenerateFrameID = false;
			racrf.FrameID = 0;
			racrf.ApplyChanges = true;
			racrf.DisableACK = true;
			racrf.UseExtendedTimeout = false;
			racrf.DestinationAddress = destinationSerialNumber;
			racrf.DestinationNetworkAddress = destinationNetworkAddress;
			racrf.Command = command;
			racrf.Data = data.getBytes();
			
			return sendFrame(racrf);
        }
	}
}
