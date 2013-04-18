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

package com.nerduino.core;

import com.nerduino.actions.FixedAction;
import com.nerduino.library.*;
import com.nerduino.processing.app.ArduinoManager;
import com.nerduino.scrolls.ScrollManager;
import com.nerduino.services.ServiceManager;
import com.nerduino.skits.SkitManager;
import com.nerduino.webhost.WebHost;
import com.pinkmatter.api.flamingo.ResizableIcons;
import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.awt.Component;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;
import org.pushingpixels.flamingo.internal.ui.ribbon.JBandControlPanel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public final class AppManager
{
	// Declarations
	public static AppManager Current;
	NerduinoHost m_nerduinoHost;
	NerduinoManager m_nerduinoManager;
	ScrollManager m_scrollManager;
	SkitManager m_skitManager;
	ServiceManager m_scriptManager;
	PointManager m_pointManager;
	ArduinoManager m_arduinoManager;
	String configFilename = "NerduinoHost.xml";
	JRibbon m_ribbon;
	
	static ArrayList<String> s_logArray = new ArrayList<String>();
	public static boolean loading = false;

	public static void initialize()
	{
		Current = new AppManager();
	}

	// Constructors
	private AppManager()
	{		
		Current = this;
		
		m_nerduinoHost = new NerduinoHost();
		m_nerduinoManager = NerduinoManager.Current;
		m_scrollManager = new ScrollManager();
		m_skitManager = new SkitManager();
		m_scriptManager = new ServiceManager();
		m_pointManager = new PointManager();
		m_arduinoManager = new ArduinoManager();

		ArduinoManager.Current.setArduinoPath("/Users/chaselaurendine/Documents/Arduino");

		Runtime runtime = Runtime.getRuntime();

		Thread shutdownThread = new Thread(new ShutdownListener());

		runtime.addShutdownHook(shutdownThread);

		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					WebHost wh = new WebHost();
				}
				catch(NoSuchMethodException ex)
				{
					Logger.getLogger(AppManager.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});

		thread.start();
		
		// deserialize configuration
		loadConfiguration();
		
		NerduinoManager.Current.engage();
	}

	void loadConfiguration()
	{
		loading = true;

		try
		{
			File f = new File(configFilename);

			if (f.exists())
			{
				DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = builderFactory.newDocumentBuilder();

				FileInputStream fis = new FileInputStream(configFilename);

				Document document = builder.parse(fis);

				Element rootElement = document.getDocumentElement();

				if (rootElement != null)
				{
					// load web host settings
					WebHost.Current.readXML(rootElement);

					// load nerduino host settings
					NerduinoHost.Current.readXML(rootElement);

					// load collections
					ScrollManager.Current.readXML(rootElement);
					SkitManager.Current.readXML(rootElement);
					//ServiceManager.Current.readXML(rootElement);
					//PointManager.Current.readXML(rootElement);
					NerduinoManager.Current.readXML(rootElement);
				}
			}
		}
		catch(IOException ex)
		{
			Logger.getLogger(AppManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		catch(SAXException ex)
		{
			Logger.getLogger(AppManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		catch(ParserConfigurationException e)
		{
		}

		loading = false;
	}

	public void saveConfiguration()
	{
		if (loading)
			return;

		try
		{
			Element e = null;
			Node n = null;

			// Document (Xerces implementation only).
			Document xmldoc = new DocumentImpl();

			// Root element.
			Element root = xmldoc.createElement("root");

			// save web host configuration
			WebHost.Current.writeXML(xmldoc, root);

			// save nerduino host configuration
			NerduinoHost.Current.writeXML(xmldoc, root);

			// save collections
			ScrollManager.Current.writeXML(xmldoc, root);
			SkitManager.Current.writeXML(xmldoc, root);
			//ServiceManager.Current.writeXML(xmldoc, root);
			NerduinoManager.Current.writeXML(xmldoc, root);

			xmldoc.appendChild(root);



			FileOutputStream fos = new FileOutputStream(configFilename);
			OutputFormat of = new OutputFormat("XML", "ISO-8859-1", true);

			of.setIndent(1);
			of.setIndenting(true);
			//of.setDoctype(null,"users.dtd");

			XMLSerializer serializer = new XMLSerializer(fos, of);

			serializer.asDOMSerializer();
			serializer.serialize(xmldoc.getDocumentElement());

			fos.close();
		}
		catch(IOException ex)
		{
		}
	}

	public void initializeExplorer()
	{
		ExplorerTopComponent explorer = ExplorerTopComponent.Current;

		org.openide.nodes.Node root = explorer.getRootNode();
		org.openide.nodes.Node[] nodes = new org.openide.nodes.Node[7];

		nodes[0] = m_nerduinoHost;
		nodes[1] = m_arduinoManager;
		nodes[2] = m_nerduinoManager;
		nodes[3] = m_pointManager;
		nodes[4] = m_skitManager;
		nodes[5] = m_scrollManager;
		nodes[6] = m_scriptManager;

		root.getChildren().add(nodes);

		//explorer.expandAll();
		
		explorer.expandNode(m_nerduinoManager);
	}

	public void setRibbon(JRibbon jRibbon)
	{
		m_ribbon = jRibbon; 
	}

	public JRibbon getRibbon()
	{
		return m_ribbon;
	}

	public void setRibbonComponentLabel(String path, String label)
	{
		if (m_ribbon != null)
		{
			String[] parts = path.split("/");
			
			if (parts.length == 0 || parts.length > 3)
				return;
			
			for(int i = 0; i < m_ribbon.getTaskCount(); i++)
			{
				RibbonTask task = m_ribbon.getTask(i);
				 
				if (task.getTitle().matches(parts[0]))
				{
					if (parts.length == 1)
					{
						task.setTitle(label);
						return;
					}
					
					for(int j = 0; j < task.getBandCount(); j++)
					{
						JRibbonBand band = (JRibbonBand) task.getBand(j);
						
						if (band.getTitle().matches(parts[1]))
						{
							if (parts.length == 2)
							{
								band.setTitle(label);
								return;
							}

							JBandControlPanel controlPanel = band.getControlPanel();
							
							for(int k = 0; k <  controlPanel.getComponentCount(); k++)
							{
								JCommandButton comp = (JCommandButton) controlPanel.getComponent(k);

								
								if (comp.getText().matches(parts[2]))
								{
									comp.setText(label);
									return;
								}
							}	
						}
					}
				}
			}
		}
	}

	public void setRibbonComponentImage(String path, String image)
	{
		if (m_ribbon != null)
		{
			String[] parts = path.split("/");
			
			if (parts.length != 3)
				return;
			
			for(int i = 0; i < m_ribbon.getTaskCount(); i++)
			{
				RibbonTask task = m_ribbon.getTask(i);
				 
				if (task.getTitle().matches(parts[0]))
				{
					for(int j = 0; j < task.getBandCount(); j++)
					{
						JRibbonBand band = (JRibbonBand) task.getBand(j);
						
						if (band.getTitle().matches(parts[1]))
						{
							JBandControlPanel controlPanel = band.getControlPanel();
							
							for(int k = 0; k <  controlPanel.getComponentCount(); k++)
							{
								JCommandButton comp = (JCommandButton) controlPanel.getComponent(k);

								if (comp.getText().matches(parts[2]))
								{
									comp.setIcon(ResizableIcons.fromResource(image));
									return;
								}
							}
							
						}
					}
				}
			}
		}
	}
	
	JComponent getNamedComponent(JComponent parent, String name)
	{
		Component[] components = parent.getComponents();
		
		for(Component child : components)
		{
			if (child.getName().matches(name))
				return (JComponent) child;
		}
			
		return null;
	}
	
	// Actions
	final class EnableHostAction extends FixedAction
	{
		private ImageIcon m_enabledIcon;
		private ImageIcon m_disabledIcon;

		public EnableHostAction()
		{
			super("Enable", "");

			java.net.URL imgURL = getClass().getResource("/com/nerduino/resources/PowerOff32.png");

			if (imgURL != null)
			{
				m_disabledIcon = new ImageIcon(imgURL);
			}

			imgURL = getClass().getResource("/com/nerduino/resources/PowerOn32.png");

			if (imgURL != null)
			{
				m_enabledIcon = new ImageIcon(imgURL);
			}

			update();
		}

		@Override
		public void update()
		{
			try
			{
				if (XBeeManager.Current.getEnabled())
				{
					putValue(SMALL_ICON, m_enabledIcon);
					putValue(NAME, "Disable");
					putValue(SHORT_DESCRIPTION, "Disable Host Communications");
				}
				else
				{
					putValue(SMALL_ICON, m_disabledIcon);
					putValue(NAME, "Enable");
					putValue(SHORT_DESCRIPTION, "Enable Host Communications");
				}
			}
			catch(Exception e)
			{
				putValue(SMALL_ICON, m_disabledIcon);
				putValue(NAME, "Enable");
				putValue(SHORT_DESCRIPTION, "Enable Host Communications");
			}
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (XBeeManager.Current.getEnabled())
			{
				XBeeManager.Current.setEnabled(false);
			}
			else
			{
				XBeeManager.Current.setEnabled(true);
			}

			update();
		}
	}

	final class ConfigHostAction extends FixedAction
	{
		public ConfigHostAction()
		{
			super("Config", "/com/nerduino/resources/Config32.png");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			NerduinoHost.Current.configure();
		}
	}

	final class PingAction extends FixedAction
	{
		public PingAction()
		{
			super("Ping All", "/com/nerduino/resources/Ping32.png");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			NerduinoManager.Current.pingAll();
		}
	}

	final class ShutdownListener implements Runnable
	{
		@Override
		public void run()
		{

			// TODO move this to the netbeans shutdown
			Current.saveConfiguration();
		}
	}

	public static void log(String text)
	{
		s_logArray.add(text);

		logUpdated();
	}

	static void logUpdated()
	{
		/*
		 for(ILogListener listener : s_logListeners)
		 {
		 try
		 {
		 listener.onLogUpdated();
		 }
		 catch(Exception e)
		 {
		 removeLogListener(listener);
		 }
		 }
		 */
	}
}
