/*
 Part of the Nerduino IOT project - http://nerduino.com

 Copyright (c) 2013 Chase Laurendine

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.nerduino.actions;

import com.nerduino.library.NerduinoManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
	category = "System",
	id = "com.nerduino.actions.CreateNerduinoBTAction")
@ActionRegistration(
	iconBase = "com/nerduino/resources/NewBTNerduino16.png",
	displayName = "#CTL_CreateNerduinoBTAction")
@ActionReferences(
{
})
@Messages("CTL_CreateNerduinoBTAction=Create Nerduino BlueTooth")
public final class CreateNerduinoBTAction implements ActionListener
{
	@Override
	public void actionPerformed(ActionEvent e)
	{
		NerduinoManager.Current.createNewBTNerduino();
	}
}
