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

package com.nerduino.services;

import com.nerduino.core.AppManager;
import com.nerduino.core.ContextAwareInstance;
import com.nerduino.nodes.TreeNode;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.actions.RenameAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

public class NerduinoService extends TreeNode
{
	// Declarations
	boolean m_configured = false;
	ServiceSourceEditor m_editor;
	
	File m_file;
	
	// Constructors
	public NerduinoService()
	{
        super(new Children.Array(), "Service", "/com/nerduino/resources/Service16.png");
    
		m_canDelete = true;
		m_canRename = true;
		
		m_file = null;
		m_name = getUniqueName("Service");
	}

	public NerduinoService(File file)
	{
        super(new Children.Array(), "Service", "/com/nerduino/resources/Service16.png");
    
		m_canDelete = true;
		m_canRename = true;
		
		m_file = file;
		m_name = m_file.getName();
		
		// strip off the '.js' extension
		m_name = m_name.substring(0, m_name.length() - 3);
	}
	
	public File getFile()
	{
		return m_file;
	}

	
	@Override
	@SuppressWarnings({"unchecked"})
	public TopComponent getTopComponent()
	{
		//Loading the multiview windows:
		FileObject multiviewsFolder = FileUtil.getConfigFile("servicemultiviews");
		FileObject[] kids = multiviewsFolder.getChildren();
		MultiViewDescription[] descriptionArray = new MultiViewDescription[kids.length];
		ArrayList<MultiViewDescription> listOfDescs = new ArrayList<MultiViewDescription>();
		
		for (FileObject kid : FileUtil.getOrder(Arrays.asList(kids), true))
		{
			MultiViewDescription attribute = (MultiViewDescription) kid.getAttribute("multiview");

			if (attribute instanceof ContextAwareInstance)
				attribute = ((ContextAwareInstance<MultiViewDescription>) attribute).createContextAwareInstance(Lookups.fixed(this));
			
			listOfDescs.add(attribute);
		}

		for (int i = 0; i < listOfDescs.size(); i++)
		{
			descriptionArray[i] = listOfDescs.get(i);
		}

		CloneableTopComponent ctc = MultiViewFactory.createCloneableMultiView(descriptionArray, descriptionArray[0]);

		return ctc;
	}

	public String getFileName()
	{
		return m_name + ".js";
	}

	@Override
	public void configure()
	{
		// show the configure data point dialog
		//SkitConfigDialog dialog = new SkitConfigDialog(new javax.swing.JFrame(), true);
		
		//dialog.setSkit(this);
		//dialog.setVisible(true);
		
		// update the tree in case the appearance changed
		//NerduinoTreeView.Current.modelUpdated(this);
	}

	@Override
	public void onEditorUpdated()
	{
	}
	
	@Override
	public Action[] getActions(boolean context) 
	{
		return new Action[]
		{
			new TreeNode.TreeNodeAction(getLookup()),
			SystemAction.get(RenameAction.class),
			new NerduinoService.DeleteNodeAction(getLookup())
		};
	}
	
	public final class DeleteNodeAction extends AbstractAction
	{
		private NerduinoService node;
		
		public DeleteNodeAction( Lookup lookup )
		{
			node = lookup.lookup( NerduinoService.class );
			
			putValue( AbstractAction.NAME, "Delete");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			if ( node!= null )
				try 
				{
					delete();
				} 
				catch (Exception ex) 
				{
				}
		}
	}
	

	public void delete()
	{
		// prompt the user to confirm the deletion
		int resp = JOptionPane.showConfirmDialog(null,
												 "Are you sure you want to delete this Service?",
												 "Service Manager",
												 JOptionPane.YES_NO_OPTION);

		if (resp == 0)
		{
			// close any open editors
			// TODO
			//closeView();
			
			// delete the associated html file
			String filename = ServiceManager.Current.getFilePath() + "/" + getFileName();

			File f = new File(filename);
			
			if (f.exists())
				f.delete();
			
			ServiceManager.Current.removeChild(this);
		}
	}

	@Override
	public void onRename(String oldName, String newName)
	{
		if (AppManager.loading)
		{
			return;
		}

		super.onRename(oldName, newName);

		String oldFileName = ServiceManager.Current.getFilePath() + "/" + oldName + ".js";
		String newFileName = ServiceManager.Current.getFilePath() + "/" + newName + ".js";

		File f = new File(oldFileName);

		if (f.exists())
		{
			f.renameTo(new File(newFileName));
		}

		AppManager.Current.saveConfiguration();
	}

	@Override
	public void doubleClick(java.awt.event.MouseEvent evt)
	{
		showTopComponent();
	}
	
	public String getSource()
	{
		String source = "";
		
		
		if (m_editor != null)
		{
			// get the current source within the editor (it may not have been saved)
			source = m_editor.getText();
		}
		else
		{
			// read the content of the source file
			StringBuffer buf = null;
		    FileReader fr = null;
			
			try 
			{
				fr = new FileReader(m_file.getAbsolutePath());
				
				int theChar;
				buf = new StringBuffer();

				while( (theChar = fr.read()) != -1 ) 
				{
					buf.append( (char) theChar );
				}
			}
			catch( IOException ioe ) 
			{
				ioe.printStackTrace();
				return "";
			}
			finally 
			{
				if( fr != null ) 
				{
					try 
					{
						fr.close();
					}
					catch( IOException ioe ) 
					{
					}
				}
			}
			
			if (buf != null)
				source = buf.toString();
		}
			
		return source;
	}
	
	public boolean apply()
	{
		String source = "";
		
		
		if (m_editor != null)
		{
			// get the current source within the editor (it may not have been saved)
			source = m_editor.getText();
		}
		else
		{
			// read the content of the source file
			StringBuffer buf = null;
		    FileReader fr = null;
			
			try 
			{
				fr = new FileReader(m_file.getAbsolutePath());
				
				int theChar;
				buf = new StringBuffer();

				while( (theChar = fr.read()) != -1 ) 
				{
					buf.append( (char) theChar );
				}
			}
			catch( IOException ioe ) 
			{
				ioe.printStackTrace();
				return false;
			}
			finally 
			{
				if( fr != null ) 
				{
					try 
					{
						fr.close();
					}
					catch( IOException ioe ) 
					{
					}
				}
			}
			
			if (buf != null)
				source = buf.toString();
		}
			
		return ServiceManager.Current.applySource(source);
	}
}
