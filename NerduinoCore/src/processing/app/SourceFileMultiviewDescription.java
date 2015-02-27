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

package processing.app;

import com.nerduino.core.ContextAwareInstance;
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

public class SourceFileMultiviewDescription implements MultiViewDescription, ContextAwareInstance<SourceFileMultiviewDescription>, Serializable
{
	private final SourceFile m_file;

	public SourceFileMultiviewDescription()
	{
		this(null);
	}

	private SourceFileMultiviewDescription(SourceFile file)
	{
		this.m_file = file;
	}

	@Override
	public int getPersistenceType()
	{
		return TopComponent.PERSISTENCE_ONLY_OPENED;
	}

	@Override
	public String getDisplayName()
	{
		return m_file.getName();
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
		return "PDE";
	}

//    public MultiViewElement createElement() {
//        return new SourceTopComponent();
//    }
	@Override
	public MultiViewElement createElement()
	{
		assert m_file != null : "Can't create MultiViewElement without knowing the file!";

		try
		{
			FileObject file = source2file(m_file);
			DataObject data = DataObject.find(file);
			//CCDataLoader loader = new CCDataLoader();
			//CCDataObject data = new CCDataObject(file, loader);
			//return new SourceFileSourceEditor(data.getLookup());
			
			//DefaultDataLoader loader = new DefaultDataLoader();
			//MultiFileLoader loader = (MultiFileLoader) DataLoaderPool.getPreferredLoader(file);
			//PDEDataObject data = new PDEDataObject(file, loader);
			
			return new ArduinoSourceEditor(data.getLookup());
		}
		catch(IOException ioe)
		{
			// XXX: this is wrong, we should return an element that would simply tell the user that
			// the article can't be loaded
			return null;
		}
	}

	@Override
	public SourceFileMultiviewDescription createContextAwareInstance(Lookup context)
	{
		return new SourceFileMultiviewDescription(context.lookup(SourceFile.class));
	}

	private static synchronized FileObject source2file(SourceFile file) throws IOException
	{
		FileObject fo = FileUtil.toFileObject(file.getFile());

		return fo;
	}
}
