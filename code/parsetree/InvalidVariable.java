package com.dyn.robot.code.parsetree;

import com.dyn.robot.code.Range;

public class InvalidVariable extends Variable {
	public InvalidVariable(Range range) {
		super(range, "???");
	}
}
