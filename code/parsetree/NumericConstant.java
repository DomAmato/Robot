package com.dyn.robot.code.parsetree;

import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.Range;

public class NumericConstant extends Expression {
	private final int m_value;

	public NumericConstant(Range range, int value) {
		super(range);
		m_value = value;
	}

	@Override
	public String evaluate(CodeBuilder builder) {
		return Integer.toString(m_value);
	}

	@Override
	public ExpressionType getType() {
		return ExpressionType.Number;
	}

	@Override
	public boolean hasSideEffects() {
		return false;
	}
}
