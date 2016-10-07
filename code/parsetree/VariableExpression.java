package com.dyn.robot.code.parsetree;

import java.util.List;
import java.util.Set;

import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.Range;
import com.dyn.robot.code.SyntaxError;
import com.dyn.robot.code.SyntaxSuggestion;

public class VariableExpression extends Expression {
	private final Variable m_variable;

	public VariableExpression(Range range, Variable variable) {
		super(range);
		m_variable = variable;
	}

	@Override
	public String evaluate(CodeBuilder builder) {
		return m_variable.getName();
	}

	@Override
	public ExpressionType getType() {
		return ExpressionType.Unknown;
	}

	@Override
	public void getUsedVariables(Set<String> variables) {
		variables.add(m_variable.getName());
	}

	@Override
	public boolean hasSideEffects() {
		return false;
	}

	@Override
	public void verify(List<SyntaxError> errors, List<SyntaxSuggestion> suggestions) {
	}
}
