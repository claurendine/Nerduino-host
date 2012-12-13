/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.actions;

import com.nerduino.core.OutputTopComponent;
import com.nerduino.processing.app.BuilderTopComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
    category = "View",
id = "com.nerduino.actions.ShowOutputAction")
@ActionRegistration(
    iconBase = "com/nerduino/resources/Output.png",
displayName = "#CTL_ShowOutputAction")
@Messages("CTL_ShowOutputAction=Output")
public final class ShowOutputAction implements ActionListener
{
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (OutputTopComponent.Current == null)
			new OutputTopComponent();
		
		if (OutputTopComponent.Current.isOpened())
			OutputTopComponent.Current.close();
		else
		{
			OutputTopComponent.Current.open();
			OutputTopComponent.Current.requestActive();
		}
	}
}
