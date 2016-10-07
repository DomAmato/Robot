package com.dyn.robot.code.parsetree;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.IconCategory;
import com.dyn.robot.code.Range;
import com.dyn.robot.code.SyntaxError;
import com.dyn.robot.code.SyntaxSuggestion;

public class FunctionCallExpression extends Expression {
	private final Function m_function;
	private final Expression m_argument;
	private final int m_count;

	public FunctionCallExpression(Range range, Function function, Expression argument, int count) {
		super(range);
		m_function = function;
		m_argument = argument;
		m_count = count;
	}

	public FunctionCallExpression(Range range, Function function, int count) {
		this(range, function, null, count);
	}

	@Override
	public String evaluate(CodeBuilder builder) {
		String call = m_function.getSignature();
		if (m_argument != null) {
			call = call.replaceAll("%arg%", m_argument.evaluate(builder));
		}
		CodeBuilder.AnnotationOptions options = builder.getAnnotationOptions();
		if ((options.getAnnotateLines()) || (options.getAnnotateSlots())) {
			String line = "(function()";
			if (options.getAnnotateSlots()) {
				line = line + "edu.setCurrentSlot(" + (getRange().getStartLine() + 1) + ");";
			}
			if (options.getAnnotateLines()) {
				line = line + "debug.step(%line%);";
			}
			line = line + "return " + call + " end)()";
			return line;
		}
		return call;
	}

	public Expression getArgument() {
		return m_argument;
	}

	public int getCount() {
		return m_count;
	}

	public Function getFunction() {
		return m_function;
	}

	@Override
	public ExpressionType getType() {
		return m_function.getReturnType();
	}

	@Override
	public void getUsedVariables(Set<String> variables) {
		if (m_argument != null) {
			m_argument.getUsedVariables(variables);
		}
	}

	@Override
	public boolean hasSideEffects() {
		return m_function.hasSideEffects();
	}

	@Override
	public void verify(List<SyntaxError> errors, List<SyntaxSuggestion> suggestions) {
		if (m_argument != null) {
			m_argument.verify(errors, suggestions);
		}
		ExpressionType requiredArgumentType = m_function.getArgumentType();
		if (requiredArgumentType != ExpressionType.Nil) {
			if (m_argument != null) {
				ExpressionType providedArgumentType = m_argument.getType();
				if (requiredArgumentType != ExpressionType.Unknown) {
					if ((providedArgumentType != ExpressionType.Error) && (providedArgumentType != requiredArgumentType)
							&& (providedArgumentType != ExpressionType.Unknown)) {
						errors.add(new SyntaxError(m_argument.getRange(), "gui.dynrobot:syntax_error.expected",
								IconCategory.getFromType(requiredArgumentType)));
					}
				}
			} else {
				errors.add(new SyntaxError(getRange(), "gui.dynrobot:syntax_error.expected",
						IconCategory.getFromType(requiredArgumentType)));
			}
		} else if (m_argument != null) {
			errors.add(new SyntaxError(m_argument.getRange(), "gui.dynrobot:syntax_error.expected",
					EnumSet.noneOf(IconCategory.class)));
		}
	}
}
