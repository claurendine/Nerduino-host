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

package com.nerduino.core;

import com.nerduino.library.FamilyUPNP;
import com.nerduino.nodes.RootNode;
import com.nerduino.nodes.TreeNode;
import java.awt.Dimension;
import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.table.TableColumnModel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.swing.outline.Outline;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.PasteAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ConvertAsProperties(
    dtd = "-//com.nerduino.core//Explorer//EN",
autostore = false)
@TopComponent.Description(
    preferredID = "ExplorerTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = true)
@ActionID(category = "Window", id = "com.nerduino.core.ExplorerTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_ExplorerAction",
preferredID = "ExplorerTopComponent")
@Messages(
{
	"CTL_ExplorerAction=Explorer",
	"CTL_ExplorerTopComponent=Explorer",
	"HINT_ExplorerTopComponent=This is the Explorer window"
})
public final class ExplorerTopComponent extends TopComponent
		implements ExplorerManager.Provider, LookupListener
{
	private static final String ROOT_NODE = "Explorer";
	private final ExplorerManager m_manager = new ExplorerManager();
	public static ExplorerTopComponent Current;
	Node m_rootNode;
	Lookup.Result<TreeNode> m_result;
	LookupListener weakLookup;
	TreeNode m_currentSelection;
	
	//static JRibbon s_ribbon;

	public ExplorerTopComponent()
	{
		Current = this;

		initComponents();
		
		setName(Bundle.CTL_ExplorerTopComponent());
		setToolTipText(Bundle.HINT_ExplorerTopComponent());
		putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
		putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);

		initComponents();
		
		myInitComponents();

		m_rootNode = new RootNode();
		
		outline1.setPreferredSize(new Dimension(16, 16));
		
		Outline o = outline1.getOutline();
		
		TableColumnModel tcm = o.getColumnModel();

		tcm.getColumn(0).setHeaderValue("Loading...");
		
		o.setRootVisible(false);
		
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{	
					// Initialize the application manager
					AppManager.initialize();

					m_manager.setRootContext(m_rootNode);

					Lookup lookup = ExplorerUtils.createLookup(m_manager, getActionMap());

					m_result = lookup.lookupResult(TreeNode.class);
					weakLookup = WeakListeners.create(LookupListener.class, Current, m_result);
					m_result.addLookupListener(weakLookup);

					AppManager.Current.initializeExplorer();

					
							
					Outline o = outline1.getOutline();

					TableColumnModel tcm = o.getColumnModel();

					tcm.getColumn(0).setHeaderValue("Nodes");

					
					FamilyUPNP.Current.engage();
					
					int i = 0;
				}
				catch(Exception e)
				{
					Exceptions.printStackTrace(e);
				}
			}
		});

		thread.start();

		initializeRibbon();
	}
	
	javax.swing.JPanel jPanel;
    javax.swing.JScrollPane jScrollPane1;
    javax.swing.JScrollPane jScrollPane2;
	org.openide.explorer.view.OutlineView outline1;
	
	void myInitComponents()
	{
        jScrollPane1 = new javax.swing.JScrollPane();
        jScrollPane2 = new javax.swing.JScrollPane();
		jPanel = new javax.swing.JPanel();

		outline1 = new org.openide.explorer.view.OutlineView();
		
        outline1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        
		jScrollPane2.setViewportView(outline1);
        
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        
		layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 64, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
	}
	
	
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 412, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 412, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

	void initializeRibbon()
	{	
		try
		{
			/*
			// convenient time to get the instance of the jribbon class
			Object obj = WindowManager.getDefault().getMainWindow();

			JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();

			
			if (frame.getRootPane().getLayeredPane().getComponent(0) instanceof JRibbon)
			{
				s_ribbon = (JRibbon) frame.getRootPane().getLayeredPane().getComponent(0);
				
				AppManager.Current.setRibbon(s_ribbon);
			}
			*/
		}
		catch(Exception e)
		{
		}
 	}

	@Override
	public void componentClosed()
	{
	}

	@Override
	public void open()
	{
		Mode m = WindowManager.getDefault().findMode("explorer");

		if (m != null)
		{
			m.dockInto(this);
		}

		super.open();
	}

	public TreeNode getSelectedNode()
	{
		return m_currentSelection;
	}
	
	public void setSelectedNode(TreeNode node)
	{
		
		Node[] nodes = new Node[] { node };
		
		try
		{
			m_manager.setExploredContextAndSelection(node, nodes);
		}
		catch(PropertyVetoException ex)
		{
			//Exceptions.printStackTrace(ex);
		}
	}

	@Override
	protected void componentActivated()
	{
		ExplorerUtils.activateActions(m_manager, true);
	}

	@Override
	protected void componentDeactivated()
	{
		ExplorerUtils.activateActions(m_manager, false);
	}

	@Override
	public ExplorerManager getExplorerManager()
	{
		return m_manager;
	}

	private void initActions()
	{
		CutAction cut = SystemAction.get(CutAction.class);
		getActionMap().put(cut.getActionMapKey(), ExplorerUtils.actionCut(m_manager));
		CopyAction copy = SystemAction.get(CopyAction.class);
		getActionMap().put(copy.getActionMapKey(), ExplorerUtils.actionCopy(m_manager));
		PasteAction paste = SystemAction.get(PasteAction.class);
		getActionMap().put(paste.getActionMapKey(), ExplorerUtils.actionPaste(m_manager));
		DeleteAction delete = SystemAction.get(DeleteAction.class);
		getActionMap().put(delete.getActionMapKey(), ExplorerUtils.actionDelete(m_manager, true));
	}

	void writeProperties(java.util.Properties p)
	{
		// better to version settings since initial version as advocated at
		// http://wiki.apidesign.org/wiki/PropertyFiles
		p.setProperty("version", "1.0");
		// TODO store your settings
	}

	void readProperties(java.util.Properties p)
	{
		String version = p.getProperty("version");
		// TODO read your settings according to their version
	}

	void addNode(Node node)
	{
		Node nodes[] = new Node[1];

		nodes[0] = node;

		m_rootNode.getChildren().add(nodes);
	}

	public Node getRootNode()
	{
		return m_rootNode;
	}

	void expandNode(Node node)
	{
		try
		{
			outline1.expandNode(node);
		}
		catch(Exception e)
		{
		}
	}


	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
    public void resultChanged(LookupEvent lookupEvent)
	{
		Lookup.Result r = (Lookup.Result) lookupEvent.getSource();
		
		Collection c = r.allInstances();
		TreeNode node = null;

		if (!c.isEmpty())
		{
			for (Iterator it = c.iterator(); it.hasNext();)
			{
				Object obj = it.next();


				if (obj instanceof TreeNode)
				{
					node = (TreeNode) obj;
					break;
				}
			}
		}

		if (node != m_currentSelection)
		{
			if (m_currentSelection != null)
			{
				m_currentSelection.onDeselected();
			}

			m_currentSelection = node;

			if (m_currentSelection != null)
			{
				m_currentSelection.onSelected();
			}
		}
	}

}
