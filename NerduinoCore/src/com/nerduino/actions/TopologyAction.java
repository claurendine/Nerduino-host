/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
		category = "System",
		id = "com.nerduino.actions.TopologyAction")
@ActionRegistration(
		iconBase = "com/nerduino/resources/Topo.png",
		displayName = "#CTL_TopologyAction")
@ActionReferences({})
@Messages("CTL_TopologyAction=Topo")
public final class TopologyAction implements ActionListener
{
	@Override
	public void actionPerformed(ActionEvent e)
	{
		// TODO engage a topology view topcomponent
	}
}
