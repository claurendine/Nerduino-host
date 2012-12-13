/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.services;

import java.awt.Color;
import java.util.Collection;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.text.Document;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;

public class ServiceSourceEditor extends CloneableEditor implements MultiViewElement
{
	private transient final Lookup lookup;
	private transient JComponent toolbar;
	private transient MultiViewElementCallback callback;

	NerduinoService m_service;
	
	ServiceSourceEditor(Lookup lookup)
	{
		super(lookup.lookup(CloneableEditorSupport.class));
		this.lookup = lookup;
	}

	@Override
	public JComponent getVisualRepresentation()
	{
		return this;
	}

	@Override
	public JComponent getToolbarRepresentation()
	{
		if (toolbar == null)
		{
			JEditorPane pane = this.pane;
			if (pane != null)
			{
				Document doc = pane.getDocument();
				if (doc instanceof NbDocument.CustomToolbar)
				{
					toolbar = ((NbDocument.CustomToolbar) doc).createToolbar(pane);
				}
			}
			if (toolbar == null)
			{
				//attempt to create own toolbar?
				toolbar = new JToolBar("Toolbar", JToolBar.HORIZONTAL);
				
				JButton applybutton = new JButton("Apply");
				
				applybutton.addMouseListener(new java.awt.event.MouseAdapter()
				{
					public void mouseClicked(java.awt.event.MouseEvent evt)
					{
						if (m_service.apply())
						{
							evt.getComponent().setForeground(Color.black);
						}
						else
						{
							evt.getComponent().setForeground(Color.red);
						}
					}
				});
				
				toolbar.add(applybutton);
			}
		}
		return toolbar;
	}

	@Override
	public void setMultiViewCallback(MultiViewElementCallback callback)
	{
		this.callback = callback;
	}

	@Override
	public void componentActivated()
	{
		super.componentActivated();
	}

	@Override
	public void componentClosed()
	{
		super.componentClosed();
	}

	@Override
	public void componentDeactivated()
	{
		super.componentDeactivated();
	}

	@Override
	public void componentHidden()
	{
		super.componentHidden();
	}

	@Override
	public void componentOpened()
	{
		super.componentOpened();
	}

	@Override
	public void componentShowing()
	{
		if (callback != null)
		{
			updateName();
		}
		super.componentShowing();
	}

	@Override
	public javax.swing.Action[] getActions()
	{
		return super.getActions();
	}

	@Override
	public org.openide.util.Lookup getLookup()
	{
		System.out.println("~~ Dumping lookup of " + s2s(this) + ":");
		Collection<?> all = lookup.lookupAll(Object.class);
		for (Object o : all)
		{
			System.out.println("~~   " + s2s(o));
		}
		System.out.println("~~ ------------------------------------");
		return lookup;
	}

	@Override
	public String preferredID()
	{
		return super.preferredID();
	}

	@Override
	public void requestVisible()
	{
		if (callback != null)
		{
			callback.requestVisible();
		}
		else
		{
			super.requestVisible();
		}
	}

	@Override
	public void requestActive()
	{
		if (callback != null)
		{
			callback.requestActive();
		}
		else
		{
			super.requestActive();
		}
	}

	@Override
	public void updateName()
	{
		super.updateName();
		if (callback != null)
		{
			callback.updateTitle(getDisplayName());
		}
	}

	@Override
	public void open()
	{
		if (callback != null)
		{
			callback.requestVisible();
		}
		else
		{
			super.open();
		}

	}

	@Override
	public CloseOperationState canCloseElement()
	{
		return CloseOperationState.STATE_OK;
	}

	private static String s2s(Object o)
	{
		return o == null ? "null" : o.getClass() + "@" + Integer.toHexString(System.identityHashCode(o));
	}
	
	public String getText()
	{
		return this.pane.getText();
	}
}
