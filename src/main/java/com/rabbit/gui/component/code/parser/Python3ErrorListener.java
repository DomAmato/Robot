package com.rabbit.gui.component.code.parser;

// Generated from Python3.g4 by ANTLR 4.5.3

import org.antlr.v4.runtime.tree.ErrorNode;

/**
 * This class provides an empty implementation of {@link Python3Listener}, which
 * can be extended to create a listener which only needs to handle a subset of
 * the available methods.
 */
public class Python3ErrorListener extends Python3BaseListener {
	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 */
	@Override
	public void visitErrorNode(ErrorNode node) {
		System.out.println("Got Error: " + node.getText());
	}
}