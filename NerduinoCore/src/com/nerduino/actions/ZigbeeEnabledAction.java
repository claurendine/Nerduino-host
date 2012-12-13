/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.actions;


import com.nerduino.library.NerduinoHost;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
		category = "System",
		id = "com.nerduino.actions.ZigbeeEnabledAction")
@ActionRegistration(
		iconBase = "com/nerduino/resources/ZigbeeEnabled.png",
		displayName = "#CTL_ZigbeeEnabledAction")
@ActionReferences({})
@Messages("CTL_ZigbeeEnabledAction=Web")
public final class ZigbeeEnabledAction implements ActionListener
{
	@Override
	public void actionPerformed(ActionEvent e)
	{
		NerduinoHost.Current.setEnabled(!NerduinoHost.Current.getEnabled());
	}
}
