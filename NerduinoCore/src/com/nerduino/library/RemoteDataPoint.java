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


import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.util.Lookup;

public class RemoteDataPoint extends PointBase
{
    // Declarations
    private Object m_valueResponse = null;
    private Boolean m_registered = false;
    public Boolean Validated;
    public NerduinoBase Parent;
	LocalDataPoint Proxy = null;
    
    // Constructors
    public RemoteDataPoint(NerduinoBase parent)
    {
		super();
		
		Parent = parent;
    }

    public RemoteDataPoint(NerduinoBase parent, short id, String name, byte attributes, DataTypeEnum dataType, byte dataLength, Object value)
    {
        super();
        
        Parent = parent;
        Id = id;
        setName(name);
        DataType = dataType;
        Attributes = attributes;
        DataLength = dataLength;
        m_value = value;
    }

    // Properties
    public Boolean getRegistered()
    { 
    	return m_registered; 
    }
    
	/*
    public Object getPointValue()
    {
        // send value update to the remote nerduino
        Parent.sendGetPointValue(Id);
		
        m_valueResponse = null;
        
        for(int i = 0; i < 300; i++)
        {
            try 
            {
                if (m_valueResponse != null)
                    return m_valueResponse;
                
                Thread.sleep(10);
            } 
            catch (InterruptedException ex) 
            {
                Logger.getLogger(Nerduino.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
 
        return null;
    }
	*/
    
	@Override
    public void setValue(Object value)
    {
		Object val = value;
		
		if (value instanceof String)
		{
			switch(DataType)
			{
				case DT_Boolean:
					if (value.equals("0"))
						val = false;
					else if (value.equals("1"))
						val = true;
					else
						val = Boolean.parseBoolean((String)value);
	
					break;
				case DT_Byte:
					val = Byte.parseByte((String)value);
					break;
				case DT_Float:
					val = Float.parseFloat((String)value);
					break;
				case DT_Integer:
					val = Integer.parseInt((String)value);
					break;
				case DT_Short:
					val = Short.parseShort((String)value);
					break;
			}
		}
		
        if (!isReadOnly() && !m_value.equals(val))
        {
			super.setValue(val);
			
            if (DataType == DataTypeEnum.DT_String)
                DataLength = (byte) ((String) value).length();
            
            // send value update to the remote nerduino
            Parent.sendSetPointValue(Id, DataType, DataLength, val);

			if (Proxy != null)
				Proxy.setValue(val);
		}
    }
    
    public void onGetPointValueResponse(byte[] data)
    {
    	Status = data[7];
    	DataType = DataTypeEnum.valueOf(data[8]);
		
		if (DataType != null)
		{
			switch(DataType)
			{
				case DT_Boolean:
					DataLength = 1;
					break;
				case DT_Byte:
					DataLength = 1;
					break;
				case DT_Short:
					DataLength = 2;
					break;
				case DT_Integer:
					DataLength = 4;
					break;
				case DT_Float:
					DataLength = 4;
					break;
				case DT_String:
					DataLength = data[9];
					break;
			}

			m_value = NerduinoHost.parseValue(data, 10, DataType, DataLength);

			//if (m_value != m_valueResponse)
				m_valueResponse = m_value;

//			InputOutput io = IOProvider.getDefault().getIO("Build", false);
//			io.getOut().println("Update " + m_value.toString());
			
			if (Proxy != null)
				Proxy.setValue(m_value);
		}
		
    	// TODO notify that the value has been updated
    }
    
    public void registerWithNoFilter(short responseToken)
    {
    	register(responseToken, FilterTypeEnum.FT_NoFilter.Value(), 
					(byte) 0, new byte[0]);
    }
    
    public void registerWithPercentFilter(short responseToken, float percent)
    {
        byte[] data = new byte[4];
        
        int fi = Float.floatToRawIntBits(percent);
                
        data[0] = (byte)((fi >> 24) & 0xff);
        data[1] = (byte)((fi >> 16) & 0xff);
        data[2] = (byte)((fi >> 8) & 0xff);
        data[3] = (byte)(fi & 0xff);
        
    	register(responseToken, FilterTypeEnum.FT_PercentChange.Value(), 
					(byte) 4, data);
    }
    
    public void registerWithChangeFilter(short responseToken, byte change)
    {
        byte[] data = new byte[1];
        
        data[0] = change;
        
    	register(responseToken, FilterTypeEnum.FT_ValueChange.Value(), 
					(byte) 1, data);
    }
    
    public void registerWithChangeFilter(short responseToken, short change)
    {
        byte[] data = new byte[2];
        
        data[0] = (byte)((change >> 8) & 0xff);
        data[1] = (byte)(change & 0xff);

		register(responseToken, FilterTypeEnum.FT_ValueChange.Value(), 
					(byte) 2, data);
    }
    
    public void registerWithChangeFilter(short responseToken, int change)
    {
        byte[] data = new byte[4];
        
        data[0] = (byte)((change >> 24) & 0xff);
        data[1] = (byte)((change >> 16) & 0xff);
        data[2] = (byte)((change >> 8) & 0xff);
        data[3] = (byte)(change & 0xff);
        
    	register(responseToken, FilterTypeEnum.FT_ValueChange.Value(), 
					(byte) 4, data);
    }
    
    public void registerWithChangeFilter(short responseToken, float change)
    {
        byte[] data = new byte[4];
        
        int fi = Float.floatToRawIntBits(change);
                
        data[0] = (byte)((fi >> 24) & 0xff);
        data[1] = (byte)((fi >> 16) & 0xff);
        data[2] = (byte)((fi >> 8) & 0xff);
        data[3] = (byte)(fi & 0xff);
        
    	register(responseToken, FilterTypeEnum.FT_ValueChange.Value(), 
					(byte) 4, data);
    }
    
    
    public void register(short responseToken, byte filterType, byte filterLength, byte[] filterValue)
    {
		Parent.sendRegisterPointCallback(Parent, responseToken, Id, filterType, filterLength, filterValue);
		
		m_registered = true;
    }

    public void unregister()
    {
    	Parent.sendUnregisterPointCallback(Parent, Id);
		
		m_registered = false;
    }
	
	/*
	@Override
	public boolean getBoolean()
	{
		if (DataType == DataTypeEnum.DT_Boolean)
			return (Boolean) m_value;
		
		return false;
	}
	
	@Override
	public void setBoolean(boolean value)
	{
		if (DataType == DataTypeEnum.DT_Boolean)
			setValue(value);
	}
	
	
	@Override
	public byte getByte()
	{
		if (DataType == DataTypeEnum.DT_Byte)
			return (Byte) m_value;
		
		return 0;
	}
	
	@Override
	public void setByte(byte value)
	{
		if (DataType == DataTypeEnum.DT_Byte)
			setValue(value);
	}
	
	
	@Override
	public short getShort()
	{
		if (DataType == DataTypeEnum.DT_Short)
			return (Short) m_value;
		
		return 0;
	}
	
	@Override
	public void setShort(short value)
	{
		if (DataType == DataTypeEnum.DT_Short)
			setValue(value);
	}

	
	@Override
	public int getInt()
	{
		if (DataType == DataTypeEnum.DT_Integer)
			return (Integer) m_value;
		
		return 0;
	}
	
	@Override
	public void setInt(int value)
	{
		if (DataType == DataTypeEnum.DT_Integer)
			setValue(value);
	}

	
	@Override
	public float getFloat()
	{
		if (DataType == DataTypeEnum.DT_Float)
			return (Float) m_value;
		
		return 0;
	}
	
	@Override
	public void setFloat(float value)
	{
		if (DataType == DataTypeEnum.DT_Float)
			setValue(value);
	}

	
	@Override
	public String getString()
	{
		if (DataType == DataTypeEnum.DT_String)
			return (String) m_value;
		
		return "";
	}
	
	@Override
	public void setString(String value)
	{
		if (DataType == DataTypeEnum.DT_String)
			setValue(value);
	}
	*/
	
	public void publish()
	{
		// check the publish bit
		if ((Attributes & 0x02) == 0x02)
		{
			String name = Parent.getName() + "_" + getName();
			
			// look for an existing LDP with this name
			Proxy = PointManager.Current.getPoint(name);
			LocalDataPoint newpoint = null;
			
			if (Proxy == null)
			{
				// create a local data point that is a proxy to this remote data point
				Proxy = new LocalDataPoint();
				
				Proxy.setName(name);
				
				newpoint = Proxy;
			}
			
			Proxy.Proxy = this;
			Proxy.Attributes = Attributes;
			Proxy.DataLength = DataLength;
			Proxy.DataType = DataType;
			Proxy.Id = Id;
			Proxy.setValue(m_value);
			
			if (newpoint != null)
				PointManager.Current.addChild(newpoint);
			
		}
		else
		{
			// register a callback for this point
			registerWithNoFilter(Id);
		}
	}

	@Override
	public Action[] getActions(boolean context)
	{
		return new Action[]
			{
				new RemoteDataPoint.DeleteAction(getLookup()),
			};
	}
	
	public final class DeleteAction extends AbstractAction
	{
		private RemoteDataPoint node;

		public DeleteAction(Lookup lookup)
		{
			node = lookup.lookup(RemoteDataPoint.class);

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
	
	public void delete()
	{
	}
}
