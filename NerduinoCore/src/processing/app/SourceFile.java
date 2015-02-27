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

import com.nerduino.core.ContextAwareInstance;
import com.nerduino.nodes.TreeNode;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

public class SourceFile extends TreeNode
{
	File m_file;
	
	public SourceFile(File file)
	{
		super(new Children.Array(), "File", "/com/nerduino/resources/File.png");
		
		m_file = file;
		m_name = m_file.getName();
	}

	public File getFile()
	{
		return m_file;
	}
	
	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public TopComponent getTopComponent()
	{
		//Loading the multiview windows:
		FileObject multiviewsFolder = FileUtil.getConfigFile("sourcemultiviews");
		FileObject[] kids = multiviewsFolder.getChildren();
		MultiViewDescription[] descriptionArray = new MultiViewDescription[kids.length];
		List<MultiViewDescription> listOfDescs = new ArrayList();

		for (FileObject kid : FileUtil.getOrder(Arrays.asList(kids), true))
		{
			Enumeration<String> attrs = kid.getAttributes();
			
			MultiViewDescription attribute = (MultiViewDescription) kid.getAttribute("multiview");

			if (attribute instanceof ContextAwareInstance)
			{
				attribute = ((ContextAwareInstance<MultiViewDescription>) attribute).createContextAwareInstance(Lookups.fixed(this));
			}

			listOfDescs.add(attribute);
		}

		for (int i = 0; i < listOfDescs.size(); i++)
		{
			descriptionArray[i] = listOfDescs.get(i);
		}

		CloneableTopComponent ctc = MultiViewFactory.createCloneableMultiView(descriptionArray, descriptionArray[0]);


		return ctc;
	}
	
	@Override
	public Node.PropertySet[] getPropertySets()
	{
		final Sheet.Set sheet = Sheet.createPropertiesSet();
	
		sheet.setDisplayName("File Information");

		addProperty(sheet, String.class, null, "Name", "Name");

		return new Node.PropertySet[] { sheet };
	}
	
	@Override
	public Action[] getActions(boolean context)
	{
		return new Action[]
			{
				new TreeNodeAction(getLookup()),
				new SourceFile.RenameAction(getLookup()),
				new SourceFile.DeleteAction(getLookup()),
			};
	}

	public final class RenameAction extends AbstractAction
	{
		private SourceFile node;

		public RenameAction(Lookup lookup)
		{
			node = lookup.lookup(SourceFile.class);

			putValue(AbstractAction.NAME, "Rename File");
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
					//Exceptions.printStackTrace(ex);
				}
			}
		}
	}
		
	public final class DeleteAction extends AbstractAction
	{
		private SourceFile node;

		public DeleteAction(Lookup lookup)
		{
			node = lookup.lookup(SourceFile.class);

			putValue(AbstractAction.NAME, "Delete File");
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
	
	public void rename()
	{
		String newname = JOptionPane.showInputDialog(null, "New File Name:", getName());
		
		if (!newname.matches(getName()))
		{
			String fullpath = m_file.getParent() + "/" + newname;
			
			File newfile = new File(fullpath);
			
			if (newfile.exists())
			{
				JOptionPane.showMessageDialog(null, "This file name already exists!");
				return;
			}
			
			m_file.renameTo(newfile);
	
			setName(newname);
		}
	}
	
	public void delete()
	{
		// prompt to verify deletion
		int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this file?", "Delete File", JOptionPane.YES_NO_OPTION);
		
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
}

