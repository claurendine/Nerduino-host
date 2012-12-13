/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.actions;

import com.nerduino.library.NerduinoManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
	category = "System",
	id = "com.nerduino.actions.CreateNerduinoUSBAction")
@ActionRegistration(
	iconBase = "com/nerduino/resources/NewUsbNerduino.png",
	displayName = "#CTL_CreateNerduinoUSBAction")
@ActionReferences(
{
})
@Messages("CTL_CreateNerduinoUSBAction=Nerduino")
public final class CreateNerduinoUSBAction implements ActionListener
{
	@Override
	public void actionPerformed(ActionEvent e)
	{
		NerduinoManager.Current.createNew();
	}
}
