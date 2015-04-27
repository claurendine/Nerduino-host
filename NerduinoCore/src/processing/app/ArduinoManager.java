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

import com.nerduino.core.AppConfiguration;
import com.nerduino.nodes.TreeNode;
import java.io.File;
import javax.swing.JFileChooser;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;

public final class ArduinoManager extends TreeNode
{
	// declarations
	public static ArduinoManager Current;
	String m_arduinoPath = null;

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

		initializePlatform();
	}
	
	
	void initializePlatform()
	{
		// scan scrolls folder in background thread
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Base.initPlatform();
				Preferences.init(null);
				try
				{
					new Base();
				}
				catch(Exception ex)
				{
					Exceptions.printStackTrace(ex);
				}


				m_sketchManager.readSketchList();
				m_libraryManager.readLibraryList();
				m_boardManager.readBoardDefinitions();
			}
		}, "Arduino Platform Initialization thread");
		
		thread.start();
	}
	
	// Properties
	public String getArduinoPath()
	{
		if (m_arduinoPath == null)
		{
			boolean found = false;
			String path = AppConfiguration.Current.getParameter("ArduinoPath");

			while (!found)
			{
				if (path != null)
				{
					File pathFile = new File(path);

					if (pathFile.exists() && pathFile.isDirectory())
					{
						File preferenceFile = new File(path + "/lib/preferences.txt");
						
						if (preferenceFile.exists())
						{
							AppConfiguration.Current.setParameter("ArduinoPath", pathFile.getAbsolutePath());
							
							m_arduinoPath = path;
							return m_arduinoPath;
						}
					}
				}
				
				JFileChooser chooser;
				
				chooser = new JFileChooser(); 
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("Select the Arduino installation directory");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				//
				// disable the "All files" option.
				//
				chooser.setAcceptAllFileFilterUsed(false);
				//    
				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) 
				{ 
					path = chooser.getSelectedFile().getPath();
					System.out.println("getCurrentDirectory(): " 
						+  chooser.getCurrentDirectory());
					System.out.println("getSelectedFile() : " 
						+  chooser.getSelectedFile());
				}
				else 
				{
					System.out.println("No Selection ");
				}
			}
		}
		
		
		return m_arduinoPath;
	}
	
	public void setArduinoPath(String value)
	{
		m_arduinoPath = value;
		AppConfiguration.Current.setParameter("ArduinoPath", m_arduinoPath);
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
