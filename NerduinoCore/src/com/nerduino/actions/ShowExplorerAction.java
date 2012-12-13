/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.actions;

import com.nerduino.core.ExplorerTopComponent;
import com.nerduino.processing.app.BuilderTopComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
		category = "View",
		id = "com.nerduino.actions.ShowExplorerAction")
@ActionRegistration(
		iconBase = "com/nerduino/resources/Explorer16.png",
		displayName = "#CTL_ShowExplorerAction")
@ActionReferences({})
@Messages("CTL_ShowExplorerAction=Show Explorer")
public final class ShowExplorerAction implements ActionListener
{
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (ExplorerTopComponent.Current == null)
		{
			ExplorerTopComponent tc = new ExplorerTopComponent();
		}
		
		if (ExplorerTopComponent.Current.isOpened())
			ExplorerTopComponent.Current.close();
		else
		{
			ExplorerTopComponent.Current.open();
			ExplorerTopComponent.Current.requestActive();
		}
	}
}
