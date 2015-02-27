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
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class SketchManager extends TreeNode
{
	// declarations
	public static SketchManager Current;

	private Children m_nodes;
	
	// Constructors
	public SketchManager()
	{
		super(new Children.Array(), "Sketches", "/com/nerduino/resources/SketchManager16.png");
		
		m_nodes = this.getChildren();
		
		Current = this;
	}
	
	
	// Properties
	public void readSketchList()
	{
		//m_nodes.clear();
		
		String path = ArduinoManager.Current.getArduinoPath() + "/Sketches";
		
		File f = new File(path);
		
		if (f.exists())
		{
			File[] files = f.listFiles();

			for(File ff : files)
			{
				if (ff.isDirectory())
				{
					if (!ff.getName().equalsIgnoreCase("source"))
					{
						// look to see if this directory contains a file with a pde or ino extension
						File[] list = ff.listFiles();

						for(File l : list)
						{
							String name = l.getAbsolutePath();

							if (name.endsWith(".pde") || name.endsWith(".ino"))
							{
								try
								{
									Sketch sw = new Sketch(name);

									addSketch(sw);
								}
								catch(IOException ex)
								{
									Exceptions.printStackTrace(ex);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public Object[] getSketchList()
	{
		Node[] nodes = m_nodes.getNodes();
		
		ArrayList<String> list = new ArrayList<String>();
		
		for(Node n : nodes)
		{
			Sketch sw = (Sketch) n;
			
			list.add(sw.getName());
		}
		
		return list.toArray(); 
	}
	
	public void addSketch(Sketch ns)
	{
		if (ns != null && !contains(ns))
		{
			org.openide.nodes.Node[] nodes = new org.openide.nodes.Node[1];
			nodes[0] = ns;
			
			m_nodes.add(nodes);
		}
	}
	
	public void removeSketch(Sketch sketch)
	{
		if (sketch != null && contains(sketch))
		{
			Node[] nodes = new Node[1];
			nodes[0] = sketch;

			m_nodes.remove(nodes);
		}	
	}
	
	public boolean contains(Sketch sketch)
	{
		for(int i = 0; i < m_nodes.getNodesCount(); i++)
		{
			Sketch node = (Sketch) m_nodes.getNodeAt(i);
		
			if (node == sketch)
				return true;
		}
		
		return false;
	}

	public Sketch createNewSketch()
	{
		Sketch nu = new Sketch();
		
		nu.setName(getUniqueName(nu.getName()));
		
		// show the configure dialog
		SketchConfigDialog dialog = new SketchConfigDialog(new javax.swing.JFrame(), true);
		
		dialog.setSketch(nu);
		dialog.setVisible(true);
		
		nu = dialog.m_sketch;

		//nu.setName(getUniqueName(nu.getName()));
		
		//if (nu != null)
		//	addSketch(nu);
		
		return nu;
	}
	
	public Sketch getSketch(String name)
	{
		if (name == null || name.isEmpty())
			return null;
			
		for(int i = 0; i < m_nodes.getNodesCount(); i++)
		{
			Sketch sketch = (Sketch) m_nodes.getNodeAt(i);
		
			if (sketch.getName().equals(name))
				return sketch;
		}
		
		return null;
	}
	
	@Override
	public Action[] getActions(boolean context)
	{
		return new Action[]
			{
				new SketchManager.CreateSketchAction(getLookup())
			};
	}

	public final class CreateSketchAction extends AbstractAction
	{
		private SketchManager node;

		public CreateSketchAction(Lookup lookup)
		{
			node = lookup.lookup(SketchManager.class);

			putValue(AbstractAction.NAME, "Create Sketch");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (node != null)
			{
				try
				{
					node.createNewSketch();
				}
				catch(Exception ex)
				{
					//Exceptions.printStackTrace(ex);
				}
			}
		}
	}
}
