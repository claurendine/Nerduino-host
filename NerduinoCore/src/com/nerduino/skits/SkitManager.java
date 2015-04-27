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

import com.nerduino.core.BaseManager;
import com.nerduino.nodes.TreeNode;
import com.nerduino.webhost.WebHost;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import static java.nio.charset.CoderResult.OVERFLOW;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.nodes.Node;
import org.openide.util.Lookup;


public class SkitManager extends BaseManager
{
	// Declarations
	public static SkitManager Current;
	boolean m_loading = false;
	
	public SkitManager()
	{
		super("Skits", "/com/nerduino/resources/SkitManager16.png");
	
		Current = this;
		
		loadSkits();
	}

	private void loadSkits()
	{
		// scan skits folder in background thread
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// get the path to the skits folder
				String filePath = getFilePath();

				File directory = new File(filePath);

				if (directory.exists() && directory.isDirectory())
				{
					File[] files = directory.listFiles();

					for(File f : files)
					{
						String filename = f.getName();

						if (filename.startsWith("bee.") || filename.startsWith("env."))
							continue; // skip the internal data files
						
						if (f.isFile() && filename.toLowerCase().endsWith(Skit.s_extension))
						{
							// strip off the file extension for the scroll name
							String skitName = filename.substring(0, filename.length() - Skit.s_extension.length());

							// create and load each skit
							Skit skit = new Skit(skitName);

							addChild(skit);					
						}
					}

					/*
					try
					{
						// register for notification of file changes in the scrolls folder
						WatchService watcher = FileSystems.getDefault().newWatchService();

						Path path = Paths.get(filePath);
						WatchKey watchKey = path.register(watcher, ENTRY_CREATE, ENTRY_DELETE);

						for (;;) 
						{
							// wait for key to be signaled
							WatchKey key;
							try 
							{
								key = watcher.take();
							} 
							catch (InterruptedException x) 
							{
								return;
							}

							for (WatchEvent<?> event: key.pollEvents()) 
							{
								WatchEvent.Kind<?> kind = event.kind();

								if (kind == OVERFLOW) 
								{
									continue;
								}

								WatchEvent<Path> ev = (WatchEvent<Path>)event;
								Path filepath = ev.context();

								String childfilename = filepath.toString();

								// verify that the file has an xml extension
								if (childfilename.toLowerCase().endsWith(Skit.s_extension))
								{
									// strip off the file extension for the scroll name
									String skitName = childfilename.substring(0, childfilename.length() - Skit.s_extension.length());

									if (kind == ENTRY_CREATE)
									{
										// ignore any file called 'index'
										if (!"index".equals(skitName))
										{
											// make sure this skit is not already loaded
											Node node = getChildren().findChild(skitName);

											if (node == null)
											{
												// create and load each skit
												Skit skit = new Skit(skitName);

												addChild(skit);
											}
										}
									}
									else if (kind == ENTRY_DELETE)
									{
										// remove the scroll
										Node node = getChildren().findChild(skitName);

										if (node != null)
										{
											removeChild((TreeNode) node);
										}
									}
								}
							}

							if (!key.reset()) 
							{
								break;
							}
						}
					}
					catch(IOException ex)
					{
					}
					*/
				}
			}
		}, "Skit loading thread");

		thread.start();
	}
	
	@Override
	public TreeNode createNewChild()
	{
		return new Skit();
	}
	
	@Override
	public boolean configureChild(TreeNode child)
	{
		Skit ns = (Skit) child;
		
		ns.setName(getUniqueName(ns.getName()));
		
		// show the configure dialog
		SkitConfigDialog dialog = new SkitConfigDialog(new javax.swing.JFrame(), true);
		
		dialog.setSkit(ns);
		dialog.setVisible(true);
		
		ns = dialog.m_skit;
		
		if (ns == null)
		{
			return false;
		}

		ns.setName(getUniqueName(ns.getName()));
		ns.m_configured = true;
		
		java.net.URL url;
		
		String mode = ns.getSkitMode();
		
		if (mode.equals("Structured"))
			url = getClass().getResource("/com/nerduino/resources/StructuredSkit.html");
		else if (mode.equals("Mobile Template"))
			url = getClass().getResource("/com/nerduino/resources/MobileSkit.html");
		else 
			url = getClass().getResource("/com/nerduino/resources/DesktopSkit.html");
		try
		{
			InputStream str = url.openStream();
			
			BufferedInputStream bin = new BufferedInputStream(str);

			byte[] contents = new byte[1024];

			int bytesRead=0;
			String strFileContents;
			String html = "";

			while( (bytesRead = bin.read(contents)) != -1)
			{
				strFileContents = new String(contents, 0, bytesRead);

				html = html + strFileContents;
			}		
			
			str.close();
			
			ns.m_html = html;
		}
		catch(IOException ex)
		{
			Logger.getLogger(SkitManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		ns.writeHtmlFile();
		
		return true;
	}
	
	public void updateSkitIndexHtml()
	{
		try
		{
			String filename =  WebHost.Current.getWebRoot() + "/index.html";
			
			FileWriter writer = new FileWriter(filename);
			
			writer.write("<!DOCTYPE html><html>\n");
			
			writer.write("<head><title>Nerduino</title><meta name='viewport' content='width=device-width, initial-scale=1'>\n"
					+ "<link rel='stylesheet' href='http://code.jquery.com/mobile/1.2.0/jquery.mobile-1.2.0.min.css' />"
					+ "<script src='http://code.jquery.com/jquery-1.8.2.min.js'></script>"
					+ "<script src='http://code.jquery.com/mobile/1.2.0/jquery.mobile-1.2.0.min.js'></script></head>\n");
			
			//writer.write("<body><div data-role='page'><div data-role='header'><h1>Nerduino</h1></div>\n");
			writer.write("<body><div data-role='page'><div data-role='header'><h1><img src='Nerduino32.png'></h1></div>\n");
			writer.write("<ul data-role='listview' data-inset='true'>\n");
			
			Node[] nodes = SkitManager.Current.getChildren().getNodes();
			
			for(Node node : nodes)
			{
				Skit skit = (Skit) node;
				String name = skit.getName();
				String nfilename = skit.getFileName();
				
				writer.write("<li><a href='");
				writer.write(nfilename);
				writer.write("'>");
				writer.write(name);
				writer.write("</a></li>\n");
			}
			
			writer.write("</ul>\n");			
			writer.write("</div></div></body>\n");
			writer.write("</html>");
			
			writer.close();
			
			//saveSkitList();
		}
		catch(IOException ex)
		{
			Logger.getLogger(Skit.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void addChild(TreeNode node)
	{
		super.addChild(node);
		
		updateSkitIndexHtml();
	}
	
	@Override
	public void removeChild(TreeNode node)
	{
		super.removeChild(node);
		
		updateSkitIndexHtml();
	}
	
	/*	
	void saveSkitList()
	{
		if (!m_loading)
		{
			ArrayList<String> names = new ArrayList<String>();
			
			for (Object obj : getChildren().snapshot())
			{
				Skit skit = (Skit) obj;

				names.add(skit.getName());
			}
			
			String[] nameList = new String[names.size()];
			
			names.toArray(nameList);
			
			AppConfiguration.Current.setList("SkitList", nameList);
		}
	}
	*/
	
	@Override
	public String getFilePath()
	{
		return WebHost.Current.getWebRoot();
//		return ArduinoManager.Current.getArduinoPath() + "/Skits";
	}

	@Override
	public Action[] getActions(boolean context)
	{
		// A list of actions for this node
		return new Action[]
			{
				new SkitManager.CreateSkitAction(getLookup())
			};
	}

	public final class CreateSkitAction extends AbstractAction
	{
		private SkitManager node;

		public CreateSkitAction(Lookup lookup)
		{
			node = lookup.lookup(SkitManager.class);

			putValue(AbstractAction.NAME, "Create Skit");
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
				}
			}
		}
	}
}
