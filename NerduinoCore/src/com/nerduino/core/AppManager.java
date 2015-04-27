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

import com.nerduino.library.*;
import processing.app.ArduinoManager;
import com.nerduino.scrolls.ScrollManager;
import com.nerduino.services.ServiceManager;
import com.nerduino.skits.SkitManager;
import com.nerduino.webhost.WebHost;
import com.pinkmatter.api.flamingo.ResizableIcons;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenu;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;
import org.pushingpixels.flamingo.internal.ui.ribbon.JBandControlPanel;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.JRibbonApplicationMenuButton;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

public final class AppManager
{
	// Declarations
	public static AppManager Current;
	NerduinoHost m_nerduinoHost;
	NerduinoManager m_nerduinoManager;
	ScrollManager m_scrollManager;
	SkitManager m_skitManager;
	PointManager m_pointManager;
	ArduinoManager m_arduinoManager;
	String m_dataPath = null;
	String configFilename = "NerduinoHost.xml";
	JRibbon m_ribbon;
	ServiceManager m_scriptManager;
	WebHost m_webHost;
	
	String m_dbFileName = "NerduinoHost.sqlit";
	SqlJetDb m_db;
	ISqlJetTable m_paramTable;

	static ArrayList<String> s_logArray = new ArrayList<String>();
	public static boolean loading = false;

	public static void initialize()
	{
		Current = new AppManager();
	}

	// Constructors
	private AppManager()
	{
		loading = true;
		Current = this;
		
		AppConfiguration config = new AppConfiguration(m_dbFileName);

		m_webHost = new WebHost();
		m_arduinoManager = new ArduinoManager();
		m_nerduinoHost = new NerduinoHost();
		m_nerduinoManager = NerduinoManager.Current;
		m_scrollManager = new ScrollManager();
		m_skitManager = new SkitManager();
		m_scriptManager = new ServiceManager();
		m_pointManager = new PointManager();

		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					m_webHost.initialize();
				}
				catch(NoSuchMethodException ex)
				{
					Logger.getLogger(AppManager.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}, "Nerduino WebHost Initialization");

		thread.start();
		
		loading = false;
		NerduinoManager.Current.engage();
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
	}
	
	public String getDataPath()
	{
		if (m_dataPath == null)
		{
			m_dataPath = AppConfiguration.Current.getParameter("DataPath");
			
			if (m_dataPath == null)
			{
				boolean found = false;
				String path = AppConfiguration.Current.getParameter("ArduinoPath");

				while (!found)
				{
					if (path != null)
					{
						File pathFile = new File(path);

						if (pathFile.exists() && pathFile.isDirectory())
						{
							AppConfiguration.Current.setParameter("DataPath", pathFile.getAbsolutePath());

							m_dataPath = path;
							return m_dataPath;
						}
					}

					JFileChooser chooser;

					chooser = new JFileChooser(); 
					chooser.setCurrentDirectory(new java.io.File("."));
					chooser.setDialogTitle("Select the Nerduino Data directory");
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					//
					// disable the "All files" option.
					//
					chooser.setAcceptAllFileFilterUsed(false);
					//    
					if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) 
					{ 
						path = chooser.getSelectedFile().getPath();
						System.out.println("getCurrentDirectory(): " 
							+  chooser.getCurrentDirectory());
						System.out.println("getSelectedFile() : " 
							+  chooser.getSelectedFile());
					}
					else 
					{
						System.out.println("No Selection ");
					}
				}
			}
		}
		
		return m_dataPath;
		
//		return System.getProperty("user.dir");
	}

	public void setDataPath(String value)
	{
		m_dataPath = value;
		
		AppConfiguration.Current.setParameter("DataPath", value);
	}

	public void setRibbon(JRibbon jRibbon)
	{
		m_ribbon = jRibbon; 
	}

	public JRibbon getRibbon()
	{
		if (m_ribbon == null)
		{
			m_ribbon = ExplorerTopComponent.s_ribbon;
		}
		
		return m_ribbon;
	}

	public void setRibbonComponentLabel(String path, String label)
	{
		getRibbon();
		
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
		getRibbon();
		
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
}
