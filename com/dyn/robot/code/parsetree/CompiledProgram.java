package com.dyn.robot.code.parsetree;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.SyntaxError;
import com.dyn.robot.code.SyntaxSuggestion;

public class CompiledProgram {
	private final String m_title;
	private final Block m_root;

	public CompiledProgram(String title, Block root) {
		m_title = title;
		m_root = root;
	}

	public void evaluate(CodeBuilder builder) {
		Set<String> variablesUsed = new TreeSet();
		m_root.getUsedVariables(variablesUsed);
		if (variablesUsed.size() > 0) {
			String line = "local ";
			int i = 0;
			for (String variable : variablesUsed) {
				line = line + variable;
				if (i < (variablesUsed.size() - 1)) {
					line = line + ", ";
				}
				i++;
			}
			builder.addLine(line);
		}
		m_root.evaluate(builder);
	}

	public String getTitle() {
		return m_title;
	}

	public void verify(List<SyntaxError> errors, List<SyntaxSuggestion> suggestions) {
		m_root.verify(errors, suggestions);
	}
}
