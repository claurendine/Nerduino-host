package com.nerduino.library;


import com.nerduino.nodes.TreeNode;
import com.nerduino.processing.app.BuilderTopComponent;
import com.nerduino.processing.app.IBuildTask;
import com.nerduino.processing.app.Sketch;
import com.nerduino.processing.app.SketchManager;
import com.nerduino.services.ServiceManager;
import com.nerduino.xbee.ZigbeeFrame;
import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class NerduinoBase extends TreeNode
{
    // Declarations
    static ArrayList s_nerduinos = new ArrayList();
    static short s_nextRoutingIndex = 1;
	
	boolean m_loading = false;
	boolean m_interactive = true;
	String m_sketch;
	String m_boardType;
    NerduinoStatusEnum m_status = NerduinoStatusEnum.Uninitialized;
	byte m_configurationToken = 0;
	
	Address m_address;
	
	DeviceTypeEnum m_deviceType;
	short m_pointCount = 0;
	long m_lastResponseMillis;
	
	Context m_context;
	Scriptable m_scope;
	
	
    public List<RemoteDataPoint> m_points = new ArrayList<RemoteDataPoint>();
    
    // Constructors
    public NerduinoBase(String baseName, String icon)
    {
        super(new Children.Array(), baseName, icon);
        
        m_canCopy = false;
        m_canDelete = true;
        m_canDrag = true;
        m_canRename = true;
        m_hasEditor = true;
        m_showPropertyBrowser = true;
		
		m_name = baseName;
		
		m_address = new Address();
		m_address.RoutingIndex = s_nextRoutingIndex++;
		
		s_nerduinos.add(this);
    }
	
	// Methods
	public Object executeScript(String script)
	{
		if (m_context == null)
		{
			m_context = Context.enter();
			m_scope = m_context.initStandardObjects();

			// load up all services
			if (ServiceManager.Current != null)
				ServiceManager.Current.applyServices(this);
		}
		
		return m_context.evaluateString(m_scope, script, "Script", 1, null );
	}
	
	public NerduinoStatusEnum getStatus()
	{
		return m_status;
	}
	
    public void setStatus(NerduinoStatusEnum status)
    {
    	// TODO detect when the status has changed and notify callbacks of the status change
    	if (m_status != status)
		{
			m_status = status;
			
			//NerduinoTreeViewOld.Current.modelUpdated(this);
		}
    }
	
	public byte getConfigurationToken()
	{
		return m_configurationToken;
	}
	
	public void setConfigurationToken(byte token)
    {
    	if (m_configurationToken != token)
		{
			m_configurationToken = token;
			
			// request meta data
		}
    }
	
	public short getPointCount()
	{
		return m_pointCount;
	}
	
	public void setPointCount(short count)
	{
		if (m_pointCount != count)
		{
			m_pointCount = count;
			
			// query for point metadata
		}
	}
		
	public boolean validateName(String name)
	{
		if (!getName().equals(name))
		{
			sendSetMetaData();
		}
		
		return true;
	}
	
	public void save()
	{
		if (m_loading)
			return;

		try
		{
			Document xmldoc = new DocumentImpl();
			Element root = xmldoc.createElement("root");
			
			writeXML(xmldoc, root);
			
			xmldoc.appendChild(root);



			FileOutputStream fos = new FileOutputStream(getFileName());
			OutputFormat of = new OutputFormat("XML", "ISO-8859-1", true);

			of.setIndent(1);
			of.setIndenting(true);
			//of.setDoctype(null,"users.dtd");

			XMLSerializer serializer = new XMLSerializer(fos, of);

			serializer.asDOMSerializer();
			serializer.serialize(xmldoc.getDocumentElement());

			fos.close();
		}
		catch(IOException ex)
		{
		}
	}

	String getFileName()
	{
		return NerduinoManager.Current.getFilePath() + "/" + m_name + ".nerd";
	}
	
	@Override
	public TopComponent getTopComponent()
	{
		Sketch sketch = SketchManager.Current.getSketch(m_sketch);
		
		if (sketch != null)
			return sketch.getTopComponent();
		
		return null;
	}
	
	public boolean getInteractive()
	{
		return m_interactive;
	}
	
	public void setInteractive(boolean value)
	{
		m_interactive = value;
		
		save();
	}
	
	public String getSketch()
	{
		return m_sketch;
	}
	
	public void setSketch(String value)
	{
		m_sketch = value;
		
		save();
		
		m_topComponent = null;
	}
	
	public String getBoardType()
	{
		return m_boardType;
	}
	
	public void setBoardType(String value)
	{
		m_boardType = value;
		
		save();
	}

	public DeviceTypeEnum getDeviceType()
	{
		return m_deviceType;
	}

	public void setDeviceType(DeviceTypeEnum deviceType)
	{
		m_deviceType = deviceType;
	}

    
	public RemoteDataPoint getPoint(short index)
    {
		for(RemoteDataPoint point : m_points)
		{
			if (point.Id == index)
				return point;
		}
		
		return null;
    }
    
    public RemoteDataPoint getPoint(String name)
    {
		for(RemoteDataPoint point : m_points)
		{
			if (point.getName().equals(name))
				return point;
		}

		return null;
    }
	
    public void getMetaData()
    {
		// abstract method to be overriden by derived classes
    }

	public void checkStatus() 
	{
		// abstract method to be overriden by derived classes
	}

	
	@Override
	public void onSelected()
	{
		super.onSelected();
		
		if (BuilderTopComponent.Current != null)
			BuilderTopComponent.Current.setNerduino(this);
	}

	public String upload(Sketch sketch)
	{
		// abstract method to be overridden by derived classes
		return null;
	}	
	
	public String engage(IBuildTask task)
	{
		// abstract method to be overridden by derived classes
		return null;
	}
	
	public double getTimeSinceLastResponse()
    {
    	long millis = System.currentTimeMillis();
    	
    	double dsecs = (double) (millis - m_lastResponseMillis) / 1000.0;
    	
    	return dsecs;
    }

	
	public void sendPing()
    {
	}
    
    public void sendInitialize()
    {
    }
    
    public void sendGetMetaData()
    {
    }
    
	public void sendSetMetaData()
	{	
	}
	
	public void sendGetPoints()
	{
	}

    public void sendGetPoint(short index)
    {
    }
    
    public void sendGetPoint(String name)
    {
    }
	
	public void sendGetPointResponse(short responseToken, LocalDataPoint point)
	{
	}
    
    public void sendGetPointValue(short index)
    {
    }
	
	public void sendGetPointValue(String name)
	{
	}
	
	public void sendGetPointValueResponse(short responseToken, short id, byte status, 
					DataTypeEnum dataType, byte dataLength, byte[] value)
	{
	}
	
	public void sendSetPointValue(short index, boolean value)
	{
		sendSetPointValue(index, DataTypeEnum.DT_Boolean, (byte) 1, value);
	}
    
    public void sendSetPointValue(short index, byte value)
    {
		sendSetPointValue(index, DataTypeEnum.DT_Byte, (byte) 1, value);
    }

    public void sendSetPointValue(short index, short value)
    {
		sendSetPointValue(index, DataTypeEnum.DT_Short, (byte) 2, value);
    }

    public void sendSetPointValue(short index, int value)
    {
		sendSetPointValue(index, DataTypeEnum.DT_Integer, (byte) 4, value);
    }
    
    public void sendSetPointValue(short index, float value)
    {
		sendSetPointValue(index, DataTypeEnum.DT_Float, (byte) 4, value);
    }

    public void sendSetPointValue(short index, byte[] value)
    {
		sendSetPointValue(index, DataTypeEnum.DT_Array, (byte) value.length, value);
    }

    public void sendSetPointValue(short index, String value)
    {
		sendSetPointValue(index, DataTypeEnum.DT_String, (byte) value.length(), value);
    }
	
	public void sendSetPointValue(short index, DataTypeEnum dataType, byte dataLength, Object m_value)
	{	
	}
	
	public void sendRegisterPointCallback(short responseToken, short index, byte filterType, byte filterLength, byte[] filterValue) 
	{	
	}
	
    public void sendUnregisterPointCallback(short index)
    {
    }
	
	public void sendGetAddressResponse(short responseToken, AddressStatusEnum status, Address address, short pointIndex)
	{
	}
	
	public void sendGetDeviceStatusResponse(long serialNumber, short networkAddress, short responseToken)
	{
	}

	public CommandResponse executeCommand(String command)
	{
		return null;
	}

	public void sendFrame(ZigbeeFrame frame)
	{
	}
	
	@Override
	public Action[] getActions(boolean context)
	{
		// A list of actions for this node
		return new Action[]
				{
					new TreeNodeAction(getLookup()),
					new NerduinoBase.RenameAction(getLookup()),
					new NerduinoBase.DeleteAction(getLookup()),
				};
	}
	
	public final class RenameAction extends AbstractAction
	{
		private NerduinoBase node;

		public RenameAction(Lookup lookup)
		{
			node = lookup.lookup(NerduinoBase.class);

			putValue(AbstractAction.NAME, "Rename Nerduino");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (node != null)
			{
				try
				{
					node.rename();
				}
				catch(Exception ex)
				{
					//Exceptions.printStackTrace(ex);
				}
			}
		}
	}
		
	public final class DeleteAction extends AbstractAction
	{
		private NerduinoBase node;

		public DeleteAction(Lookup lookup)
		{
			node = lookup.lookup(NerduinoBase.class);

			putValue(AbstractAction.NAME, "Delete Nerduino");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (node != null)
			{
				try
				{
					node.delete();
				}
				catch(Exception ex)
				{
					//Exceptions.printStackTrace(ex);
				}
			}
		}
	}
	
	public void rename()
	{
		String oldname = getName();
		
		String oldfilename = getFileName();
		
		String newname = JOptionPane.showInputDialog(null, "New Name:", oldname);
		
		if (!newname.matches(oldname))
		{
			String fullpath = NerduinoManager.Current.getFilePath() + "/" + newname + ".nerd";
			
			File newfile = new File(fullpath);
			
			if (newfile.exists())
			{
				JOptionPane.showMessageDialog(null, "This name already exists!");
				return;
			}
			
			File file = new File(oldfilename);
			
			file.renameTo(newfile);
			
			setName(newname);
			
			save();
		}
	}

	
	public void delete()
	{
		// prompt to verify deletion
		int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this nerduino?", "Delete Nerduino", JOptionPane.YES_NO_OPTION);
		
		if (response == JOptionPane.YES_OPTION)
		{
			try
			{				
				File file = new File(getFileName());
				
				if (file.exists())
				{
					file.delete();
				}
				
				destroy();
			}
			catch(IOException ex)
			{
			}
		}
	}
}
