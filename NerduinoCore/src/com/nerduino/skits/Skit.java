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

package com.nerduino.skits;

import com.nerduino.actions.StandardAction;
import com.nerduino.core.AppManager;
import com.nerduino.core.ContextAwareInstance;
import com.nerduino.nodes.TreeNode;
import com.nerduino.processing.app.ArduinoSourceEditor;
import com.nerduino.webhost.WebHost;
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

public class Skit extends TreeNode
{
	// Declarations
	private static ConfigureAction s_configureAction;
	private static Skit s_activeSkit;
	boolean m_configured = false;
	String m_skitMode = "Structured";
	String m_html;
	public ArduinoSourceEditor m_editor;

	public String getFileName()
	{
		// temporarily returning xml extension so that the editor will not kickstart a synchronization.. it hangs
		//return m_name + ".html";
		return m_name + ".xml";
	}

	public String getSource()
	{
		return m_html;
	}

	public void setSource(String html)
	{
		m_html = html;
	}

	public File getFile()
	{
		return new File(getHtmlFileName());
	}
	
	final class ConfigureAction extends StandardAction
	{
		public ConfigureAction()
		{
			super("Configure Skit", "/com/nerduino/resources/ConfigureSkit32.png");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (s_activeSkit != null)
				s_activeSkit.configure();
		}
	}

	// Constructors
	public Skit()
	{
		super(new Children.Array(), "NewSkit", "/com/nerduino/resources/Skit16.png");
		
		m_canDelete = true;
		m_canRename = true;
	}

	@Override
	public void configure()
	{
		// show the configure data point dialog
		SkitConfigDialog dialog = new SkitConfigDialog(new javax.swing.JFrame(), true);

		dialog.setSkit(this);
		dialog.setVisible(true);

		// update the tree in case the appearance changed
		//NerduinoTreeView.Current.modelUpdated(this);
	}

	@Override
	public TopComponent getTopComponent()
	{
		//Loading the multiview windows:
		FileObject multiviewsFolder = FileUtil.getConfigFile("skitmultiviews");
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
			new CloneNodeAction(getLookup()),
			new DeleteNodeAction(getLookup())
		};
	}
	
	public final class CloneNodeAction extends AbstractAction
	{
		private Skit node;
		
		public CloneNodeAction( Lookup lookup )
		{
			node = lookup.lookup( Skit.class );
			
			putValue( AbstractAction.NAME, "Clone");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			if ( node!= null )
				try 
				{
					node.cloneSkit(node);
				} 
				catch (Exception ex) 
				{
				}
		}
	}
	
	public void cloneSkit(Skit skit)
	{
		// make sure that the new project name is valid and unique
		TreeNode parent = (TreeNode) getParentNode();
		
		String newname = parent.getUniqueName(m_name);
		
		Skit newSkit = new Skit();
		
		newSkit.setName(newname);
		newSkit.setSource(m_html);
		
		SkitManager.Current.addChild(newSkit);

		newSkit.select();
	}

	public final class DeleteNodeAction extends AbstractAction
	{
		private Skit node;
		
		public DeleteNodeAction( Lookup lookup )
		{
			node = lookup.lookup( Skit.class );
			
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
												 "Are you sure you want to delete this Skit?",
												 "Skit Manager",
												 JOptionPane.YES_NO_OPTION);

		if (resp == 0)
		{
			// close any open editors
			closeTopComponent();
			
			// delete the associated html file
			String filename = getHtmlFileName();
			
			File f = new File(filename);
			
			if (f.exists())
				f.delete();
			
			SkitManager.Current.removeChild(this);
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

		String oldFileName = WebHost.Current.getWebRoot() + "/" + oldName + ".html";
		String newFileName = WebHost.Current.getWebRoot() + "/" + newName + ".html";

		File f = new File(oldFileName);

		if (f.exists())
			f.renameTo(new File(newFileName));

		SkitManager.Current.updateSkitIndexHtml();

		AppManager.Current.saveConfiguration();
	}

	public void writeHtmlFile()
	{
		try
		{
			String filename = getHtmlFileName();

			FileWriter fw = new FileWriter(filename);

			fw.write(m_html);

			fw.close();
		}
		catch(IOException ex)
		{
			Logger.getLogger(Skit.class.getName()).log(Level.SEVERE, null, ex);
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
		m_skitMode = node.getAttribute("Mode");

		// read in the html file
		String filename = getHtmlFileName();

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

				m_html = "";

				while ((bytesRead = bin.read(contents)) != -1)
				{
					strFileContents = new String(contents, 0, bytesRead);

					m_html = m_html + strFileContents;
				}
			}
			catch(IOException ex)
			{
				Logger.getLogger(Skit.class.getName()).log(Level.SEVERE, null, ex);
			}
			finally
			{
				try
				{
					fin.close();
				}
				catch(IOException ex)
				{
					Logger.getLogger(Skit.class.getName()).log(Level.SEVERE, null, ex);
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
		element.setAttribute("Mode", m_skitMode);

		node.appendChild(element);
	}

	public String getHtmlFileName()
	{
		return WebHost.Current.getWebRoot() + "/" + getName() + ".html";
	}
}
