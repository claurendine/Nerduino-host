/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.actions;

import com.nerduino.processing.app.Sketch;
import com.nerduino.processing.app.SketchManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
    category = "System",
	id = "com.nerduino.actions.CreateSketchAction")
@ActionRegistration(
	iconBase = "com/nerduino/resources/NewSketch.png",
    displayName = "#CTL_CreateSketchAction")
@Messages("CTL_CreateSketchAction=Sketch")
public final class CreateSketchAction implements ActionListener
{
	@Override
	public void actionPerformed(ActionEvent e)
	{
		Sketch sketch = SketchManager.Current.createNewSketch();
	}
}
