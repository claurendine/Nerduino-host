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

package processing.app;

import com.nerduino.nodes.TreeNode;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import processing.app.packages.Library;

public class LibraryGroup extends TreeNode
{
	// declarations
	private Children m_nodes;
	String m_relativePath;
	
	// Constructors
	public LibraryGroup(String name, String relativepath)
	{
		super(new Children.Array(), name, "/com/nerduino/resources/LibraryGroup16.png");
	
		m_relativePath = relativepath;
		m_nodes = this.getChildren();
	}

	String getFilePath()
	{
		return ArduinoManager.Current.getArduinoPath() + "/" + m_relativePath;
	}
	
	// Properties
	public void readLibraryList()
	{
		File f = new File(getFilePath());
		
		File[] files = f.listFiles();
		
		for(File ff : files)
		{
			if (ff.isDirectory())
			{
				String name = ff.getName();
				
				if (!name.equalsIgnoreCase("source"))
				{
					File hfile = new File(ff.getAbsolutePath() + "/" + name + ".h");
					
					// look to see if this directory contains a file with a .h extension
					if (hfile.exists())
					{
						Library library = new Library(ff);
						
						addLibrary(library);
					}
				}
			}
		}
	}
	
	public Object[] getLibraryList()
	{
		Node[] nodes = m_nodes.getNodes();
		
		ArrayList<String> list = new ArrayList<String>();
		
		for(Node n : nodes)
		{
			Library sw = (Library) n;
			
			list.add(sw.getName());
		}
		
		return list.toArray(); 
	}
	
	public void addLibrary(Library library)
	{
		if (library != null && !contains(library))
		{
			org.openide.nodes.Node[] nodes = new org.openide.nodes.Node[1];
			nodes[0] = library;
			
			m_nodes.add(nodes);
		}
	}
	
	public void removeLibrary(Library library)
	{
		if (library != null && contains(library))
		{
			Node[] nodes = new Node[1];
			nodes[0] = library;

			m_nodes.remove(nodes);
		}	
	}
	
	public boolean contains(Library library)
	{
		for(int i = 0; i < m_nodes.getNodesCount(); i++)
		{
			Library node = (Library) m_nodes.getNodeAt(i);
		
			if (node == library)
				return true;
		}
		
		return false;
	}

	public Library createNewLibrary()
	{
		FileWriter outFile = null;
		
		try
		{
			// prompt for the new library name
			String newname = JOptionPane.showInputDialog(null, "New Library Name?", 
				"Library", JOptionPane.QUESTION_MESSAGE);
			
			// make sure the library name is unique
			newname = getUniqueName(newname);
			
			// create the library folder
			File newfolder = new File(getFilePath() + "/" + newname);
			newfolder.mkdir();
			
			// create the default .h and .cpp files
			String hcode = "#ifndef ~class_h\n#define ~class_h\n\n#include <inttypes.h>\n\n~class ~classClass\n{\n  public:\n};\n\nextern ~classClass ~class;\n\n#endif\n";
			String cppcode = "#include \"Arduino.h\"\n#include \"~class.h\"\n\n~classClass ~class;\n";
			
			String hsource = hcode.replaceAll("~class", newname);
			String cppsource = cppcode.replaceAll("~class", newname);
			
			outFile = new FileWriter(newfolder.getAbsolutePath() + "/" + newname + ".h");
			PrintWriter out = new PrintWriter(outFile);
			
			out.print(hsource);
			out.close();
			
			outFile = new FileWriter(newfolder.getAbsolutePath() + "/" + newname + ".cpp");
			out = new PrintWriter(outFile);
			
			out.print(cppsource);
			out.close();
			
			// add the library to the librarian
			Library library = new Library(newfolder);

			addLibrary(library);
			
			return library;
		}
		catch(IOException ex)
		{
		}
		finally
		{
			try
			{
				outFile.close();
			}
			catch(IOException ex)
			{
			}
		}
		
		return null;
	}
	
	@Override
	public Action[] getActions(boolean context)
	{
		return new Action[]
			{
				new LibraryGroup.CreateLibraryAction(getLookup())
			};
	}

	public final class CreateLibraryAction extends AbstractAction
	{
		private LibraryGroup node;

		public CreateLibraryAction(Lookup lookup)
		{
			node = lookup.lookup(LibraryGroup.class);

			putValue(AbstractAction.NAME, "Create Library");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (node != null)
			{
				try
				{
					node.createNewLibrary();
				}
				catch(Exception ex)
				{
					//Exceptions.printStackTrace(ex);
				}
			}
		}
	}

}
