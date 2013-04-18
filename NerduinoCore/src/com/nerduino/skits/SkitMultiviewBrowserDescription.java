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

/*
package com.nerduino.skits;

import com.nerduino.core.ContextAwareInstance;
import java.awt.Image;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import org.jfx.browser.BrowserEditor;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

public class SkitMultiviewBrowserDescription implements MultiViewDescription, ContextAwareInstance<SkitMultiviewBrowserDescription>, Serializable
{
	private final Skit m_skit;
	private static FileSystem m_files = null;

	public SkitMultiviewBrowserDescription()
	{
		this(null);
	}

	private SkitMultiviewBrowserDescription(Skit skit)
	{
		this.m_skit = skit;
	}

	@Override
	public int getPersistenceType()
	{
		return TopComponent.PERSISTENCE_ONLY_OPENED;
	}

	@Override
	public String getDisplayName()
	{
		return "Browser";
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
		return "browser";
	}

	@Override
	public MultiViewElement createElement()
	{
		assert m_skit != null : "Can't create MultiViewElement without knowing the skit!";

		try
		{
			FileObject file = skit2file(m_skit);
			DataObject data = DataObject.find(file);
			
			//return new BrowserEditor(data.getLookup());
			return new BrowserEditor();
		}
		catch(IOException ioe)
		{
			// XXX: this is wrong, we should return an element that would simply tell the user that
			// the article can't be loaded
			return null;
		}
	}

	@Override
	public SkitMultiviewBrowserDescription createContextAwareInstance(Lookup context)
	{
		return new SkitMultiviewBrowserDescription(context.lookup(Skit.class));
	}

	private static synchronized FileObject skit2file(Skit skit) throws IOException
	{
		if (m_files == null)
			m_files = FileUtil.createMemoryFileSystem();
		
		String fileName = skit.getFileName();
		
		FileObject file = m_files.findResource(fileName);
		
		if (file == null)
		{
			file = m_files.getRoot().createData(fileName);

			OutputStream os = file.getOutputStream();

			try
			{
				String program = skit.getSource();
				
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
*/