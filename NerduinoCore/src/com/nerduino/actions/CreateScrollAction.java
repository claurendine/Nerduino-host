/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.actions;

import com.nerduino.scrolls.ScrollManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
	category = "System",
	id = "com.nerduino.actions.CreateScrollAction")
@ActionRegistration(
	iconBase = "com/nerduino/resources/NewScroll.png",
	displayName = "#CTL_CreateScrollAction")
@ActionReferences(
{
})
@Messages("CTL_CreateScrollAction=Scroll")
public final class CreateScrollAction implements ActionListener
{
	@Override
	public void actionPerformed(ActionEvent e)
	{
		ScrollManager.Current.createNew();
	}
}
