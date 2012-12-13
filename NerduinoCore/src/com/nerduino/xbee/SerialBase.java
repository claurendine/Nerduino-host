package com.nerduino.xbee;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.sql.rowset.serial.SerialException;


public class SerialBase implements SerialPortEventListener
{
    public byte NextFrameID = 0;

    // Definitions
	
	
    // Events
    Vector<FrameReceivedListener> frameReceivedListeners = new Vector<FrameReceivedListener>();
    Vector<PacketReceivedListener> packetReceivedListeners = new Vector<PacketReceivedListener>();
    Vector<DataReceivedListener> dataReceivedListeners = new Vector<DataReceivedListener>();
    
    public void addFrameReceivedListener(FrameReceivedListener listener)
    {
    	if (!frameReceivedListeners.contains(listener))
            frameReceivedListeners.add(listener);
    }
    
    public void removeFrameReceivedListener(FrameReceivedListener listener)
    {
    	if (frameReceivedListeners.contains(listener))
            frameReceivedListeners.remove(listener);
    }
    
    public void addPacketReceivedListener(PacketReceivedListener listener)
    {
    	if (!packetReceivedListeners.contains(listener))
            packetReceivedListeners.add(listener);    	
    }
    
    public void removePacketReceivedListener(PacketReceivedListener listener)
    {
    	if (packetReceivedListeners.contains(listener))
            packetReceivedListeners.remove(listener);    	
    }
    
    public void addDataReceivedListener(DataReceivedListener listener)
    {
    	if (!dataReceivedListeners.contains(listener))
            dataReceivedListeners.add(listener);    	
    }
    
    public void removeDataReceivedListener(DataReceivedListener listener)
    {
    	if (dataReceivedListeners.contains(listener))
            dataReceivedListeners.remove(listener);    	
    }
	
    protected void onModemStatus(ModemStatusEnum modemStatus)
    {
//        if (ModemStatus != null)
//            ModemStatus(modemStatus, EventArgs.Empty);
    }

    
    protected void onFrameReceived(ZigbeeFrame frame)
    {
    	if (frameReceivedListeners != null && frameReceivedListeners.size() > 0)
    	{
    		for(FrameReceivedListener listener : frameReceivedListeners)
    		{
    			listener.frameReceived(frame);
    		}
    	}
    }
    
    protected void onDataReceived(byte[] data)
    {
    	if (dataReceivedListeners.size() > 0)
    	{
    		DataReceivedEvent e = new DataReceivedEvent();
    		
    		e.Data = data;
    		
    		for(DataReceivedListener listener : dataReceivedListeners)
    		{
    			listener.dataReceived(e);
    		}
    	}
    
    }

    protected void onPacketReceived(Packet packet)
    {
    	if (packetReceivedListeners.size() > 0)
    	{
    		PacketReceivedEvent e = new PacketReceivedEvent();
    		
    		e.Packet = packet;
    		
    		for(PacketReceivedListener listener : packetReceivedListeners)
    		{
    			listener.packetReceived(e);
    		}
    	}

    }

    protected void onReceiveZigbeeReceivePacket(ZigbeeReceivePacketFrame frame)
    {
 //       if (ReceiveZigbeeReceivePacket != null)
 //           ReceiveZigbeeReceivePacket(frame, EventArgs.Empty);
    }
 
    // Declarations
    //byte m_nextFrameID = 1;

    Object[] m_responses = new Object[256];
    ZigbeeFrameWithResponse[] m_responseFrames = new ZigbeeFrameWithResponse[256];
    
    Boolean m_enabled = false;
    
    byte[] m_buffer = new byte[4096];
    int m_bufferLength;
    
    SerialPort m_port;
    InputStream m_inputStream;
    OutputStream m_outputStream;
    
    String m_comPort;
    int m_baudRate = 9600;
    int m_dataBits = SerialPort.DATABITS_8;
    int m_stopBits = SerialPort.STOPBITS_1;
    int m_parity = SerialPort.PARITY_NONE;
	
    Boolean m_active = false;
    
    byte m_responseFrameId;
    String m_responseCommand;
    byte m_responseStatus;
    byte[] m_responseData;
    int m_responseAddresHigh;
    int m_responseAddressLow;
    short m_responseSourceAddress;
	
	Boolean m_initialized;
    long m_serialNumber;
    short m_firmwareVersion;
    short m_hardwareVersion;
    int m_deviceType;
    
    byte[] m_outBuffer;
    
    java.io.ByteArrayOutputStream m_outMemoryStream = new java.io.ByteArrayOutputStream(); 
    java.io.ByteArrayInputStream m_inMemoryStream; // = new java.io.ByteArrayInputStream(m_memoryBuffer);

    int m_outBufferLength;
    byte[] m_inBuffer;

	
    // Constructors
    public SerialBase ()
    {
        m_outBuffer = new byte[256];
        m_inBuffer = new byte[256];
    }

    // Properties
    public String getComPort()
    {
        return m_comPort; 
    }
    
    public void setComPort(String value)
    { 
    	m_comPort = value;
    }

    public int getBaudRate()
    {
        return m_baudRate; 
    }
    
    public void setBaudRate(int value)
    { 
    	m_baudRate = value;
    }

    public int getDataBits()
    {
        return m_dataBits; 
    }
    
    public void setDataBits(int value)
    {
    	m_dataBits = value;
    }
    
    public Boolean getEnabled()
    {
        return m_enabled; 
    }
    
    public void setEnabled(Boolean value)
    {
        m_enabled = value;

        if (m_enabled)
        {
        	try
	        {
	            connect();
	        } 
        	catch (SerialException e) 
        	{
				m_enabled = false;
            }
        }
        else
            disconnect();
    }

	
	// Methods
    void connect() throws SerialException
    {
        // open the serial port
        //m_port.Close();

    	try 
    	{
            Enumeration<?> portList;
			portList = CommPortIdentifier.getPortIdentifiers();
    	    while (portList.hasMoreElements()) 
    	    {
    	        CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();

    	        if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL
						&& portId.getName().equals(m_comPort)) 
				{
				   m_port = (SerialPort)portId.open("xbee", 2000);
				   m_inputStream = m_port.getInputStream();
				   m_outputStream = m_port.getOutputStream();
				   m_port.setSerialPortParams(m_baudRate, m_dataBits, m_stopBits, m_parity);
				   m_port.addEventListener(this);
				   m_port.notifyOnDataAvailable(true);
				   m_port.notifyOnOutputEmpty(true);

				   break;
				}
    	    }
    	}
    	catch (PortInUseException e) 
    	{
    	      throw new SerialException("Serial port " + m_comPort + " already in use. Check no other program is using it.");
    	} 
    	catch (Exception e) 
    	{
    		m_port = null;
    		m_inputStream = null;
    		m_outputStream = null;
    		
    		throw new SerialException("Error opening serial port "+ m_comPort + ".");
    	}

	    if (m_port == null) 
	    	throw new SerialException("Serial port '" + m_comPort + "' not found.");
    	
    	m_initialized = false;
        
        m_active = true;
        
        m_initialized = true;        
    }

    void disconnect()
    {
        // close the serial port
    	try 
    	{
	      // do io streams need to be closed first?
	      if (m_inputStream != null) 
	    	  m_inputStream.close();

		  if (m_outputStream != null) 
	    	  m_outputStream.close();
	    } 
    	catch (Exception e) 
    	{
	    }
	    
        m_active = false;
        
    	m_inputStream = null;
    	m_outputStream = null;

	    try 
	    {
	    	if (m_port != null) 
	    		m_port.close();  // close the port
	    } 
	    catch (Exception e) 
	    {
	    }

	    m_port = null;
    }


    public byte[] sendString(String command)
    {
    	try
    	{
	        if (m_active)
	        {
	            m_bufferLength = 0;
	
	            try 
	            {
	    			m_outputStream.write(command.getBytes(), 0, command.length());
	    		} 
	            catch (IOException e) 
	            {
	    		}
	
	            // wait for a response
	            Thread.sleep(100);
	
	            return m_buffer;	        	
	        }
    	} 
    	catch (InterruptedException e) 
    	{
		}
    	
   		return new byte[0];
    }

    public byte[] sendData(byte[] data)
    {
    	try
    	{
		    if (m_active)
	        {
		        try 
		        {
					m_outputStream.write(data, 0, data.length);
				} 
		        catch (IOException e) 
		        {
				}
	
	            // wait for a response
	            Thread.sleep(100);
	
	            return m_buffer;
	        }
    	}
    	catch (InterruptedException e) 
    	{
		}    	

    	return new byte[0];
    }

    
	public byte[] sendFrame(ZigbeeFrame frame)
	{
        if (!m_active)
            return null;

        int oBufferLength = frame.Send();

        try 
        {
			m_outputStream.write(frame.Buffer, 0, oBufferLength);
		} 
        catch (IOException e) 
        {
 		}

        return m_buffer;
	}

    public byte[] sendFrame(ZigbeeFrame frame, byte frameID)
    {
        if (!m_active)
            return null;

        int oBufferLength = frame.Send(frameID);

        try 
        {
			m_outputStream.write(frame.Buffer, 0, oBufferLength);
		} 
        catch (IOException e) 
        {
		}

        return m_buffer;
    }
	
    public byte[] sendCommand(String command, byte data)
    {
        if (!m_active)
            return null;

		/*
        if (m_apiEnable == APIEnableEnum.Disabled)
        {
            String str = Integer.toHexString((int) data);
            
            return sendCommand(command + str);
        }
        else
		*/
        {
			ATCommandFrame acf = new ATCommandFrame(command, data, this);
			
			return sendFrame(acf);
        }
    }

    public byte sendCommandByte(String command) throws Exception
    {
        byte data = 0;

        if (m_active)
        {
			/*
	        if (m_apiEnable == APIEnableEnum.Disabled)
	        {
	            byte[] response = sendCommand(command);
	
	            String ret = "0x" + bytesToString(response);
	            
	            data = Byte.decode(ret);
	        }
	        else
			*/
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
    
    public short sendCommandShort(String command) throws Exception
    {
        short data = 0;

        if (m_active)
        {
			/*
	        if (m_apiEnable == APIEnableEnum.Disabled)
	        {
	            byte[] response = sendCommand(command);
	
	            String ret = "0x" + bytesToString(response);
	
	            data = Short.decode(ret);
	        }
	        else
			*/
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

    public int sendCommandInt(String command) throws Exception
    {
        int data = 0;

        if (m_active)
        {
			/*
	        if (m_apiEnable == APIEnableEnum.Disabled)
	        {
	            byte[] response = sendCommand(command);
	
	            String val = "0x" + bytesToString(response);
	
	            data = Integer.decode(val);
	        }
	        else
			*/
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

    public long sendCommandLong(String command) throws Exception
    {
        long data = 0;

        if (m_active)
        {
			/*
	        if (m_apiEnable == APIEnableEnum.Disabled)
	        {
	            byte[] response = sendCommand(command);
	
	            String ret = "0x" + bytesToString(response);
	
	            data = Long.decode(ret);
	        }
	        else
			*/
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

	
    public String sendCommandString(String command)
    {
        String data = null;

        if (m_active)
        {
			/*
	        if (m_apiEnable == APIEnableEnum.Disabled)
	        {
	            byte[] response = sendCommand(command);
	
	            data = bytesToString(response);
	        }
	        else
			*/
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

    String bytesToString(byte[] data)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < data.length; i++)
        {
            sb.append((char) data[i]);
        }

        return sb.toString();
    }

    byte[] waitForResponseFrame(byte id)
    {   
        int i = 0;
        
        try
        {
            while (m_responses[id] == null)
            {
                Thread.sleep(1);
                //Application.DoEvents();
                i++;

                if (i > 1000)
                    break;
            }

            byte[] response = (byte[]) m_responses[id];

            m_responses[id] = null;

            return response;
        }
        catch (InterruptedException e) 
    	{
		}
        
        return null;
    }

    public byte[] sendCommand(String command, short data)
    {
        if (!m_active)
            return null;

		/*
        if (m_apiEnable == APIEnableEnum.Disabled)
        {
        	String str = Integer.toHexString((int) data); 

            return sendCommand(command + str);
        }
        else
		*/
        {
			ATCommandFrame acf = new ATCommandFrame(command, data, this);
			
			return sendFrame(acf);
        }
    }

    public byte[] sendCommand(String command, int data)
    {
        if (!m_active)
            return null;

		/*
        if (m_apiEnable == APIEnableEnum.Disabled)
        {
        	String str = Integer.toHexString(data); 

            return sendCommand(command + str);
        }
        else
		*/
        {
			ATCommandFrame acf = new ATCommandFrame(command, data, this);
			
			return sendFrame(acf);
        }
    }

	public byte[] sendCommand(String command, long data)
    {
        if (!m_active)
            return null;

		/*
        if (m_apiEnable == APIEnableEnum.Disabled)
        {
        	String str = Long.toHexString(data);

            return sendCommand(command + str);
        }
        else
		*/
        {
			ATCommandFrame acf = new ATCommandFrame(command, data, this);
			
			return sendFrame(acf);
        }
    }

	
    public byte[] sendCommand(String command, String data)
    {
        if (!m_active)
            return null;

		/*
        if (m_apiEnable == APIEnableEnum.Disabled)
            return sendCommand(command + data);
        else
		*/
        {
			ATCommandFrame acf = new ATCommandFrame(command, data, this);
			
			return sendFrame(acf);
        }
	}

    public byte[] sendCommand(String command)
    {
		if (m_active)
		 {
			 /*
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
			 */
			 {
				 if (command.length() < 2)
				 	 return new byte[0];
				 
				 ATCommandFrame acf = new ATCommandFrame(command, this);

				 return sendFrame(acf);
			 }
		 }
	 	
    	return m_buffer;
    }

    public void parseAPIResponse()
    {
        while (m_bufferLength > 0)
        {
            // scan the buffer for the beginning of a frame
            int start = -1;
            
            for (int i = 0; i < m_bufferLength; i++)
            {
                if (m_buffer[i] == (byte)0x7e)
                {
                    start = i;
                    break;
                }
            }
            
            if (start < 0) // there is no frame data, so kick out
                return;

            if (start > 0) // strip off the data ahead of the frame
            {
                for (int i = start; i < m_bufferLength; i++)
                {
                    m_buffer[i - start] = m_buffer[i];
                }

                m_bufferLength -= start;
            }
            
            // attempt to read the frame to see if it is complete
            if (m_bufferLength < 5) // not enough data to be a complete frame, so kick out
                return;
            
            // read the message length
            byte msb = m_buffer[1];
            byte lsb = m_buffer[2];
            
            int length = 0x100 * msb + lsb;

            if (m_bufferLength < length + 4) // length + frame marker + message length + checksum
                return; // incomplete frame, so kick out and wait till the buffer is full
            
            // analyze checksum
            byte checksum = -1; //255;
            byte[] frameData = new byte[length];

            for (int i = 0; i < length; i++)
            {
                frameData[i] = m_buffer[3 + i];
                checksum -= frameData[i];
            }

            byte readChecksum = (byte)m_buffer[length + 3];

            // if the checksum does not match then skip the frame

            // it looks like each returned frame includes a '?' at the end of the frame instead of a chechksum

            if (checksum == readChecksum)
                processFrame(frameData);
            
            // remove this frame from the buffer
            start = length + 4;

            if (m_bufferLength > start)
            {
                for (int i = start; i < m_bufferLength; i++)
                {
                    m_buffer[i - start] = m_buffer[i];
                }

                m_bufferLength -= start;
            }
            else
                m_bufferLength = 0;
        }
    }

    void onAtCommandResponse(byte frameid, byte[] response)
    {
        m_responses[frameid] = response;
        
        if (m_responseFrames[frameid] != null)
            m_responseFrames[frameid].OnResponse(response);
    }

    public void processFrame(byte[] data)
    {
        FrameTypeEnum ftype = FrameTypeEnum.valueOf(data[0]);
        
        ZigbeeFrame frame = null;

        switch (ftype)
        {
            case ATCommand:
                {
                    ATCommandFrame newframe = new ATCommandFrame(this);

                    frame = newframe;
                    frame.ReadFrame(data); // data.length);
                }
                break;
            case ATCommandQueue:
                {
                    ATCommandQueueFrame newframe = new ATCommandQueueFrame(this);

                    frame = newframe;
                    frame.ReadFrame(data); // data.length);
                }
                break;
            case TransmitRequest:
                {
                    TransmitRequestFrame newframe = new TransmitRequestFrame(this);

                    frame = newframe;
                    frame.ReadFrame(data); // data.length);
                }
                break;
            case ExplicitAddressingZigbeeCommand:
                {
                    ExplicitAddressingZigbeeCommandFrame newframe = new ExplicitAddressingZigbeeCommandFrame(this);

                    frame = newframe;
                    frame.ReadFrame(data);
                }
                break;
            case RemoteATCommandRequest:
                {
                    RemoteATCommandRequestFrame newframe = new RemoteATCommandRequestFrame(this);

                    frame = newframe;
                    frame.ReadFrame(data);
                }
                break;
            case CreateSourceRoute:
                {
                    CreateSourceRouteFrame newframe = new CreateSourceRouteFrame(this);

                    frame = newframe;
                    frame.ReadFrame(data);
                }
                break;
            case ATCommandResponse:
                {
                    ATCommandResponseFrame newframe = new ATCommandResponseFrame(this);

                    frame = newframe;
                    frame.ReadFrame(data);

                    onAtCommandResponse(newframe.FrameID, newframe.Data);
                }
                break;
            case ModemStatus:
                {
                    ModemStatusFrame newframe = new ModemStatusFrame(this);

                    frame = newframe;
                    frame.ReadFrame(data);

                    onModemStatus(newframe.Status);
                }
                break;
            case ZigbeeTransmitStatus:
                {
                    ZigbeeTransmitStatusFrame newframe = new ZigbeeTransmitStatusFrame(this);

                    frame = newframe;
                    frame.ReadFrame(data);
                }
                break;
            case ZigbeeReceivePacket:
                {
                    ZigbeeReceivePacketFrame newframe = new ZigbeeReceivePacketFrame(this);

                    frame = newframe;
                    frame.ReadFrame(data);

                    onReceiveZigbeeReceivePacket(newframe);
                }
                break;
            case ZigbeeExplicitRxIndicator:
                {
                    ZigbeeExplicitRxIndicatorFrame newframe = new ZigbeeExplicitRxIndicatorFrame(this);

                    frame = newframe;
                    frame.ReadFrame(data);
                }
                break;
            case ZigbeeIODataSampleRxIndicator:
                {
                    ZigbeeIODataSampleRxIndicatorFrame newframe = new ZigbeeIODataSampleRxIndicatorFrame(this);

                    frame = newframe;
                    frame.ReadFrame(data);
                }
                break;
            case XBeeSensorReadIndicator:
                {
                    XBeeSensorReadIndicatorFrame newframe = new XBeeSensorReadIndicatorFrame(this);

                    frame = newframe;
                    frame.ReadFrame(data);
                }
                break;
            case NodeIdentificationIndicator:
                {
                    NodeIdentificationIndicatorFrame newframe = new NodeIdentificationIndicatorFrame(this);

                    frame = newframe;
                    frame.ReadFrame(data);
                }
                break;
            case RemoteCommandResponse:
                {
                    RemoteCommandResponseFrame newframe = new RemoteCommandResponseFrame(this);

                    frame = newframe;
                    frame.ReadFrame(data);

                    onAtCommandResponse(newframe.FrameID, newframe.Data);
                }
                break;
            case ReceivePacket64BitAddress:
                {
                    Packet packet = new Packet();
                    
                    packet.AddressHigh = BitConverter.GetInt(data, 1);
                    packet.AddressLow = BitConverter.GetInt(data, 4);
                    
                    packet.RSSI = data[8];
                    packet.Options = data[9];
                    
                    packet.Data = new byte[data.length - 10];
                    
                    for (int i = 0; i < packet.Data.length; i++)
                    {
                        packet.Data[i] = data[i + 10];
                    }
                    
                    onPacketReceived(packet);
                }
                
                break;
            case ReceivePacket16BitAddress:
                {
                    Packet packet = new Packet();

                    packet.AddressSource = BitConverter.GetShort(data, 1);
                    
                    packet.RSSI = data[3];
                    packet.Options = data[4];

                    packet.Data = new byte[data.length - 5];

                    for (int i = 0; i < packet.Data.length; i++)
                    {
                        packet.Data[i] = data[i + 5];
                    }

                    onPacketReceived(packet);
                }
                
                break;
            case OverTheAirFirmwareUpdateStatus:
                {
                    OverTheAirFirmwareUpdateStatusFrame newframe = new OverTheAirFirmwareUpdateStatusFrame(this);

                    frame = newframe;
                    frame.ReadFrame(data);
                }
                break;
            case RouteRecordIndicator:
                {
                    RouteRecordIndicatorFrame newframe = new RouteRecordIndicatorFrame(this);

                    frame = newframe;
                    frame.ReadFrame(data);
                }
                break;
            case ManyToOneRouteRequestIndicator:
                {
                    ManyToOneRouteRequestIndicatorFrame newframe = new ManyToOneRouteRequestIndicatorFrame(this);

                    frame = newframe;
                    frame.ReadFrame(data);
                }
                break;
            default:
                break;
        }

        if (frame != null)
            onFrameReceived(frame);
    }

    public void transmitRequest(long address, short networkAddress, TransmitRequestOptionEnum options, byte[] data)
    {
		TransmitRequestFrame trf = new TransmitRequestFrame(this);

        trf.DestinationAddress = address;
        trf.DestinationNetworkAddress = networkAddress;
        trf.Options = options;
		trf.Data = data;
        
		sendFrame(trf);
    }

    public Boolean reserveFrameID(ZigbeeFrameWithResponse frame, byte frameID)
    {
        if (m_responseFrames[frameID] != null)
            return false; // already reserved

        m_responseFrames[frameID] = frame;

        return true;
    }

    public void releaseFrameID(byte frameID)
    {
        m_responseFrames[frameID] = null;
    }

	public byte getNextFrameID() 
	{
		return 1;
	}

	@Override
	public void serialEvent(SerialPortEvent event) 
	{
		switch(event.getEventType()) 
		{
        	case SerialPortEvent.BI:
        	case SerialPortEvent.OE:
        	case SerialPortEvent.FE:
        	case SerialPortEvent.PE:
        	case SerialPortEvent.CD:
        	case SerialPortEvent.CTS:
        	case SerialPortEvent.DSR:
        	case SerialPortEvent.RI:
        	case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
        		break;
        	case SerialPortEvent.DATA_AVAILABLE:
        		try 
        		{
        			int dataLength = m_inputStream.available();

        	        while (dataLength > 0)
        	        {
        	        	byte[] data = new byte[dataLength];

        	        	m_inputStream.read(data);

						/*
        	        	if (m_apiEnable == APIEnableEnum.Disabled)
        	                onDataReceived(data);
        	            else
						*/
        	        	{
        	        		// append data to the receive buffer
        	        		for (int i = 0; i < dataLength; i++)
        	        		{
        	        			m_buffer[m_bufferLength++] = data[i];
        	        		}

        	        		parseAPIResponse();
        	        	}
                        
                        dataLength = m_inputStream.available();
        	        }
        		} 
        		catch (IOException e) {}
        		
        		break;
        }
    }
	
	public byte[] sendRemoteCommand(short destinationNetworkAddress, long destinationSerialNumber, String command, String data)
	{
	    if (!m_active)
            return null;

		/*
        if (m_apiEnable == APIEnableEnum.Disabled)
        {
			// TODO
            //return sendRemoteCommand(command + data);
			return null;
        }
        else
		*/
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
