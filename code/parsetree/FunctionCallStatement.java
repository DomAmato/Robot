package com.dyn.robot.code.parsetree;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.IconCategory;
import com.dyn.robot.code.Range;
import com.dyn.robot.code.SyntaxError;
import com.dyn.robot.code.SyntaxSuggestion;

public class FunctionCallStatement extends Statement {
	private final FunctionCallExpression m_functionCall;

	public FunctionCallStatement(Range range, FunctionCallExpression functionCall) {
		super(range);
		m_functionCall = functionCall;
	}

	@Override
	public void evaluate(CodeBuilder builder) {
		String line = m_functionCall.evaluate(builder);
		CodeBuilder.AnnotationOptions options = builder.getAnnotationOptions();
		if (options.getAddAssertions()) {
			if (m_functionCall.getFunction().getAssert()) {
				line = "assert( " + line + " )";
			}
		}
		if (!options.equals(CodeBuilder.AnnotationOptions.None)) {
			line = line + ";";
		}
		int repetitions = m_functionCall.getCount();
		if (repetitions > 1) {
			builder.addLine("for n = 1, " + Integer.toString(repetitions) + " do");
			builder.indent();
			builder.addLine(line);
			builder.outdent();
			builder.addLine("end");
		} else {
			builder.addLine(line);
		}
	}

	@Override
	public void getUsedVariables(Set<String> variables) {
		m_functionCall.getUsedVariables(variables);
	}

	@Override
	public EnumSet<IconCategory> getValidFollowOnCategories() {
		Expression providedArgument = m_functionCall.getArgument();
		if (providedArgument != null) {
			return EnumSet.of(IconCategory.getOperatorFromType(providedArgument.getType()));
		}
		return EnumSet.noneOf(IconCategory.class);
	}

	@Override
	public void verify(List<SyntaxError> errors, List<SyntaxSuggestion> suggestions) {
		m_functionCall.verify(errors, suggestions);
		if (!m_functionCall.hasSideEffects()) {
			errors.add(new SyntaxError(m_functionCall.getRange(), "gui.dynrobot:syntax_error.expected",
					IconCategory.Statement));
		}
	}
}
