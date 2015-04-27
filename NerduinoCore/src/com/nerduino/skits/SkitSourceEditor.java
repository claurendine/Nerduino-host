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

import java.util.Collection;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.text.Document;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

public class SkitSourceEditor extends CloneableEditor implements MultiViewElement
{
	private transient final Lookup lookup;
	private transient JComponent toolbar;
	private transient MultiViewElementCallback callback;
	
	SkitSourceEditor(Lookup lookup)
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
			if (pane != null)
			{
				Document doc = pane.getDocument();
				
				if (doc instanceof NbDocument.CustomToolbar)
					toolbar = ((NbDocument.CustomToolbar) doc).createToolbar(pane);
			}
	
			if (toolbar == null)
			//attempt to create own toolbar?
			{
				toolbar = new JPanel();
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
			updateName();
	
		super.componentShowing();
	}

	@Override
	public javax.swing.Action[] getActions()
	{
		return super.getActions();
	}

	@Override
	public Lookup getLookup()
	{
		System.out.println("~~ Dumping lookup of " + s2s(this) + ":");
		Collection<?> all = lookup.lookupAll(Object.class);
		for (Object o : all)
		{
			System.out.println("~~   " + s2s(o));
		}
		System.out.println("~~ ------------------------------------");
		
		InstanceContent content = new InstanceContent();
		Lookup temp = new AbstractLookup(content);
		
		return new ProxyLookup(lookup, temp);
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
			callback.requestVisible();
		else
			super.requestVisible();
	}

	@Override
	public void requestActive()
	{
		if (callback != null)
			callback.requestActive();
		else
			super.requestActive();
	}

	@Override
	public void updateName()
	{
		super.updateName();

		if (callback != null)
			callback.updateTitle(getDisplayName());
	}

	@Override
	public void open()
	{
		if (callback != null)
			callback.requestVisible();
		else
			super.open();
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
}
