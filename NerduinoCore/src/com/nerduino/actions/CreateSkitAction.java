/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.actions;

import com.nerduino.skits.SkitManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
	category = "System",
	id = "com.nerduino.actions.CreateSkitAction")
@ActionRegistration(
	iconBase = "com/nerduino/resources/NewSkit.png",
	displayName = "#CTL_CreateSkitAction")
@ActionReferences(
{
})
@Messages("CTL_CreateSkitAction=Skit")
public final class CreateSkitAction implements ActionListener
{
	@Override
	public void actionPerformed(ActionEvent e)
	{
		SkitManager.Current.createNew();
	}
}
