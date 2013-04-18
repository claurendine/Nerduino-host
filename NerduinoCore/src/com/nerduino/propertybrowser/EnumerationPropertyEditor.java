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

package com.nerduino.propertybrowser;

import java.awt.Component;
import java.beans.PropertyEditorSupport;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

public class EnumerationPropertyEditor extends PropertyEditorSupport implements ExPropertyEditor, InplaceEditor.Factory, ListSelectionListener
{	
	JList list;
	
	public Object[] getList()
	{
		Object[] objs = new Object[] { "A", "B", "C" };
		
		return objs;
	}
	
	@Override
	public String getAsText()
	{
		Object obj = getValue();
		
		if (obj == null)
			return "";
		
		return obj.toString();
	}
	
	@Override
	public void setAsText(String value)
	{
		setValue(value);
	}
	
	@Override
	public Component getCustomEditor() 
	{
		if (list == null)
		{
			list = new JList();
			
			list.addListSelectionListener(this);
		}
		
		list.setListData(getList());
		
		Object value = getValue();
		
		list.setSelectedValue(value, true);
		
		return list;
	}

	@Override
	public boolean supportsCustomEditor() 
	{
		return true;
	}
	
	public void attachEnv(PropertyEnv env) 
	{
		env.registerInplaceEditorFactory(this);
	}

	private InplaceEditor ed = null;

	public InplaceEditor getInplaceEditor() 
	{
		if (ed == null) 
			ed = new Inplace();
		
		return ed;
	}

	public void valueChanged(ListSelectionEvent lse)
	{
		String val = list.getSelectedValue().toString();

		setAsText(val);
	}
}
