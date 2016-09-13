package com.dyn.robot.code.grammar;

import java.io.Serializable;

public class Token implements Serializable {
	private static final long serialVersionUID = 1L;

	public static Token newToken(int ofKind) {
		return newToken(ofKind, null);
	}

	public static Token newToken(int ofKind, String image) {
		switch (ofKind) {
		}
		return new Token(ofKind, image);
	}

	public int kind;
	public int beginLine;
	public int beginColumn;
	public int endLine;
	public int endColumn;
	public String image;

	public Token next;

	public Token specialToken;

	public Token() {
	}

	public Token(int kind) {
		this(kind, null);
	}

	public Token(int kind, String image) {
		this.kind = kind;
		this.image = image;
	}

	public Object getValue() {
		return null;
	}

	@Override
	public String toString() {
		return image;
	}
}
