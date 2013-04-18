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

import com.nerduino.core.ContextAwareInstance;
import java.awt.Image;
import java.io.IOException;
import java.io.Serializable;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

public class ServiceMultiviewDescription implements MultiViewDescription, ContextAwareInstance<ServiceMultiviewDescription>, Serializable
{
	private final NerduinoService m_service;

	public ServiceMultiviewDescription()
	{
		this(null);
	}

	private ServiceMultiviewDescription(NerduinoService service)
	{
		this.m_service = service;
	}

	@Override
	public int getPersistenceType()
	{
		return TopComponent.PERSISTENCE_ONLY_OPENED;
	}

	@Override
	public String getDisplayName()
	{
		return "Service";
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
		return "service";
	}

//    public MultiViewElement createElement() {
//        return new SourceTopComponent();
//    }
	@Override
	public MultiViewElement createElement()
	{
		assert m_service != null : "Can't create MultiViewElement without knowing the service!";

		try
		{
			FileObject file = service2file(m_service);
			DataObject data = DataObject.find(file);
			
			m_service.m_editor = new ServiceSourceEditor(data.getLookup());
			
			m_service.m_editor.m_service = m_service;
			
			return m_service.m_editor;
		}
		catch(IOException ioe)
		{
			// XXX: this is wrong, we should return an element that would simply tell the user that
			// the article can't be loaded
			return null;
		}
	}

	@Override
	public ServiceMultiviewDescription createContextAwareInstance(Lookup context)
	{
		return new ServiceMultiviewDescription(context.lookup(NerduinoService.class));
	}

	private static FileSystem m_files = null;

	private static synchronized FileObject service2file(NerduinoService service) throws IOException
	{
		FileObject file = FileUtil.toFileObject(service.getFile());
		
		return file;
	}
}
