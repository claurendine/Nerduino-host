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
import com.nerduino.uPnP.XmlUtil;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.mozilla.javascript.Context;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class NerduinoService extends TreeNode
{
	// Declarations
	boolean m_configured = false;
	ServiceSourceEditor m_editor;
	static String s_extension = ".xml";
	
	ArrayList<ServiceTrigger> m_triggers = new ArrayList<ServiceTrigger>();
	String m_source;
	
	// Constructors
	public NerduinoService()
	{
        super(new Children.Array(), "Service", "/com/nerduino/resources/Service16.png");
    
		m_canDelete = true;
		m_canRename = true;
		
		m_name = getUniqueName("Service");
	}

	public NerduinoService(String name)
	{
        super(new Children.Array(), "Service", "/com/nerduino/resources/Service16.png");
    
		m_canDelete = true;
		m_canRename = true;
		
		m_name = name;
	}

	
	public NerduinoService(File file)
	{
        super(new Children.Array(), "Service", "/com/nerduino/resources/Service16.png");
    
		m_canDelete = true;
		m_canRename = true;
		
		m_name = file.getName();
		
		// strip off the extension
		m_name = m_name.substring(0, m_name.length() - s_extension.length());
		
		parseXML();
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
			{
				Lookup lu = Lookups.fixed(this);
				attribute = ((ContextAwareInstance<MultiViewDescription>) attribute).createContextAwareInstance(lu);
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

	public String getFileName()
	{
		return ServiceManager.Current.getFilePath() + "/" + m_name + s_extension;
	}

	
	@Override
	public void destroy()
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
			
			// delete the associated file
			String filename = getFileName();

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
			return;

		if (oldName.equals(newName))
			return;
		
		setName(newName);
		
		String oldFileName = ServiceManager.Current.getFilePath() + "/" + oldName + s_extension;
		String newFileName = ServiceManager.Current.getFilePath() + "/" + newName + s_extension;
		
		File f = new File(oldFileName);
		
		if (f.exists())
			f.renameTo(new File(newFileName));
	}

	public String getSource()
	{
		if (m_editor != null)
		{
			// trigger the editor to save its content
			//m_editor.
			
			parseXML();
		}
		
		return m_source;
	}
	
	public boolean apply()
	{			
		return ServiceManager.Current.applySource(getSource());
	}
	
	@Override
	public Action[] getCustomActions(boolean context) 
	{
		return new Action[]
		{
			new NerduinoService.CloneNodeAction(getLookup()),
		};
	}

	void testTriggers(Context context)
	{
		for(ServiceTrigger trigger : m_triggers)
		{
			trigger.testTrigger(context);
		}
	}

	void load()
	{
		parseXML();

		m_configured = true;
	}

	public final class CloneNodeAction extends AbstractAction
	{
		private NerduinoService node;
		
		public CloneNodeAction( Lookup lookup )
		{
			node = lookup.lookup( NerduinoService.class );
			
			putValue( AbstractAction.NAME, "Clone");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			if ( node!= null )
				try 
				{
					node.cloneService();
				} 
				catch (Exception ex) 
				{
				}
		}
	}
	
	public void cloneService()
	{
		try
		{
			// make sure that the new project name is valid and unique
			TreeNode parent = (TreeNode) getParentNode();
			
			String newname = parent.getUniqueName(m_name);
			
			
			String oldFileName = ServiceManager.Current.getFilePath() + "/" + m_name + s_extension;
			String newFileName = ServiceManager.Current.getFilePath() + "/" + newname + s_extension;
			
			File f = new File(oldFileName);
			
			if (f.exists())
			{
				NerduinoService newService = new NerduinoService(newname);

				ServiceManager.Current.addChild(newService);

				Path oldPath = Paths.get(oldFileName);
				Path newPath = Paths.get(newFileName);

				Files.copy(oldPath, newPath);
				
				newService.load();

				newService.select();
			}
		}
		catch(IOException ex)
		{
			Exceptions.printStackTrace(ex);
		}
	}

	void parseXML()
	{
		try
		{
			// clear content
			m_source = null;
			m_triggers.clear();
			
			// parse source xml
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			
			FileInputStream fis = new FileInputStream(getFileName());
			
			Document document = builder.parse(fis);
			
			Element rootElement = document.getDocumentElement();
			
			if (rootElement != null)
			{
				// look for triggers
				NodeList nodes = rootElement.getElementsByTagName("trigger");
				
				for(int i = 0; i < nodes.getLength(); i++)
				{
					Element element = (Element) nodes.item(i);

					ServiceTrigger trigger = new ServiceTrigger(this);

					trigger.loadXML(element);

					m_triggers.add(trigger);
				}
				
				// parse the source javascript
				m_source = XmlUtil.GetChildElementText(rootElement, "source");
			}
		}
		catch(ParserConfigurationException ex)
		{
//			Exceptions.printStackTrace(ex);
		}
		catch(FileNotFoundException ex)
		{
//			Exceptions.printStackTrace(ex);
		}
		catch(SAXException ex)
		{
//			Exceptions.printStackTrace(ex);
		}
		catch(IOException ex)
		{
//			Exceptions.printStackTrace(ex);
		}
	}
	
	public void writeSourceFile(String source)
	{
		try
		{
			String filename = getFileName();
			
			FileWriter fw = new FileWriter(filename);
			
			fw.write(source);
			
			fw.close();
		}
		catch(IOException ex)
		{
			Logger.getLogger(NerduinoService.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
}
