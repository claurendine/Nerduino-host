/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.core;

import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall
{
	@Override
	public void restored()
	{
		// TODO
	}
	
	@Override
	public boolean closing()
	{
		AppManager.Current.saveConfiguration();
		
		return true;
	}
}
