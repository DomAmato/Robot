package com.dyn.robot.code;

import java.util.EnumSet;

import net.minecraft.util.StatCollector;

public class SyntaxError {
	private Range m_range;
	private String m_message;

	public SyntaxError(Range range, String unlocalisedMessage) {
		this(range, unlocalisedMessage, (EnumSet) null);
	}

	public SyntaxError(Range range, String unlocalisedMessage, EnumSet<IconCategory> suggestions) {
		m_range = range;
		m_message = constructMessage(unlocalisedMessage, suggestions);
	}

	public SyntaxError(Range range, String unlocalisedMessage, IconCategory suggestion) {
		this(range, unlocalisedMessage, EnumSet.of(suggestion));
	}

	public SyntaxError(Range range, String unlocalisedMessage, IconCategory suggestion,
			IconCategory... moreSuggestions) {
		this(range, unlocalisedMessage, EnumSet.of(suggestion, moreSuggestions));
	}

	private String constructMessage(String unlocalisedMessage, EnumSet<IconCategory> suggestions) {
		if (suggestions != null) {
			return StatCollector.translateToLocalFormatted(unlocalisedMessage,
					new Object[] { IconCategory.getLocalisedList(suggestions) });
		}
		return StatCollector.translateToLocal(unlocalisedMessage);
	}

	public String getMessage() {
		return m_message;
	}

	public Range getRange() {
		return m_range;
	}
}
