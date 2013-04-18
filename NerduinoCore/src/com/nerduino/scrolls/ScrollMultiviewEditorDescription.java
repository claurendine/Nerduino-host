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

import com.nerduino.core.ContextAwareInstance;
import com.nerduino.scrolls.Scroll;
import java.awt.Image;
import java.io.IOException;
import java.io.OutputStream;
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

public class ScrollMultiviewEditorDescription implements MultiViewDescription, ContextAwareInstance<ScrollMultiviewEditorDescription>, Serializable
{
	private final Scroll m_scroll;
	private static FileSystem m_files = null;

	public ScrollMultiviewEditorDescription()
	{
		this(null);
	}

	private ScrollMultiviewEditorDescription(Scroll scroll)
	{
		this.m_scroll = scroll;
	}

	@Override
	public int getPersistenceType()
	{
		return TopComponent.PERSISTENCE_ONLY_OPENED;
	}

	@Override
	public String getDisplayName()
	{
		return "Scroll";
	}

	@Override
	public Image getIcon()
	{
		return null;
	}

	@Override
	public HelpCtx getHelpCtx()
	{
		return HelpCtx.DEFAULT_HELP;
	}

	@Override
	public String preferredID()
	{
		return "scroll";
	}

	@Override
	public MultiViewElement createElement()
	{
		assert m_scroll != null : "Can't create MultiViewElement without knowing the scroll!";

		try
		{
			FileObject file = skit2file(m_scroll);
			DataObject data = DataObject.find(file);
			
			return new ScrollSourceEditor(data.getLookup());
		}
		catch(IOException ioe)
		{
			// XXX: this is wrong, we should return an element that would simply tell the user that
			// the article can't be loaded
			return null;
		}
	}

	@Override
	public ScrollMultiviewEditorDescription createContextAwareInstance(Lookup context)
	{
		return new ScrollMultiviewEditorDescription(context.lookup(Scroll.class));
	}

	private static synchronized FileObject skit2file(Scroll scroll) throws IOException
	{
		if (m_files == null)
			m_files = FileUtil.createMemoryFileSystem();
		
		String fileName = scroll.getFileName();
		
		FileObject file = m_files.findResource(fileName);
		
		if (file == null)
		{
			file = m_files.getRoot().createData(fileName);

			OutputStream os = file.getOutputStream();

			try
			{
				String program = scroll.getSource();
				
				os.write(program.getBytes());
			}
			finally
			{
				os.close();
			}
		}

		return file;
	}
}
