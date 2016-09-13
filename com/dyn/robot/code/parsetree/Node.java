package com.dyn.robot.code.parsetree;

import java.util.List;

import com.dyn.robot.code.Range;
import com.dyn.robot.code.SyntaxError;
import com.dyn.robot.code.SyntaxSuggestion;

public abstract class Node {
	private final Range m_range;

	public Node(Range range) {
		m_range = range;
	}

	public Range getRange() {
		return m_range;
	}

	public void verify(List<SyntaxError> errors, List<SyntaxSuggestion> suggestions) {
	}
}
