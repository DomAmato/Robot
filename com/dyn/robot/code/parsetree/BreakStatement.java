package com.dyn.robot.code.parsetree;

import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.Range;

public class BreakStatement extends Statement {
	public BreakStatement(Range range) {
		super(range);
	}

	@Override
	public void evaluate(CodeBuilder builder) {
		CodeBuilder.AnnotationOptions options = builder.getAnnotationOptions();
		if ((options.getAnnotateLines()) || (options.getAnnotateSlots())) {
			builder.addLine("do break end;");
		} else {
			builder.addLine("break");
		}
	}
}
