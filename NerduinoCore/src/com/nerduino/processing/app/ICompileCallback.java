/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerduino.processing.app;

/**
 *
 * @author chaselaurendine
 */
public interface ICompileCallback
{
	void setProgress(int progress);
	void setCompileSuccess(boolean success);
	void setErrorCount(int count);
	void setStatus(String status);
}
