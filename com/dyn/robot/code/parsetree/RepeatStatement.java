package com.dyn.robot.code.parsetree;

import java.util.List;
import java.util.Set;

import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.IconCategory;
import com.dyn.robot.code.Range;
import com.dyn.robot.code.SyntaxError;
import com.dyn.robot.code.SyntaxSuggestion;

public class RepeatStatement extends Statement {
	private final Expression m_count;
	private final Block m_block;

	public RepeatStatement(Range range, Expression count, Block block) {
		super(range);
		m_count = count;
		m_block = block;
	}

	@Override
	public void evaluate(CodeBuilder builder) {
		builder.addLine("for n = 1, " + m_count.evaluate(builder) + " do");
		builder.indent();
		m_block.evaluate(builder);
		builder.outdent();
		builder.addLine("end");
	}

	@Override
	public void getUsedVariables(Set<String> variables) {
		m_count.getUsedVariables(variables);
		m_block.getUsedVariables(variables);
	}

	@Override
	public boolean isMultiLine() {
		return true;
	}

	@Override
	public void verify(List<SyntaxError> errors, List<SyntaxSuggestion> suggestions) {
		m_count.verify(errors, suggestions);

		ExpressionType startType = m_count.getType();
		if ((startType != ExpressionType.Error) && (startType != ExpressionType.Number)
				&& (startType != ExpressionType.Unknown)) {
			errors.add(new SyntaxError(m_count.getRange(), "gui.dynrobot:syntax_error.expected",
					IconCategory.getFromType(ExpressionType.Number)));
		}
		m_block.verify(errors, suggestions);
	}
}
