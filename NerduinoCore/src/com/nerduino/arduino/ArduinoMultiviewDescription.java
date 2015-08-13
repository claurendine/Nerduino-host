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

import com.nerduino.core.ContextAwareInstance;
import com.nerduino.scrolls.Scroll;
import com.nerduino.skits.Skit;
import java.awt.Image;
import java.io.IOException;
import java.io.Serializable;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

public class ArduinoMultiviewDescription implements MultiViewDescription, ContextAwareInstance<ArduinoMultiviewDescription>, Serializable
{
	Skit m_skit;
	Scroll m_scroll;
	
	public ArduinoMultiviewDescription()
	{
		m_scroll = null;
		m_skit = null;
	}

	
	private ArduinoMultiviewDescription(Skit skit)
	{
		m_skit = skit;
		m_scroll = null;
	}
	
	private ArduinoMultiviewDescription(Scroll scroll)
	{
		m_skit = null;
		m_scroll = scroll;
	}
	
	@Override
	public int getPersistenceType()
	{
		return TopComponent.PERSISTENCE_ONLY_OPENED;
	}

	@Override
	public String getDisplayName()
	{
		return "Source";
	}

	@Override
	public Image getIcon()
	{
		return null;//ImageUtilities.loadImage("/org/myorg/Generalfiletype/Datasource.gif");
	}

	@Override
	public HelpCtx getHelpCtx()
	{
		return HelpCtx.DEFAULT_HELP;
	}

	@Override
	public String preferredID()
	{
		return "arduino";
	}

	@Override
	public MultiViewElement createElement()
	{
		//assert m_sketchCode != null : "Can't create MultiViewElement without knowing the sketch!";
		
		try
		{
			if (m_scroll != null)
			{
				FileObject file = arduino2file(m_scroll);
				DataObject data = DataObject.find(file);

				m_scroll.m_editor = new ArduinoSourceEditor(data.getLookup());
				m_scroll.m_editor.m_scroll = m_scroll;

				m_scroll.m_editor.m_displayName = m_scroll.getName();
				
				return m_scroll.m_editor;
			}
			
			if (m_skit != null)
			{
				FileObject file = arduino2file(m_skit);
				DataObject data = DataObject.find(file);

				m_skit.m_editor = new ArduinoSourceEditor(data.getLookup());
				m_skit.m_editor.m_skit = m_skit;
				
				m_skit.m_editor.m_displayName = m_skit.getName();
				
				return m_skit.m_editor;
			}
			
			
			return null;
		}
		catch(IOException ioe)
		{
			// XXX: this is wrong, we should return an element that would simply tell the user that
			// the article can't be loaded
			return null;
		}
	}

	@Override
	public ArduinoMultiviewDescription createContextAwareInstance(Lookup context)
	{
		Skit skit = context.lookup(Skit.class);
		
		if (skit != null)		
			return new ArduinoMultiviewDescription(skit);
		
		
		return null;
	}

	private static synchronized FileObject arduino2file(Skit skit) throws IOException
	{
		FileObject file = FileUtil.toFileObject(skit.getFile());
		
		return file;
	}
	
	private static synchronized FileObject arduino2file(Scroll scroll) throws IOException
	{
		FileObject file = FileUtil.toFileObject(scroll.getFile());
		
		return file;
	}
}
