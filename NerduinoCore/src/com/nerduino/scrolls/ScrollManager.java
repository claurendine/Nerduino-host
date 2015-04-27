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

import com.nerduino.core.AppConfiguration;
import com.nerduino.core.AppManager;
import com.nerduino.core.BaseManager;
import com.nerduino.library.DeviceTypeEnum;
import com.nerduino.library.NerduinoStatusEnum;
import com.nerduino.library.RemoteDataPoint;
import com.nerduino.nodes.TreeNode;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.util.Lookup;
import org.mozilla.javascript.Context;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;



public class ScrollManager extends BaseManager
{
	// Declarations
	public static ScrollManager Current;
	
	float m_scanInterval = 1.0f;
	boolean m_loading;

	public ScrollManager()
	{
		super("Scrolls", "/com/nerduino/resources/ScrollManager16.png");
		
		Current = this;
		
		m_scanInterval = AppConfiguration.Current.getParameterFloat("ScrollScanInterval", 1.0f);
		
		loadScrolls();
		scanScrolls();
	}
	
	private void loadScrolls()
	{
		// scan scrolls folder in background thread
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// get the path to the scrolls folder
				String filePath = getFilePath();

				File directory = new File(filePath);

				if (directory.exists() && directory.isDirectory())
				{
					File[] files = directory.listFiles();

					for(File f : files)
					{
						String filename = f.getName();

						if (f.isFile() && filename.toLowerCase().endsWith(".xml"))
						{
							// strip off the file extension for the scroll name
							String scrollName = filename.substring(0, filename.length() - 4);

							// scan for scroll files
							// create and load each scroll
							Scroll scroll = new Scroll(scrollName);

							scroll.load();

							addChild(scroll);					
						}
					}

					/*
					try
					{
						// register for notification of file changes in the scrolls folder
						WatchService watcher = FileSystems.getDefault().newWatchService();

						Path path = Paths.get(filePath);
						WatchKey watchKey = path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

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
								if (childfilename.toLowerCase().endsWith(".xml"))
								{
									// strip off the file extension for the scroll name
									String scrollName = childfilename.substring(0, childfilename.length() - 4);

									if (kind == ENTRY_CREATE)
									{
										// make sure this scroll is not already loaded
										Node node = getChildren().findChild(scrollName);

										if (node == null)
										{
											// create and load each scroll
											Scroll scroll = new Scroll(scrollName);

											scroll.load();

											addChild(scroll);
										}
									}
									else if (kind == ENTRY_DELETE)
									{
										// remove the scroll
										Node node = getChildren().findChild(scrollName);

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
		}, "Scroll loading thread");

		thread.start();
	}

	private void scanScrolls()
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{	
					while(true)
					{
						if (!AppManager.loading)
						{
							Context context = Context.enter();

							for (Object obj : getChildren().snapshot())
							{
								Scroll scroll = (Scroll) obj;

								scroll.testTrigger(context);
							}
						}
						
						Thread.sleep((int) (m_scanInterval * 1000.0));
					}
				}
				catch(Exception e)
				{
				}
			}
		}, "Scroll trigger thread");

		thread.start();
	
	}
	
	public float getScanInterval()
	{
		return m_scanInterval;
	}
	
	public void setScanInterval(float value)
	{
		m_scanInterval = value;
		
		AppConfiguration.Current.setParameter("ScrollScanInterval", Float.toString(value));
	}
	
	@Override
	public TreeNode createNewChild()
	{
		Scroll newScroll = new Scroll(getUniqueName("Scroll"));
		
		String source = 
			"<?xml version='1.0' encoding='UTF-8'?>\r\n" 
			+ "<root>\r\n"
			+ "  <configuration interval='0.05'>\r\n"
			+ "  <!--\r\n"
			+ "  <trigger>javascript resulting in boolean, when true then start playing the scroll content</trigger>\r\n"
			+ "  -->\r\n"
			+ "  <container time='0.0' span='10.0' loopCount='1'>\r\n"
			+ "    <!--\r\n"
			+ "    <script time='0.5'>javacript</script>\r\n"
			+ "    <keyFrame time='0.4' span='0.5' prototype='pointname=%x%;' inerpolate='linear'>\r\n"
			+ "      <sample time='0.0' x='5.0'/>\r\n"
			+ "    </keyFrame>\r\n"
			+ "    <container time='1.0' span='3.0' loopCount='3'>\r\n"
			+ "      <script time='0.0'>javacript</script>\r\n"
			+ "    </container>\r\n"
			+ "    -->\r\n"
			+ "  </container>\r\n"
			+ "</root>\r\n";

		newScroll.setSource(source);
		
		newScroll.writeSourceFile();
		
		return newScroll;
	}

	@Override
	public String getFilePath()
	{
		return AppManager.Current.getDataPath() + "/Scrolls";
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

	public Scroll getScroll(String scrollName)
	{
		for (Object obj : getChildren().snapshot())
		{
			Scroll scroll = (Scroll) obj;
			
			if (scroll.getName().equals(scrollName))
			{
				return scroll;
			}
		}	
		
		return null;
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
				}
			}
		}
	}
		
	@Override
	public Node.PropertySet[] getPropertySets()
	{
		final Sheet.Set sheet = Sheet.createPropertiesSet();

		sheet.setDisplayName("Scroll Settings");
		addProperty(sheet, float.class, null, "ScanInterval", "The interval (in seconds) that scroll triggers are evaluated.");

		return new Node.PropertySet[]
			{
				sheet
			};
	}
}
