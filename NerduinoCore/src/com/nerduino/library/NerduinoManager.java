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

import com.nerduino.core.AppManager;
import com.nerduino.core.BaseManager;
import com.nerduino.nodes.TreeNode;
import processing.app.Sketch;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


public class NerduinoManager extends BaseManager
{
	// Declarations
	public static NerduinoManager Current;
	
	File m_file;
	
	// Constructors
	public NerduinoManager() 
	{
        super("Things", "/com/nerduino/resources/NerduinoManager16.png");
        
        Current = this;
		
		m_file = new File(getFilePath());
		m_hasEditor = false;
		
		loadChildren();
		
		// create a thread to process nerduino states
		Thread processThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				for (;;) 
				{
					try
					{
						for(Node node : getNodes())
						{
							NerduinoBase nerd = (NerduinoBase) node;
							
							if (nerd.m_active)
							{
								nerd.process();
								
								if (!nerd.getEngaging())
								{
									double dt = nerd.getTimeSinceLastResponse();

									if (dt > 5.0)
									{
										if (!nerd.ping())
										{
											nerd.setStatus(NerduinoStatusEnum.Offline);
										}
									}
								}
							}
						}

						
						Thread.sleep(1);
					}
					catch(InterruptedException ex)
					{
						Exceptions.printStackTrace(ex);
					}
				}
			}
		});
		
		processThread.start();
	}
	
	private void loadChildren()
	{
		File[] files = m_file.listFiles();
		
		if (files != null)
		{
			for(File file : files)
			{
				if (file.getName().endsWith(".nerd"))
				{
					try
					{
						DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder builder;

						builder = builderFactory.newDocumentBuilder();

						FileInputStream fis = new FileInputStream(file);

						Document document = builder.parse(fis);

						Element rootElement = document.getDocumentElement();

						if (rootElement != null)
						{
							try
							{
								String type = rootElement.getAttribute("Type");

								NerduinoBase nerduino = null;

								if (type.matches("USB"))
									nerduino = new NerduinoUSB();
								else if (type.matches("XBee"))
									nerduino = new NerduinoXBee();
								else if (type.matches("TCP"))
									nerduino = new NerduinoTcp();
								else if (type.matches("BT"))
									nerduino = new NerduinoBT();

								if (nerduino != null)
								{
									nerduino.readXML(rootElement);
								}

								addChild(nerduino);
							}
							catch(Exception e)
							{
							}
						}
					}
					catch(IOException ex)
					{
					}
					catch(ParserConfigurationException ex)
					{
					}
					catch(SAXException ex)
					{
					}
				}
			}
		}
	}
		
	public Boolean getEnabled()
	{
		return false;
	}
	
	public void setEnabled(Boolean val)
	{	
	}
	
	@Override
	public String getFilePath()
	{
		return AppManager.Current.getDataPath() + "/Nerduinos";
	}

	
	// Methods
	public void discover() 
	{
		// Broadcast a zigbee discovery message to get all nerduinos to respond
		
		// TODO
	}
    
	public void pingAll()
	{
		
	}
	
	public void scan() 
	{
		// Loop through each nerduino to check status.  Used to track if nerduinos have 
		// dropped offline or are non-responsive

		for(Node node : getNodes())
		{
			NerduinoBase nerd = (NerduinoBase) node;
			
			nerd.checkStatus();
		}
	}

	public NerduinoXBee getNerduino(long serialNumber, short networkAddress) 
	{
		// TODO consider using a hash table for quick lookup
		NerduinoXBee nerd = getNerduino(serialNumber);
		
		if (nerd == null)
		{		
			nerd = new NerduinoXBee();
			
			nerd.m_address.SerialNumber = serialNumber;
			nerd.m_address.NetworkAddress = networkAddress;
			
			addChild(nerd);
		}
		
		return nerd;
	}

	public Node[] getNodes()
	{
		return m_children.getNodes();
	}
	
	public NerduinoXBee getNerduino(long serialNumber) 
	{
		// TODO consider using a hash table for quick lookup
		for(Node node : getNodes())
		{
			if (node instanceof NerduinoXBee)
			{
				NerduinoXBee nerd = (NerduinoXBee) node;
				
				if (nerd.m_address.SerialNumber == serialNumber)
					return nerd;
			}
		}
		        
		return null;
	}

	public NerduinoXBee getNerduino(short networkAddress) 
	{
		// TODO consider using a hash table for quick lookup
		for(Node node : getNodes())
		{
			if (node instanceof NerduinoXBee)
			{
				NerduinoXBee nerd = (NerduinoXBee) node;
				
				if (nerd.m_address.NetworkAddress == networkAddress)
					return nerd;
			}
		}

		return null;
	}

	public NerduinoBase getNerduino(String name) 
	{
		// TODO consider using a hash table for quick lookup
		for(Node node : getNodes())
		{
			NerduinoBase nerd = (NerduinoBase) node;
			
			if (nerd.getName().equals(name))
				return nerd;
		}
		
		return null;
	}
	
	public Object getPointValue(String path)
	{
		// if the path includes a '.' delimiter then lookup an associated point
		if (path.contains("."))
		{
			int p = path.indexOf(".");
			
			String name = path.substring(0, p);
			String pointName = path.substring(p + 1);

			NerduinoBase nerduino = NerduinoManager.Current.getNerduino(name);
				
			if (nerduino != null)
			{
				PointBase point = nerduino.getPoint(pointName);

				if (point != null)
					return point.getValue();
			}
		}
		else
		{
			String pointName = path;
			
			LocalDataPoint point = PointManager.Current.getPoint(pointName);
			
			if (point != null)
				return point.getValue();
		}
		
		return null;
	}
		
	public void setPointValue(String path, String value)
	{
		// if the path includes a '.' delimiter then lookup an associated point
		if (path.contains("."))
		{
			int p = path.indexOf(".");
			
			String name = path.substring(0, p);
			String pointName = path.substring(p + 1);

			NerduinoBase nerduino = getNerduino(name);

			if (nerduino != null)
			{
				PointBase point = nerduino.getPoint(pointName);

				if (point != null)
					point.setValue(value);
			}
		}
		else
		{
			String pointName = path;
			
			LocalDataPoint point = PointManager.Current.getPoint(pointName);
			
			if (point != null)
				point.setValue(value);
		}
	}
	
	public boolean contains(NerduinoBase nerd)
	{
		for(Node node : getNodes())
		{
			if (nerd == node)
				return true;
		}

		return false;
	}
	
	@Override
	public TreeNode createNewChild()
	{
		return new NerduinoUSB();
	}
	
	public TreeNode createNewXBeeNerduino()
	{
		TreeNode newnode =  new NerduinoXBee();
		
		if (configureChild(newnode))
		{
			addChild(newnode);

			newnode.select();

			return newnode;
		}
		
		return null;
	}
	
	public TreeNode createNewTCPNerduino()
	{
		TreeNode newnode =  new NerduinoTcp();
		
		if (configureChild(newnode))
		{
			addChild(newnode);

			newnode.select();

			return newnode;
		}
		
		return null;
	}
	
	public TreeNode createNewBTNerduino()
	{
		TreeNode newnode =  new NerduinoBT();
		
		if (configureChild(newnode))
		{
			addChild(newnode);

			newnode.select();

			return newnode;
		}
		
		return null;
	}
	
	@Override
	public boolean configureChild(TreeNode node)
	{
		NerduinoBase nb = (NerduinoBase) node;
		
		if (nb.configureNewNerduino())
		{
			nb.setName(getUniqueName(nb.getName()));
			
			nb.save();
			
			addChild(nb);
			
			return true;
		}
		
		return false;
	}

	public void engage()
	{
		for(Node node : getNodes())
		{
			NerduinoBase nerd = (NerduinoBase) node;
			
			nerd.engage();
		}
	}
	
	@Override
	public Action[] getActions(boolean context)
	{
		// A list of actions for this node
		return new Action[]
			{
				new NerduinoManager.CreateNerduinoAction(getLookup())
			};
	}

	public Object[] getNerduinos(Sketch sketch)
	{
		String sname = sketch.getName();
		
		ArrayList<NerduinoBase> nerds = new ArrayList<NerduinoBase>();
		
		for(Node node : getNodes() )
		{
			NerduinoBase nerd = (NerduinoBase) node;
			
			if (nerd != null)
			{
				String sketchName = nerd.getSketch();
				
				if (sketchName != null && sketchName.equals(sname))
					nerds.add(nerd);
			}
		}
		
		return nerds.toArray();
	}

	public final class CreateNerduinoAction extends AbstractAction
	{
		private NerduinoManager node;

		public CreateNerduinoAction(Lookup lookup)
		{
			node = lookup.lookup(NerduinoManager.class);

			putValue(AbstractAction.NAME, "Create Nerduino");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (node != null)
			{
				try
				{
					node.createNewChild();
				}
				catch(Exception ex)
				{
					//Exceptions.printStackTrace(ex);
				}
			}
		}
	}
}
