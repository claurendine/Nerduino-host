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

import com.nerduino.core.AppManager;
import com.nerduino.core.ContextAwareInstance;
import com.nerduino.nodes.TreeNode;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Scroll extends TreeNode
{
	// Declarations
	boolean m_configured = false;
	String m_source = "";
	
	// Constructors
    public Scroll()
    {
        super(new Children.Array(), "Scroll", "/com/nerduino/resources/Scroll16.png");
    
		m_canDelete = true;
		m_canRename = true;
	}
	
	@Override
	public TopComponent getTopComponent()
	{
		//Loading the multiview windows:
		FileObject multiviewsFolder = FileUtil.getConfigFile("scrollmultiviews");
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
		return m_name + ".xml";
	}
	
	public String getSource()
	{
		return m_source;
	}

	public void setSource(String source)
	{
		m_source = source;
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
		/*
		 if (m_editor != null)
		 {
		 m_html = m_editor.getDocument();
			
		 writeHtmlFile();

		 AppManager.Current.saveConfiguration();
		 }
		 */
	}
	
	@Override
	public Action[] getActions(boolean context) 
	{
		return new Action[]
		{
			new TreeNode.TreeNodeAction(getLookup()),
			SystemAction.get(RenameAction.class),
			new Scroll.CloneNodeAction(getLookup()),
			new Scroll.DeleteNodeAction(getLookup())
		};
	}
	
	public final class CloneNodeAction extends AbstractAction
	{
		private Scroll node;
		
		public CloneNodeAction( Lookup lookup )
		{
			node = lookup.lookup( Scroll.class );
			
			putValue( AbstractAction.NAME, "Clone");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			if ( node!= null )
				try 
				{
					node.cloneScroll();
				} 
				catch (Exception ex) 
				{
				}
		}
	}
	
	public void cloneScroll()
	{
		// make sure that the new project name is valid and unique
		TreeNode parent = (TreeNode) getParentNode();
		
		String newname = parent.getUniqueName(m_name);
		
		Scroll newScroll = new Scroll();
		
		newScroll.setName(newname);
		newScroll.setSource(m_source);
		
		ScrollManager.Current.addChild(newScroll);

		newScroll.select();
	}

	public final class DeleteNodeAction extends AbstractAction
	{
		private Scroll node;
		
		public DeleteNodeAction( Lookup lookup )
		{
			node = lookup.lookup( Scroll.class );
			
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
												 "Are you sure you want to delete this Scroll?",
												 "Scroll Manager",
												 JOptionPane.YES_NO_OPTION);

		if (resp == 0)
		{
			// close any open editors
			// TODO
			//closeView();
			
			// delete the associated html file
			String filename = getFileName();
			
			File f = new File(filename);
			
			if (f.exists())
				f.delete();
			
			ScrollManager.Current.removeChild(this);
		}
	}

	@Override
	public void onRename(String oldName, String newName)
	{
		if (AppManager.loading)
			return;

		super.onRename(oldName, newName);

		String oldFileName = ScrollManager.Current.getFilePath() + "/" + oldName + ".xml";
		String newFileName = ScrollManager.Current.getFilePath() + "/" + newName + ".xml";

		File f = new File(oldFileName);

		if (f.exists())
			f.renameTo(new File(newFileName));

		AppManager.Current.saveConfiguration();
	}

	public void writeSourceFile()
	{
		try
		{
			String filename = getFileName();

			FileWriter fw = new FileWriter(filename);

			fw.write(m_source);

			fw.close();
		}
		catch(IOException ex)
		{
			Logger.getLogger(Scroll.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void doubleClick(java.awt.event.MouseEvent evt)
	{
		showTopComponent();
	}

	@Override
	public void readXML(Element node)
	{
		setName(node.getAttribute("Name"));

		// read in the source file
		String filename = getFileName();

		File f = new File(filename);

		if (f.exists())
		{
			FileInputStream fin = null;
			try
			{
				fin = new FileInputStream(f);
				BufferedInputStream bin = new BufferedInputStream(fin);

				byte[] contents = new byte[1024];

				int bytesRead;

				String strFileContents;

				m_source = "";

				while ((bytesRead = bin.read(contents)) != -1)
				{
					strFileContents = new String(contents, 0, bytesRead);

					m_source = m_source + strFileContents;
				}
			}
			catch(IOException ex)
			{
				Logger.getLogger(Scroll.class.getName()).log(Level.SEVERE, null, ex);
			}
			finally
			{
				try
				{
					fin.close();
				}
				catch(IOException ex)
				{
					Logger.getLogger(Scroll.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}

		m_configured = true;
	}

	@Override
	public void writeXML(Document doc, Element node)
	{
		Element element = doc.createElement("Node");

		element.setAttribute("Name", getName());

		node.appendChild(element);
	}	
}
