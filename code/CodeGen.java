package com.dyn.robot.code;

import com.dyn.robot.programs.Program;

public class CodeGen {
	public static CodeGenResult check(Program program) {
		return new CodeGenResult(true, null, null, null);
	}

	public static CodeGenResult compile(Program program, CodeBuilder.AnnotationOptions options) {
		return new CodeGenResult(true, program, null, null);
	}
}
