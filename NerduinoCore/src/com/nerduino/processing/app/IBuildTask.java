/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.processing.app;

import com.nerduino.library.NerduinoBase;

/**
 *
 * @author chaselaurendine
 */
public interface IBuildTask
{
	void configure(Sketch sketch, NerduinoBase nerduino);
	void execute();
	void setSuccess(boolean success);
	boolean isSelected();
	void setProgress(int progress);
	void reset();
}
