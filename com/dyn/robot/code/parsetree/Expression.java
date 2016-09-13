package com.dyn.robot.code.parsetree;

import java.util.Set;

import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.Range;

public abstract class Expression extends Node {
	public Expression(Range range) {
		super(range);
	}

	public abstract String evaluate(CodeBuilder paramCodeBuilder);

	public abstract ExpressionType getType();

	public void getUsedVariables(Set<String> variables) {
	}

	public abstract boolean hasSideEffects();
}
