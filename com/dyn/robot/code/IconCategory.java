package com.dyn.robot.code;

import java.util.EnumSet;

import com.dyn.robot.code.parsetree.ExpressionType;
import com.google.common.base.CaseFormat;

import net.minecraft.util.StatCollector;

public enum IconCategory {
	Variable, Statement, SingleLineStatement, MultiLineStatement, Expression, BooleanExpression, NumericExpression, StringExpression, Operator, BooleanOperator, NumericOperator, StringOperator, If, Then, ElseIf, Else, End, While, Do, For, Repeat, Break, To, Assign;

	public static IconCategory getFromType(ExpressionType type) {
		switch (type) {
		case Boolean:
			return BooleanExpression;
		case Number:
			return NumericExpression;
		case String:
			return StringExpression;
		}
		return Expression;
	}

	public static String getLocalisedList(EnumSet<IconCategory> categories) {
		StringBuilder list = new StringBuilder();
		int numLeft = categories.size();
		String comma = StatCollector.translateToLocal("gui.dynrobot:list_seperator") + " ";
		String or = StatCollector.translateToLocal("gui.dynrobot:final_list_seperator") + " ";
		for (IconCategory category : categories) {
			list.append(StatCollector.translateToLocal(category.getUnlocalisedName()));
			numLeft--;
			if (numLeft > 1) {
				list.append(comma);
			} else if (numLeft == 1) {
				list.append(or);
			}
		}
		return list.toString();
	}

	public static IconCategory getOperatorFromType(ExpressionType type) {
		switch (type) {
		case Number:
			return NumericOperator;
		case Boolean:
			return BooleanOperator;
		case String:
			return StringOperator;
		}
		return Operator;
	}

	private String m_unlocalisedName;

	private IconCategory() {
		m_unlocalisedName = ("gui.dynrobot:icon_category."
				+ CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, toString()));
	}

	public String getUnlocalisedName() {
		return m_unlocalisedName;
	}
}
