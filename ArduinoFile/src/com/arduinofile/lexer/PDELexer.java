/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arduinofile.lexer;

import com.arduinofile.jcclexer.JavaCharStream;
import com.arduinofile.jcclexer.JavaParserTokenManager;
import com.arduinofile.jcclexer.Token;
import com.arduinofile.jcclexer.TokenMgrError;
import org.netbeans.api.lexer.PartType;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

class PDELexer implements Lexer<PDETokenId>
{
	private LexerRestartInfo<PDETokenId> info;
	private JavaParserTokenManager javaParserTokenManager;

	PDELexer(LexerRestartInfo<PDETokenId> info)
	{
		this.info = info;
		JavaCharStream stream = new JavaCharStream(info.input());
		javaParserTokenManager = new JavaParserTokenManager(stream);
	}

	@Override
	public org.netbeans.api.lexer.Token<PDETokenId> nextToken()
	{
		try	
		{
			Token token = javaParserTokenManager.getNextToken();
			if (info.input().readLength() < 1)
				return null;
			
			return info.tokenFactory().createToken(PDELanguageHierarchy.getToken(token.kind));
		}
		catch(TokenMgrError e)
		{
			return info.tokenFactory().createToken(PDELanguageHierarchy.getToken(0));
		}
	}

	@Override
	public Object state()
	{
		return null;
	}

	@Override
	public void release()
	{
	}
}