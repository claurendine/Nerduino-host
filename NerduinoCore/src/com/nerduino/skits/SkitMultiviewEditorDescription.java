/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.skits;

import com.nerduino.core.ContextAwareInstance;
import com.nerduino.skits.Skit;
import java.awt.Image;
import java.io.File;
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

public class SkitMultiviewEditorDescription implements MultiViewDescription, ContextAwareInstance<SkitMultiviewEditorDescription>, Serializable
{
	private final Skit m_skit;
	private static FileSystem m_files = null;

	public SkitMultiviewEditorDescription()
	{
		this(null);
	}

	private SkitMultiviewEditorDescription(Skit skit)
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
		return "Skit";
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
		return "skit";
	}

	@Override
	public MultiViewElement createElement()
	{
		assert m_skit != null : "Can't create MultiViewElement without knowing the skit!";

		try
		{
			FileObject file = skit2file(m_skit);
			DataObject data = DataObject.find(file);
			
			return new SkitSourceEditor(data.getLookup());
		}
		catch(IOException ioe)
		{
			// XXX: this is wrong, we should return an element that would simply tell the user that
			// the article can't be loaded
			return null;
		}
	}

	@Override
	public SkitMultiviewEditorDescription createContextAwareInstance(Lookup context)
	{
		return new SkitMultiviewEditorDescription(context.lookup(Skit.class));
	}

	private static synchronized FileObject skit2file(Skit skit) throws IOException
	{
		String filepath = skit.getHtmlFileName();
		File f = new File(filepath);
		
		FileObject file = FileUtil.toFileObject(f);
		
		return file;
	}
}
