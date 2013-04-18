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

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.sql.rowset.serial.SerialException;


public class Serial extends SerialBase implements SerialPortEventListener
{    
    SerialPort m_port;
    InputStream m_inputStream;
    OutputStream m_outputStream;
    
    String m_comPort;
    int m_baudRate = 9600;
    int m_dataBits = SerialPort.DATABITS_8;
    int m_stopBits = SerialPort.STOPBITS_1;
    int m_parity = SerialPort.PARITY_NONE;
	
	
    // Constructors
    public Serial ()
    {
		m_active = false;
		
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
    
	@Override
    public Boolean getEnabled()
    {
        return m_enabled; 
    }
    
	@Override
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

	@Override
	public void writeData(byte[] data, int length)
	{
		try 
		{
			m_outputStream.write(data, 0, length);
		} 
		catch (IOException e) 
		{
		}
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
						
						serialReceived(data, dataLength);
						
                        dataLength = m_inputStream.available();
        	        }
        		} 
        		catch (IOException e) {}
        		
        		break;
        }
    }
}
