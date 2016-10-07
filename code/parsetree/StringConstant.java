package com.dyn.robot.code.parsetree;

import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.Range;

public class StringConstant extends Expression {
	private final String m_value;

	public StringConstant(Range range, String value) {
		super(range);
		m_value = value;
	}

	@Override
	public String evaluate(CodeBuilder builder) {
		return "\"" + m_value + "\"";
	}

	@Override
	public ExpressionType getType() {
		return ExpressionType.String;
	}

	@Override
	public boolean hasSideEffects() {
		return false;
	}
}
