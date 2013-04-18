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

package com.nerduino.nodes;

import com.nerduino.core.ExplorerTopComponent;
import com.nerduino.core.PropertiesTopComponent;
import java.awt.Component;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;
import org.openide.windows.TopComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

//public class TreeNode extends DefaultMutableTreeNode
public class TreeNode extends AbstractNode
{
	// Declarations
	protected String m_iconPath;
	protected Boolean m_canDelete = false;
	protected Boolean m_canRename = false;
	protected Boolean m_hasEditor = true;
	protected Boolean m_showPropertyBrowser = true;
	protected Boolean m_canCopy = false;
	protected Boolean m_canDrag = false;
	protected TopComponent m_topComponent;
	protected String m_name;

	//Constructors
	public TreeNode(Children parent, String name, String iconPath)
	{
		super(parent);

		m_name = name;
		m_iconPath = iconPath;
	}

	@Override
	public String toString()
	{
		return m_name;
	}

	@Override
	public String getName()
	{
		return m_name;
	}

	@Override
	public void setName(String name)
	{
		String oldName = m_name;

		if (m_name.equals(name))
		{
			return;
		}

		m_name = getUniqueName(name);

		super.setName(m_name);

		onRename(oldName, m_name);

		// TODO mark the tab text
		if (m_topComponent != null)
		{
			m_topComponent.setDisplayName(m_name);
		}

//		NerduinoTreeView.Current.modelUpdated(this);
	}

	Component action1;
	
	public Component getAction1()
	{
		if (action1 == null)
		{
			action1 = new EmptyCommand();
			
			action1.setSize(60, 18);
		}

		return action1;
	}

	public void onRename(String oldName, String newName)
	{
	}

	public String getUniqueName(String name)
	{
		return getUniqueName(getChildren().snapshot(), name);
	}

	public String getUniqueName(List list, String name)
	{
		if (name == null || name.length() == 0)
		{
			name = "Name";
		}

		if (list == null || list.isEmpty())
		{
			return name;
		}

		String newname = name;
		Integer index = 1;

		String numbers = "0123456789";
		String rootname = name;

		int length = rootname.length();

		String ss = rootname.substring(length - 1, length);
		CharSequence cs = ss;

		Boolean cc = numbers.contains(cs);

		while (length > 1 && numbers.contains(rootname.substring(length - 1, length)))
		{
			rootname = rootname.substring(0, length - 1);
			length--;
		}

		// get the root name
		Boolean found = true;

		while (found)
		{
			found = false;

			// loop through the list to find a unique name
			for (Object obj : list)
			{
				String n = obj.toString();

				if (n.equals(newname))
				{
					found = true;
					break;
				}
			}

			if (found)
			{
				newname = rootname + index.toString();
				index++;
			}
		}

		return newname;
	}

	public TopComponent getTopComponent()
	{
		return null;
	}

	public void select()
	{
		ExplorerTopComponent.Current.setSelectedNode(this);
	}

	public void click(java.awt.event.MouseEvent evt)
	{
	}

	public void doubleClick(java.awt.event.MouseEvent evt)
	{
		configure();
	}

	public void configure()
	{
		showTopComponent();
	}

	public void showTopComponent()
	{
		if (m_topComponent == null)
		{
			m_topComponent = getTopComponent();
			m_topComponent.open();
		}

		if (m_topComponent != null)
		{
			if (!m_topComponent.isOpened())
			{
				m_topComponent = getTopComponent();
				m_topComponent.open();
				
			}

			m_topComponent.requestActive();

			// repeated on purpose to assert the display name
			m_topComponent.requestActive();

		}
	}

	public void closeTopComponent()
	{
		if (m_topComponent != null)
			m_topComponent.close();
	}

	public void onTopComponentClosed()
	{
		m_topComponent = null;
	}

	public void setRibbonContext()
	{
		// TODO
	}

	public void onSelected()
	{
		PropertiesTopComponent.Current.setObject(this);
	}

	public void onDeselected()
	{
	}

	public void readXML(Element node)
	{
	}

	public void writeXML(Document doc, Element node)
	{
	}

	public void onEditorUpdated()
	{
	}

	@Override
	protected Sheet createSheet()
	{
		// Create an empty sheet
		Sheet sheet = Sheet.createDefault();

		// Create a set of properties
		Sheet.Set set = Sheet.createPropertiesSet();

		// Add the set of properties to the sheet
		sheet.put(set);

		return sheet;
	}

	@Override
	public Action getPreferredAction()
	{
		if (m_hasEditor)
		{
			return new TreeNodeAction(getLookup());
		}

		return null;
	}

	@Override
	public Action[] getActions(boolean context)
	{
		// A list of actions for this node
		if (m_hasEditor)
		{
			return new Action[]
					{
						new TreeNodeAction(getLookup())
					};
		}

		return new Action[]
				{
				};
	}

	public final class TreeNodeAction extends AbstractAction
	{
		private TreeNode node;

		public TreeNodeAction(Lookup lookup)
		{
			node = lookup.lookup(TreeNode.class);

			putValue(AbstractAction.NAME, "Edit");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (node != null)
			{
				try
				{
					node.doubleClick(null);
				}
				catch(Exception ex)
				{
					//Exceptions.printStackTrace(ex);
				}
			}
		}
	}

	@Override
	public Node cloneNode()
	{
		return null;
	}

	@Override
	public Image getIcon(int type)
	{
		java.net.URL imgURL = getClass().getResource(m_iconPath);

		if (imgURL != null)
		{
			return new ImageIcon(imgURL).getImage();
		}
		else
		{
//            System.err.println("Couldn't find file: " + imgURL.toString());
			return null;
		}
	}

	@Override
	public Image getOpenedIcon(int type)
	{
		return getIcon(type);
	}

	@Override
	public HelpCtx getHelpCtx()
	{
		return null;
	}

	@Override
	public boolean canRename()
	{
		return m_canRename;
	}

	@Override
	public boolean canDestroy()
	{
		return m_canDelete;
	}

	@Override
	public PropertySet[] getPropertySets()
	{
		final Sheet.Set ps = Sheet.createPropertiesSet();
		/*	
		 try 
		 {
		 Property indexProp = new PropertySupport.Reflection(this, String.class, "getName", null);

		 indexProp.setName("index");

		 ps.put(indexProp);
		 } 
		 catch (NoSuchMethodException ex) 
		 {
		 }
		 */
		return new PropertySet[]
				{
					ps
				};
	}

	@Override
	public Transferable clipboardCopy() throws IOException
	{
		return null;
	}

	@Override
	public Transferable clipboardCut() throws IOException
	{
		return null;
	}

	@Override
	public Transferable drag() throws IOException
	{
		return null;
	}

	@Override
	public boolean canCopy()
	{
		return m_canCopy;
	}

	@Override
	public boolean canCut()
	{
		return m_canCopy && m_canDelete;
	}

	@Override
	public PasteType getDropType(Transferable t, int action, int index)
	{
		return null;
	}

	@Override
	public NewType[] getNewTypes()
	{
		return null;
	}

	@Override
	public boolean hasCustomizer()
	{
		return false;
	}

	@Override
	public Component getCustomizer()
	{
		return null;
	}

	@Override
	public Handle getHandle()
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	protected void addProperty(Sheet.Set sheet, Class valueType, Class editorType, String name, String description)
	{
		try
		{
			PropertySupport.Reflection prop = new PropertySupport.Reflection(this, valueType, name);
			prop.setName(name);
			prop.setShortDescription(description);
			
			if (editorType != null)
				prop.setPropertyEditorClass(editorType);
			
			sheet.put(prop);
		}
		catch(NoSuchMethodException ex)
		{
			//Exceptions.printStackTrace(ex);
		}
	}
}
