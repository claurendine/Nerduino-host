/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.actions;

import com.nerduino.core.PropertiesTopComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
		category = "View",
		id = "com.nerduino.actions.ShowPropertiesAction")
@ActionRegistration(
		iconBase = "com/nerduino/resources/Properties16.png",
		displayName = "#CTL_ShowPropertiesAction")
@ActionReferences({})
@Messages("CTL_ShowPropertiesAction=Show Properties")
public final class ShowPropertiesAction implements ActionListener
{
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (PropertiesTopComponent.Current.isOpened())
			PropertiesTopComponent.Current.close();
		else
		{
			PropertiesTopComponent.Current.open();
			PropertiesTopComponent.Current.requestActive();
		}
	}
}
