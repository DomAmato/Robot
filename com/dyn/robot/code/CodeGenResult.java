package com.dyn.robot.code;

import java.util.List;

import com.dyn.robot.programs.Program;

public class CodeGenResult {
	private boolean m_success;
	private Program m_program;
	private List<SyntaxError> m_errors;
	private List<SyntaxSuggestion> m_suggestions;

	public CodeGenResult(boolean success, Program program, List<SyntaxError> errors,
			List<SyntaxSuggestion> suggestions) {
		m_success = success;
		m_program = program;
		m_errors = errors;
		m_suggestions = suggestions;
	}

	public List<SyntaxError> getErrors() {
		return m_errors;
	}

	public Program getProgram() {
		return m_program;
	}

	public boolean getSuccess() {
		return m_success;
	}

	public List<SyntaxSuggestion> getSuggestions() {
		return m_suggestions;
	}
}
