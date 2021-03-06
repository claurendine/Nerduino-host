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

package com.nerduino.library;

import com.nerduino.xbee.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class LocalDataPoint extends PointBase
{
	// Declarations
	//public RemoteDataPoint Proxy = null;
	public PointBase Proxy = null;
	
	static short nextIndex = 0;
	
    // Constructors
    public LocalDataPoint()
    {
		super();
		
		m_canRename = true;
		m_canDelete = true;
		
		this.Id = nextIndex++;
    }
    
	@Override
	public void showTopComponent()
	{
		// show the configure data point dialog
		LocalDataPointConfigDialog dialog = new LocalDataPointConfigDialog(new javax.swing.JFrame(), true);
		
		dialog.setPoint(this);
		dialog.setVisible(true);
	}
	
	public void onRegisterPointCallback(NerduinoBase requestedBy, short responseToken, byte filterType, byte filterLength, byte[] filterValue)
	{
    	// determine if this nerduino already has a registered callback, if so remove the existing 
    	// callback and replace it with this new one
		for(int i = 0; i < m_callbacks.size(); i++)
		{
			ValueCallback cb = m_callbacks.get(i);

			if (cb.ResponseToken == responseToken && requestedBy == cb.Nerduino)
			{
				// remove this callback
				m_callbacks.remove(i);
				break;
			}
		}
		
    	ValueCallback callback = new ValueCallback();
    	
    	callback.ResponseToken = responseToken;
		
    	callback.FilterType = FilterTypeEnum.valueOf(filterType);
    	callback.Nerduino = requestedBy;
    	callback.DataPoint = this;
        
    	// make sure that the data types match
    	switch (callback.FilterType)
    	{
            case FT_NoFilter:
                break;
    		case FT_PercentChange:
    			if (callback.FilterDataType != DataTypeEnum.DT_Float)
    				return; // not a valid data type
                
               	callback.FilterLength = filterLength;
            	callback.FilterValue = NerduinoHost.parseValue(filterValue, 0, callback.FilterDataType, callback.FilterLength);
                
    			break;
    		case FT_ValueChange:
    			if (callback.FilterDataType != DataType)
    				return; // not a valid data type
                
            	callback.FilterLength = filterLength;
                callback.FilterValue = NerduinoHost.parseValue(filterValue, 0, callback.FilterDataType, callback.FilterLength);
                
                break;
    	}
    	
    	m_callbacks.add(callback);
    	
    	// immediately send updated value
    	callback.sendUpdate(m_value);
	}
	
	public void onRegisterPointCallback(NerduinoBase nerduino, byte[] data)
	{
		byte offset = 3;
		
		short responseToken = (short) (data[offset++]*0x100 + data[offset++]);

		byte addremove = data[offset++];
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
		
		if (addremove == 0)
		{
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
    	NerduinoXBee nerduino = NerduinoManager.Current.getNerduino(zrf.SourceAddress, zrf.SourceNetworkAddress);
    	
		onRegisterPointCallback(nerduino, zrf.Data);
    }
    
    public void onUnregisterPointCallback(ZigbeeReceivePacketFrame zrf)
    {
    	// lookup the nerduino that is to be called back
    	NerduinoXBee nerduino = NerduinoManager.Current.getNerduino(zrf.SourceAddress, zrf.SourceNetworkAddress);
    	
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
    	if (!isReadOnly())
    	{
			DataTypeEnum dtype = DataTypeEnum.valueOf(zrf.Data[3]);

			if (dtype == DataType)
			{
				byte dlength = zrf.Data[4];

				Object value = NerduinoHost.parseValue(zrf.Data, 5, dtype, dlength);

				// the data type must match
				setValue(value);
			}
		}
    }
    
	public void sendGetPointValueResponse(NerduinoBase nerduino, short responseToken)
	{
		nerduino.sendGetPointValueResponse(responseToken, Id, Status, 
				DataType, NerduinoHost.toBytes(m_value));
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
	public PropertySet[] getPropertySets()
	{
		try
		{
			final Sheet.Set pointsSheet = Sheet.createPropertiesSet();
			
			pointsSheet.setDisplayName("Point");
			
			switch(DataType)
			{
				case DT_Boolean:
				{
					PropertySupport.Reflection<Boolean> prop = new PropertySupport.Reflection<Boolean>(this, boolean.class, "Boolean");
					prop.setName(getName());
					pointsSheet.put(prop);
				}
					break;
				case DT_Byte:
				{
					PropertySupport.Reflection<Byte> prop = new PropertySupport.Reflection<Byte>(this, byte.class, "Byte");
					prop.setName(getName());
					pointsSheet.put(prop);
				}
					break;
				case DT_Short:
				{
					PropertySupport.Reflection<Short> prop = new PropertySupport.Reflection<Short>(this, short.class, "Short");
					prop.setName(getName());
					pointsSheet.put(prop);
				}
					break;
				case DT_Integer:
				{
					PropertySupport.Reflection<Integer> prop = new PropertySupport.Reflection<Integer>(this, int.class, "Int");
					prop.setName(getName());
					pointsSheet.put(prop);
				}
					break;
				case DT_Float:
				{
					PropertySupport.Reflection<Float> prop = new PropertySupport.Reflection<Float>(this, float.class, "Float");
					prop.setName(getName());
					pointsSheet.put(prop);
				}
					break;
				case DT_String:
				{
					PropertySupport.Reflection<String> prop = new PropertySupport.Reflection<String>(this, String.class, "String");
					prop.setName(getName());
					pointsSheet.put(prop);
				}
					break;
			}
			
			return new PropertySet[] { pointsSheet };
		}
		catch(NoSuchMethodException ex)
		{
			Exceptions.printStackTrace(ex);
			return null;
		}
	}
	
	@Override
	public void setValue(Object value)
    {
		if (m_setting)
			return;
		
		m_setting = true;
		
        if (m_value != value)
        {
			if (Proxy != null)
				Proxy.setValue(value);

			try
			{
				initializeValue(value);
				
				if (m_nerduino != null)
					m_nerduino.sendSetPointValue(null, Id, DataType, value);
				
				// notify all callbacks of the new value
				for(ValueCallback callback : m_callbacks)
				{
					callback.valueUpdated();
				}
			}
			catch(Exception e)
			{
			}
        }
		
		m_setting = false;
    }

	/*
	@Override
	public Action[] getActions(boolean context)
	{
		return new Action[]
			{
				new LocalDataPoint.RenameAction(getLookup()),
				new LocalDataPoint.DeleteAction(getLookup())
			};
	}
	*/
	
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
				}
			}
		}
	}
	
	@Override
	public void onRename(String oldname, String newName)
	{
		setName(newName);
		
		PointManager.Current.saveConfiguration();
	}
	
	@Override
	public void destroy()
	{
		// prompt to verify deletion
		int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this point?", "Delete Point", JOptionPane.YES_NO_OPTION);
		
		if (response == JOptionPane.YES_OPTION)
		{
			try
			{
				super.destroy();

				PointManager.Current.saveConfiguration();
			}
			catch(IOException ex)
			{
				Exceptions.printStackTrace(ex);
			}
		}
	}
}
