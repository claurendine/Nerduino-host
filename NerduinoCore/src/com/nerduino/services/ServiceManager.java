package com.nerduino.services;

import com.nerduino.core.BaseManager;
import com.nerduino.library.LocalDataPointConfigDialog;
import com.nerduino.library.NerduinoBase;
import com.nerduino.library.NerduinoHost;
import com.nerduino.library.NerduinoManager;
import com.nerduino.nodes.TreeNode;
import com.nerduino.processing.app.SourceFile;
import com.nerduino.processing.app.SourceFolder;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class ServiceManager extends BaseManager
{
	// Declarations
	public static ServiceManager Current;
	File m_file;
	
	Context m_context;
	Scriptable m_scope;
	private Children m_nodes;
	
	
	public ServiceManager()
	{
		super("Services", "/com/nerduino/resources/ServiceManager16.png");
		
		Current = this;
		
		m_nodes = this.getChildren();

		m_context = Context.enter();
		m_scope = m_context.initStandardObjects();
		
		m_file = new File(getFilePath());
		
		loadChildren();
	}

	void loadChildren()
	{
		File[] files = m_file.listFiles();
		
		for(File file : files)
		{
			if (file.getName().endsWith(".js"))
			{
				NerduinoService ns = new NerduinoService(file);
				
				ns.apply();

				addNode(ns);
			}
		}
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
		try
		{
			// get a unique name and create a new script file
			NerduinoService service = new NerduinoService();
			
			ServiceConfigDialog dialog = new ServiceConfigDialog(new javax.swing.JFrame(), true);
			
			dialog.setService(service);
			dialog.setVisible(true);

			service = dialog.m_service;

			service.setName(getUniqueName(service.getName()));		

			String newfilename = m_file.getAbsolutePath() + "/" + service.getFileName();
			
			service.m_file = new File(newfilename);
			
			service.m_file.createNewFile();
		
			service.showTopComponent();

			return service;
		}
		catch(IOException ex)
		{
			Exceptions.printStackTrace(ex);
		}
		
		return null;
	}

	@Override
	public String getFilePath()
	{
		return NerduinoHost.Current.getDataPath() + "/Services";
	}

	public void applyServices(NerduinoBase nerduino)
	{
		Node[]  nodes = m_nodes.getNodes();
		
		for(Node n : nodes)
		{
			if (n instanceof NerduinoService)
			{
				try
				{
					NerduinoService service = (NerduinoService) n;

					nerduino.executeScript(service.getSource());
				}
				catch(Exception e)
				{
				}
			}
		}
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
}
