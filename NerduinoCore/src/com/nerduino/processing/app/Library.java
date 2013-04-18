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

package com.nerduino.processing.app;

import com.nerduino.nodes.TreeNode;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class Library extends TreeNode
{
	File m_file;
	private Children m_nodes;

	public Library(File file)
	{
		super(new Children.Array(), "Library", "/com/nerduino/resources/Library16.png");
		
		m_file = file;
		m_name = m_file.getName();
		m_nodes = this.getChildren();
		
		loadChildren();
	}
	
	void loadChildren()
	{
		File[] files = m_file.listFiles();
		
		for(File file : files)
		{
			if (file.isDirectory())
			{
				SourceFolder sf = new SourceFolder(file);
				
				addNode(sf);
			}
			else
			{
				SourceFile sf = new SourceFile(file);
				
				addNode(sf);
			}
		}
	}
	
	public String getFilePath()
	{
		return m_file.getPath();
	}
	
	@Override
	public Node.PropertySet[] getPropertySets()
	{
		final Sheet.Set sheet = Sheet.createPropertiesSet();
	
		sheet.setDisplayName("Library Information");

		addProperty(sheet, String.class, null, "Name", "Name");

		return new Node.PropertySet[] { sheet };
	}
	
	public void addNode(Node node)
	{
		if (node != null && !contains(node))
		{
			org.openide.nodes.Node[] nodes = new org.openide.nodes.Node[1];
			nodes[0] = node;
			
			m_nodes.add(nodes);
		}
	}
	
	
	public boolean contains(Node node)
	{
		for(int i = 0; i < m_nodes.getNodesCount(); i++)
		{
			if (node == m_nodes.getNodeAt(i))
				return true;
		}
		
		return false;
	}
	
	@Override
	public Action[] getActions(boolean context)
	{
		return new Action[]
			{
				new Library.CreateFileAction(getLookup()),
				new Library.CreateFolderAction(getLookup()),
				new Library.DeleteAction(getLookup()),
			};
	}

	public final class CreateFileAction extends AbstractAction
	{
		private Library node;

		public CreateFileAction(Lookup lookup)
		{
			node = lookup.lookup(Library.class);

			putValue(AbstractAction.NAME, "Create File");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (node != null)
			{
				try
				{
					node.createFile();
				}
				catch(Exception ex)
				{
					//Exceptions.printStackTrace(ex);
				}
			}
		}
	}
	
	public final class CreateFolderAction extends AbstractAction
	{
		private Library node;

		public CreateFolderAction(Lookup lookup)
		{
			node = lookup.lookup(Library.class);

			putValue(AbstractAction.NAME, "Create Folder");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (node != null)
			{
				try
				{
					node.createFolder();
				}
				catch(Exception ex)
				{
					//Exceptions.printStackTrace(ex);
				}
			}
		}
	}
	
	public final class DeleteAction extends AbstractAction
	{
		private Library node;

		public DeleteAction(Lookup lookup)
		{
			node = lookup.lookup(Library.class);

			putValue(AbstractAction.NAME, "Delete Library");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (node != null)
			{
				try
				{
					node.delete();
				}
				catch(Exception ex)
				{
					//Exceptions.printStackTrace(ex);
				}
			}
		}
	}
	
	public void delete()
	{
		if (m_file.listFiles().length > 0)
		{
			JOptionPane.showMessageDialog(null, "The Library folder must be empty before it can be deleted!");
			return;
		}
		
		// prompt to verify deletion
		int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this Library?", "Delete Library", JOptionPane.YES_NO_OPTION);
		
		if (response == JOptionPane.YES_OPTION)
		{
			try
			{
				m_file.delete();
				
				destroy();
			}
			catch(IOException ex)
			{
			}
		}		
	}
	
	public void createFile()
	{
		try
		{
			// prompt for the new file name
			String filename = JOptionPane.showInputDialog(null, "New File Name", "File");
			
			if (filename.isEmpty())
				return;
			
			File newfile = new File(getFilePath() + "/" + filename);
			
			if (newfile.exists())
			{
				JOptionPane.showMessageDialog(null, "This file already exists!");
				return;
			}
			
			newfile.createNewFile();
			
			SourceFile sf = new SourceFile(newfile);
			
			addNode(sf);

			sf.select();
		}
		catch(IOException ex)
		{
		}
	}

	public void createFolder()
	{
		// prompt for the new folder name
		String foldername = JOptionPane.showInputDialog(null, "New Folder Name", "Folder");
		
		if (foldername.isEmpty())
			return;
		
		File newfolder = new File(getFilePath() + "/" + foldername);
		
		if (newfolder.exists())
		{
			JOptionPane.showMessageDialog(null, "This file already exists!");
			return;
		}
		
		newfolder.mkdir();
		
		SourceFolder sf = new SourceFolder(newfolder);
		
		addNode(sf);
		
		sf.select();
	}
}
