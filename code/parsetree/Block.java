package com.dyn.robot.code.parsetree;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.dyn.robot.code.CodeBuilder;
import com.dyn.robot.code.IconCategory;
import com.dyn.robot.code.Range;
import com.dyn.robot.code.SyntaxError;
import com.dyn.robot.code.SyntaxSuggestion;

public class Block extends Node {
	private final List<Statement> m_statements;
	private final EnumSet<IconCategory> m_validEndCategories;
	private final int m_parentIndent;
	private final boolean m_allowBreak;

	public Block(Range range, List<Statement> statements, int parentIndent, boolean allowBreak,
			IconCategory... validEndCategories) {
		super(range);
		m_statements = statements;
		if (validEndCategories.length > 0) {
			m_validEndCategories = EnumSet.of(validEndCategories[0], validEndCategories);
		} else {
			m_validEndCategories = EnumSet.noneOf(IconCategory.class);
		}
		m_allowBreak = allowBreak;
		m_parentIndent = parentIndent;
	}

	private int addLineWithIndent(int position, int indent) {
		int x = Math.min(indent, 11);
		int y = position >= 0 ? (position / 12) + 1 : 0;
		return x + (y * 12);
	}

	public void evaluate(CodeBuilder builder) {
		for (int i = 0; i < m_statements.size(); i++) {
			Statement statement = m_statements.get(i);
			statement.evaluate(builder);
		}
	}

	public int getParentIndent() {
		return m_parentIndent;
	}

	public void getUsedVariables(Set<String> variables) {
		for (int i = 0; i < m_statements.size(); i++) {
			Statement statement = m_statements.get(i);
			statement.getUsedVariables(variables);
		}
	}

	private EnumSet<IconCategory> union(EnumSet<IconCategory> a, IconCategory b) {
		EnumSet<IconCategory> result = a.clone();
		result.add(b);
		return result;
	}

	@Override
	public void verify(List<SyntaxError> errors, List<SyntaxSuggestion> suggestions) {
		Range blockRange = getRange();
		int limit;
		if (m_statements.size() > 0) {
			Statement firstStatement = m_statements.get(0);
			limit = firstStatement.getRange().getStartLine();
		} else {
			limit = blockRange.getEndLine();
		}
		int firstLine = addLineWithIndent(blockRange.getStartLine() - 1, m_parentIndent + 1);
		if (firstLine < limit) {
			EnumSet<IconCategory> categories = union(m_validEndCategories, IconCategory.Statement);
			if ((m_allowBreak) && (m_statements.size() == 0)) {
				categories.add(IconCategory.Break);
			}
			suggestions.add(new SyntaxSuggestion(new Range(firstLine, 1), false, categories));
		}
		boolean breakFound = false;
		for (int i = 0; i < m_statements.size(); i++) {
			Statement statement = m_statements.get(i);
			statement.verify(errors, suggestions);
			if (breakFound) {
				errors.add(new SyntaxError(statement.getRange(), "gui.dynrobot:syntax_error.expected",
						m_validEndCategories));
			} else if ((statement instanceof BreakStatement)) {
				breakFound = true;
				if (!m_allowBreak) {
					errors.add(new SyntaxError(statement.getRange(), "gui.dynrobot:syntax_error.expected",
							union(m_validEndCategories, IconCategory.Statement)));
				}
			}
			Range statementRange = statement.getRange();
			if (i < (m_statements.size() - 1)) {
				Statement nextStatement = m_statements.get(i + 1);
				limit = nextStatement.getRange().getStartLine();
			} else {
				limit = blockRange.getEndLine();
			}
			int nextSpace = statementRange.getEndLine();
			int nextLine = addLineWithIndent(statementRange.getEndLine() - 1, m_parentIndent + 1);
			if ((!statement.isMultiLine()) && (nextSpace < limit)) {
				EnumSet<IconCategory> categories = m_validEndCategories.clone();
				if (!breakFound) {
					categories.addAll(statement.getValidFollowOnCategories());
					categories.add(IconCategory.SingleLineStatement);
					if ((m_allowBreak) && (i == (m_statements.size() - 1))) {
						categories.add(IconCategory.Break);
					}
				}
				if (categories.size() > 0) {
					suggestions.add(new SyntaxSuggestion(new Range(nextSpace, 1), false, categories));
				}
			}
			if (((statement.isMultiLine()) || (nextLine > nextSpace)) && (nextLine < limit)) {
				EnumSet<IconCategory> categories = m_validEndCategories.clone();
				if (!breakFound) {
					categories.add(IconCategory.Statement);
					if (nextLine == nextSpace) {
						categories.addAll(statement.getValidFollowOnCategories());
					}
					if ((m_allowBreak) && (i == (m_statements.size() - 1))) {
						categories.add(IconCategory.Break);
					}
				}
				if (categories.size() > 0) {
					suggestions.add(new SyntaxSuggestion(new Range(nextLine, 1), false, categories));
				}
			}
		}
	}
}
