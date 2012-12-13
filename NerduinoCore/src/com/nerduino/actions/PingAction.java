/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.actions;

import com.nerduino.library.NerduinoManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

@ActionID(
		category = "System",
		id = "com.nerduino.actions.PingAction")
@ActionRegistration(
		iconBase = "com/nerduino/resources/Ping.png",
		displayName = "#CTL_PingAction")
@ActionReferences({})
@Messages("CTL_PingAction=Ping")
public final class PingAction implements ActionListener
{
	@Override
	public void actionPerformed(ActionEvent e)
	{
		NerduinoManager.Current.pingAll();
		
		StatusDisplayer.getDefault().setStatusText("Pinging!");
		
		//InputOutput io = IOProvider.getDefault().getIO(Bundle.CTL_ShowXMLStructureActionListener(), false);
		InputOutput io = IOProvider.getDefault().getIO("Build", false);
		
		io.select(); //"XML Structure" tab is selected
		try
		{
			io.getOut().reset();
		}
		catch(IOException ex)
		{
			Exceptions.printStackTrace(ex);
		}
		io.getOut().println("Ping!");
	}
}
