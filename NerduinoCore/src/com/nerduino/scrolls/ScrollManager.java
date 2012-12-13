package com.nerduino.scrolls;

import com.nerduino.core.BaseManager;
import com.nerduino.nodes.TreeNode;
import com.nerduino.services.ServiceManager;
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
