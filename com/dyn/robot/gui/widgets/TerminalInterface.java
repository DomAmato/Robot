package com.dyn.robot.gui.widgets;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.dyn.DYNServerMod;
import com.rabbit.gui.component.GuiWidget;
import com.rabbit.gui.render.Renderer;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.client.gui.FixedWidthFontRenderer;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.terminal.TextBuffer;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.util.Colour;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;

public class TerminalInterface extends GuiWidget {
	private static final ResourceLocation background = new ResourceLocation("computercraft",
			"textures/gui/termBackground.png");
	private static float TERMINATE_TIME = 0.5F;

	private IComputer m_computer;
	private float m_terminateTimer;
	private float m_rebootTimer;
	private float m_shutdownTimer;
	private int m_lastClickButton;
	private int m_lastClickX;
	private int m_lastClickY;
	private boolean m_focus;
	private boolean m_allowFocusLoss;
	private boolean m_locked;
	private ArrayList<Integer> m_keysDown;

	public TerminalInterface(int x, int y, int termWidth, int termHeight, IComputer computer) {
		super(x, y, termWidth, termHeight);

		m_computer = computer;
		if (m_computer == null) {
			DYNServerMod.logger.info("Computer seems to be null...");
		}

		m_terminateTimer = 0.0F;
		m_rebootTimer = 0.0F;
		m_shutdownTimer = 0.0F;

		m_lastClickButton = -1;
		m_lastClickX = -1;
		m_lastClickY = -1;

		m_focus = true;
		m_allowFocusLoss = true;
		m_locked = false;

		m_keysDown = new ArrayList();
	}

	public void handleKeyboardInput() {
		for (int i = m_keysDown.size() - 1; i >= 0; i--) {
			int key = m_keysDown.get(i).intValue();
			if (!Keyboard.isKeyDown(key)) {
				m_keysDown.remove(i);
				if ((m_focus) && (!m_locked)) {
					queueEvent("key_up", new Object[] { Integer.valueOf(key) });
				}
			}
		}
	}

	@Override
	public void onDraw(int mouseX, int mouseY, float partialTicks) {

		Terminal terminal = m_computer != null ? m_computer.getTerminal() : null;
		if (terminal != null) {
			boolean greyscale = !m_computer.isColour();
			synchronized (terminal) {
				FixedWidthFontRenderer fontRenderer = (FixedWidthFontRenderer) ComputerCraft
						.getFixedWidthFontRenderer();
				boolean tblink = (m_focus) && (terminal.getCursorBlink()) && (ComputerCraft.getGlobalCursorBlink());
				int tw = terminal.getWidth();
				int th = terminal.getHeight();
				int tx = terminal.getCursorX();
				int ty = terminal.getCursorY();

				new TextBuffer(' ', tw);
				// if (m_topMargin > 0) {
				// fontRenderer.drawString(emptyLine, x, y,
				// terminal.getTextColourLine(0),
				// terminal.getBackgroundColourLine(0), 0, 0, greyscale);
				// }
				// if (m_bottomMargin > 0) {
				// fontRenderer.drawString(emptyLine, x,
				// y + ((th - 1) * FixedWidthFontRenderer.FONT_HEIGHT),
				// terminal.getTextColourLine(th - 1),
				// terminal.getBackgroundColourLine(th - 1), 0,
				// 0, greyscale);
				// }
				int termX = x;
				int termY = y;
				for (int line = 0; line < th; line++) {
					TextBuffer text = terminal.getLine(line);
					TextBuffer colour = terminal.getTextColourLine(line);
					TextBuffer backgroundColour = terminal.getBackgroundColourLine(line);
					fontRenderer.drawString(text, termX, termY, colour, backgroundColour, 0, 0, greyscale);
					if ((tblink) && (ty == line)) {
						if ((tx >= 0) && (tx < tw)) {
							TextBuffer cursor = new TextBuffer('_', 1);
							TextBuffer cursorColour = new TextBuffer(
									"0123456789abcdef".charAt(terminal.getTextColour()), 1);
							fontRenderer.drawString(cursor, termX + (FixedWidthFontRenderer.FONT_WIDTH * tx), termY,
									cursorColour, null, 0.0D, 0.0D, greyscale);
						}
					}
					termY += FixedWidthFontRenderer.FONT_HEIGHT;
				}
			}
		} else {
			DYNServerMod.logger.info("Terminal is null...");
			Minecraft.getMinecraft().getTextureManager().bindTexture(background);
			Colour black = Colour.Black;
			GlStateManager.color(black.getR(), black.getG(), black.getB(), 1.0F);
			try {
				Renderer.drawTexturedModalRect(x, y, 0, 0, getWidth(), getHeight());
			} finally {
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			}
		}
	}

	@Override
	public void onKeyTyped(char ch, int key) {
		if ((m_focus) && (!m_locked)) {
			if (ch == '\026') {
				String clipboard = GuiScreen.getClipboardString();
				if (clipboard != null) {
					int newLineIndex1 = clipboard.indexOf("\r");
					int newLineIndex2 = clipboard.indexOf("\n");
					if ((newLineIndex1 >= 0) && (newLineIndex2 >= 0)) {
						clipboard = clipboard.substring(0, Math.min(newLineIndex1, newLineIndex2));
					} else if (newLineIndex1 >= 0) {
						clipboard = clipboard.substring(0, newLineIndex1);
					} else if (newLineIndex2 >= 0) {
						clipboard = clipboard.substring(0, newLineIndex2);
					}
					clipboard = ChatAllowedCharacters.filterAllowedCharacters(clipboard);
					if (!clipboard.isEmpty()) {
						if (clipboard.length() > 512) {
							clipboard = clipboard.substring(0, 512);
						}
						queueEvent("paste", new Object[] { clipboard });
					}
				}
				return;
			}
			if ((m_terminateTimer <= 0.0F) && (m_rebootTimer <= 0.0F) && (m_shutdownTimer <= 0.0F)) {
				boolean repeat = Keyboard.isRepeatEvent();
				if (key > 0) {
					if (!repeat) {
						m_keysDown.add(Integer.valueOf(key));
					}
					queueEvent("key", new Object[] { Integer.valueOf(key), Boolean.valueOf(repeat) });
				}
				if (((ch >= ' ') && (ch <= '~')) || ((ch >= ' ') && (ch <= 'ÿ'))) {
					queueEvent("char", new Object[] { Character.toString(ch) });
				}
			}
		}
	}

	@Override
	public boolean onMouseClicked(int mouseX, int mouseY, int button, boolean overlap) {
		if ((mouseX >= x) && (mouseX < (x + getWidth())) && (mouseY >= y) && (mouseY < (y + getHeight()))) {
			if ((!m_focus) && (button == 0)) {
				m_focus = true;
			}
			if (m_focus) {
				if ((!m_locked) && (m_computer != null) && (m_computer.isColour()) && (button >= 0) && (button <= 2)) {
					Terminal term = m_computer.getTerminal();
					if (term != null) {
						int charX = (mouseX - (x)) / FixedWidthFontRenderer.FONT_WIDTH;
						int charY = (mouseY - (y)) / FixedWidthFontRenderer.FONT_HEIGHT;
						charX = Math.min(Math.max(charX, 0), term.getWidth() - 1);
						charY = Math.min(Math.max(charY, 0), term.getHeight() - 1);

						m_computer.queueEvent("mouse_click", new Object[] { Integer.valueOf(button + 1),
								Integer.valueOf(charX + 1), Integer.valueOf(charY + 1) });

						m_lastClickButton = button;
						m_lastClickX = charX;
						m_lastClickY = charY;
					}
				}
			}
		} else if ((m_focus) && (button == 0) && (m_allowFocusLoss)) {
			m_focus = false;
		}
		return m_focus;
	}

	@Override
	public void onMouseInput() {
		int mouseX = (Mouse.getEventX() * width) / Minecraft.getMinecraft().displayWidth;
		int mouseY = height - ((Mouse.getEventY() * height) / Minecraft.getMinecraft().displayHeight) - 1;
		if ((mouseX >= x) && (mouseX < (x + getWidth())) && (mouseY >= y) && (mouseY < (y + getHeight()))
				&& (m_computer != null) && (m_computer.isColour())) {
			Terminal term = m_computer.getTerminal();
			if (term != null) {
				int charX = (mouseX - (x)) / FixedWidthFontRenderer.FONT_WIDTH;
				int charY = (mouseY - (y)) / FixedWidthFontRenderer.FONT_HEIGHT;
				charX = Math.min(Math.max(charX, 0), term.getWidth() - 1);
				charY = Math.min(Math.max(charY, 0), term.getHeight() - 1);
				if ((m_lastClickButton >= 0) && (!Mouse.isButtonDown(m_lastClickButton))) {
					if ((m_focus) && (!m_locked)) {
						m_computer.queueEvent("mouse_up", new Object[] { Integer.valueOf(m_lastClickButton + 1),
								Integer.valueOf(charX + 1), Integer.valueOf(charY + 1) });
					}
					m_lastClickButton = -1;
				}
				int wheelChange = Mouse.getEventDWheel();
				if ((wheelChange == 0) && (m_lastClickButton == -1)) {
					return;
				}
				if ((m_focus) && (!m_locked)) {
					if (wheelChange < 0) {
						m_computer.queueEvent("mouse_scroll", new Object[] { Integer.valueOf(1),
								Integer.valueOf(charX + 1), Integer.valueOf(charY + 1) });
					} else if (wheelChange > 0) {
						m_computer.queueEvent("mouse_scroll", new Object[] { Integer.valueOf(-1),
								Integer.valueOf(charX + 1), Integer.valueOf(charY + 1) });
					}
					if ((m_lastClickButton >= 0) && ((charX != m_lastClickX) || (charY != m_lastClickY))) {
						m_computer.queueEvent("mouse_drag", new Object[] { Integer.valueOf(m_lastClickButton + 1),
								Integer.valueOf(charX + 1), Integer.valueOf(charY + 1) });

						m_lastClickX = charX;
						m_lastClickY = charY;
					}
				}
			}
		}
	}

	@Override
	public void onUpdate() {
		if ((m_focus) && (!m_locked) && ((Keyboard.isKeyDown(29)) || (Keyboard.isKeyDown(157)))) {
			if (Keyboard.isKeyDown(20)) {
				if (m_terminateTimer < TERMINATE_TIME) {
					m_terminateTimer += 0.05F;
					if (m_terminateTimer >= TERMINATE_TIME) {
						queueEvent("terminate");
					}
				}
			} else {
				m_terminateTimer = 0.0F;
			}
			if (Keyboard.isKeyDown(19)) {
				if (m_rebootTimer < TERMINATE_TIME) {
					m_rebootTimer += 0.05F;
					if (m_rebootTimer >= TERMINATE_TIME) {
						if (m_computer != null) {
							m_computer.reboot();
						}
					}
				}
			} else {
				m_rebootTimer = 0.0F;
			}
			if (Keyboard.isKeyDown(31)) {
				if (m_shutdownTimer < TERMINATE_TIME) {
					m_shutdownTimer += 0.05F;
					if (m_shutdownTimer >= TERMINATE_TIME) {
						if (m_computer != null) {
							m_computer.shutdown();
						}
					}
				}
			} else {
				m_shutdownTimer = 0.0F;
			}
		} else {
			m_terminateTimer = 0.0F;
			m_rebootTimer = 0.0F;
			m_shutdownTimer = 0.0F;
		}
	}

	private void queueEvent(String event) {
		if (m_computer != null) {
			m_computer.queueEvent(event);
		}
	}

	private void queueEvent(String event, Object[] args) {
		if (m_computer != null) {
			m_computer.queueEvent(event, args);
		}
	}

	public void setAllowFocusLoss(boolean allowFocusLoss) {
		m_allowFocusLoss = allowFocusLoss;
		m_focus = ((m_focus) || (!allowFocusLoss));
	}

	public void setLocked(boolean locked) {
		m_locked = locked;
	}

	public boolean suppressKeyPress(char c, int k) {
		if (m_focus) {
			return k != 1;
		}
		return false;
	}
}
