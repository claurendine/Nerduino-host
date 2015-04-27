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

package com.nerduino.webhost;

import com.nerduino.core.AppConfiguration;
import com.nerduino.core.AppManager;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

public class WebHost
{
 	String m_webroot; 
	int m_port = 8081;
	Boolean m_enabled = true;
	
	public static WebHost Current;
	
	public WebHost()
    {
		Current = this;

		m_webroot = AppConfiguration.Current.getParameter("WebHostWebroot");
		m_port = AppConfiguration.Current.getParameterInt("WebHostPort", 8081);
		m_enabled = AppConfiguration.Current.getParameterBool("WebHostEnabled", true);
		
		if (m_webroot == null || m_webroot.length() == 0)
		{
			m_webroot = System.getProperty("user.dir") + "/webroot";
		}
	}
	
	public void initialize() throws NoSuchMethodException 
    {
		try 
		{
			Server server = new Server(8081);
	        
			// Create the servlet handler and define the Chat servlet
			
			ResourceHandler resource_handler = new ResourceHandler();
			resource_handler.setDirectoriesListed(true);
			resource_handler.setWelcomeFiles(new String[]{ "index.html" });
			resource_handler.setResourceBase(m_webroot);
			
			NerduinoWebSocketHandler dlh = new NerduinoWebSocketHandler();
			
			HandlerList handlers = new HandlerList();
			handlers.setHandlers(new Handler[] { dlh, resource_handler, new DefaultHandler() });
			server.setHandler(handlers);
			
			server.start();
            server.join();
        } 
		catch (Throwable e) 
		{
            e.printStackTrace();
        }
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

		}
		else
		{			
			AppManager.Current.setRibbonComponentImage("Home/Host Settings/Web", "com/nerduino/resources/HostDisabled.png");
		}
		
		AppConfiguration.Current.setParameter("WebHostEnabled", Boolean.toString(value));
	}
	
	public String getHttpAddress()
	{
		String address = "";
		
		InetAddress ip;
		
		try 
		{
			ip = InetAddress.getLocalHost();
			
			address = "http://" + ip.getHostAddress() + ":" + Integer.toString(m_port) + "/";
		} 
		catch (UnknownHostException e) 
		{
		}
		
		return address;
	}
	
	public void setHttpAddress(String address)
	{
	}
	
	public int getPort()
	{
		return m_port;
	}
	
	public void setPort(int value)
	{
		m_port = value;
		
		AppConfiguration.Current.setParameter("WebHostPort", Integer.toString(value));
	}
	
	public void setWebRoot(String path)
	{
		m_webroot = path;
		
		AppConfiguration.Current.setParameter("WebHostWebroot", m_webroot);
	}
	
	public String getWebRoot()
	{
		return m_webroot;
	}
}
