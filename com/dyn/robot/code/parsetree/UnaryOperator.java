package com.dyn.robot.code.parsetree;

import com.dyn.robot.code.Range;

public class UnaryOperator extends Node {
	private final String m_codeRepresentation;
	private final ExpressionType m_input;
	private final ExpressionType m_output;

	public UnaryOperator(Range range, String codeRepresentation, ExpressionType input, ExpressionType output) {
		super(range);
		m_codeRepresentation = codeRepresentation;
		m_input = input;
		m_output = output;
	}

	public ExpressionType getExpectedInput() {
		return m_output;
	}

	public String getRepresentation() {
		return m_codeRepresentation;
	}

	public ExpressionType getType(ExpressionType right) {
		if (right != ExpressionType.Error) {
			if (right == m_input) {
				return m_output;
			}
			if (right == ExpressionType.Unknown) {
				return ExpressionType.Unknown;
			}
		}
		return ExpressionType.Error;
	}
}
