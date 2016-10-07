package com.dyn.robot.code;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class CodeBuilder {
	public static class AnnotationOptions {
		public static AnnotationOptions None = new AnnotationOptions(false, false, false, false);
		public static AnnotationOptions Full = new AnnotationOptions(true, true, true, true);
		public static AnnotationOptions SubProgram = new AnnotationOptions(false, false, false, true);
		private boolean m_annotateSlots;
		private boolean m_annotateLines;
		private boolean m_annotateVariables;
		private boolean m_addAssertions;

		private AnnotationOptions(boolean annotateSlots, boolean annotateLines, boolean annotateVariables,
				boolean addAssertions) {
			m_annotateSlots = annotateSlots;
			m_annotateLines = annotateLines;
			m_annotateVariables = annotateVariables;
			m_addAssertions = addAssertions;
		}

		@Override
		public boolean equals(Object o) {
			return o == this;
		}

		public boolean getAddAssertions() {
			return m_addAssertions;
		}

		public boolean getAnnotateLines() {
			return m_annotateLines;
		}

		public boolean getAnnotateSlots() {
			return m_annotateSlots;
		}

		public boolean getAnnotateVariables() {
			return m_annotateVariables;
		}
	}

	private final List<String> m_code;
	private int m_indentation;

	private AnnotationOptions m_annotationOptions;

	public CodeBuilder() {
		m_code = new ArrayList();
		m_indentation = 0;
		m_annotationOptions = AnnotationOptions.None;
	}

	public void addLine(String line) {
		m_code.add(StringUtils.repeat(' ', m_indentation * 2)
				+ line.replaceAll("%line%", Integer.toString(getLineNumber())));
	}

	public AnnotationOptions getAnnotationOptions() {
		return m_annotationOptions;
	}

	public List<String> getCode() {
		return m_code;
	}

	public int getLineNumber() {
		return m_code.size() + 1;
	}

	public void indent() {
		m_indentation += 1;
	}

	public void outdent() {
		m_indentation -= 1;
	}

	public void setAnnotationOptions(AnnotationOptions options) {
		m_annotationOptions = options;
	}
}
