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

package com.nerduino.processing.app;

import com.nerduino.skits.Skit;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;

public class ArduinoSourceEditor extends CloneableEditor implements MultiViewElement
{
	static ArduinoSourceEditor currentSourceEditor;
	
	private transient final Lookup lookup;
	private transient JComponent toolbar;
	private transient MultiViewElementCallback callback;
	
	Sketch m_sketch;
	String m_displayName = "";
	Skit m_skit;

	ArduinoSourceEditor(Lookup lookup)
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
			if (pane != null && pane.getDocument() instanceof NbDocument.CustomToolbar)
				toolbar = ((NbDocument.CustomToolbar) pane.getDocument()).createToolbar(pane);
			
			if (toolbar == null)
				//attempt to create own toolbar?
				toolbar = new JPanel();
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
		currentSourceEditor = this;
		
		super.componentActivated();
		
		callback.getTopComponent().setDisplayName(m_displayName);
		
		if (m_sketch != null && BuilderTopComponent.Current != null)
		{			
			BuilderTopComponent.Current.setSketch(m_sketch);
		}
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
	}

	@Override
	public void open()
	{
		if (callback != null)
			callback.requestVisible();
		else
			super.open();
	}
	
	public String getText()
	{
		return this.pane.getText();
	}

	@Override
	public CloseOperationState canCloseElement()
	{
		return CloseOperationState.STATE_OK;
	}
}
