/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arduinofile.parser;

import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.netbeans.spi.editor.bracesmatching.support.BracesMatcherSupport;

public class PDEBracesMatcherFactory implements BracesMatcherFactory
{
	@Override
	public BracesMatcher createMatcher(MatcherContext context)
	{
		
		return BracesMatcherSupport.defaultMatcher(context, -1, -1);
	}
}