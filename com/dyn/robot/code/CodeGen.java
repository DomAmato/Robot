package com.dyn.robot.code;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dyn.robot.code.grammar.TokenMgrError;
import com.dyn.robot.programs.Program;

public class CodeGen {
	public static CodeGenResult check(Program program) {
		return new CodeGenResult(true, null, null, null);
	}

	public static CodeGenResult compile(Program program, CodeBuilder.AnnotationOptions options) {
		return new CodeGenResult(true, program, null, null);
	}

	private static SyntaxError createSyntaxError(TokenMgrError e) {
		String errorMessage = e.getMessage();
		Matcher matcher = Pattern.compile("at line (\\d+)").matcher(errorMessage);
		if (matcher.find()) {
			String lineString = matcher.group(1);
			int lineNumber = Integer.parseInt(lineString);
			return new SyntaxError(new Range(lineNumber, 1), "gui.dynrobot:syntax_error.generic");
		}
		return new SyntaxError(new Range(0, 1), "gui.dynrobot:syntax_error.generic");
	}

	private static String encodeProgram(Program program) {
		StringBuilder encodedText = new StringBuilder();
		for (int i = 0; i < program.getLength(); i++) {
			String line = program.getLine(i);
			encodedText.append(line);
			encodedText.append('\n');
		}
		return encodedText.toString();
	}
}
