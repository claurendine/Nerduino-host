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
import org.openide.nodes.Children;

public class LibraryManager extends TreeNode
{
	// declarations
	public static LibraryManager Current;
	
	private Children m_nodes;
	
	// Constructors
	public LibraryManager()
	{
		super(new Children.Array(), "Libraries", "/com/nerduino/resources/LibraryManager16.png");
		
		m_nodes = this.getChildren();
		
		Current = this;
		
		addLibraryGroup(new LibraryGroup("Common", "libraries"));
		addLibraryGroup(new LibraryGroup("AVR", "hardware/arduino/avr/libraries"));
		addLibraryGroup(new LibraryGroup("SAM", "hardware/arduino/sam/libraries"));
	}
	
	public void addLibraryGroup(LibraryGroup group)
	{
		if (group != null && !contains(group))
		{
			org.openide.nodes.Node[] nodes = new org.openide.nodes.Node[1];
			nodes[0] = group;
			
			m_nodes.add(nodes);
		}
	}
		
	public boolean contains(LibraryGroup group)
	{
		for(int i = 0; i < m_nodes.getNodesCount(); i++)
		{
			LibraryGroup node = (LibraryGroup) m_nodes.getNodeAt(i);
		
			if (node == group)
				return true;
		}
		
		return false;
	}
	
	public void readLibraryList()
	{
		for(int i = 0; i < m_nodes.getNodesCount(); i++)
		{
			LibraryGroup group = (LibraryGroup) m_nodes.getNodeAt(i);
			
			group.readLibraryList();
		}
	}
}
