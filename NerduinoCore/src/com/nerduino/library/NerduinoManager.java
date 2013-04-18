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

import com.nerduino.core.BaseManager;
import com.nerduino.nodes.TreeNode;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;


public class NerduinoManager extends BaseManager
{
	// Declarations
	public static NerduinoManager Current;
	
	File m_file;
	
	public Boolean getEnabled()
	{
		return false;
	}
	
	public void setEnabled(Boolean val)
	{
		
	}
	
	
	// Constructors
	public NerduinoManager() 
	{
        super("Nerduinos", "/com/nerduino/resources/NerduinoManager16.png");
        
        Current = this;
		
		m_file = new File(getFilePath());
		m_hasEditor = false;
		
		loadChildren();
	}
	
	void loadChildren()
	{
		File[] files = m_file.listFiles();
		
		for(File file : files)
		{
			if (file.getName().endsWith(".nerd"))
			{
				try
				{
					DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder;
					
					builder = builderFactory.newDocumentBuilder();
				
					FileInputStream fis;
						fis = new FileInputStream(file);
					
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
							else if (type.matches("Zigbee"))
								nerduino = new NerduinoXBee();

							nerduino.readXML(rootElement);

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

	@Override
	public String getFilePath()
	{
		return NerduinoHost.Current.getDataPath() + "/Nerduinos";
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

		for(Node node : m_children.getNodes())
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

	public NerduinoXBee getNerduino(long serialNumber) 
	{
		// TODO consider using a hash table for quick lookup
		for(Node node : m_children.getNodes())
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
		for(Node node : m_children.getNodes())
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
		for(Node node : m_children.getNodes())
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

			NerduinoBase nerduino = NerduinoManager.Current.getNerduino(name);

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
		for(Node node : m_children.getNodes())
		{
			if (nerd == node)
				return true;
		}

		return false;
	}
	
	@Override
	public void readXML(Element node)
	{
	}

	@Override
	public void writeXML(Document doc, Element parent)
	{
	}

	@Override
	public TreeNode createNewChild()
	{
		return new NerduinoUSB();
	}
	
	@Override
	public boolean configureChild(TreeNode node)
	{
		NerduinoUSB nu = (NerduinoUSB) node;
		
		nu.setName(getUniqueName(nu.getName()));
		
		// show the configure dialog
		NerduinoUSBConfigDialog dialog = new NerduinoUSBConfigDialog(new javax.swing.JFrame(), true);
		
		dialog.setNerduinoUSB(nu);
		dialog.setVisible(true);
		
		nu = dialog.m_nerduino;

		if (nu != null)
		{
			nu.setName(getUniqueName(nu.getName()));
			
			nu.save();
			
			addChild(nu);
			
			return true;
		}
		
		return false;
	}

	public void engage()
	{
		for(Node node : m_children.getNodes())
		{
			NerduinoBase nerd = (NerduinoBase) node;

			nerd.engage(null);
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
