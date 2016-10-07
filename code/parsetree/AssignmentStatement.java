package com.dyn.robot.code.parsetree;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.IconCategory;
import com.dyn.robot.code.Range;
import com.dyn.robot.code.SyntaxError;
import com.dyn.robot.code.SyntaxSuggestion;

public class AssignmentStatement extends Statement {
	private final int m_executeLine;
	private final Variable m_variable;
	private final Expression m_right;

	public AssignmentStatement(Range range, int executeLine, Variable variable, Expression right) {
		super(range);
		m_executeLine = executeLine;
		m_variable = variable;
		m_right = right;
	}

	@Override
	public void evaluate(CodeBuilder builder) {
		CodeBuilder.AnnotationOptions options = builder.getAnnotationOptions();
		if ((options.getAnnotateLines()) || (options.getAnnotateSlots()) || (options.getAnnotateVariables())) {
			String line = "(function(...)";
			if (options.getAnnotateSlots()) {
				line = line + "edu.setCurrentSlot(" + (m_executeLine + 1) + ");";
			}
			if (options.getAnnotateLines()) {
				line = line + "debug.step(%line%);";
			}
			line = line + m_variable.getName() + "=...;";
			if (options.getAnnotateVariables()) {
				line = line + "edu.setVariable(\"" + m_variable.getName() + "\",tostring(" + m_variable.getName()
						+ "))";
			}
			line = line + "end)(" + m_right.evaluate(builder) + ");";
			builder.addLine(line);
		} else {
			String line = m_variable.getName() + " = " + m_right.evaluate(builder);
			builder.addLine(line);
		}
	}

	@Override
	public void getUsedVariables(Set<String> variables) {
		variables.add(m_variable.getName());
		m_right.getUsedVariables(variables);
	}

	@Override
	public EnumSet<IconCategory> getValidFollowOnCategories() {
		return EnumSet.of(IconCategory.getOperatorFromType(m_right.getType()));
	}

	@Override
	public void verify(List<SyntaxError> errors, List<SyntaxSuggestion> suggestions) {
		m_right.verify(errors, suggestions);
	}
}
