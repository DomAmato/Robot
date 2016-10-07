package com.dyn.robot.code.parsetree;

import java.util.List;
import java.util.Set;

import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.IconCategory;
import com.dyn.robot.code.Range;
import com.dyn.robot.code.SyntaxError;
import com.dyn.robot.code.SyntaxSuggestion;

public class UnaryExpression extends Expression {
	private final UnaryOperator m_operator;
	private final Expression m_right;

	public UnaryExpression(Range range, UnaryOperator operator, Expression right) {
		super(range);
		m_operator = operator;
		m_right = right;
	}

	@Override
	public String evaluate(CodeBuilder builder) {
		String representation = m_operator.getRepresentation();
		if (representation.equals("-")) {
			return representation + m_right.evaluate(builder);
		}
		return representation + " " + m_right.evaluate(builder);
	}

	@Override
	public ExpressionType getType() {
		return m_operator.getType(m_right.getType());
	}

	@Override
	public void getUsedVariables(Set<String> variables) {
		m_right.getUsedVariables(variables);
	}

	@Override
	public boolean hasSideEffects() {
		return m_right.hasSideEffects();
	}

	@Override
	public void verify(List<SyntaxError> errors, List<SyntaxSuggestion> suggestions) {
		m_right.verify(errors, suggestions);
		if (getType() == ExpressionType.Error) {
			ExpressionType expectedType = m_operator.getExpectedInput();
			if (expectedType != ExpressionType.Unknown) {
				ExpressionType suppliedType = m_right.getType();
				if ((suppliedType != ExpressionType.Unknown) && (suppliedType != expectedType)
						&& (suppliedType != ExpressionType.Error)) {
					errors.add(new SyntaxError(m_right.getRange(), "gui.dynrobot:syntax_error.expected",
							IconCategory.getFromType(expectedType)));
				}
			}
		}
	}
}
