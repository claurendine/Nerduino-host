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

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.openide.util.Exceptions;

//import javax.sql.rowset.serial.SerialException;


public class Serial extends SerialBase implements SerialPortEventListener
{    
    SerialPort m_port;
    
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

	boolean m_rts;
			
	
	public boolean getRTS()
	{
		return m_rts;
	}
	
	public void setRTS(boolean val)
	{
		try
		{
			m_rts = val;
			m_port.setRTS(val);
			m_port.setDTR(val);
		}
		catch(SerialPortException ex)
		{
			Exceptions.printStackTrace(ex);
		}
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
        	catch (Exception e) 
        	{
				m_enabled = false;
            }
        }
        else
            disconnect();
    }

	// Methods
    void connect() throws Exception
    {
		if (m_comPort == null || m_comPort.isEmpty())
			return;
		
		if (m_port == null)
			m_port = new SerialPort(m_comPort); 
		
		try 
		{
            m_port.openPort();//Open port
            m_port.setParams(m_baudRate, m_dataBits, m_stopBits, m_parity);//Set params
            m_port.addEventListener(this);//Add SerialPortEventListener
        }
        catch (SerialPortException ex) 
		{
    		throw new Exception("Error opening serial port "+ m_comPort + ".");
		}
		
	    if (m_port == null) 
	    	throw new Exception("Serial port '" + m_comPort + "' not found.");
    	
    	m_initialized = false;
        
        m_active = true;
        
        m_initialized = true;        
    }

    void disconnect()
    {
        // close the serial port
        m_active = false;
        
	    try 
	    {
			if (m_port != null)
			{
				m_port.removeEventListener();
				
				//m_port.addEventListener( null );
                
				m_port.closePort();
				//m_port = null;
			}
	    } 
	    catch (Exception e) 
	    {
	    }
    }

	@Override
	public void writeData(byte[] data, int length)
	{
		try 
		{
			if (data.length == length)
				m_port.writeBytes(data);
			else
			{
				byte[] subdata = new byte[length];
				
				System.arraycopy(data, 0, subdata, 0, length);

				m_port.writeBytes(subdata);
			}
		} 
		catch (SerialPortException e) 
		{
		}
	}

	@Override
	public void serialEvent(SerialPortEvent event) 
	{
		switch(event.getEventType()) 
		{
        	case SerialPortEvent.BREAK:
        	case SerialPortEvent.CTS:
        	case SerialPortEvent.DSR:
        	case SerialPortEvent.RING:
        	case SerialPortEvent.RLSD:
        	case SerialPortEvent.TXEMPTY:
				break;
			case SerialPortEvent.ERR:
				break;
        	case SerialPortEvent.RXCHAR:
        	case SerialPortEvent.RXFLAG:
        		try 
        		{
					int dataLength = m_port.getInputBufferBytesCount();

        	        while (dataLength > 0)
        	        {
        	        	byte[] data;
						
						data = m_port.readBytes(dataLength);
						
						serialReceived(data, dataLength);
						
						dataLength = m_port.getInputBufferBytesCount();
        	        }
        		} 
        		catch (SerialPortException e) {}
        		
        		break;
        }
    }
}
