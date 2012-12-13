/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arduinofile;

import com.arduinofile.lexer.PDETokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;

@LanguageRegistration(mimeType = "text/x-arduino")
public class PDELanguage extends DefaultLanguageConfig
{
	@Override
	public Language getLexerLanguage()
	{
		return PDETokenId.getLanguage();
	}

	@Override
	public String getDisplayName()
	{
		return "PDE";
	}
}