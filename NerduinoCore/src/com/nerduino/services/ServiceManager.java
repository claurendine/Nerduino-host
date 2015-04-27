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

import com.nerduino.core.AppConfiguration;
import com.nerduino.core.AppManager;
import com.nerduino.core.BaseManager;
import com.nerduino.library.NerduinoBase;
import com.nerduino.library.NerduinoManager;
import com.nerduino.nodes.TreeNode;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;


public class ServiceManager extends BaseManager
{
	// Declarations
	public static ServiceManager Current;
	File m_file;
	
	Context m_context;
	Scriptable m_scope;
	private Children m_nodes;
	ServiceManagerScope m_managerScope;
	
	float m_scanInterval = 1.0f;
	
	
	public ServiceManager()
	{
		super("Services", "/com/nerduino/resources/ServiceManager16.png");
		
		Current = this;
		
		m_nodes = this.getChildren();

		m_context = Context.enter();
		
		m_managerScope = new ServiceManagerScope();
		m_scope = m_context.initStandardObjects(m_managerScope);
		
		m_file = new File(getFilePath());
		
		m_scanInterval = AppConfiguration.Current.getParameterFloat("ServiceScanInterval", 1.0f);

		
		loadServices();
		scanServices();
	}

	private void loadServices()
	{
		File[] files = m_file.listFiles();
		
		if (files != null)
		{
			for(File file : files)
			{
				if (file.getName().endsWith(NerduinoService.s_extension ))
				{
					NerduinoService ns = new NerduinoService(file);

					ns.apply();

					addNode(ns);
				}
			}
		}
	}
		
	private void scanServices()
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
								NerduinoService service = (NerduinoService) obj;

								service.testTriggers(context);
							}
						}
						
						Thread.sleep((int) (m_scanInterval * 1000.0));
					}
				}
				catch(Exception e)
				{
				}
			}
		}, "Service trigger thread");

		thread.start();
	
	}
	
	public void addNode(Node node)
	{
		if (node != null && !contains(node))
		{
			org.openide.nodes.Node[] nodes = new org.openide.nodes.Node[1];
			nodes[0] = node;
			
			m_nodes.add(nodes);
		}
	}
	
	
	public boolean contains(Node node)
	{
		for(int i = 0; i < m_nodes.getNodesCount(); i++)
		{
			if (node == m_nodes.getNodeAt(i))
				return true;
		}
		
		return false;
	}

	
	@Override
	protected TreeNode createNewChild()
	{
		NerduinoService service = new NerduinoService();
		
		ServiceConfigDialog dialog = new ServiceConfigDialog(new javax.swing.JFrame(), true);
		
		dialog.setService(service);
		dialog.setVisible(true);
		
		service.setName(getUniqueName(service.getName()));
		
		String name = service.getName();
		String source =
				"<?xml version='1.0' encoding='UTF-8'?>\r\n" 
				+ "<root>\r\n"
				+ "  <!--\r\n"
				+ "  <trigger method='" + name + "' async='60000'>javascript resulting in boolean, when true then start playing the scroll content</trigger>\r\n"
				+ "  -->\r\n"
				+ "  <source>\r\n"
				+ "    function " + name + "()\r\n"
				+ "	      // do something awesome\r\n"
				+ "    end \r\n"
				+ "  </source>\r\n"
				+ "</root>\r\n";
		
		service.writeSourceFile(source);
		service.showTopComponent();
		
		return service;
	}
	
	@Override
	public String getFilePath()
	{
		return AppManager.Current.getDataPath() + "/Services";
	}

	public void applyServices(Context context)
	{
		Node[]  nodes = m_nodes.getNodes();
		
		for(Node n : nodes)
		{
			if (n instanceof NerduinoService)
			{
				try
				{
					NerduinoService service = (NerduinoService) n;

					String script = service.getSource();
					
					context.evaluateString(m_scope, script, "Script", 1, null );
					
					//nerduino.executeScript(service.getSource());
				}
				catch(Exception e)
				{
					String err = e.getMessage();
				}
			}
		}
	}
	
		
	public float getScanInterval()
	{
		return m_scanInterval;
	}
	
	public void setScanInterval(float value)
	{
		m_scanInterval = value;
		
		AppConfiguration.Current.setParameter("ServiceScanInterval", Float.toString(value));
	}

	
	public Scriptable getScope()
	{
		return m_scope;
	}
	
	
	boolean applySource(String source)
	{
		// first try to apply the code to the local context..  
		try
		{		
			Object obj = m_context.evaluateString( m_scope, source, "Script", 1, null );
		}
		catch(Exception e)
		{
			// compile error
			return false;
		}
		
		// if this succeeds, propogate the source to all nerduinos
		Node[]  nodes = NerduinoManager.Current.getChildren().getNodes();
		
		for(Node n : nodes)
		{
			if (n instanceof NerduinoBase)
			{
				NerduinoBase nerd = (NerduinoBase) n;
				
				nerd.executeScript(source);
			}
		}
		
		return true;
	}
	
	@Override
	public Action[] getActions(boolean context)
	{
		return new Action[]
				{
					new ServiceManager.CreateServiceAction(getLookup())
				};
	}

	public Object execute(Context context, String script)
	{
		return context.evaluateString(m_scope, script, "Script", 1, null);
	}

	public final class CreateServiceAction extends AbstractAction
	{
		private ServiceManager node;

		public CreateServiceAction(Lookup lookup)
		{
			node = lookup.lookup(ServiceManager.class);

			putValue(AbstractAction.NAME, "Create Service");
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
	
		
	@Override
	public Node.PropertySet[] getPropertySets()
	{
		final Sheet.Set sheet = Sheet.createPropertiesSet();

		sheet.setDisplayName("Service Settings");
		addProperty(sheet, float.class, null, "ScanInterval", "The interval (in seconds) that service triggers are evaluated.");

		return new Node.PropertySet[]
			{
				sheet
			};
	}
}
