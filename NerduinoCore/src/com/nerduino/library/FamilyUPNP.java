/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.library;

import com.nerduino.core.AppManager;
import com.nerduino.nodes.TreeNode;
import com.nerduino.uPnP.NerduinoUPNP;
import com.nerduino.uPnP.UpnpDeviceType;
import com.nerduino.uPnP.UpnpPoint;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.DeviceList;
import org.cybergarage.upnp.Service;
import org.cybergarage.upnp.ServiceList;
import org.cybergarage.upnp.device.DeviceChangeListener;
import org.cybergarage.upnp.event.EventListener;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import processing.app.ArduinoManager;

/**
 *
 * @author chaselaurendine
 */
public class FamilyUPNP extends FamilyBase
{
		// Declarations
	public static FamilyUPNP Current;
	
	boolean m_discovering = false;
	HashMap<InetAddress, NerduinoUPNP> m_deviceMap = new HashMap<InetAddress, NerduinoUPNP>();

	DatagramSocket  m_receiveSocket = null;
	MulticastSocket m_discoverSocket = null;
	DatagramPacket m_discoverPacket = null;

	
	@SuppressWarnings("UseOfObsoleteCollectionType")
	Hashtable<String, Boolean> m_ports;

	ArrayList<UpnpDeviceType> m_deviceTypes = new ArrayList<UpnpDeviceType>();
	ArrayList<NerduinoUPNP> m_engageList = new ArrayList<NerduinoUPNP>();
	boolean m_engaging;
	
	String m_map = "";
	
	public ControlPoint m_controlPoint;
	
	public FamilyUPNP()
	{
		super();

		Current = this;
	}
	
	public void engage()
	{
		try
		{
			// parse the xml string
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			
			String filename = AppManager.Current.getDataPath() + "/UpnpMap.xml";
			
			File file = new File(filename);
			
			if (file.exists())
			{
				InputStream is = new FileInputStream(filename);

				Document document = builder.parse(is);

				is.close();


				StringBuilder fileData = new StringBuilder(1000);

				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));

				char[] buf = new char[1024];

				int numRead = 0;

				while ((numRead = reader.read(buf)) != -1) 
				{
					String readData = String.valueOf(buf, 0, numRead);

					fileData.append(readData);

					buf = new char[1024];
				}

				reader.close();

				m_map = fileData.toString();


				Element rootElement = document.getDocumentElement();


				if (rootElement != null)
				{
					NodeList nodes = rootElement.getElementsByTagName("DeviceType");

					for(int i = 0; i < nodes.getLength(); i++)
					{
						Element edt = (Element) nodes.item(i);

						UpnpDeviceType dt = new UpnpDeviceType();

						dt.parseXML(edt);

						m_deviceTypes.add(dt);
					}
				}
			}
		}
		catch(ParserConfigurationException ex)
		{
			Exceptions.printStackTrace(ex);
		}
		catch(FileNotFoundException ex)
		{
			Exceptions.printStackTrace(ex);
		}
		catch(SAXException ex)
		{
			Exceptions.printStackTrace(ex);
		}
		catch(IOException ex)
		{
			Exceptions.printStackTrace(ex);
		}	
			
		m_controlPoint = new ControlPoint();

		m_controlPoint.start();


		DeviceList dl = m_controlPoint.getDeviceList();

		for(int i = 0; i< dl.size(); i++)
		{
			Device device = dl.getDevice(i);

			DeviceList sdl = device.getDeviceList();

			if (sdl.size() > 0)
			{
				for(int j = 0; j < sdl.size(); j++)
				{
					Device sdevice = sdl.getDevice(j);

					createNerduino(sdevice);
				}
			}
			else
			{
				createNerduino(device);
			}	
		}
		
		m_controlPoint.addEventListener(new EventListener()
			{
				@Override
				public void eventNotifyReceived(String uuid, long seq, String varName, String value)
				{
					// loop through devices looking for the matching uuid
					for(NerduinoUPNP nerd : m_engageList)
					{
						ServiceList sl = nerd.m_upnpDevice.getServiceList();
						
						for(int i = 0; i < sl.size(); i++)
						{
							Service service = sl.getService(i);
							
							String sid = service.getSID();
							
							if (sid.equals(uuid))
							{
								// loop through nerduino points looking for a matching state variable
								for(LocalDataPoint pt : nerd.m_localDataPoints)
								{
									if (pt instanceof UpnpPoint)
									{
										UpnpPoint upt = (UpnpPoint) pt;
										
										if (varName.equals(upt.m_eventStateVariable))
										{
											upt.remoteSetValue(value);
											return;
										}
									}
								}
							}
						}
					}
				}
			});
		
		m_controlPoint.addDeviceChangeListener(new DeviceChangeListener()
		{
			@Override
			public void deviceAdded(Device device) 
			{
				DeviceList sdl = device.getDeviceList();

				if (sdl.size() > 0)
				{
					for(int j = 0; j < sdl.size(); j++)
					{
						Device sdevice = sdl.getDevice(j);

						createNerduino(sdevice);
					}	
				}
				else
				{
					createNerduino(device);				
				}
			}

			@Override
			public void deviceRemoved(Device device) 
			{

			}
		});

		m_controlPoint.search();

	}
	
		
	@Override
	public String getFamilyType()
	{
		return "UPNP";
	}

	public String getMap()
	{
		return m_map;
	}
	
	public void setMap(String value)
	{
		m_map = value;
		
		// clear any currently defined upnp datatypes
		m_deviceMap.clear();
		
		if (m_map != null)
		{
			try {
				// parse the xml string
				DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = builderFactory.newDocumentBuilder();

				InputStream is = new ByteArrayInputStream(value.getBytes());
				
				Document document = builder.parse(is);
				
				
				Element rootElement = document.getDocumentElement();
				
				if (rootElement != null)
				{
					NodeList nodes = rootElement.getElementsByTagName("DeviceType");
					
					for(int i = 0; i < nodes.getLength(); i++)
					{
						Element edt = (Element) nodes.item(i);
						
						UpnpDeviceType dt = new UpnpDeviceType();
						
						dt.parseXML(edt);
						
						m_deviceTypes.add(dt);
					}
				}
				
				// save the xml to a file
				
				String filename = ArduinoManager.Current.getArduinoPath() + "/UpnpMap.xml";
				BufferedWriter writer = null;
				try
				{
					writer = new BufferedWriter( new FileWriter( filename));
					writer.write( value);
				}
				catch ( IOException e)
				{
				}
				finally
				{
					try
					{
						if ( writer != null)
						writer.close( );
					}
					catch ( IOException e)
					{
					}
				}
			}
			catch(ParserConfigurationException ex) 
			{
				Exceptions.printStackTrace(ex);
			}
			catch(SAXException ex)
			{
				Exceptions.printStackTrace(ex);
			}
			catch(IOException ex)
			{
				Exceptions.printStackTrace(ex);
			}
		}
	}
	
	@Override
	public NerduinoBase CreateNerduino()
	{
		return new NerduinoUPNP();
	}

	private void createNerduino(Device device)
	{
		NerduinoUPNP uDevice = new NerduinoUPNP();

		uDevice.setUpnpDevice(device);
		
		// configure points/methods
		
		String deviceType = device.getDeviceType();
		
		for(UpnpDeviceType dt : m_deviceTypes)
		{
			if (deviceType.contains(dt.getPattern()))
			{
				uDevice.setUpnpDeviceType(dt);
				
				break;
			}
		}
		
		uDevice.setStatus(NerduinoStatusEnum.Online);

		m_engageList.add(uDevice);
		NerduinoManager.Current.addChild((TreeNode) uDevice);
	}
}
