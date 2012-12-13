/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.actions;


import com.nerduino.webhost.WebHost;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
		category = "System",
		id = "com.nerduino.actions.WebServerEnabledAction")
@ActionRegistration(
		iconBase = "com/nerduino/resources/HostEnabled.png",
		displayName = "#CTL_WebServerEnabledAction")
@ActionReferences({})
@Messages("CTL_WebServerEnabledAction=Web")
public final class WebServerEnabledAction implements ActionListener
{
	@Override
	public void actionPerformed(ActionEvent e)
	{
		WebHost.Current.setEnabled(!WebHost.Current.getEnabled());
	}
}
