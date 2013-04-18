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
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

public class ArduinoManager extends TreeNode
{
	// declarations
	public static ArduinoManager Current;
	String m_arduinoPath = "/Users/chaselaurendine/Documents/Arduino";
	//ArrayList<Board> m_boards = new ArrayList<Board>();

	BoardManager m_boardManager;
	LibraryManager m_libraryManager;
	SketchManager m_sketchManager;
	
	private Children m_nodes;
	
	// Constructors
	public ArduinoManager()
	{
		super(new Children.Array(), "Arduino", "/com/nerduino/resources/Arduino16.png");
		
		m_nodes = this.getChildren();
		m_hasEditor = false;
		
		Current = this;

		m_sketchManager = new SketchManager();
		m_libraryManager = new LibraryManager();
		m_boardManager = new BoardManager();
		
		org.openide.nodes.Node[] nodes = new org.openide.nodes.Node[3];
		nodes[0] = m_sketchManager;
		nodes[1] = m_libraryManager;
		nodes[2] = m_boardManager;
		
		m_nodes.add(nodes);

		Preferences.init(null);
		
		new Base();
	}
	
	// Properties
	public String getArduinoPath()
	{
		return m_arduinoPath;
	}
	
	public void setArduinoPath(String value)
	{
		m_arduinoPath = value;
		
		m_sketchManager.readSketchList();
		m_libraryManager.readLibraryList();
		m_boardManager.readBoardDefinitions();
	}
	
	public String getBuildPath()
	{
		return getBuildFolder().getAbsolutePath();
	}
	
	public File getBuildFolder()
	{
		return new File(getArduinoPath(), "Build");
	}
}
