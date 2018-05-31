package com.rabbit.gui.component.code.parser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

public class DescriptiveErrorListener extends BaseErrorListener {
	public static DescriptiveErrorListener INSTANCE = new DescriptiveErrorListener();

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
			String msg, RecognitionException e) {

		Token token = (Token) offendingSymbol;
		if (token.getType() != Recognizer.EOF) {
			System.out.println("line " + line + ":" + charPositionInLine + " Token " + token.getType() + " " + msg);
		}
	}
}