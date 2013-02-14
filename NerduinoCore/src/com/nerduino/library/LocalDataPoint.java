package com.nerduino.library;

import com.nerduino.xbee.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class LocalDataPoint extends PointBase
{
	// Declarations
	public RemoteDataPoint Proxy = null;
	
	static short nextIndex = 0;
	
    // Constructors
    public LocalDataPoint()
    {
		super();
		
		this.Id = nextIndex++;
    }
    
	@Override
	public void configure()
	{
		// show the configure data point dialog
		LocalDataPointConfigDialog dialog = new LocalDataPointConfigDialog(new javax.swing.JFrame(), true);
		
		dialog.setPoint(this);
		dialog.setVisible(true);
	}
	
    // Properties
	
	
    // Methods
	void onRegisterPointCallback(NerduinoBase nerduino, byte[] data)
	{
		byte offset = 3;
		
		short responseToken = BitConverter.GetShort(data, offset);
		offset += 2;

		byte idtype = data[offset++];
						
		switch(idtype)
		{
			case 0: // by name
				byte slength = data[offset++];
				offset += slength;
				
				break;
			case 1: // by index
				offset += 2;

				break;
		}
		
    	// determine if this nerduino already has a registered callback, if so remove the existing 
    	// callback and replace it with this new one
		for(int i = 0; i < m_callbacks.size(); i++)
		{
			ValueCallback cb = m_callbacks.get(i);

			if (cb.ResponseToken == responseToken && nerduino == cb.Nerduino)
			{
				// remove this callback
				m_callbacks.remove(i);
				break;
			}
		}
		
		
    	ValueCallback callback = new ValueCallback();
    	
    	callback.ResponseToken = responseToken;
		
    	callback.FilterType = FilterTypeEnum.valueOf(data[offset++]);
    	callback.Nerduino = nerduino;
    	callback.DataPoint = this;
        
    	// make sure that the data types match
    	switch (callback.FilterType)
    	{
            case FT_NoFilter:
                break;
    		case FT_PercentChange:
    			if (callback.FilterDataType != DataTypeEnum.DT_Float)
    				return; // not a valid data type
                
               	callback.FilterLength = data[offset++];
            	callback.FilterValue = NerduinoHost.parseValue(data, offset, callback.FilterDataType, callback.FilterLength);
                
    			break;
    		case FT_ValueChange:
    			if (callback.FilterDataType != DataType)
    				return; // not a valid data type
                
            	callback.FilterLength = data[offset++];
                callback.FilterValue = NerduinoHost.parseValue(data, offset, callback.FilterDataType, callback.FilterLength);
                
                break;
    	}
    	
    	m_callbacks.add(callback);
    	
    	// immediately send updated value
    	callback.sendUpdate(m_value);
	}
	
	void onUnregisterPointCallback(NerduinoBase nerduino)
	{
		// determine if this nerduino already has a registered callback, if so remove the existing 
    	// callback and replace it with this new one
		for(int i = 0; i < m_callbacks.size(); i++)
		{
			ValueCallback cb = m_callbacks.get(i);

			if (nerduino == cb.Nerduino)
			{
				// remove this callback
				m_callbacks.remove(i);
				break;
			}
		}
	}

    public void onRegisterPointCallback(ZigbeeReceivePacketFrame zrf)
    {
    	// lookup the nerduino that is to be called back
    	NerduinoZigbee nerduino = NerduinoManager.Current.getNerduino(zrf.SourceAddress, zrf.SourceNetworkAddress);
    	
		onRegisterPointCallback(nerduino, zrf.Data);
    }
    
    public void onUnregisterPointCallback(ZigbeeReceivePacketFrame zrf)
    {
    	// lookup the nerduino that is to be called back
    	NerduinoZigbee nerduino = NerduinoManager.Current.getNerduino(zrf.SourceAddress, zrf.SourceNetworkAddress);
    	
    	// determine if this nerduino already has a registered callback, if so remove the existing 
    	// callback and replace it with this new one
    	
    	for(int i = 0; i < m_callbacks.size(); i++)
    	{
    		ValueCallback callback = m_callbacks.get(i);
    		
    		if (callback.Nerduino == nerduino)
    		{
    			m_callbacks.remove(i);
    			
    			return;
    		}
    	}

    }
    
    public void onSetPointValue(ZigbeeReceivePacketFrame zrf)
    {
    	if (isReadOnly())
    	{
    		return;
    	}
    	
    	DataTypeEnum dtype = DataTypeEnum.valueOf(zrf.Data[3]);
    	
    	if (dtype == DataType)
    	{
    		byte dlength = zrf.Data[4];
    	
    		Object value = NerduinoHost.parseValue(zrf.Data, 5, dtype, dlength);
    	
    		// the data type must match
    	
    		setValue(value);
    	}
    }
    
	public void sendGetPointValueResponse(NerduinoBase nerduino, short responseToken)
	{
		nerduino.sendGetPointValueResponse(responseToken, Id, Status, 
				DataType, DataLength, NerduinoHost.toBytes(m_value));
	}
    
    public void scanCallbacks()
    {
    	// remove any nerduinos that are offline for too long a time from the callbacks list
    	for(int i = m_callbacks.size() - 1; i >= 0; i--)
    	{
    		ValueCallback callback = m_callbacks.get(i);
    		
    		if (callback.Nerduino.getStatus() != NerduinoStatusEnum.Online 
					&& callback.Nerduino.getTimeSinceLastResponse() > 3000.0)
    		{
    			// if not online for more than five minutes the remove it from the list
    			m_callbacks.remove(i);
    		}
    	}
    }
	
	@Override
	public void doubleClick(java.awt.event.MouseEvent evt) 
    {
		configure();
    }
	
	@Override
	public PropertySet[] getPropertySets()
	{
		try
		{
			final Sheet.Set pointsSheet = Sheet.createPropertiesSet();
			
			pointsSheet.setDisplayName("Point");
			
			PropertySupport.Reflection prop = null;
			
			switch(DataType)
			{
				case DT_Boolean:
					prop = new PropertySupport.Reflection(this, boolean.class, "Boolean");
					break;
				case DT_Byte:
					prop = new PropertySupport.Reflection(this, byte.class, "Byte");
					break;
				case DT_Short:
					prop = new PropertySupport.Reflection(this, short.class, "Short");
					break;
				case DT_Integer:
					prop = new PropertySupport.Reflection(this, int.class, "Int");
					break;
				case DT_Float:
					prop = new PropertySupport.Reflection(this, float.class, "Float");
					break;
				case DT_String:
					prop = new PropertySupport.Reflection(this, String.class, "String");
					break;
			}

			prop.setName(getName());

			pointsSheet.put(prop);

			return new PropertySet[] { pointsSheet };
		}
		catch(NoSuchMethodException ex)
		{
			Exceptions.printStackTrace(ex);
			return null;
		}
	}
	
	public void setValue(Object value)
    {
        if (m_value != value)
        {
			if (Proxy != null)
				Proxy.setValue(value);

			super.setValue(value);
        }
    }
	
		@Override
	public Action[] getActions(boolean context)
	{
		return new Action[]
			{
				new LocalDataPoint.RenameAction(getLookup()),
				new LocalDataPoint.DeleteAction(getLookup()),
			};
	}

	public final class RenameAction extends AbstractAction
	{
		private LocalDataPoint node;

		public RenameAction(Lookup lookup)
		{
			node = lookup.lookup(LocalDataPoint.class);

			putValue(AbstractAction.NAME, "Rename Point");
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
		private LocalDataPoint node;

		public DeleteAction(Lookup lookup)
		{
			node = lookup.lookup(LocalDataPoint.class);

			putValue(AbstractAction.NAME, "Delete Point");
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
		
		String newname = JOptionPane.showInputDialog(null, "New Name:", oldname);
		
		if (!newname.matches(oldname))
		{
			setName(newname);
			
			PointManager.Current.saveConfiguration();
		}
	}

	
	public void delete()
	{
		// prompt to verify deletion
		int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this point?", "Delete Point", JOptionPane.YES_NO_OPTION);
		
		if (response == JOptionPane.YES_OPTION)
		{
			try
			{
				destroy();

				PointManager.Current.saveConfiguration();
			}
			catch(IOException ex)
			{
				Exceptions.printStackTrace(ex);
			}
		}
	}
}
