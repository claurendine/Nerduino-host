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

package com.nerduino.actions;

import com.nerduino.core.ExplorerTopComponent;
import com.nerduino.library.NerduinoBase;
import com.nerduino.nodes.TreeNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.awt.StatusDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import processing.app.Board;
import processing.app.BoardManager;
import processing.app.Preferences;
import processing.app.Sketch;
import processing.app.SketchManager;

@ActionID(
	category = "System",
	id = "com.nerduino.actions.CompileAction")
@ActionRegistration(
	surviveFocusChange = true,
	iconBase = "com/nerduino/resources/Check16.png",
	displayName = "#CTL_CompileAction")
@ActionReferences(
{
})
@Messages("CTL_CompileAction=Nerduino")
public final class CompileAction implements ActionListener
{
	public static boolean s_busy;
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (!s_busy)
		{
			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					s_busy = true;
					
					// get the currently selected target nerduino
					TreeNode node = ExplorerTopComponent.Current.getSelectedNode();
					
					if (node instanceof NerduinoBase)
					{
						NerduinoBase nerduino = (NerduinoBase) node;
						
						if (nerduino != null)
						{
							StatusDisplayer.getDefault().setStatusText("Compiling " + nerduino.getSketch() + " ...");
							
							nerduino.compile();
							
							s_busy = false;
							return;
						}
					}
					
					StatusDisplayer.getDefault().setStatusText("Select a Nerduino to compile for.");
					
					s_busy = false;
				}
			});

			thread.start();
		}
	}	
}

