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
import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PointManager extends BaseManager
{
	// Declarations
	public static PointManager Current;
	boolean m_loading = false;
	
	// Constructors
	public PointManager()
	{
		super("Points", "/com/nerduino/resources/PointsManager16.png");
     
		Current = this;
		
		loadConfiguration();
	}
	
	@Override
	public TreeNode createNewChild()
	{
		return new LocalDataPoint();
	}
	
	@Override
	public boolean configureChild(TreeNode child)
	{
		LocalDataPoint ldp = (LocalDataPoint) child;
		
		ldp.setName(getUniqueName(ldp.getName()));
		
		// show the configure data point dialog
		LocalDataPointConfigDialog dialog = new LocalDataPointConfigDialog(new javax.swing.JFrame(), true);
		
		dialog.setPoint(ldp);
		dialog.setVisible(true);
		
		ldp = dialog.m_point;

		if (ldp != null)
		{
			ldp.setName(getUniqueName(ldp.getName()));
			
			return true;
		}
		
		return false;
	}
	
	public LocalDataPoint getPoint(String name)
	{
		Node child = m_children.findChild(name);

		if (child != null)
			return (LocalDataPoint) child;
		
		return null;
	}
	
	public LocalDataPoint getPoint(short id)
	{
		for(Node child : m_children.getNodes())
		{
			LocalDataPoint point = (LocalDataPoint) child;

			if (point.Id == id)
				return point;
		}
	
		return null;
	}
	
	@Override
	public String getFilePath()
	{
		return AppManager.Current.getDataPath() + "/Points.xml";
	}
	
	@Override
	public void addChild(TreeNode node)
	{
		super.addChild(node);
		
		if (!m_loading)
			saveConfiguration();
	}
	
	private void loadConfiguration()
	{
		try
		{
			File f = new File(getFilePath());
			
			if (f.exists())
			{
				DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = builderFactory.newDocumentBuilder();
				
				FileInputStream fis = new FileInputStream(getFilePath());
				
				Document document = builder.parse(fis);
				
				Element rootElement = document.getDocumentElement();
				
				if (rootElement != null)
				{
					PointManager.Current.readXML(rootElement);
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
	}

	public void saveConfiguration()
	{
		if (m_loading)
			return;
		
		try
		{
			Element e = null;
			org.w3c.dom.Node n = null;
			
			// Document (Xerces implementation only).
			Document xmldoc = new DocumentImpl();
			
			// Root element.
			Element root = xmldoc.createElement("root");
			
			writeXML(xmldoc, root);
			
			xmldoc.appendChild(root);
			
			
			FileOutputStream fos = new FileOutputStream(getFilePath());
			OutputFormat of = new OutputFormat("XML", "ISO-8859-1", true);
			
			of.setIndent(1);
			of.setIndenting(true);
			
			XMLSerializer serializer = new XMLSerializer(fos, of);
			
			serializer.asDOMSerializer();
			serializer.serialize(xmldoc.getDocumentElement());
			
			fos.close();
		}
		catch(IOException ex)
		{
		}
	}
	
	@Override
	public void readXML(Element node)
	{
		m_loading = true;
		
		if (node != null)
		{
			NodeList nodes = node.getElementsByTagName("Point");
			
			if (nodes != null && nodes.getLength() > 0)
			{
				for(int i = 0; i < nodes.getLength(); i++)
				{
					Element element = (Element) nodes.item(i);
					
					LocalDataPoint newpoint = new LocalDataPoint();
					
					newpoint.setName(element.getAttribute("Name"));
					newpoint.DataLength = Byte.valueOf(element.getAttribute("DataLength"));
					newpoint.Attributes = Byte.valueOf(element.getAttribute("Attributes"));
					newpoint.setDataType(DataTypeEnum.valueOf(element.getAttribute("DataType")));
					newpoint.setValue(element.getAttribute("Value"));
					
					addChild(newpoint);					
				}
			}
		}
		
		m_loading = false;
	}

	@Override
	public void writeXML(Document doc, Element parent)
	{
		for(Node node : m_children.getNodes())
		{
			LocalDataPoint point = (LocalDataPoint) node;

			Element element = doc.createElement("Point");

			element.setAttribute("Name", point.getName());
			element.setAttribute("DataLength", Byte.toString(point.DataLength));
			element.setAttribute("Attributes", Byte.toString(point.Attributes));
			element.setAttribute("DataType", point.DataType.toString());
			element.setAttribute("Value", point.getValue().toString()); 
			
			parent.appendChild(element);
		}
	}

	@Override
	public Action[] getActions(boolean context)
	{
		return new Action[]
			{
				new PointManager.CreatePointAction(getLookup())
			};
	}

	public final class CreatePointAction extends AbstractAction
	{
		private final PointManager node;

		public CreatePointAction(Lookup lookup)
		{
			node = lookup.lookup(PointManager.class);

			putValue(AbstractAction.NAME, "Create Point");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (node != null)
			{
				try
				{
					node.createNew();
				}
				catch(Exception ex)
				{
					//Exceptions.printStackTrace(ex);
				}
			}
		}
	}
}
