package com.dyn.robot.code.parsetree;

import java.util.List;

import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.IconCategory;
import com.dyn.robot.code.Range;
import com.dyn.robot.code.SyntaxError;
import com.dyn.robot.code.SyntaxSuggestion;

public class InvalidExpression extends Expression {
	private SyntaxError m_error;
	private SyntaxSuggestion m_suggestion;

	public InvalidExpression(Range range, ExpressionType expectedType) {
		super(range);
		m_error = new SyntaxError(range, "gui.dynrobot:syntax_error.expected", IconCategory.getFromType(expectedType));
		m_suggestion = new SyntaxSuggestion(range, true, IconCategory.getFromType(expectedType));
	}

	@Override
	public String evaluate(CodeBuilder builder) {
		return "???";
	}

	@Override
	public ExpressionType getType() {
		return ExpressionType.Error;
	}

	@Override
	public boolean hasSideEffects() {
		return false;
	}

	@Override
	public void verify(List<SyntaxError> errors, List<SyntaxSuggestion> suggestions) {
		errors.add(m_error);
		suggestions.add(m_suggestion);
	}
}
