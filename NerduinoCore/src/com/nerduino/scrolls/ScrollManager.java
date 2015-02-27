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

package com.nerduino.scrolls;

import com.nerduino.core.BaseManager;
import com.nerduino.nodes.TreeNode;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.util.Lookup;


public class ScrollManager extends BaseManager
{
	// Declarations
	public static ScrollManager Current;

	public ScrollManager()
	{
		super("Scrolls", "/com/nerduino/resources/ScrollManager16.png");
		
		Current = this;
	}
	
	@Override
	public TreeNode createNewChild()
	{
		return new Scroll();
	}

	@Override
	public String getFilePath()
	{
		return "/Users/chaselaurendine/Documents/Nerduino/Scrolls";
	}
	
	@Override
	public Action[] getActions(boolean context)
	{
		// A list of actions for this node
		return new Action[]
			{
				new ScrollManager.CreateScrollAction(getLookup())
			};
	}

	public final class CreateScrollAction extends AbstractAction
	{
		private ScrollManager node;

		public CreateScrollAction(Lookup lookup)
		{
			node = lookup.lookup(ScrollManager.class);

			putValue(AbstractAction.NAME, "Create Scroll");
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
