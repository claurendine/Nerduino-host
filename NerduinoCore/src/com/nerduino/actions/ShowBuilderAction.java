/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.actions;

import com.nerduino.processing.app.BuilderTopComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
    category = "View",
id = "com.nerduino.actions.ShowBuilderAction")
@ActionRegistration(
    iconBase = "com/nerduino/resources/Builder16.png",
displayName = "#CTL_ShowBuilderAction")
@Messages("CTL_ShowBuilderAction=Builder")
public final class ShowBuilderAction implements ActionListener
{
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (BuilderTopComponent.Current == null)
			new BuilderTopComponent();
		
		if (BuilderTopComponent.Current.isOpened())
			BuilderTopComponent.Current.close();
		else
		{
			BuilderTopComponent.Current.open();
			BuilderTopComponent.Current.requestActive();
		}
	}
}
