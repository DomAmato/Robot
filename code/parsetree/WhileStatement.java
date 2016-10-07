package com.dyn.robot.code.parsetree;

import java.util.List;
import java.util.Set;

import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.IconCategory;
import com.dyn.robot.code.Range;
import com.dyn.robot.code.SyntaxError;
import com.dyn.robot.code.SyntaxSuggestion;

public class WhileStatement extends Statement {
	private final Expression m_condition;
	private final Block m_block;

	public WhileStatement(Range range, Expression condition, Block block) {
		super(range);
		m_condition = condition;
		m_block = block;
	}

	@Override
	public void evaluate(CodeBuilder builder) {
		builder.addLine("while " + m_condition.evaluate(builder) + " do");
		builder.indent();
		m_block.evaluate(builder);
		builder.outdent();
		builder.addLine("end");
	}

	@Override
	public void getUsedVariables(Set<String> variables) {
		m_condition.getUsedVariables(variables);
		m_block.getUsedVariables(variables);
	}

	@Override
	public boolean isMultiLine() {
		return true;
	}

	@Override
	public void verify(List<SyntaxError> errors, List<SyntaxSuggestion> suggestions) {
		m_condition.verify(errors, suggestions);
		if ((m_condition.getType() != ExpressionType.Error) && (m_condition.getType() != ExpressionType.Boolean)
				&& (m_condition.getType() != ExpressionType.Unknown)) {
			errors.add(new SyntaxError(m_condition.getRange(), "gui.dynrobot:syntax_error.expected",
					IconCategory.getFromType(ExpressionType.Boolean)));
		}
		m_block.verify(errors, suggestions);
	}
}
