package com.nerduino.library;

import com.nerduino.xbee.*;
import java.awt.Image;
import java.util.*;
import java.util.logging.*;
import javax.swing.ImageIcon;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NerduinoZigbee extends NerduinoBase
{

    // Definitions
    class Callback
    {
        public byte type;
        public Boolean waiting;
        public Date startTime;
        //public double startTime;
        public MessageEnum id;
        public int timeout;
        public Boolean success;
        public int dataLength;
        public byte[] data;
    }


    // Declarations
    static double NERDUINO_TIMOUT = 30.0;
    
    short m_networkAddress;
    long m_serialNumber;
    short m_parentAddress;
    byte m_signalStrength;

    //public List<RemoteDataPoint> m_points = new ArrayList<RemoteDataPoint>();
    //List<CommandResponse> m_executeResponses = new ArrayList<CommandResponse>();
    CommandResponse m_executeResponse = null;
    RemoteDataPoint m_pointResponse = null;

    Callback m_callback;

	
    // Constructors
    public NerduinoZigbee()
    {
        super("Nerd", "/NerduinoHostApp/resources/Nerduino16.png");
        
    	//touch();
    }

	// Methods
	public void touch()
	{
		m_lastResponseMillis = System.currentTimeMillis();
		
		if (m_status != NerduinoStatusEnum.Online)
			setStatus(NerduinoStatusEnum.Online);
	}
    
	    
    public void setConfigurationToken(byte configurationToken)
    {
    	if (m_configurationToken != configurationToken)
    	{
    		m_configurationToken = configurationToken;
    		
            getMetaData();
            
    		// get an updated property list
    		getPoints();
    		
    		// TODO notify callback that the configuration has changed
    	}
    }
    
    public void onGetMetaDataResponse(ZigbeeReceivePacketFrame zrf)
    {
    	touch();
    	
        //short responseToken = (short) (zrf.Data[1] * 0x100 + zrf.Data[2]);
        
		int i = 4;

		byte length = zrf.Data[i++];

		StringBuilder sb = new StringBuilder();

		for(int j = 0; j < length; j++)
		{
			sb.append((char) zrf.Data[i++]);
		}

		setName(sb.toString());

		setStatus(NerduinoStatusEnum.valueOf(zrf.Data[i++]));

		byte configurationToken = zrf.Data[i++];

		short count = BitConverter.GetShort(zrf.Data, i);
		i+=2;

		m_deviceType = DeviceTypeEnum.valueOf(zrf.Data[i++]);
		setConfigurationToken(configurationToken);
    }

    public void onPingResponse(ZigbeeReceivePacketFrame zrf)
    {
    	touch();
    	
    	//short responseToken = (short) (zrf.Data[1] * 0x100 + zrf.Data[2]);
    	byte status = zrf.Data[4];
    	byte configurationToken = zrf.Data[5];
    	
    	setStatus(NerduinoStatusEnum.valueOf(status));
    	setConfigurationToken(configurationToken);
    }

    public void onCheckin(ZigbeeReceivePacketFrame zrf)
    {
    	touch();
    	
        m_networkAddress = zrf.SourceNetworkAddress;
        
    	byte status = zrf.Data[2];
    	byte configurationToken = zrf.Data[3];
    	
		// TODO verify the status values.. probably includes a state for sleeping
		if (status == 0)
			setStatus(NerduinoStatusEnum.Online);
    	else
			setStatus(NerduinoStatusEnum.Distress);
		
		setConfigurationToken(configurationToken);
    }
    
    public void onExecuteCommandResponse(ZigbeeReceivePacketFrame zrf)
	{
/*
    	touch();
    	
		// place the response in the indexed response array and notify that a response has been received
    	short responseToken = BitConverter.GetShort(zrf.Data, 2);
    	
    	CommandResponse response = new CommandResponse();
    	
    	response.Status =  zrf.Data[4];
    	response.Length = zrf.Data[5];
    	response.Data = new byte[response.Length];
    	
    	for(int i = 0; i < response.Length; i++)
    		response.Data[i] = zrf.Data[6 + i];
    	
        m_executeResponse = response;
        
//    	if (responseToken < 0 || responseToken >= m_executeResponses.size())
//    	{
    		// no spot to store the response
//    		return;
//    	}
    	
//    	m_executeResponses.set(responseToken, response);
    	
    	// TODO notify that a response has been received.
*/
	}
    
    public void onGetPointResponse(ZigbeeReceivePacketFrame zrf)
    {
        touch();
        
        //short responseToken = (short)(zrf.Data[1] * 0x100 + zrf.Data[2]);
        short pointIndex = BitConverter.GetShort(zrf.Data, 4);
        
        if (pointIndex == -1)
        {
        	// remove any non-validated points
        	removeUnusedPoints();
        	
        	// TODO notify callback that the points list has been updated
        	return;
        }
        
        byte attributes = zrf.Data[6];
        DataTypeEnum dataType = DataTypeEnum.valueOf(zrf.Data[7]);
        byte dataLength = zrf.Data[8];
        byte status = zrf.Data[9];
        byte nameLength = zrf.Data[10];
        
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < nameLength; i++)
        {
            sb.append((char)zrf.Data[11 + i]);
        }
        
        String name = sb.toString();
        
        int offset = 11 + nameLength;

        Object value = NerduinoHost.parseValue(zrf.Data, offset, dataType, dataLength);

    	
        // look for an existing point with this pointid
        for(int i = 0; i < m_points.size(); i++)
        {
            RemoteDataPoint point = (RemoteDataPoint) m_points.get(i);
			
            if (point.Id == pointIndex)
            {
                // if it already exists then validate the point and update props
                point.Validated = true;

                point.Status = status;
                point.setName(name);
                point.DataType = dataType;
                point.DataLength = dataLength;
                point.Attributes = attributes;
	            point.setValue(value);

                m_pointResponse = point;
                
                return;
            }
        }

        RemoteDataPoint newpoint = new RemoteDataPoint(this, pointIndex, name, attributes, dataType, dataLength, value);
        newpoint.Validated = true;

        m_points.add(newpoint);
        
        m_pointResponse = newpoint;
    }

    public RemoteDataPoint getPointAt(int pointIndex)
    {
    	for(int i = 0; i< m_points.size(); i++)
        {
        	RemoteDataPoint rdp = (RemoteDataPoint) m_points.get(i);
        	
        	if (rdp != null && rdp.Id == pointIndex)
        		return rdp;
        }
        
        return null;
    }
    
    public void onGetPointValueResponse(ZigbeeReceivePacketFrame zrf)
    {
        touch();
        
        short responseToken = BitConverter.GetShort(zrf.Data, 2);
        short pointIndex = BitConverter.GetShort(zrf.Data, 4);
        
        RemoteDataPoint rdp = getPointAt(pointIndex);
        
    	if (rdp != null)
    	{
    		rdp.onGetPointValueResponse(zrf.Data);
    	}
    }

    public void removeUnusedPoints()
    {
        for (int i = m_points.size() - 1; i >= 0; i--)
        {
        	RemoteDataPoint point = (RemoteDataPoint) m_points.get(i);
        	
            if (point == null || !point.Validated)
                m_points.remove(i);
        }
    }

	@Override
    public void sendPing()
    {
		NerduinoHost.Current.sendPing(m_serialNumber, m_networkAddress,(short) 0);
    }
    
	@Override
    public void sendInitialize()
    {
    	NerduinoHost.Current.sendInitialize(m_serialNumber, m_networkAddress);
    }
    
	@Override
    public void sendGetMetaData()
    {
    	NerduinoHost.Current.sendGetMetaData(m_serialNumber, m_networkAddress,(short) 0);
    }
    
	/*
	@Override
    public void executeCommand(String command, short responseToken) 
    {
    	NerduinoHost.Current.sendExecuteCommand(m_serialNumber, m_networkAddress,(short) 1, (byte) command.length(), command.getBytes());
    }
    */
	
	@Override
    public void sendGetPoint(short index)
    {
    	byte[] data = new byte[2];
    	
    	data[0] = (byte)(index / 0x100);
        data[1] = (byte)(index & 0xff);
        
    	NerduinoHost.Current.sendGetPoint(m_serialNumber, m_networkAddress, (short) 0, (byte) 0, (byte) 2, data); 
    }
    
	@Override
    public void sendGetPoint(String name)
    {
    	NerduinoHost.Current.sendGetPoint(m_serialNumber, m_networkAddress, (short) 0, (byte) 1, (byte) name.length(), name.getBytes());     
    }
    
	@Override
    public void sendGetPointValue(short index)
    {
    	NerduinoHost.Current.sendGetPointValue(m_serialNumber, m_networkAddress, (short) 0, index);
    }
    
	@Override
	public void sendSetPointValue(short index, DataTypeEnum dataType, byte dataLength, Object m_value)
	{	
    	NerduinoHost.Current.sendSetPointValue(m_serialNumber, m_networkAddress, index, dataType, dataLength, m_value);
	}

	
	@Override
    public void sendUnregisterPointCallback(short index)
    {
    	NerduinoHost.Current.sendUnregisterPointCallback(m_serialNumber, m_networkAddress, index);
    }
	
	@Override
	public void sendGetDeviceStatusResponse(long serialNumber, short networkAddress, short responseToken)
	{
		NerduinoHost.Current.sendGetDeviceStatusResponse(serialNumber, networkAddress, responseToken, m_signalStrength, m_configurationToken, (short) getTimeSinceLastResponse());
	}

    
    public void getPoints()
    {
        // mark all of the existing points as waiting for validation
        for(PointBase point : m_points)
        {
        	if (point != null)
        		((RemoteDataPoint) point).Validated = false;
        }
        
        byte[] data = new byte[0];
        
    	NerduinoHost.Current.sendGetPoint(m_serialNumber, m_networkAddress, (short) 0, (byte) 1, (byte) 0, data);    	
    }
    
    @Override
    public RemoteDataPoint getPoint(String name)
    {
        byte length = (byte) name.length();
        
    	NerduinoHost.Current.sendGetPoint(m_serialNumber, m_networkAddress, (short) 0, (byte) 0, (byte) length, name.getBytes()); 
        
        m_pointResponse = null;
        
        for(int i = 0; i < 3000; i++)
        {
            try 
            {
                if (m_pointResponse != null)
                {
                    return m_pointResponse;
                }
                
                Thread.sleep(1);
            } 
            catch (InterruptedException ex) 
            {
                Logger.getLogger(NerduinoZigbee.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
 
        return null;
    }
    
    void waitForCommandResponse(MessageEnum command, int timeout)
    {
        m_callback.success = false;
        m_callback.timeout = timeout;
        m_callback.id = command;
        m_callback.type = 1;
        m_callback.waiting = true;
        m_callback.startTime = new Date();

        /*
        while (!m_callback.success && DateTime.Now m_callback.startTime)
        {
            DoEvents();
        }
        */
    }

	@Override
    public void getMetaData()
    {
    	// mark all existing points as invalid
        for (PointBase point : m_points)
        {
			((RemoteDataPoint) point).Validated = false;
        }
        
        // send metadata request to nerduino
        sendGetMetaData();
    }

	@Override
	public void checkStatus() 
	{
		// if the nerduino is offline, sleeping, or in distress, the nerduino will remain in this state
		// until it receives a message from the device
		
		if (getStatus() == NerduinoStatusEnum.Online)
		{
			if (getTimeSinceLastResponse() > NERDUINO_TIMOUT)
			{
				setStatus(NerduinoStatusEnum.Offline);
			}
		}
	}
	
	public void bootload(String hexFile)
	{
		// reprogram the nerduino with the specified hex file
		
		// TODO
		
		// trigger a reset on the nerduino
		// follow the STK500 v2 protocol to validate the hardware and then upload and validate the hex file	
	}
	
	@Override
	public Image getIcon(int type)
	{
        java.net.URL imgURL = null;
		
		switch(getStatus())
		{
			case Uninitialized:
				imgURL = getClass().getResource("/NerduinoHostApp/resources/NerduinoUninitialized16.png");
				break;
			case Online:
				imgURL = getClass().getResource("/NerduinoHostApp/resources/NerduinoOnline16.png");
				break;
			case Offline:
				imgURL = getClass().getResource("/NerduinoHostApp/resources/NerduinoOffline16.png");
				break;
			case Sleeping:
				imgURL = getClass().getResource("/NerduinoHostApp/resources/NerduinoSleeping16.png");
				break;
			case Distress:
				imgURL = getClass().getResource("/NerduinoHostApp/resources/NerduinoDistress16.png");
				break;
		}
        
        if (imgURL != null) 
            return new ImageIcon(imgURL).getImage();
        else 
        {
//            System.err.println("Couldn't find file: " + imgURL.toString());
            return null;
        }
    }
	
	@Override
	public void readXML(Element node)
	{
		m_name = node.getAttribute("Name");
		String str = node.getAttribute("SerialNumber");
		
		m_serialNumber = Long.decode(str);
		
		setStatus(NerduinoStatusEnum.Offline);
		
		str = node.getAttribute("Configuration");

		if (str != null && str.length() > 0)
			m_configurationToken = Byte.decode(str);		
	}
	
	@Override
	public void writeXML(Document doc, Element element)
	{
		if (getStatus() != NerduinoStatusEnum.Uninitialized)
		{
			element.setAttribute("Name", m_name);
			element.setAttribute("SerialNumber", ((Long) m_serialNumber).toString());
			element.setAttribute("Configuration", ((Byte) m_configurationToken).toString());
			
			element.setAttribute("Type", "Zigbee");
		}
	}
}
