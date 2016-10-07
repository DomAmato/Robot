package com.dyn.robot.code.parsetree;

import java.util.EnumSet;
import java.util.Set;

import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.IconCategory;
import com.dyn.robot.code.Range;

public abstract class Statement extends Node {
	public Statement(Range range) {
		super(range);
	}

	public abstract void evaluate(CodeBuilder paramCodeBuilder);

	public void getUsedVariables(Set<String> variables) {
	}

	public EnumSet<IconCategory> getValidFollowOnCategories() {
		return EnumSet.noneOf(IconCategory.class);
	}

	public boolean isMultiLine() {
		return false;
	}
}
