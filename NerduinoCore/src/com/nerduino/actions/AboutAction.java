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

import com.nerduino.core.AboutDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
		category = "View",
		id = "com.nerduino.actions.AboutAction")
@ActionRegistration(
		iconBase = "com/nerduino/resources/help.png",
		displayName = "#CTL_AboutAction")
@ActionReferences({})
@Messages("CTL_AboutAction=About")
@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
public final class AboutAction implements ActionListener
{
	@Override
	public void actionPerformed(ActionEvent e)
	{
		AboutDialog ad = new AboutDialog(null, true);
		
		ad.show(true);
	}	
}


		
		