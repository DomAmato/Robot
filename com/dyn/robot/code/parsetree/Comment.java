package com.dyn.robot.code.parsetree;

import com.dyn.robot.code.Range;

public class Comment extends Node {
	private final String m_text;

	public Comment(Range range, String text) {
		super(range);
		m_text = text;
	}

	public String getText() {
		return m_text;
	}
}
