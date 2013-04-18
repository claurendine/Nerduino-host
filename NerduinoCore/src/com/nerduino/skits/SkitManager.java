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

import com.nerduino.core.AppManager;
import com.nerduino.core.BaseManager;
import com.nerduino.nodes.TreeNode;
import com.nerduino.webhost.WebHost;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.nodes.Node;
import org.openide.util.Lookup;


public class SkitManager extends BaseManager
{
	// Declarations
	public static SkitManager Current;

	public SkitManager()
	{
		super("Skits", "/com/nerduino/resources/SkitManager16.png");
	
		Current = this;
	}

	@Override
	public TreeNode createNewChild()
	{
		return new Skit();
	}
	
	@Override
	public boolean configureChild(TreeNode child)
	{
		Skit ns = (Skit) child;
		
		ns.setName(getUniqueName(ns.getName()));
		
		// show the configure dialog
		SkitConfigDialog dialog = new SkitConfigDialog(new javax.swing.JFrame(), true);
		
		dialog.setSkit(ns);
		dialog.setVisible(true);
		
		ns = dialog.m_skit;
		
		if (ns == null)
		{
			return false;
		}

		ns.setName(getUniqueName(ns.getName()));
		ns.m_configured = true;
		
		java.net.URL url;
		
		if (ns.m_skitMode.equals("Structured"))
			url = getClass().getResource("/com/nerduino/resources/StructuredSkit.html");
		else if (ns.m_skitMode.equals("Mobile Template"))
			url = getClass().getResource("/com/nerduino/resources/MobileSkit.html");
		else 
			url = getClass().getResource("/com/nerduino/resources/DesktopSkit.html");
		try
		{
			InputStream str = url.openStream();
			
			BufferedInputStream bin = new BufferedInputStream(str);

			byte[] contents = new byte[1024];

			int bytesRead=0;
			String strFileContents;
			String html = "";

			while( (bytesRead = bin.read(contents)) != -1)
			{
				strFileContents = new String(contents, 0, bytesRead);

				html = html + strFileContents;
			}		
			
			str.close();
			
			ns.m_html = html;
		}
		catch(IOException ex)
		{
			Logger.getLogger(SkitManager.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		ns.writeHtmlFile();
		
		return true;
	}
		
	@Override
	public void saveConfiguration()
	{
		if (!AppManager.loading)
		{
			updateSkitIndexHtml();

			AppManager.Current.saveConfiguration();
		}
	}
	
	public void updateSkitIndexHtml()
	{
		try
		{
			String filename =  WebHost.Current.getWebRoot() + "/index.html";
			
			FileWriter writer = new FileWriter(filename);
			
			writer.write("<!DOCTYPE html><html>\n");
			
			writer.write("<head><title>Nerduino</title><meta name='viewport' content='width=device-width, initial-scale=1'>\n"
					+ "<link rel='stylesheet' href='http://code.jquery.com/mobile/1.2.0/jquery.mobile-1.2.0.min.css' />"
					+ "<script src='http://code.jquery.com/jquery-1.8.2.min.js'></script>"
					+ "<script src='http://code.jquery.com/mobile/1.2.0/jquery.mobile-1.2.0.min.js'></script></head>\n");
			
			//writer.write("<body><div data-role='page'><div data-role='header'><h1>Nerduino</h1></div>\n");
			writer.write("<body><div data-role='page'><div data-role='header'><h1><img src='Nerduino32.png'></h1></div>\n");
			writer.write("<ul data-role='listview' data-inset='true'>\n");
			
			Node[] nodes = SkitManager.Current.getChildren().getNodes();
			
			for(Node node : nodes)
			{
				Skit skit = (Skit) node;
				String name = skit.getName();
				
				writer.write("<li><a href='");
				writer.write(name);
				writer.write(".html'>");
				writer.write(name);
				writer.write("</a></li>\n");
			}
			
			writer.write("</ul>\n");			
			writer.write("</div></div></body>\n");
			writer.write("</html>");
			
			writer.close();
		}
		catch(IOException ex)
		{
			Logger.getLogger(Skit.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	@Override
	public String getFilePath()
	{
		return "/Users/chaselaurendine/Documents/Nerduino/Services";
	}

	@Override
	public Action[] getActions(boolean context)
	{
		// A list of actions for this node
		return new Action[]
				{
					new SkitManager.CreateSkitAction(getLookup())
				};
	}

	public final class CreateSkitAction extends AbstractAction
	{
		private SkitManager node;

		public CreateSkitAction(Lookup lookup)
		{
			node = lookup.lookup(SkitManager.class);

			putValue(AbstractAction.NAME, "Create Skit");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (node != null)
			{
				try
				{
					node.createNew();
				}
				catch(Exception ex)
				{
					//Exceptions.printStackTrace(ex);
				}
			}
		}
	}
}
