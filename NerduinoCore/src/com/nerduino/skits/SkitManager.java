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
			String filename =  WebHost.Current.getWebRoot() + "/SkitIndex.html";
			
			FileWriter fw = new FileWriter(filename);
			
			fw.write("<html><head>My Skits</head><body><br/>");
			
			for(Node node : m_children.getNodes())
			{
				Skit skit = (Skit) node;
				
				fw.write("<p style=\"margin-top: 0\">");
				fw.write("<a href=\"" + skit.getName() + ".html\">" + skit.getName() + "</a>");
				fw.write("</p>");
			}
			
			fw.write("</body></html>");
			
			fw.close();
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
