package com.dyn.robot.code.parsetree;

import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.Range;

public class BooleanConstant extends Expression {
	private final boolean m_value;

	public BooleanConstant(Range range, boolean value) {
		super(range);
		m_value = value;
	}

	@Override
	public String evaluate(CodeBuilder builder) {
		return m_value ? "true" : "false";
	}

	@Override
	public ExpressionType getType() {
		return ExpressionType.Boolean;
	}

	@Override
	public boolean hasSideEffects() {
		return false;
	}
}
