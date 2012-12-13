/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.skits;

import java.io.IOException;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.text.Document;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
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
	
	private static PaletteController palette = null;
	
	
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
		
		FileObject fo = FileUtil.getConfigFile("Actions/Window/org-netbeans-modules-palette-ShowPaletteAction.instance");
		
		if (fo != null && fo.isValid())
		{
			try
			{
				DataObject dob = DataObject.find(fo);
				InstanceCookie ic = dob.getLookup().lookup(InstanceCookie.class);
				if (ic != null)
				{
					Object instance = ic.instanceCreate();
					
					if (instance instanceof Action)
					{
						Action a = (Action) instance;
						a.actionPerformed(null);
					}
				}
			}
			catch(Exception e)
			{
			}
		}
	}

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
		
		PaletteController controller = createPalette();
		content.add(controller);
		
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

	public static PaletteController createPalette()
	{
		try
		{
			if (null == palette)
			{
				palette = PaletteFactory.createPalette("HTMLPalette", new PaletteActions()
				{
					@Override
					public Action[] getImportActions()
					{
						return null;
					}

					@Override
					public Action[] getCustomPaletteActions()
					{
						return null;
					}

					@Override
					public Action[] getCustomCategoryActions(Lookup lkp)
					{
						return null;
					}

					@Override
					public Action[] getCustomItemActions(Lookup lkp)
					{
						return null;
					}

					@Override
					public Action getPreferredAction(Lookup lkp)
					{
						return null;
					}
				}, null, null);
			}
			return palette;
		}
		catch(IOException ex)
		{
		}
		return null;
	}
}
