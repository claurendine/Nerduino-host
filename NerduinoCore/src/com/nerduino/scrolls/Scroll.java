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
import com.nerduino.services.ServiceManager;
import com.nerduino.uPnP.XmlUtil;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
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
import org.xml.sax.SAXException;
import com.nerduino.arduino.ArduinoSourceEditor;

public class Scroll extends TreeNode
{
	// Declarations
	boolean m_configured = false;
	boolean m_playing = false;
	String m_source = "";
	String m_trigger = null;
	String m_while = null;
	String m_until = null;
	float m_interval = 0.05f;

	static Context s_commonContext = null;
	static String s_extension = ".xml";
	public ArduinoSourceEditor m_editor;
	
	Container m_container;
	
	// Constructors
    public Scroll()
    {
        super(new Children.Array(), "Scroll", "/com/nerduino/resources/Scroll16.png");
    
		m_canDelete = true;
		m_canRename = true;
	}

	Scroll(String scrollName)
	{
        super(new Children.Array(), "Scroll", "/com/nerduino/resources/Scroll16.png");
		
		m_name = scrollName;
		m_canDelete = true;
		m_canRename = true;
	}
	
	@Override
	@SuppressWarnings({"unchecked"})
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
		return ScrollManager.Current.getFilePath() + "/" + m_name + s_extension;
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
	public Action[] getCustomActions(boolean context) 
	{
		return new Action[]
		{
			new Scroll.CloneNodeAction(getLookup()),
			new Scroll.PlayAction(getLookup())
		};
	}
	
	public void playAsync()
	{
		if (m_playing)
			return;
		
		// create thread and call play from this thread
		Thread pthread = new Thread(new Runnable() 
			{
				@Override
				public void run()
				{
					try
					{
						// create a new context for this thread
						Context context = Context.enter();
						
						play(context);
					}
					catch(Exception e)
					{
					}
				}
			});
		
		pthread.start();
	}

	public void play()
	{
		if (m_playing)
			return;
		
		if (s_commonContext == null)
		{
			// create the common context using the current thread.. assumed to be the primary ui thread
			s_commonContext = Context.enter();
		}
		
		play(s_commonContext);
	}
	
	public void play(Context context)
	{
		if (m_playing)
			return;
	
		m_playing = true;
		
		if (m_container != null)
		{
			m_container.reset();
			
			int cont = 1;
			double currentTime = 0.0;
			double lastTime = 0.0;
			long intervalTime = (long) (m_interval * 1000.0);
			
			while(cont > 0)
			{
				long startTime = System.currentTimeMillis();
				
				if (m_while != null)
				{
					Object obj = execute(context, m_while);

					if (obj != null && obj instanceof Boolean && !((Boolean) obj))
					{
						m_playing = false;		
						return;
					}
				}				
	
				cont = m_container.play(context, lastTime, currentTime);

				if (cont == 0)
				{
					m_playing = false;		
					return;
				}
				
				lastTime = currentTime;
				currentTime += m_interval;
				
				if (m_until != null)
				{
					Object obj = execute(context, m_until);

					if (obj != null && obj instanceof Boolean && !((Boolean) obj))
					{
						m_playing = false;		
						return;
					}
				}
				
				long measuredTime = System.currentTimeMillis() - startTime;
				
				try
				{
					long sleepTime = (int) (m_interval * 1000.0f) - measuredTime;
					
					if (sleepTime > intervalTime)
						sleepTime = intervalTime;
					
					if (sleepTime > 0)
						Thread.sleep(sleepTime);
				}
				catch(InterruptedException ex)
				{
					Exceptions.printStackTrace(ex);
				}
			};
		}
		
		m_playing = false;		
	}

	Object execute(Context context, String script)
	{
		try
		{
			return ServiceManager.Current.execute(context, script);
		}
		catch(Exception e)
		{
		}
				
		return true;
	}
	
	void testTrigger(Context context)
	{
		if (!m_playing && m_trigger != null)
		{
			Object ret = execute(context, m_trigger);
			
			if (ret instanceof Boolean && ((Boolean) ret))
			{
				playAsync();
			}
		}
	
	}

	void load()
	{
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
					if (fin != null)
						fin.close();
					
					parseScrollXML();
				}
				catch(IOException ex)
				{
					Logger.getLogger(Scroll.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}

		m_configured = true;
	}

	public final class PlayAction extends AbstractAction
	{
		private Scroll node;
		
		public PlayAction( Lookup lookup )
		{
			node = lookup.lookup( Scroll.class );
			
			putValue( AbstractAction.NAME, "Play");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			try 
			{
				playAsync();
			} 
			catch (Exception ex) 
			{
			}
		}
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
		try
		{
			// make sure that the new project name is valid and unique
			TreeNode parent = (TreeNode) getParentNode();
			
			String newname = parent.getUniqueName(m_name);
			
			
			String oldFileName = ScrollManager.Current.getFilePath() + "/" + m_name + s_extension;
			String newFileName = ScrollManager.Current.getFilePath() + "/" + newname + s_extension;
			
			File f = new File(oldFileName);
			
			if (f.exists())
			{
				Scroll newScroll = new Scroll(newname);

				ScrollManager.Current.addChild(newScroll);

				Path oldPath = Paths.get(oldFileName);
				Path newPath = Paths.get(newFileName);

				Files.copy(oldPath, newPath);
				
				newScroll.load();

				newScroll.select();
			}
		}
		catch(IOException ex)
		{
			Exceptions.printStackTrace(ex);
		}
	}

	@Override
	public void destroy()
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
			
			// delete the associated file
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

		if (oldName.equals(newName))
			return;
		
		setName(newName);

		
		String oldFileName = ScrollManager.Current.getFilePath() + "/" + oldName + s_extension;
		String newFileName = ScrollManager.Current.getFilePath() + "/" + newName + s_extension;

		File f = new File(oldFileName);

		if (f.exists())
			f.renameTo(new File(newFileName));
	}

	public File getFile()
	{
		return new File(getFileName());
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
	
	void parseScrollXML()
	{
		try
		{
			// clear content
			m_container = null;
			m_trigger = null;
			m_while = null;
			m_until = null;
			m_interval = 0.1f;
			
			// parse source xml
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			
			FileInputStream fis = new FileInputStream(getFileName());
			
			Document document = builder.parse(fis);
			
			Element rootElement = document.getDocumentElement();
			
			if (rootElement != null)
			{
				// look for configuration element
				Element econfig = XmlUtil.GetChildElement(rootElement, "configuration");
				
				try
				{
					m_interval = Float.parseFloat(econfig.getAttribute("interval"));
				}
				catch(Exception e)
				{
				}
				
				// look for trigger, while, until statements
				m_trigger = XmlUtil.GetChildElementText(rootElement, "trigger");
				m_while = XmlUtil.GetChildElementText(rootElement, "while");
				m_until = XmlUtil.GetChildElementText(rootElement, "until");
				
				// parse root container
				Element econtainer = XmlUtil.GetChildElement(rootElement, "container");
				
				if (econtainer != null)
				{
					m_container = new Container();
					
					m_container.loadXML(econtainer);
					
					m_container.validate();
				}
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
}
