/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(
    category = "View",
id = "com.nerduino.actions.ShowPaletteAction")
@ActionRegistration(
    iconBase = "com/nerduino/resources/Palette16.png",
displayName = "#CTL_ShowPaletteAction")
@Messages("CTL_ShowPaletteAction=Palette")
public final class ShowPaletteAction implements ActionListener
{
	@Override
	public void actionPerformed(ActionEvent e)
	{
		TopComponent tcPalette = WindowManager.getDefault().findTopComponent("CommonPalette");
		
		if (tcPalette != null)
			if (tcPalette.isOpened())
				tcPalette.close();
			else
				tcPalette.open();
	}
}
