/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arduinofile.lexer;

import java.util.*;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

public class PDELanguageHierarchy extends LanguageHierarchy<PDETokenId>
{
	private static List<PDETokenId> tokens;
	private static Map<Integer, PDETokenId> idToToken;

	private static void init()
	{
		tokens = Arrays.<PDETokenId>asList(new PDETokenId[]
				{
					new PDETokenId("EOF", "whitespace", 0),
					new PDETokenId("WHITESPACE", "whitespace", 1),
					new PDETokenId("SINGLE_LINE_COMMENT", "comment", 4),
					new PDETokenId("FORMAL_COMMENT", "comment", 5),
					new PDETokenId("MULTI_LINE_COMMENT", "comment", 6),
					new PDETokenId("ABSTRACT", "keyword", 8),
					new PDETokenId("ASSERT", "keyword", 9),
					new PDETokenId("BOOLEAN", "keyword", 10),
					new PDETokenId("BREAK", "keyword", 11),
					new PDETokenId("BYTE", "keyword", 12),
					new PDETokenId("CASE", "keyword", 13),
					new PDETokenId("CATCH", "keyword", 14),
					new PDETokenId("CHAR", "keyword", 15),
					new PDETokenId("CLASS", "keyword", 16),
					new PDETokenId("CONST", "keyword", 17),
					new PDETokenId("CONTINUE", "keyword", 18),
					new PDETokenId("_DEFAULT", "keyword", 19),
					new PDETokenId("DO", "keyword", 20),
					new PDETokenId("DOUBLE", "keyword", 21),
					new PDETokenId("ELSE", "keyword", 22),
					new PDETokenId("ENUM", "keyword", 23),
					new PDETokenId("EXTENDS", "keyword", 24),
					new PDETokenId("FALSE", "keyword", 25),
					new PDETokenId("FINAL", "keyword", 26),
					new PDETokenId("FINALLY", "keyword", 27),
					new PDETokenId("FLOAT", "keyword", 28),
					new PDETokenId("FOR", "keyword", 29),
					new PDETokenId("GOTO", "keyword", 30),
					new PDETokenId("IF", "keyword", 31),
					new PDETokenId("IMPLEMENTS", "keyword", 32),
					new PDETokenId("IMPORT", "keyword", 33),
					new PDETokenId("INSTANCEOF", "keyword", 34),
					new PDETokenId("INT", "keyword", 35),
					new PDETokenId("INTERFACE", "keyword", 36),
					new PDETokenId("LONG", "keyword", 37),
					new PDETokenId("NATIVE", "keyword", 38),
					new PDETokenId("NEW", "keyword", 39),
					new PDETokenId("NULL", "keyword", 40),
					new PDETokenId("PACKAGE", "keyword", 41),
					new PDETokenId("PRIVATE", "keyword", 42),
					new PDETokenId("PROTECTED", "keyword", 43),
					new PDETokenId("PUBLIC", "keyword", 44),
					new PDETokenId("RETURN", "keyword", 45),
					new PDETokenId("SHORT", "keyword", 46),
					new PDETokenId("STATIC", "keyword", 47),
					new PDETokenId("STRICTFP", "keyword", 48),
					new PDETokenId("SUPER", "keyword", 49),
					new PDETokenId("SWITCH", "keyword", 50),
					new PDETokenId("SYNCHRONIZED", "keyword", 51),
					new PDETokenId("THIS", "keyword", 52),
					new PDETokenId("THROW", "keyword", 53),
					new PDETokenId("THROWS", "keyword", 54),
					new PDETokenId("TRANSIENT", "keyword", 55),
					new PDETokenId("TRUE", "keyword", 56),
					new PDETokenId("TRY", "keyword", 57),
					new PDETokenId("VOID", "keyword", 58),
					new PDETokenId("VOLATILE", "keyword", 59),
					new PDETokenId("WHILE", "keyword", 60),
					new PDETokenId("INTEGER_LITERAL", "literal", 61),
					new PDETokenId("DECIMAL_LITERAL", "literal", 62),
					new PDETokenId("HEX_LITERAL", "literal", 63),
					new PDETokenId("OCTAL_LITERAL", "literal", 64),
					new PDETokenId("FLOATING_POINT_LITERAL", "literal", 65),
					new PDETokenId("DECIMAL_FLOATING_POINT_LITERAL", "literal", 66),
					new PDETokenId("DECIMAL_EXPONENT", "number", 67),
					new PDETokenId("HEXADECIMAL_FLOATING_POINT_LITERAL", "literal", 68),
					new PDETokenId("HEXADECIMAL_EXPONENT", "number", 69),
					new PDETokenId("CHARACTER_LITERAL", "literal", 70),
					new PDETokenId("STRING_LITERAL", "literal", 71),
					new PDETokenId("IDENTIFIER", "identifier", 72),
					new PDETokenId("LETTER", "literal", 73),
					new PDETokenId("PART_LETTER", "literal", 74),
					new PDETokenId("LPAREN", "operator", 75),
					new PDETokenId("RPAREN", "operator", 76),
					new PDETokenId("LBRACE", "operator", 77),
					new PDETokenId("RBRACE", "operator", 78),
					new PDETokenId("LBRACKET", "operator", 79),
					new PDETokenId("RBRACKET", "operator", 80),
					new PDETokenId("SEMICOLON", "operator", 81),
					new PDETokenId("COMMA", "operator", 82),
					new PDETokenId("DOT", "operator", 83),
					new PDETokenId("AT", "operator", 84),
					new PDETokenId("ASSIGN", "operator", 85),
					new PDETokenId("LT", "operator", 86),
					new PDETokenId("BANG", "operator", 87),
					new PDETokenId("TILDE", "operator", 88),
					new PDETokenId("HOOK", "operator", 89),
					new PDETokenId("COLON", "operator", 90),
					new PDETokenId("EQ", "operator", 91),
					new PDETokenId("LE", "operator", 92),
					new PDETokenId("GE", "operator", 93),
					new PDETokenId("NE", "operator", 94),
					new PDETokenId("SC_OR", "operator", 95),
					new PDETokenId("SC_AND", "operator", 96),
					new PDETokenId("INCR", "operator", 97),
					new PDETokenId("DECR", "operator", 98),
					new PDETokenId("PLUS", "operator", 99),
					new PDETokenId("MINUS", "operator", 100),
					new PDETokenId("STAR", "operator", 101),
					new PDETokenId("SLASH", "operator", 102),
					new PDETokenId("BIT_AND", "operator", 103),
					new PDETokenId("BIT_OR", "operator", 104),
					new PDETokenId("XOR", "operator", 105),
					new PDETokenId("REM", "operator", 106),
					new PDETokenId("LSHIFT", "operator", 107),
					new PDETokenId("PLUSASSIGN", "operator", 108),
					new PDETokenId("MINUSASSIGN", "operator", 109),
					new PDETokenId("STARASSIGN", "operator", 110),
					new PDETokenId("SLASHASSIGN", "operator", 111),
					new PDETokenId("ANDASSIGN", "operator", 112),
					new PDETokenId("ORASSIGN", "operator", 113),
					new PDETokenId("XORASSIGN", "operator", 114),
					new PDETokenId("REMASSIGN", "operator", 115),
					new PDETokenId("LSHIFTASSIGN", "operator", 116),
					new PDETokenId("RSIGNEDSHIFTASSIGN", "operator", 117),
					new PDETokenId("RUNSIGNEDSHIFTASSIGN", "operator", 118),
					new PDETokenId("ELLIPSIS", "operator", 119),
					new PDETokenId("RUNSIGNEDSHIFT", "operator", 120),
					new PDETokenId("RSIGNEDSHIFT", "operator", 121),
					new PDETokenId("GT", "operator", 122),
					new PDETokenId("POUND", "operator", 123)
				});

		idToToken = new HashMap<Integer, PDETokenId>();

		for (PDETokenId token : tokens)
		{
			idToToken.put(token.ordinal(), token);
		}
	}

	static synchronized PDETokenId getToken(int id)
	{
		if (idToToken == null)
		{
			init();
		}
		return idToToken.get(id);
	}

	@Override
	protected synchronized Collection<PDETokenId> createTokenIds()
	{
		if (tokens == null)
		{
			init();
		}
		return tokens;
	}

	@Override
	protected synchronized Lexer<PDETokenId> createLexer(LexerRestartInfo<PDETokenId> info)
	{
		return new PDELexer(info);
	}

	@Override
	protected String mimeType()
	{
		return "text/x-arduino";
	}
}