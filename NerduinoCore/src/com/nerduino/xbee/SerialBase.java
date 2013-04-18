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

import java.util.Vector;


public class SerialBase
{
    public byte NextFrameID = 0;
	public byte Protocol = 0; // 0 - zigbee, 1 = nerduino

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
    Object[] m_responses = new Object[256];
    ZigbeeFrameWithResponse[] m_responseFrames = new ZigbeeFrameWithResponse[256];
    
    Boolean m_enabled = false;
    
    byte[] m_buffer = new byte[4096];
	byte[] m_data = new byte[64];
    int m_bufferLength;
    
    Boolean m_active = true;
    
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

	// Methods
	public Boolean getEnabled()
	{
		return false;
	}

	public void setEnabled(Boolean value)
	{
	}

	
	public void writeData(byte[] data, int length)
	{
	}
    
	public byte[] sendFrame(ZigbeeFrame frame)
	{
        if (!m_active)
            return null;

        int oBufferLength = frame.Send();
		
		writeData(frame.Buffer, oBufferLength);
		
        return m_buffer;
	}

    public byte[] sendFrame(ZigbeeFrame frame, byte frameID)
    {
        if (!m_active)
            return null;

        int oBufferLength = frame.Send(frameID);
		
		writeData(frame.Buffer, oBufferLength);
		
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
			ATCommandFrame acf = new ATCommandFrame(command, data);
			
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
				ATCommandFrame acf = new ATCommandFrame(command);
				
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
				ATCommandFrame acf = new ATCommandFrame(command);
				
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
				ATCommandFrame acf = new ATCommandFrame(command);
				
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
				ATCommandFrame acf = new ATCommandFrame(command);
				
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
				ATCommandFrame acf = new ATCommandFrame(command);
				
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
			ATCommandFrame acf = new ATCommandFrame(command, data);
			
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
			ATCommandFrame acf = new ATCommandFrame(command, data);
			
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
			ATCommandFrame acf = new ATCommandFrame(command, data);
			
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
			ATCommandFrame acf = new ATCommandFrame(command, data);
			
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
				 
				 ATCommandFrame acf = new ATCommandFrame(command);

				 return sendFrame(acf);
			 }
		 }
	 	
    	return m_buffer;
    }
	
	public void parseNerduinoResponse()
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
            if (m_bufferLength < 4) // not enough data to be a complete frame, so kick out
                return;
            
            // read the message target byte
			byte target = m_buffer[1];
			
			if (target == 0 || target == 1)
			{
				// read the data length
				byte offset = 2;
				byte addressIndex = 0;
				
				if (target == 0)
					addressIndex = m_buffer[offset++];
				
				byte message = m_buffer[offset++];
				byte length = m_buffer[offset++];
				
				if (m_bufferLength < length + 4) // length + header 
				    return; // incomplete frame, so kick out and wait till the buffer is full
				
				// read the data
				for(int i = 0; i < length; i++)
				{
					m_data[i] =  m_buffer[offset++];
				}
				
	            processNerduinoMessage(target, addressIndex, message, length, m_data);
				
				start = length + 4;
			}
			else
			{
				// bad data, skip these bytes
				start = 2;
			}
            
            // remove this frame from the buffer
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
	
    public void processNerduinoMessage(byte target, byte addressIndex, byte message, byte length, byte[] m_data)
	{
		
	}

    public void processFrame(byte[] data)
    {
        FrameTypeEnum ftype = FrameTypeEnum.valueOf(data[0]);
        
        ZigbeeFrame frame = null;

        switch (ftype)
        {
            case ATCommand:
                {
                    ATCommandFrame newframe = new ATCommandFrame();

                    frame = newframe;
                    frame.ReadFrame(data); // data.length);
                }
                break;
            case ATCommandQueue:
                {
                    ATCommandQueueFrame newframe = new ATCommandQueueFrame();

                    frame = newframe;
                    frame.ReadFrame(data); // data.length);
                }
                break;
            case TransmitRequest:
                {
                    TransmitRequestFrame newframe = new TransmitRequestFrame();

                    frame = newframe;
                    frame.ReadFrame(data); // data.length);
                }
                break;
            case ExplicitAddressingZigbeeCommand:
                {
                    ExplicitAddressingZigbeeCommandFrame newframe = new ExplicitAddressingZigbeeCommandFrame();

                    frame = newframe;
                    frame.ReadFrame(data);
                }
                break;
            case RemoteATCommandRequest:
                {
                    RemoteATCommandRequestFrame newframe = new RemoteATCommandRequestFrame();

                    frame = newframe;
                    frame.ReadFrame(data);
                }
                break;
            case CreateSourceRoute:
                {
                    CreateSourceRouteFrame newframe = new CreateSourceRouteFrame();

                    frame = newframe;
                    frame.ReadFrame(data);
                }
                break;
            case ATCommandResponse:
                {
                    ATCommandResponseFrame newframe = new ATCommandResponseFrame();

                    frame = newframe;
                    frame.ReadFrame(data);

                    onAtCommandResponse(newframe.FrameID, newframe.Data);
                }
                break;
            case ModemStatus:
                {
                    ModemStatusFrame newframe = new ModemStatusFrame();

                    frame = newframe;
                    frame.ReadFrame(data);

                    onModemStatus(newframe.Status);
                }
                break;
            case ZigbeeTransmitStatus:
                {
                    ZigbeeTransmitStatusFrame newframe = new ZigbeeTransmitStatusFrame();

                    frame = newframe;
                    frame.ReadFrame(data);
                }
                break;
            case ZigbeeReceivePacket:
                {
                    ZigbeeReceivePacketFrame newframe = new ZigbeeReceivePacketFrame();

                    frame = newframe;
                    frame.ReadFrame(data);

                    onReceiveZigbeeReceivePacket(newframe);
                }
                break;
            case ZigbeeExplicitRxIndicator:
                {
                    ZigbeeExplicitRxIndicatorFrame newframe = new ZigbeeExplicitRxIndicatorFrame();

                    frame = newframe;
                    frame.ReadFrame(data);
                }
                break;
            case ZigbeeIODataSampleRxIndicator:
                {
                    ZigbeeIODataSampleRxIndicatorFrame newframe = new ZigbeeIODataSampleRxIndicatorFrame();

                    frame = newframe;
                    frame.ReadFrame(data);
                }
                break;
            case XBeeSensorReadIndicator:
                {
                    XBeeSensorReadIndicatorFrame newframe = new XBeeSensorReadIndicatorFrame();

                    frame = newframe;
                    frame.ReadFrame(data);
                }
                break;
            case NodeIdentificationIndicator:
                {
                    NodeIdentificationIndicatorFrame newframe = new NodeIdentificationIndicatorFrame();

                    frame = newframe;
                    frame.ReadFrame(data);
                }
                break;
            case RemoteCommandResponse:
                {
                    RemoteCommandResponseFrame newframe = new RemoteCommandResponseFrame();

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
                    OverTheAirFirmwareUpdateStatusFrame newframe = new OverTheAirFirmwareUpdateStatusFrame();

                    frame = newframe;
                    frame.ReadFrame(data);
                }
                break;
            case RouteRecordIndicator:
                {
                    RouteRecordIndicatorFrame newframe = new RouteRecordIndicatorFrame();

                    frame = newframe;
                    frame.ReadFrame(data);
                }
                break;
            case ManyToOneRouteRequestIndicator:
                {
                    ManyToOneRouteRequestIndicatorFrame newframe = new ManyToOneRouteRequestIndicatorFrame();

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
		TransmitRequestFrame trf = new TransmitRequestFrame();

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

	public void serialReceived(byte[] data, int dataLength) 
	{
		if (dataLength > 0)
		{
			// append data to the receive buffer
			for (int i = 0; i < dataLength; i++)
			{
				m_buffer[m_bufferLength++] = data[i];
			}
			
			switch(Protocol)
			{
				case 0: // zigbee
					parseAPIResponse();
					break;
				case 1: // nerduino
					parseNerduinoResponse();
					break;
			}
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
			RemoteATCommandRequestFrame racrf = new RemoteATCommandRequestFrame();
			
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
