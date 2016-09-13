package com.dyn.robot.code.parsetree;

import com.dyn.robot.code.Range;

public class PureFunction extends Function {
	public PureFunction(Range range, String signature, ExpressionType returnType, boolean _assert) {
		super(range, signature, returnType, _assert);
	}

	public PureFunction(Range range, String signature, ExpressionType returnType, ExpressionType argumentType,
			boolean _assert) {
		super(range, signature, returnType, argumentType, _assert);
	}

	@Override
	public boolean hasSideEffects() {
		return false;
	}
}
