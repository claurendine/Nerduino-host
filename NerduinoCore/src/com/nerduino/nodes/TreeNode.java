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
import com.nerduino.library.PointManager;
import java.awt.Component;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;
import org.openide.windows.TopComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

//public class TreeNode extends DefaultMutableTreeNode
@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
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
			return;

		super.setName(name);

		m_name = name;
		
		onRename(oldName, m_name);
		
		if (m_topComponent != null)
			m_topComponent.setDisplayName(m_name);
	}
	
	public void onRename(String oldName, String newName)
	{
	}
	
	// get a unique name from this node's children
	public String getUniqueName(String name)
	{
		return getUniqueName(getChildren().snapshot(), name);
	}

	public String getUniqueName(List<Node> list, String name)
	{
		if (name == null || name.length() == 0)
			name = "Name";

		if (list == null || list.isEmpty())
			return name;

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

			
			m_topComponent.setDisplayName(m_name);
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
			return new TreeNodeAction(getLookup());
		
		return null;
	}

	@Override
	public Action[] getActions(boolean context)
	{
		super.getActions(context);
		
		ArrayList<Action> actions = new ArrayList<Action>();
		
		// A list of actions for this node
		if (m_hasEditor)
			actions.add(new TreeNodeAction(getLookup()));
		
		if (m_canRename)
			actions.add(new RenameAction(getLookup()));
		
		if (m_canDelete)
			actions.add(new DeleteAction(getLookup()));
		
		Action[] customActions = getCustomActions(context);
		
		if (customActions != null)
		{
			for(Action ca : customActions)
			{
				actions.add(ca);
			}
		}
		
		Action[] array = new Action[actions.size()];
		
		return actions.toArray(array);
	}
	
	public Action[] getCustomActions(boolean context)
	{
		return null;
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
					node.showTopComponent();
				}
				catch(Exception ex)
				{
					Exceptions.printStackTrace(ex);
				}
			}
		}
	}
	
	public final class RenameAction extends AbstractAction
	{
		private TreeNode node;

		public RenameAction(Lookup lookup)
		{
			node = lookup.lookup(TreeNode.class);
			
			putValue(AbstractAction.NAME, "Rename");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (node != null)
			{
				try
				{
					node.rename();
				}
				catch(Exception ex)
				{
					Exceptions.printStackTrace(ex);
				}
			}
		}
	}
	
	public final class DeleteAction extends AbstractAction
	{
		private TreeNode node;

		public DeleteAction(Lookup lookup)
		{
			node = lookup.lookup(TreeNode.class);
			
			putValue(AbstractAction.NAME, "Delete");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (node != null)
			{
				try
				{
					node.destroy();
				}
				catch(Exception ex)
				{
					Exceptions.printStackTrace(ex);
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
			return new ImageIcon(imgURL).getImage();
		else
			return null;
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
	
	public void rename()
	{
		// prompt for a new name
		String oldname = getName();
		
		String newname = JOptionPane.showInputDialog(null, "New Name:", oldname);
		
		if (!newname.matches(oldname))
		{
			TreeNode parent = (TreeNode) getParentNode();
			
			if (parent != null)
				newname = parent.getUniqueName(newname);
			
			onRename(oldname, newname);
		}
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
			Exceptions.printStackTrace(ex);
		}
	}
	
	protected void addProperty(Sheet.Set sheet, Object target, Class valueType, Class editorType, String name, String description)
	{
		PropertySupport.Reflection prop = null;
		
		try
		{
			prop = new PropertySupport.Reflection(target, valueType, name);
		}
		catch(NoSuchMethodException ex)
		{
			try
			{
				prop = new PropertySupport.Reflection(target, valueType, "get" + name, null);
			}
			catch(NoSuchMethodException ex1)
			{
				//Exceptions.printStackTrace(ex1);
			}
		}
		
		if (prop != null)
		{
			prop.setName(name);
			prop.setShortDescription(description);
			
			if (editorType != null)
				prop.setPropertyEditorClass(editorType);
			
			sheet.put(prop);
		}
	}
}
