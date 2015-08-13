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

package com.nerduino.arduino;

import com.nerduino.core.ExplorerTopComponent;
import com.nerduino.library.NerduinoBase;
import com.nerduino.scrolls.Scroll;
import com.nerduino.services.NerduinoService;
import com.nerduino.skits.Skit;
import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;

public class ArduinoSourceEditor extends CloneableEditor implements MultiViewElement
{
	static ArduinoSourceEditor currentSourceEditor;
	
	private transient final Lookup lookup;
	private transient JComponent toolbar;
	private transient MultiViewElementCallback callback;
	private JLabel tooltipLabel;
	Object[] m_nerds;
	NerduinoBase m_targetNerd;
	JComboBox<Object> m_targetList;
	boolean m_busy;
	
	String m_displayName = "";
	Skit m_skit;
	Scroll m_scroll;
	NerduinoService m_service;

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
	
	void updateTargets()
	{
	}
	
	@Override
	public JComponent getToolbarRepresentation()
	{		
		return null;
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
		
		if (m_targetNerd != null)
		{
			ExplorerTopComponent.Current.setSelectedNode(m_targetNerd);
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
	public void doLayout()
	{
		super.doLayout();
		
		Component[] components = this.getComponents();
		
		if (components.length > 1)
			components[1].setVisible(false);
	}

	@Override
	public org.openide.util.Lookup getLookup()
	{
		return lookup;
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
	
	public NerduinoBase getTarget()
	{
		return m_targetNerd;
	}
	
	public void setTarget(NerduinoBase nerduino)
	{
		m_targetList.setSelectedItem(nerduino);
	}

	@Override
	public CloseOperationState canCloseElement()
	{
		return CloseOperationState.STATE_OK;
	}
}
