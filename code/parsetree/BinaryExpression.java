package com.dyn.robot.code.parsetree;

import java.util.List;
import java.util.Set;

import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.IconCategory;
import com.dyn.robot.code.Range;
import com.dyn.robot.code.SyntaxError;
import com.dyn.robot.code.SyntaxSuggestion;

public class BinaryExpression extends Expression {
	private final BinaryOperator m_operator;
	private final Expression m_left;
	private final Expression m_right;

	public BinaryExpression(Range range, Expression left, BinaryOperator operator, Expression right) {
		super(range);
		m_operator = operator;
		m_left = left;
		m_right = right;
	}

	@Override
	public String evaluate(CodeBuilder builder) {
		return m_left.evaluate(builder) + " " + m_operator.getRepresentation() + " " + m_right.evaluate(builder);
	}

	@Override
	public ExpressionType getType() {
		return m_operator.getType(m_left.getType(), m_right.getType());
	}

	@Override
	public void getUsedVariables(Set<String> variables) {
		m_left.getUsedVariables(variables);
		m_right.getUsedVariables(variables);
	}

	@Override
	public boolean hasSideEffects() {
		return (m_left.hasSideEffects()) || (m_right.hasSideEffects());
	}

	@Override
	public void verify(List<SyntaxError> errors, List<SyntaxSuggestion> suggestions) {
		m_left.verify(errors, suggestions);
		m_right.verify(errors, suggestions);
		if (getType() == ExpressionType.Error) {
			ExpressionType leftType = m_left.getType();
			ExpressionType expectedLeft = m_operator.getExpectedInputLeft();
			ExpressionType expectedRight = m_operator.getExpectedInputRight(leftType);
			if (expectedLeft != ExpressionType.Unknown) {
				if ((leftType != ExpressionType.Error) && (leftType != expectedLeft)
						&& (leftType != ExpressionType.Unknown)) {
					expectedRight = expectedLeft;
					errors.add(new SyntaxError(m_operator.getRange(), "gui.dynrobot:syntax_error.expected",
							IconCategory.getOperatorFromType(expectedLeft)));
				}
			}
			ExpressionType rightType = m_right.getType();
			if (expectedRight != ExpressionType.Unknown) {
				if ((rightType != ExpressionType.Error) && (rightType != expectedRight)
						&& (rightType != ExpressionType.Unknown)) {
					errors.add(new SyntaxError(m_right.getRange(), "gui.dynrobot:syntax_error.expected",
							IconCategory.getFromType(expectedRight)));
				}
			}
		}
	}
}
