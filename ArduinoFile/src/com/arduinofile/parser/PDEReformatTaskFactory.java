/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arduinofile.parser;

import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ReformatTask;

public class PDEReformatTaskFactory implements ReformatTask.Factory
{
	@Override
	public ReformatTask createTask(Context context)
	{
		return new PDEReformatTask(context);
	}
}