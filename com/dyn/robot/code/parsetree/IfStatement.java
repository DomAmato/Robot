package com.dyn.robot.code.parsetree;

import java.util.List;
import java.util.Set;

import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.IconCategory;
import com.dyn.robot.code.Range;
import com.dyn.robot.code.SyntaxError;
import com.dyn.robot.code.SyntaxSuggestion;

public class IfStatement extends Statement {
	public static class ElseIf {
		private final Expression m_condition;
		private final Block m_block;

		public ElseIf(Expression condition, Block block) {
			m_condition = condition;
			m_block = block;
		}
	}

	private final Expression m_condition;
	private final Block m_block;
	private final List<ElseIf> m_elseIfs;

	private Block m_elseBlock;

	public IfStatement(Range range, Expression condition, Block block, List<ElseIf> elseIfs, Block elseBlock) {
		super(range);
		m_condition = condition;
		m_block = block;
		m_elseIfs = elseIfs;
		m_elseBlock = elseBlock;
	}

	@Override
	public void evaluate(CodeBuilder builder) {
		builder.addLine("if " + m_condition.evaluate(builder) + " then");
		builder.indent();
		m_block.evaluate(builder);
		builder.outdent();
		for (int i = 0; i < m_elseIfs.size(); i++) {
			ElseIf elseIf = m_elseIfs.get(i);
			builder.addLine("elseif " + elseIf.m_condition.evaluate(builder) + " then");
			builder.indent();
			elseIf.m_block.evaluate(builder);
			builder.outdent();
		}
		if (m_elseBlock != null) {
			builder.addLine("else");
			builder.indent();
			m_elseBlock.evaluate(builder);
			builder.outdent();
		}
		builder.addLine("end");
	}

	@Override
	public void getUsedVariables(Set<String> variables) {
		m_condition.getUsedVariables(variables);
		m_block.getUsedVariables(variables);
		for (int i = 0; i < m_elseIfs.size(); i++) {
			ElseIf elseIf = m_elseIfs.get(i);
			elseIf.m_condition.getUsedVariables(variables);
			elseIf.m_block.getUsedVariables(variables);
		}
		if (m_elseBlock != null) {
			m_elseBlock.getUsedVariables(variables);
		}
	}

	@Override
	public boolean isMultiLine() {
		return true;
	}

	@Override
	public void verify(List<SyntaxError> errors, List<SyntaxSuggestion> suggestions) {
		m_condition.verify(errors, suggestions);

		ExpressionType conditionType = m_condition.getType();
		if ((conditionType != ExpressionType.Error) && (conditionType != ExpressionType.Boolean)
				&& (conditionType != ExpressionType.Unknown)) {
			errors.add(new SyntaxError(m_condition.getRange(), "gui.dynrobot:syntax_error.expected",
					IconCategory.getFromType(ExpressionType.Boolean)));
		}
		m_block.verify(errors, suggestions);
		for (int i = 0; i < m_elseIfs.size(); i++) {
			ElseIf elseIf = m_elseIfs.get(i);
			elseIf.m_condition.verify(errors, suggestions);

			ExpressionType elseIfConditionType = elseIf.m_condition.getType();
			if ((elseIfConditionType != ExpressionType.Error) && (elseIfConditionType != ExpressionType.Boolean)
					&& (elseIfConditionType != ExpressionType.Unknown)) {
				errors.add(new SyntaxError(elseIf.m_condition.getRange(), "gui.dynrobot:syntax_error.expected",
						IconCategory.getFromType(ExpressionType.Boolean)));
			}
			elseIf.m_block.verify(errors, suggestions);
		}
		if (m_elseBlock != null) {
			m_elseBlock.verify(errors, suggestions);
		}
	}
}
