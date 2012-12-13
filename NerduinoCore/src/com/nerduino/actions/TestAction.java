/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.actions;

import com.nerduino.library.CommandResponse;
import com.nerduino.library.NerduinoBase;
import com.nerduino.library.NerduinoManager;
import com.nerduino.library.RemoteDataPoint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

@ActionID(
		category = "View",
		id = "com.nerduino.actions.TestAction")
@ActionRegistration(
		iconBase = "com/nerduino/resources/Properties16.png",
		displayName = "#CTL_TestAction")
@ActionReferences({})
@Messages("CTL_TestAction=Test")
public final class TestAction implements ActionListener
{
	@Override
	public void actionPerformed(ActionEvent e)
	{
		InputOutput io = IOProvider.getDefault().getIO("Test", false);
		io.select();
		//io.getOut().println("Hello World!");
		
		// get the test nerduino
		NerduinoBase nerduino = (NerduinoBase) NerduinoManager.Current.getChildren().getNodeAt(0);

		io.getOut().println("Send Hello!");

		CommandResponse response = nerduino.executeCommand("Hello");
		
		switch(response.Status)
		{
			case RS_Complete:
				io.getOut().println(response.toString());

				break;
			case RS_Timeout:
				io.getOut().println("Response Timeout!");

				break;
			case RS_CommandNotRecognized:
				io.getOut().println("Unrecognized Command!");

				break;
		}
/*
		// get the first remotedatapoint
		RemoteDataPoint point = nerduino.getPoint("Count");
		
		if (point != null)
		{
			if (point.getRegistered())
				point.unregister();
			else
//				point.registerWithNoFilter((short) 0);
				point.registerWithChangeFilter((short) 0, (byte) 4);
		}
*/
	}
}
