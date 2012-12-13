/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.actions;

import com.nerduino.services.ServiceManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
    category = "System",
	id = "com.nerduino.actions.CreateServiceAction")
@ActionRegistration(
	iconBase = "com/nerduino/resources/NewService.png",
    displayName = "#CTL_CreateServiceAction")
@Messages("CTL_CreateServiceAction=Service")
public final class CreateServiceAction implements ActionListener
{
	@Override
	public void actionPerformed(ActionEvent e)
	{
		ServiceManager.Current.createNew();
	}
}
