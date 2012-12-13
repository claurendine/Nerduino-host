/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.actions;

import com.nerduino.library.PointManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
	category = "System",
	id = "com.nerduino.actions.CreatePointAction")
@ActionRegistration(
	iconBase = "com/nerduino/resources/NewPoint.png",
	displayName = "#CTL_CreatePointAction")
@ActionReferences(
{
})
@Messages("CTL_CreatePointAction=Skit")
public final class CreatePointAction implements ActionListener
{
	@Override
	public void actionPerformed(ActionEvent e)
	{
		PointManager.Current.createNew();
	}
}
