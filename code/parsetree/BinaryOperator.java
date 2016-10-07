package com.dyn.robot.code.parsetree;

import com.dyn.robot.code.Range;

public class BinaryOperator extends Node {
	private final String m_codeRepresentation;
	private final ExpressionType m_input;
	private final ExpressionType m_output;

	public BinaryOperator(Range range, String codeRepresentation, ExpressionType input, ExpressionType output) {
		super(range);
		m_codeRepresentation = codeRepresentation;
		m_input = input;
		m_output = output;
	}

	public ExpressionType getExpectedInputLeft() {
		return m_input;
	}

	public ExpressionType getExpectedInputRight(ExpressionType left) {
		if (m_input == ExpressionType.Unknown) {
			return left;
		}
		return m_input;
	}

	public String getRepresentation() {
		return m_codeRepresentation;
	}

	public ExpressionType getType(ExpressionType left, ExpressionType right) {
		if ((left != ExpressionType.Error) && (right != ExpressionType.Error)) {
			if (m_input == ExpressionType.Unknown) {
				return m_output;
			}
			if ((left == m_input) && (right == m_input)) {
				return m_output;
			}
			if (((left == ExpressionType.Unknown) || (left == m_input))
					&& ((right == ExpressionType.Unknown) || (right == m_input))) {
				return ExpressionType.Unknown;
			}
		}
		return ExpressionType.Error;
	}
}
