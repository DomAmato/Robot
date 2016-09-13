package com.dyn.robot.code.parsetree;

import com.dyn.robot.code.Range;

public class Variable extends Node {
	private final String m_name;

	public Variable(Range range, String name) {
		super(range);
		m_name = name;
	}

	public String getName() {
		return m_name;
	}
}
