package com.dyn.robot.code;

import java.util.EnumSet;

import net.minecraft.util.StatCollector;

public class SyntaxSuggestion {
	private Range m_range;
	private EnumSet<IconCategory> m_suggestions;
	private boolean m_required;
	private String m_mouseMessage;
	private String m_keyboardMessage;
	private String m_disabledMessage;

	public SyntaxSuggestion(Range range, boolean required, EnumSet<IconCategory> suggestions) {
		m_range = range;
		m_suggestions = suggestions;
		m_required = required;
	}

	public SyntaxSuggestion(Range range, boolean required, IconCategory suggestion) {
		this(range, required, EnumSet.of(suggestion));
	}

	public SyntaxSuggestion(Range range, boolean required, IconCategory suggestion, IconCategory... moreSuggestions) {
		this(range, required, EnumSet.of(suggestion, moreSuggestions));
	}

	private String constructMessage(boolean editable, boolean keyboard) {
		if (editable) {
			if (keyboard) {
				if (m_required) {
					return StatCollector.translateToLocalFormatted("gui.dynrobot:syntax_suggestion.required.keyboard",
							new Object[] { IconCategory.getLocalisedList(m_suggestions) });
				}
				return StatCollector.translateToLocalFormatted("gui.dynrobot:syntax_suggestion.optional.keyboard",
						new Object[] { IconCategory.getLocalisedList(m_suggestions) });
			}
			if (m_required) {
				return StatCollector.translateToLocalFormatted("gui.dynrobot:syntax_suggestion.required",
						new Object[] { IconCategory.getLocalisedList(m_suggestions) });
			}
			return StatCollector.translateToLocalFormatted("gui.dynrobot:syntax_suggestion.optional",
					new Object[] { IconCategory.getLocalisedList(m_suggestions) });
		}
		if (m_required) {
			return StatCollector.translateToLocalFormatted("gui.dynrobot:syntax_suggestion.optional.locked",
					new Object[] { IconCategory.getLocalisedList(m_suggestions) });
		}
		return StatCollector.translateToLocalFormatted("gui.dynrobot:syntax_suggestion.optional.locked",
				new Object[] { IconCategory.getLocalisedList(m_suggestions) });
	}

	public String getMessage(boolean editable, boolean keyboard) {
		if (editable) {
			if (keyboard) {
				if (m_keyboardMessage == null) {
					m_keyboardMessage = constructMessage(editable, keyboard);
				}
				return m_keyboardMessage;
			}
			if (m_mouseMessage == null) {
				m_mouseMessage = constructMessage(editable, keyboard);
			}
			return m_mouseMessage;
		}
		if (m_disabledMessage == null) {
			m_disabledMessage = constructMessage(editable, keyboard);
		}
		return m_disabledMessage;
	}

	public Range getRange() {
		return m_range;
	}

	public EnumSet<IconCategory> getSuggestions() {
		return m_suggestions;
	}

	public boolean isRequired() {
		return m_required;
	}
}
