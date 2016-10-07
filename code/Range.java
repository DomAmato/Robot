package com.dyn.robot.code;

public class Range {
	private int m_startLine;
	private int m_size;

	public Range(int firstLine, int size) {
		m_startLine = firstLine;
		m_size = size;
	}

	public Range(Range firstRange, Range secondRange) {
		this(firstRange.getStartLine(), firstRange.getSize());
		expandToFit(secondRange);
	}

	@Override
	public boolean equals(Object other) {
		if ((other instanceof Range)) {
			return equals((Range) other);
		}
		return false;
	}

	public boolean equals(Range other) {
		if (other == this) {
			return true;
		}
		if (other != null) {
			return (other.getStartLine() == getStartLine()) && (other.getSize() == getSize());
		}
		return false;
	}

	public void expandToFit(Range other) {
		int startLine = getStartLine();
		int endLine = getEndLine();
		m_startLine = Math.min(startLine, other.getStartLine());
		m_size = (Math.max(endLine, other.getEndLine()) - m_startLine);
	}

	public void expandToTouch(Range other) {
		m_size = (Math.max(getEndLine(), other.getStartLine()) - m_startLine);
	}

	public int getEndLine() {
		return m_startLine + m_size;
	}

	public int getSize() {
		return m_size;
	}

	public int getStartLine() {
		return m_startLine;
	}
}
