package com.dyn.robot.code.parsetree;

import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.Range;

public class CommentStatement extends Statement {
	private final Comment m_comment;

	public CommentStatement(Range range, Comment comment) {
		super(range);
		m_comment = comment;
	}

	@Override
	public void evaluate(CodeBuilder builder) {
		builder.addLine("-- " + m_comment.getText());
	}
}
