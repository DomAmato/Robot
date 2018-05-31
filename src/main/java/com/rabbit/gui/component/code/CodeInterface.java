package com.rabbit.gui.component.code;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rabbit.gui.RabbitGui;
import com.rabbit.gui.component.GuiWidget;
import com.rabbit.gui.component.code.parser.AntlrAutoCompletionSuggester;
import com.rabbit.gui.component.code.parser.AntlrAutoCompletionSuggester.EditorContext;
import com.rabbit.gui.component.code.parser.AntlrAutoCompletionSuggester.TokenType;
import com.rabbit.gui.component.code.parser.CollectorTokenSource;
import com.rabbit.gui.component.code.parser.DescriptiveErrorListener;
import com.rabbit.gui.component.code.parser.Python3Lexer;
import com.rabbit.gui.component.code.parser.Python3Parser;
import com.rabbit.gui.component.control.MultiTextbox;
import com.rabbit.gui.component.control.TextBox;
import com.rabbit.gui.component.display.Shape;
import com.rabbit.gui.component.display.ShapeType;
import com.rabbit.gui.render.Renderer;
import com.rabbit.gui.render.TextRenderer;
import com.rabbit.gui.utils.UtilityFunctions;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CodeInterface extends MultiTextbox {

	protected int maxStringLength = Integer.MAX_VALUE;
	protected Shape errorBox;
	private int errLine = -1;
	private String errCode;
	private boolean hasError = false;
	private String formattedText = "";

	protected boolean drawHoverText = true;
	protected List<String> originalHoverText = new ArrayList<>();

	protected List<String> hoverText = new ArrayList<>();

	protected boolean drawToLeft;

	private int suggestionCooldown = 50;

	// the variable name and its respective class
	private Map<String, String> predefinedUserVariables = Maps.newHashMap();
	private Map<String, String> userVariables = Maps.newHashMap();
	private Map<String, List<String>> classMembers = Maps.newHashMap();

	private final String SYMBOL = TextFormatting.WHITE.toString();
	private final String NUMBER = TextFormatting.BLUE.toString();
	private final String STRING = TextFormatting.YELLOW.toString();
	private final String VARIABLE = TextFormatting.AQUA.toString();
	private final String FUNCTION = TextFormatting.GOLD.toString();
	private final String MEMBER_VAR = TextFormatting.LIGHT_PURPLE.toString();
	private final String MEMBER_FUNCTION = TextFormatting.GREEN.toString();
	private final String SYNTAX = TextFormatting.RED.toString();
	private final String COMMENT = TextFormatting.DARK_GRAY.toString();
	private final String RESET = TextFormatting.RESET.toString();

	public CodeInterface(int xPos, int yPos, int width, int height) {
		super(xPos, yPos, width, height);
		errorBox = new Shape(0, 0, getWidth(), 0, ShapeType.RECT, Color.red);
	}

	public CodeInterface(int xPos, int yPos, int width, int height, String initialText) {
		super(xPos, yPos, width, height, initialText);
		formatText();
		errorBox = new Shape(0, 0, getWidth(), 0, ShapeType.RECT, Color.red);
	}

	public void addClassMembers(String clazz, List<String> mappings) {
		if (classMembers.containsKey(clazz)) {
			classMembers.replace(clazz, mappings);
		} else {
			classMembers.put(clazz, mappings);
		}
	}

	public CodeInterface addHoverText(String text) {
		originalHoverText.add(text);
		return this;
	}

	public void addPredefinedUserVariables(String variableName, String clazz) {
		if (predefinedUserVariables.containsKey(clazz)) {
			predefinedUserVariables.replace(variableName, clazz);
		} else {
			predefinedUserVariables.put(variableName, clazz);
		}
	}

	public void clearError() {
		errLine = -1;
		errCode = "";
		errorBox.setHeight(0);
		hasError = false;
	}

	public CodeInterface doesDrawHoverText(boolean state) {
		drawHoverText = state;
		return this;
	}

	@Override
	protected void drawBox() {
		if (isVisible()) {
			GlStateManager.pushMatrix();
			{
				if (isBackgroundVisible()) {
					drawTextBoxBackground();
				}
				TextRenderer.getFontRenderer().setUnicodeFlag(drawUnicode);
				int color = isEnabled ? getEnabledColor() : getDisabledColor();
				boolean renderCursor = isFocused() && (((cursorCounter / 6) % 2) == 0);
				int startLine = getStartLineY();
				int maxLineAmount = (height / TextRenderer.getFontRenderer().FONT_HEIGHT) + startLine;
				List<String> lines = getFormattedLines();
				int lineCount = 0;
				int maxWidth = scrollBar.isVisible() ? width - 14 : width - 4;
				for (String wholeLine : lines) {
					String line = "";
					if (TextRenderer.getFontRenderer().getStringWidth(wholeLine) > maxWidth) {
						for (char c : wholeLine.toCharArray()) {
							if (TextRenderer.getFontRenderer().getStringWidth(line + c) > maxWidth) {

								if ((lineCount >= startLine) && (lineCount < maxLineAmount)) {
									TextRenderer.getFontRenderer().drawString(line, getX() + 4, getY() + 4
											+ ((lineCount - startLine) * TextRenderer.getFontRenderer().FONT_HEIGHT),
											color);
								}
								line = "";
								lineCount++;
							}
							line += c;
						}
					} else {
						line = wholeLine;
					}
					if ((lineCount >= startLine) && (lineCount < maxLineAmount)) {
						TextRenderer.getFontRenderer().drawString(line, getX() + 4,
								getY() + 4 + ((lineCount - startLine) * TextRenderer.getFontRenderer().FONT_HEIGHT),
								color);
					}
					++lineCount;
				}
				textAreaHeight = (lineCount * TextRenderer.getFontRenderer().FONT_HEIGHT) + (height / 2);

				/*
				 * Find and render the cursor for some reason the formatted text doesnt render
				 * the cursor in the right place
				 */
				lines = getLines();
				int charCount = 0;
				lineCount = 0;
				int from = Math.min(getCursorPosition(), selectionEnd);
				int to = Math.max(getCursorPosition(), selectionEnd);
				boolean renderSelection = !getSelectedText().isEmpty();
				boolean renderSelectionLine = false;
				for (String wholeLine : lines) {
					String line = "";
					if ((TextRenderer.getFontRenderer().getStringWidth(wholeLine) > maxWidth)
							|| ((getCursorPosition() > charCount)
									&& (getCursorPosition() < (charCount + wholeLine.length())))
							|| ((renderSelection && ((from >= charCount) && (from <= (charCount + wholeLine.length()))))
									|| ((to >= charCount) && (to <= (charCount + wholeLine.length()))))) {

						for (char c : wholeLine.toCharArray()) {
							if (TextRenderer.getFontRenderer().getStringWidth(line + c) > maxWidth) {
								if (hasError && (Math.abs(lineCount - errLine) <= 1)) {
									if (!errCode.isEmpty() && wholeLine.contains(errCode) && (lineCount != errLine)) {
										errLine = lineCount;
										errorBox.setY(getY() + 4 + ((lineCount - startLine)
												* TextRenderer.getFontRenderer().FONT_HEIGHT));
										errorBox.setHeight(TextRenderer.getFontRenderer().FONT_HEIGHT * 2);
									} else if (lineCount == errLine) {
										errorBox.setY(getY() + 4 + ((lineCount - startLine)
												* TextRenderer.getFontRenderer().FONT_HEIGHT));
										errorBox.setHeight(TextRenderer.getFontRenderer().FONT_HEIGHT * 2);
									}
								}
								if (renderSelectionLine) {
									if (from <= (charCount - line.length())) {
										int startX = getX() + 3;
										int lineY = getY()
												+ ((lineCount - startLine) * TextRenderer.getFontRenderer().FONT_HEIGHT)
												+ 4;
										renderSelectionRect(startX, lineY,
												startX + TextRenderer.getFontRenderer().getStringWidth(line) + 2,
												lineY + TextRenderer.getFontRenderer().FONT_HEIGHT);
									} else {
										int startX = getX() + TextRenderer.getFontRenderer().getStringWidth(line) + 3;
										int lineY = getY()
												+ ((lineCount - startLine) * TextRenderer.getFontRenderer().FONT_HEIGHT)
												+ 4;
										renderSelectionRect(startX, lineY,
												startX + TextRenderer.getFontRenderer()
														.getStringWidth(line.substring(charCount - from)) + 2,
												lineY + TextRenderer.getFontRenderer().FONT_HEIGHT);
									}
								}
								line = "";
								lineCount++;
							}
							if (renderSelection) {
								if (charCount == from) {
									renderSelectionLine = true;
									int startX = getX() + TextRenderer.getFontRenderer().getStringWidth(line) + 3;
									int lineY = getY()
											+ ((lineCount - startLine) * TextRenderer.getFontRenderer().FONT_HEIGHT)
											+ 4;

									if (TextRenderer.getFontRenderer().getStringWidth(wholeLine) > maxWidth) {

									}

									if (wholeLine.contains(getSelectedText())) {
										renderSelectionLine = false;
										renderSelection = false;
										// the selection is only on this line
										renderSelectionRect(startX, lineY,
												startX + TextRenderer.getFontRenderer()
														.getStringWidth(getSelectedText()) + 2,
												lineY + TextRenderer.getFontRenderer().FONT_HEIGHT);
									}
								} else if (charCount == to) {
									renderSelectionLine = false;
									renderSelection = false;
									int startX = getX() + 3;
									int lineY = getY()
											+ ((lineCount - startLine) * TextRenderer.getFontRenderer().FONT_HEIGHT)
											+ 4;
									renderSelectionRect(startX, lineY,
											startX + TextRenderer.getFontRenderer().getStringWidth(line) + 2,
											lineY + TextRenderer.getFontRenderer().FONT_HEIGHT);
								} else if (charCount > to) {
									renderSelectionLine = false;
									renderSelection = false;
								}
							}
							if ((charCount == getCursorPosition()) && (lineCount >= startLine)
									&& (lineCount < maxLineAmount)) {

								int cursorX = getX() + TextRenderer.getFontRenderer().getStringWidth(line) + 3;
								int cursorY = getY()
										+ ((lineCount - startLine) * TextRenderer.getFontRenderer().FONT_HEIGHT) + 4;
								if (renderCursor) {
									if (suggestionCooldown > 0) {
										suggestionCooldown -= 2;
									}
									if ((getText().length() == getCursorPosition()) || (c == '\n')) {
										TextRenderer.getFontRenderer().drawString("_", cursorX, cursorY, 0xFFFFFFFF);
									} else {
										Renderer.drawRect(cursorX, cursorY, cursorX + 1,
												cursorY + TextRenderer.getFontRenderer().FONT_HEIGHT, 0xFFFFFFFF);
									}
								}
								if ((suggestionCooldown <= 0) && !wholeLine.isEmpty() && !wholeLine.trim().isEmpty()
										&& (from == to)) {
									setHoverText(Lists.newArrayList(getRecommendation(wholeLine)));
									if (drawHoverText) {
										verifyHoverText(cursorX + 5, cursorY);
										if (drawToLeft) {
											int tlineWidth = 0;
											for (String hline : hoverText) {
												tlineWidth = TextRenderer.getFontRenderer()
														.getStringWidth(hline) > tlineWidth
																? TextRenderer.getFontRenderer().getStringWidth(hline)
																: tlineWidth;
											}
											Renderer.drawHoveringText(hoverText, cursorX - tlineWidth - 20,
													cursorY + 12);
										} else {
											Renderer.drawHoveringText(hoverText, cursorX + 5, cursorY + 12);
										}
									}
								}
							}
							charCount++;
							line += c;
						}
					} else {
						line = wholeLine;
						charCount += wholeLine.length();
						if (renderSelectionLine && (charCount >= to)) {
							renderSelectionLine = false;
							renderSelection = false;
						}
					}

					if ((lineCount >= startLine) && (lineCount < maxLineAmount)) {
						if (hasError && (Math.abs(lineCount - errLine) <= 1)) {
							// its possible the code error is empty because the way
							// python shell reports its errors...
							if (!errCode.isEmpty() && wholeLine.contains(errCode) && (lineCount != errLine)) {
								errLine = lineCount;
								errorBox.setY(getY() + 4
										+ ((lineCount - startLine) * TextRenderer.getFontRenderer().FONT_HEIGHT));
								errorBox.setHeight(TextRenderer.getFontRenderer().FONT_HEIGHT);
							} else if (lineCount == errLine) {
								errorBox.setY(getY() + 4
										+ ((lineCount - startLine) * TextRenderer.getFontRenderer().FONT_HEIGHT));
								errorBox.setHeight(TextRenderer.getFontRenderer().FONT_HEIGHT);
							}
						}
						if (charCount == getCursorPosition()) {
							int cursorX = getX() + TextRenderer.getFontRenderer().getStringWidth(line) + 3;
							int cursorY = getY()
									+ ((lineCount - startLine) * TextRenderer.getFontRenderer().FONT_HEIGHT) + 4;
							if (renderCursor) {
								if (suggestionCooldown > 0) {
									suggestionCooldown -= 2;
								}
								if ((getText().length() == getCursorPosition()) || (getText().toCharArray()[Math
										.min(charCount, getText().toCharArray().length - 1)] == '\n')) {
									TextRenderer.getFontRenderer().drawString("_", cursorX, cursorY, 0xFFFFFFFF);
								} else {
									Renderer.drawRect(cursorX, cursorY, cursorX + 1,
											cursorY + TextRenderer.getFontRenderer().FONT_HEIGHT, 0xFFFFFFFF);
								}
							}
							if ((suggestionCooldown <= 0) && !wholeLine.isEmpty() && !wholeLine.trim().isEmpty()
									&& (from == to)) {
								setHoverText(Lists.newArrayList(getRecommendation(wholeLine)));
								if (drawHoverText) {
									verifyHoverText(cursorX + 5, cursorY);
									if (drawToLeft) {
										int tlineWidth = 0;
										for (String hline : hoverText) {
											tlineWidth = TextRenderer.getFontRenderer()
													.getStringWidth(hline) > tlineWidth
															? TextRenderer.getFontRenderer().getStringWidth(hline)
															: tlineWidth;
										}
										Renderer.drawHoveringText(hoverText, cursorX - tlineWidth - 20, cursorY + 12);
									} else {
										Renderer.drawHoveringText(hoverText, cursorX + 5, cursorY + 12);
									}
								}
							}
						}
					}
					if (renderSelectionLine) {
						if (from <= (charCount - line.length())) {
							// render the whole line
							int startX = getX() + 3;
							int lineY = getY() + ((lineCount - startLine) * TextRenderer.getFontRenderer().FONT_HEIGHT)
									+ 4;
							renderSelectionRect(startX, lineY,
									startX + TextRenderer.getFontRenderer().getStringWidth(line) + 2,
									lineY + TextRenderer.getFontRenderer().FONT_HEIGHT);
						} else {
							// render from the selection over
							String substring = line.substring(line.length() - (charCount - from));
							int startX = getX()
									+ TextRenderer.getFontRenderer().getStringWidth(line.replace(substring, "")) + 3;
							int lineY = getY() + ((lineCount - startLine) * TextRenderer.getFontRenderer().FONT_HEIGHT)
									+ 4;
							renderSelectionRect(startX, lineY,
									startX + TextRenderer.getFontRenderer().getStringWidth(substring) + 2,
									lineY + TextRenderer.getFontRenderer().FONT_HEIGHT);
						}
						if (renderSelectionLine && (charCount >= to)) {
							renderSelectionLine = false;
							renderSelection = false;
						}

					}
					++lineCount;
					++charCount;
				}
				scrollBar.setVisiblie(textAreaHeight > (height - 4));
				scrollBar.setHandleMouseWheel(
						(textAreaHeight > (height - 4)) && isUnderMouse(Mouse.getX(), Mouse.getY()));
				scrollBar.setScrollerSize((super.getScrollerSize()));
				GlStateManager.resetColor();
			}
			GlStateManager.popMatrix();
			TextRenderer.getFontRenderer().setUnicodeFlag(false);
		}
	}

	private void formatText() {
		StringBuilder builder = new StringBuilder();
		for (String line : getLines()) {
			Python3Lexer lexer = new Python3Lexer(new ANTLRInputStream(line));
			CollectorTokenSource tokenSource = new CollectorTokenSource(lexer);
			CommonTokenStream tokens = new CommonTokenStream(tokenSource);
			Python3Parser parser = new Python3Parser(tokens);
			parser.removeErrorListeners();
			lexer.removeErrorListeners();
			parser.file_input();

			for (int i = 0; i < tokenSource.getCollectedTokens().size(); i++) {
				Token token = tokens.get(i);
				if (token.getType() == Token.EOF) {
					break;
				} else if (token.getType() <= Python3Lexer.BREAK) {
					builder.append(SYNTAX + token.getText() + RESET);
				} else if (token.getType() == Python3Lexer.NAME) {
					// a name ends up being nearly everything so lets break it
					// down a little
					Token nextToken = null;
					Token prevToken = null;
					if (i < (tokenSource.getCollectedTokens().size() - 1)) {
						nextToken = tokens.get(i + 1);
					}
					if (i > 0) {
						prevToken = tokens.get(i - 1);
					}
					// if the next token is an open paren its a function
					// if it follows a dot its a member variable
					// if that has a paren its a member function
					if ((prevToken != null) && (prevToken.getType() == Python3Lexer.DOT)) {
						if ((nextToken != null) && (nextToken.getType() == Python3Lexer.OPEN_PAREN)) {
							// its a member function
							builder.append(MEMBER_FUNCTION + token.getText() + RESET);
						} else {
							// its a member of some sort
							builder.append(MEMBER_VAR + token.getText() + RESET);
						}
					} else if ((nextToken != null) && (nextToken.getType() == Python3Lexer.OPEN_PAREN)) {
						// a function
						builder.append(FUNCTION + token.getText() + RESET);
					} else if ((prevToken != null) && (prevToken.getType() == Python3Parser.UNKNOWN_CHAR)
							&& (prevToken.getText().equals("\"") || prevToken.getText().equals("'"))) {
						// part of a string literal that has not been closed yet
						builder.append(token.getText() + RESET);
					} else {
						builder.append(VARIABLE + token.getText() + RESET);
					}
				} else if (token.getType() == Python3Lexer.STRING_LITERAL) {
					builder.append(STRING + token.getText() + RESET);
				} else if ((token.getType() >= Python3Lexer.BYTES_LITERAL)
						&& (token.getType() <= Python3Lexer.IMAG_NUMBER)) {
					builder.append(NUMBER + token.getText() + RESET);
				} else if ((token.getType() >= Python3Lexer.DOT) && (token.getType() <= Python3Lexer.IDIV_ASSIGN)) {
					builder.append(SYMBOL + token.getText() + RESET);
				} else if (token.getType() == Python3Parser.COMMENT) {
					builder.append(COMMENT + token.getText() + RESET);
				} else if (token.getType() == Python3Parser.SPACES) {
					builder.append(token.getText());
				} else if (token.getType() == Python3Parser.INDENT) {
					builder.append(token.getText());
				} else {
					if ((token.getType() == Python3Parser.UNKNOWN_CHAR)
							&& (token.getText().equals("\"") || token.getText().equals("'"))) {
						builder.append(STRING + token.getText());
					} else {
						if ((token.getType() != Python3Parser.NEWLINE) && (token.getType() != Python3Parser.DEDENT)) {
							RabbitGui.logger.info("Attempting to format unhandled token: " + token.getType() + ", "
									+ token.getText());
						}
					}
				}
			}
			builder.append("\n");
		}
		formattedText = builder.toString();
	}

	public List<String> getClassMembers(String clazz) {
		return classMembers.get(clazz);
	}

	public List<String> getFormattedLines() {
		return Arrays.asList(formattedText.split("\n"));
	}

	public Set<String> getRecommendation(String line) {
		AntlrAutoCompletionSuggester autoComplete = new AntlrAutoCompletionSuggester(Python3Parser.ruleNames,
				Python3Parser.VOCABULARY, Python3Parser._ATN);

		EditorContext context = autoComplete.new EditorContext(line);
		Set<TokenType> suggestions = autoComplete.suggestions(context);
		Set<String> recommendations = new TreeSet<>();

		// suggester adds a fake token at the end, drop it
		List<Token> tokens = UtilityFunctions.minusLast(context.preceedingTokens());
		Token curText = null;
		if ((tokens.size() > 1) && (UtilityFunctions.getLastElement(UtilityFunctions.minusLast(tokens))
				.getType() == Python3Lexer.DOT)) {
			curText = UtilityFunctions.getLastElement(tokens);
			tokens = UtilityFunctions.minusLast(tokens);
		}
		if ((classMembers.size() > 0) && (tokens.size() > 1)
				&& (UtilityFunctions.getLastElement(tokens).getType() == Python3Lexer.DOT)) {
			// The last element is a dot so we are probably
			// accessing member variables
			if (curText == null) {
				if (UtilityFunctions.getLastElement(UtilityFunctions.minusLast(tokens))
						.getType() == Python3Lexer.NAME) {
					// and the thing preceeding the dot is name so we
					// are accessing a classes variables
					if (userVariables.containsKey(
							UtilityFunctions.getLastElement(UtilityFunctions.minusLast(tokens)).getText())) {
						recommendations.addAll(classMembers.get(userVariables
								.get(UtilityFunctions.getLastElement(UtilityFunctions.minusLast(tokens)).getText())));
					}
					if (predefinedUserVariables.containsKey(
							UtilityFunctions.getLastElement(UtilityFunctions.minusLast(tokens)).getText())) {
						recommendations.addAll(classMembers.get(predefinedUserVariables
								.get(UtilityFunctions.getLastElement(UtilityFunctions.minusLast(tokens)).getText())));
					}
				}
			} else {
				if (predefinedUserVariables
						.containsKey(UtilityFunctions.getLastElement(UtilityFunctions.minusLast(tokens)).getText())) {
					for (String elem : classMembers.get(predefinedUserVariables
							.get(UtilityFunctions.getLastElement(UtilityFunctions.minusLast(tokens)).getText()))) {
						if (elem.contains(curText.getText())) {
							recommendations.add(elem);
						}
					}
				}
				if (userVariables
						.containsKey(UtilityFunctions.getLastElement(UtilityFunctions.minusLast(tokens)).getText())) {
					for (String elem : classMembers.get(userVariables
							.get(UtilityFunctions.getLastElement(UtilityFunctions.minusLast(tokens)).getText()))) {
						if (elem.contains(curText.getText())) {
							recommendations.add(elem);
						}
					}
				}
			}
		}
		if (recommendations.isEmpty()) {
			for (TokenType token : suggestions) {
				String proposition = Python3Lexer.VOCABULARY.getLiteralName(token.getType());
				if (proposition != null) {
					// TODO: probably best if we filter out symbols? how to give
					// smarter feedback
					if (proposition.startsWith("'") && proposition.endsWith("'")) {
						proposition = proposition.substring(1, proposition.length() - 1);
						recommendations.add(proposition);
					}
				}
			}
		}

		return recommendations;
	}

	@Override
	public int getStartLineY() {
		if ((scrollBar != null) && scrollBar.isVisible()) {
			return MathHelper.ceil((scrollBar.getScrolledAmt() * (textAreaHeight - getHeight()))
					/ TextRenderer.getFontRenderer().FONT_HEIGHT);
		}
		return 0;
	}

	@Override
	protected boolean handleInput(char typedChar, int typedKeyIndex) {
		if (hasError && (typedKeyIndex == Keyboard.KEY_RETURN)) {
			errLine++;
		}
		boolean status = super.handleInput(typedChar, typedKeyIndex);
		if (typedKeyIndex == Keyboard.KEY_TAB) {
			pushText("    ");
		}
		return status;
	}

	public void notifyError(int line, String code, String error) {
		errLine = line;
		// code can be empty
		if ((code != null) && !code.isEmpty()) {
			errCode = code.trim();
		} else if (error.contains("NameError")) {
			try {
				errCode = getLines().get(errLine);
			} catch (Exception e) {
				// index out of bounds
			}
		}
		if (errLine >= 0) {
			hasError = true;
		}
	}

	public CodeInterface setHoverText(List<String> text) {
		originalHoverText = text;
		return this;
	}

	@Override
	public TextBox setText(String newText) {
		text = newText;
		suggestionCooldown = 50;
		testForErrors();
		formatText();
		return this;
	}

	@Override
	public void setup() {
		super.setup();
		registerComponent(errorBox);
	}

	@Override
	public GuiWidget setX(int x) {
		super.setX(x);
		errorBox.setX(x);
		return this;
	}

	@Override
	public GuiWidget setY(int y) {
		super.setY(y);
		return this;
	}

	public void testForErrors() {
		Python3Lexer lexer = new Python3Lexer(new ANTLRInputStream(getText()));
		CollectorTokenSource tokenSource = new CollectorTokenSource(lexer);
		CommonTokenStream tokens = new CommonTokenStream(tokenSource);
		Python3Parser parser = new Python3Parser(tokens);
		parser.removeErrorListeners();
		parser.addErrorListener(DescriptiveErrorListener.INSTANCE);
		parser.file_input();

		List<Token> res = new LinkedList<>();
		for (Token token : tokenSource.getCollectedTokens()) {
			if ((token.getChannel() == 0) && (token.getType() != Python3Parser.NEWLINE)) {
				res.add(token);
			}
		}

		userVariables.clear();

		int i = 0;
		for (Token token : res) {
			if (token.getType() == Python3Parser.ASSIGN) {
				try {
					if ((res.get(i - 1).getType() == Python3Parser.NAME)
							&& (res.get(i + 1).getType() == Python3Parser.NAME)) {
						// ok we have an assignment lets remember the object map
						// now
						if (!userVariables.containsKey(res.get(i - 1).getText())) {
							userVariables.put(res.get(i - 1).getText(), res.get(i + 1).getText());
						} else {
							notifyError(getText().lastIndexOf(res.get(i - 1).getText()), res.get(i - 1).getText(),
									"Redefinition Error: variable was already defined");
							break;
						}
					}
				} catch (IndexOutOfBoundsException e) {
					// name is not assigned its ok we might be in the process of
					// writing
				}
			}
			i++;
		}
	}

	protected void verifyHoverText(int mouseX, int mouseY) {
		int tlineWidth = 0;
		for (String line : originalHoverText) {
			tlineWidth = TextRenderer.getFontRenderer().getStringWidth(line) > tlineWidth
					? TextRenderer.getFontRenderer().getStringWidth(line)
					: tlineWidth;
		}
		if (((tlineWidth + mouseX) > width) && ((mouseX + 1) > (width / 2))) {
			// the button is on the right half of the screen
			drawToLeft = true;
		}
		List<String> newHoverText = new ArrayList<>();
		if (drawToLeft) {
			for (String line : originalHoverText) {
				int lineWidth = TextRenderer.getFontRenderer().getStringWidth(line) + 12;
				// if the line length is longer than the button is from the left
				// side of the screen we have to split
				if (lineWidth > mouseX) {
					// the line is too long lets split it
					StringBuilder builder = new StringBuilder();
					for (String substring : line.split(" ")) {
						// we can fit the string, we are ok
						if ((TextRenderer.getFontRenderer().getStringWidth(builder.toString())
								+ TextRenderer.getFontRenderer().getStringWidth(substring)) < (mouseX - 12)) {
							builder.append(substring + " ");
						} else {
							newHoverText.add(builder.toString());
							builder.append(substring + " ");
						}
					}
					newHoverText.add(builder.toString());
				} else {
					newHoverText.add(line);
				}
			}
		} else {
			for (String line : originalHoverText) {
				int lineWidth = TextRenderer.getFontRenderer().getStringWidth(line) + 12;
				// we just need to know what the right most side of the button
				// is
				if (lineWidth > (width - mouseX)) {
					// the line is too long lets split it
					StringBuilder builder = new StringBuilder();
					for (String substring : line.split(" ")) {
						// we can fit the string, we are ok
						if ((TextRenderer.getFontRenderer().getStringWidth(builder.toString())
								+ TextRenderer.getFontRenderer().getStringWidth(substring)) < (width - mouseX - 12)) {
							builder.append(substring + " ");
						} else {
							newHoverText.add(builder.toString());
							builder.append(substring + " ");
						}
					}
					newHoverText.add(builder.toString());
				} else {
					newHoverText.add(line);
				}
			}
		}
		hoverText = newHoverText;
	}
}
