package com.dyn.robot.code.parsetree;

import java.util.List;

import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.IconCategory;
import com.dyn.robot.code.Range;
import com.dyn.robot.code.SyntaxError;
import com.dyn.robot.code.SyntaxSuggestion;

public class InvalidStatement extends Statement {
	private SyntaxError m_error;
	private SyntaxSuggestion m_suggestion;

	public InvalidStatement(Range range) {
		super(range);
		m_error = new SyntaxError(range, "gui.dynrobot:syntax_error.expected", IconCategory.Statement);
		m_suggestion = new SyntaxSuggestion(range, true, IconCategory.Statement);
	}

	@Override
	public void evaluate(CodeBuilder builder) {
		builder.addLine("???");
	}

	@Override
	public void verify(List<SyntaxError> errors, List<SyntaxSuggestion> suggestions) {
		errors.add(m_error);
		suggestions.add(m_suggestion);
	}
}
