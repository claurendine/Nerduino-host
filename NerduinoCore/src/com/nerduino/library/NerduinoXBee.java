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

package com.nerduino.library;

import com.nerduino.xbee.*;
import java.awt.Image;
import javax.swing.ImageIcon;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NerduinoXBee extends NerduinoFull
{
    // Declarations
    static double NERDUINO_TIMOUT = 30.0;
    
    short m_parentAddress;
    byte m_signalStrength;
	
	Serial m_serialProgrammer;
	String m_comPort;
	int m_baudRate = 115200;
	
    // Constructors
    public NerduinoXBee()
    {
        super("Nerd", "/NerduinoHostApp/resources/Nerduino16.png");
        
    	//touch();
    }

	@Override
	public boolean configureNewNerduino()
	{
		setName(getUniqueName(getName()));
		
		// show the configure dialog
		NerduinoXBeeConfigDialog dialog = new NerduinoXBeeConfigDialog(new javax.swing.JFrame(), true);
		
		dialog.setNerduinoXBee(this);
		dialog.setVisible(true);
		
		return (dialog.m_nerduino != null);
	}

	public String getComPort()
	{
		return m_comPort;
	}

	public void setComPort(String port)
	{
		m_comPort = port;
		
		save();
	}

	public int getBaudRate()
	{
		return m_baudRate;
	}

	public void setBaudRate(int value)
	{
		m_baudRate = value;
		
		save();
	}
	
	// Methods
    public void onPingResponse(ZigbeeReceivePacketFrame zrf)
    {
    	onPingResponse(zrf.Data, 4);
    }

    public void onCheckin(ZigbeeReceivePacketFrame zrf)
    {
    	m_address.NetworkAddress = zrf.SourceNetworkAddress;
        
		onCheckin(zrf.Data, 4);
    }
    
    public void onExecuteCommandResponse(ZigbeeReceivePacketFrame zrf)
	{
		onExecuteCommandResponse(zrf.Data, 4);
	}
    
    public void onGetPointResponse(ZigbeeReceivePacketFrame zrf)
    {
		onGetPointResponse(zrf.Data, 4);
    }

    public void onGetPointValueResponse(ZigbeeReceivePacketFrame zrf)
    {
        onGetPointValueResponse(zrf.Data, 4);
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
		
		m_address.SerialNumber = Long.decode(str);
		
		setStatus(NerduinoStatusEnum.Offline);
		
		m_comPort = node.getAttribute("Port");		
		m_baudRate = Integer.valueOf(node.getAttribute("BaudRate"));
	}
	
	@Override
	public void writeXML(Document doc, Element element)
	{
		if (getStatus() != NerduinoStatusEnum.Uninitialized)
		{
			element.setAttribute("Name", m_name);
			element.setAttribute("SerialNumber", ((Long) m_address.SerialNumber).toString());
			
			element.setAttribute("Type", "XBee");
			
			element.setAttribute("Port", m_comPort);
			element.setAttribute("BaudRate", Integer.toString(m_baudRate));
		}
	}
}
