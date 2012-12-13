package com.nerduino.propertybrowser;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;

public class Inplace implements InplaceEditor
{

	public Inplace()
	{
	}

	
	private final JLabel picker = new JLabel();
	private PropertyEditor editor = null;

	public void connect(PropertyEditor propertyEditor, PropertyEnv env) 
	{
		editor = propertyEditor;
		reset();
	}

	public JComponent getComponent() 
	{
		return picker;
	}

	public void clear() 
	{
		//avoid memory leaks:
		editor = null;
		model = null;
	}

	public Object getValue() 
	{
		return picker.getText();
	}

	public void setValue(Object object) 
	{
		picker.setText (object.toString());
	}

	public boolean supportsTextEntry() 
	{
		return true;
	}

	public void reset() 
	{
		Object obj = editor.getValue();
		if (obj != null) 
			picker.setText(obj.toString());
	}

	public KeyStroke[] getKeyStrokes() 
	{
		return new KeyStroke[0];
	}

	public PropertyEditor getPropertyEditor() 
	{
		return editor;
	}

	public PropertyModel getPropertyModel() 
	{
		return model;
	}

	private PropertyModel model;
	public void setPropertyModel(PropertyModel propertyModel) 
	{
		this.model = propertyModel;
	}

	public boolean isKnownComponent(Component component) 
	{
		return component == picker || picker.isAncestorOf(component);
	}

	public void addActionListener(ActionListener actionListener) 
	{
	   //do nothing - not needed for this component
	}

	public void removeActionListener(ActionListener actionListener) 
	{
	   //do nothing - not needed for this component
	}
}
