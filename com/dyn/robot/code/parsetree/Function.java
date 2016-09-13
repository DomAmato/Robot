package com.dyn.robot.code.parsetree;

import com.dyn.robot.code.Range;

public class Function extends Node {
	private final String m_signature;
	private final ExpressionType m_returnType;
	private final ExpressionType m_argumentType;
	private final boolean m_assert;

	public Function(Range range, String signature, ExpressionType returnType, boolean _assert) {
		this(range, signature, returnType, ExpressionType.Nil, _assert);
	}

	public Function(Range range, String signature, ExpressionType returnType, ExpressionType argumentType,
			boolean _assert) {
		super(range);
		m_signature = signature;
		m_returnType = returnType;
		m_argumentType = argumentType;
		m_assert = _assert;
	}

	public ExpressionType getArgumentType() {
		return m_argumentType;
	}

	public boolean getAssert() {
		return m_assert;
	}

	public ExpressionType getReturnType() {
		return m_returnType;
	}

	public String getSignature() {
		return m_signature;
	}

	public boolean hasSideEffects() {
		return true;
	}
}
