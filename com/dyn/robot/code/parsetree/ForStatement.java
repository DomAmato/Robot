package com.dyn.robot.code.parsetree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.IconCategory;
import com.dyn.robot.code.Range;
import com.dyn.robot.code.SyntaxError;
import com.dyn.robot.code.SyntaxSuggestion;

public class ForStatement extends Statement {
	private final int m_executeLine;
	private final Variable m_variable;
	private final Expression m_start;
	private final Expression m_end;
	private final Block m_block;

	public ForStatement(Range range, int executeLine, Variable variable, Expression start, Expression end,
			Block block) {
		super(range);
		m_executeLine = executeLine;
		m_variable = variable;
		m_start = start;
		m_end = end;
		m_block = block;
	}

	@Override
	public void evaluate(CodeBuilder builder) {
		CodeBuilder.AnnotationOptions options = builder.getAnnotationOptions();
		if ((options.getAnnotateLines()) || (options.getAnnotateSlots())) {
			String line = "do local _old = " + m_variable.getName() + ";";
			if (options.getAnnotateSlots()) {
				line = line + "edu.setCurrentSlot(" + (m_start.getRange().getStartLine() + 1) + ");";
			}
			line = line + "local _start = " + m_start.evaluate(builder) + ";" + "if type(_start) ~= \"number\" then "
					+ "error( \"'for' limit must be a number\" ) " + "end;";
			if (options.getAnnotateSlots()) {
				line = line + "edu.setCurrentSlot(" + (m_end.getRange().getStartLine() + 1) + ");";
			}
			line = line + "local _end = " + m_end.evaluate(builder) + ";" + "if type(_end) ~= \"number\" then "
					+ "error( \"'for' limit must be a number\" ) " + "end;" + "local _ok, _err = pcall(function() "
					+ "local _current = _start;" + "while _current <= _end do ";
			if (options.getAnnotateSlots()) {
				line = line + "edu.setCurrentSlot(" + (m_executeLine + 1) + ");";
			}
			if (options.getAnnotateLines()) {
				line = line + "debug.step(%line%);";
			}
			line = line + "local " + m_variable.getName() + " = _current;";
			if (options.getAnnotateVariables()) {
				line = line + "edu.setVariable(\"" + m_variable.getName() + "\",tostring(" + m_variable.getName()
						+ "));";
			}
			builder.addLine(line);
		} else {
			String line = "for " + m_variable.getName() + " = " + m_start.evaluate(builder) + ", "
					+ m_end.evaluate(builder) + " do";
			builder.addLine(line);
		}
		builder.indent();
		m_block.evaluate(builder);
		builder.outdent();
		if ((options.getAnnotateLines()) || (options.getAnnotateSlots()) || (options.getAnnotateVariables())) {
			String line = "_current = _current + 1;end end);if not _ok then error(_err,0);end;";
			if (options.getAnnotateVariables()) {
				line = line + "if _old ~= nil then edu.setVariable(\"" + m_variable.getName() + "\", tostring(_old));"
						+ "else " + "edu.clearVariable(\"" + m_variable.getName() + "\");" + "end;";
			}
			line = line + "end";

			builder.addLine(line);
		} else {
			String line = "end";
			builder.addLine(line);
		}
	}

	@Override
	public void getUsedVariables(Set<String> variables) {
		m_start.getUsedVariables(variables);
		m_end.getUsedVariables(variables);

		Set<String> subVariables = new HashSet();
		m_block.getUsedVariables(subVariables);
		subVariables.remove(m_variable.getName());
		variables.addAll(subVariables);
	}

	@Override
	public boolean isMultiLine() {
		return true;
	}

	@Override
	public void verify(List<SyntaxError> errors, List<SyntaxSuggestion> suggestions) {
		m_start.verify(errors, suggestions);

		ExpressionType startType = m_start.getType();
		if ((startType != ExpressionType.Error) && (startType != ExpressionType.Number)
				&& (startType != ExpressionType.Unknown)) {
			errors.add(new SyntaxError(m_start.getRange(), "gui.dynrobot:syntax_error.expected",
					IconCategory.getFromType(ExpressionType.Number)));
		}
		m_end.verify(errors, suggestions);

		ExpressionType endType = m_end.getType();
		if ((endType != ExpressionType.Error) && (endType != ExpressionType.Number)
				&& (endType != ExpressionType.Unknown)) {
			errors.add(new SyntaxError(m_end.getRange(), "gui.dynrobot:syntax_error.expected",
					IconCategory.getFromType(ExpressionType.Number)));
		}
		m_block.verify(errors, suggestions);
	}
}
