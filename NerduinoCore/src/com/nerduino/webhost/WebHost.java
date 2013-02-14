package com.nerduino.webhost;

import com.nerduino.core.AppManager;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class WebHost
{
//	final MyServ srv;;
 	String m_webroot = "/Users/chaselaurendine/Documents/SvnProjects/WebServer";
	int m_port = 4444;
	Boolean m_enabled = true;
	
	public static WebHost Current;
	
/*	
	class MyServ extends Acme.Serve.Serve 
	{
		// Overriding method for public access
		@Override
		public void setMappingTable(PathTreeDictionary mappingtable) 
		{ 
			super.setMappingTable(mappingtable);
		}

		// add the method below when .war deployment is needed
		@Override
		public void addWarDeployer(String deployerFactory, String throttles) 
		{
			super.addWarDeployer(deployerFactory, throttles);
		}
	};
*/	
	
	public WebHost() throws NoSuchMethodException 
    {
		try 
		{
			Current = this;
		
            Server server = new Server(8081);
	        
			// Create the servlet handler and define the Chat servlet
			
			ResourceHandler resource_handler = new ResourceHandler();
			resource_handler.setDirectoriesListed(true);
			resource_handler.setWelcomeFiles(new String[]{ "index.html" });
			resource_handler.setResourceBase(m_webroot);
			
			NerduinoWebSocketHandler dlh = new NerduinoWebSocketHandler();
			
			HandlerList handlers = new HandlerList();
			handlers.setHandlers(new Handler[] { dlh, resource_handler, new DefaultHandler() });
			server.setHandler(handlers);
			
			server.start();
            server.join();
        } 
		catch (Throwable e) 
		{
            e.printStackTrace();
        }

		/*
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
        
        try
        {
            jsEngine.eval("function sayHello() {" +
                "  println('Hello, world!');" +
                "}");
            Invocable invocableEngine = (Invocable) jsEngine;
            invocableEngine.invokeFunction("sayHello");
        }
        catch(ScriptException ex)
        {
            ex.printStackTrace();
        }
        
		setWebRoot(m_webroot);
		
		srv = new MyServ();
		
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				srv.notifyStop();
				srv.destroyAllServlets();
			}
		}));
		
		setEnabled(true);
		
		srv.serve();
		*/
    }
	
	public Boolean getEnabled()
	{
		return m_enabled;
	}
	
	public void setEnabled(Boolean value)
	{
		m_enabled = value;
				
		if (m_enabled)
		{
			/*
			Acme.Serve.Serve.PathTreeDictionary aliases = new Acme.Serve.Serve.PathTreeDictionary();
			aliases.put("/*", new java.io.File(m_webroot));
		    srv.setMappingTable(aliases);

			
			// setting properties for the server, and exchangeable Acceptors
			java.util.Properties properties = new java.util.Properties();

			properties.put("port", m_port);
			properties.setProperty(Acme.Serve.Serve.ARG_NOHUP, "nohup");
			srv.arguments = properties;
			srv.addDefaultServlets(null); // optional file servlet
			srv.addServlet("/nerduino/*", new NerduinoServlet());
			//srv.addServlet("/*", new NerduinoServlet());
			
			AppManager.Current.setRibbonComponentImage("Home/Host Settings/Web", "com/nerduino/resources/HostEnabled.png");
			
			//srv.serve();
			*/
		}
		else
		{
			AppManager.log("Web Host Disabled.");
			AppManager.log("");
			
			AppManager.Current.setRibbonComponentImage("Home/Host Settings/Web", "com/nerduino/resources/HostDisabled.png");
		}
	}
	
	public String getHttpAddress()
	{
		String address = "";
		
		InetAddress ip;
		
		try 
		{
			ip = InetAddress.getLocalHost();
			
			address = "http://" + ip.getHostAddress() + ":" + Integer.toString(m_port) + "/";
		} 
		catch (UnknownHostException e) 
		{
		}
		
		return address;
	}
	
	public void setHttpAddress(String address)
	{
	}
	
	public int getPort()
	{
		return m_port;
	}
	
	public void setPort(int value)
	{
		m_port = value;
	}
	
	public void setWebRoot(String path)
	{
		m_webroot = path;
		
	}
	
	public String getWebRoot()
	{
		return m_webroot;
	}
	
	public void readXML(Element node)
	{
		if (node != null)
		{
			NodeList nl = node.getElementsByTagName("WebHost");

			if (nl != null && nl.getLength() > 0)
			{
				Element config = (Element) nl.item(0);

				m_webroot = config.getAttribute("Webroot");
				String str = config.getAttribute("Port");
				
				try
				{
					m_port = Integer.decode(str);
				}
				catch(Exception e)
				{
					m_port = 4444;
				}
				
				if (m_webroot == null || m_webroot.length() == 0)
				{
					m_webroot = System.getProperty("user.dir") + "/webroot";
				}
			}

			//setEnabled(true);
		}
	}

	public void writeXML(Document doc, Element node)
	{
		Element element = doc.createElement("WebHost");
		
		element.setAttribute("Webroot", m_webroot);
		element.setAttribute("Port", ((Integer) m_port).toString());
		
		node.appendChild(element);
	}
}
