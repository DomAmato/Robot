package com.dyn.robot.gui;

import java.awt.Color;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.network.NetworkManager;
import com.dyn.robot.network.messages.MessageEndRobotTerminal;
import com.dyn.robot.network.messages.MessageRobotTerminalInput;
import com.dyn.robot.network.messages.MessageStartRobotTerminal;
import com.google.common.collect.Lists;
import com.rabbit.gui.component.control.TextBox;
import com.rabbit.gui.component.display.ScrollTextLabel;
import com.rabbit.gui.component.display.Shape;
import com.rabbit.gui.component.display.ShapeType;
import com.rabbit.gui.component.display.TextLabel;
import com.rabbit.gui.render.TextRenderer;
import com.rabbit.gui.show.Show;

import net.minecraft.client.Minecraft;

public class CommandLineInterface extends Show {
	public static List<String> prevCommands;
	public static int cmdIndex;
	public static String lastSent;
	public static int indentations;
	private EntityRobot robot;
	private ScrollTextLabel output;
	private TextLabel entryStyle;

	public CommandLineInterface(EntityRobot robot) {
		this.robot = robot;
		NetworkManager.sendToServer(new MessageStartRobotTerminal(robot.getEntityId()));
		CommandLineInterface.lastSent = "";
		CommandLineInterface.indentations = 0;
		CommandLineInterface.cmdIndex = 0;
		CommandLineInterface.prevCommands = Lists.newArrayList();
	}

	@Override
	public void onClose() {
		robot.stopExecutingCode();
		RobotMod.isSpectatingRobot = false;
		NetworkManager.sendToServer(new MessageEndRobotTerminal(robot.getEntityId()));
		Minecraft.getMinecraft().setRenderViewEntity(Minecraft.getMinecraft().player);
	}

	@Override
	public void setup() {

		registerComponent(output = (ScrollTextLabel) new ScrollTextLabel(5, 2 + (int) (height * .7), width - 10,
				(int) (height * .3) - 25, "").setColor(Color.white).setMultilined(true));
		registerComponent(entryStyle = new TextLabel(5, height - 20, width, 20, Color.white, ">>>"));
		registerComponent(
				new TextBox(7 + TextRenderer.getFontRenderer().getStringWidth(">>>"), height - 20, width - 15, 20) {
					@Override
					protected boolean handleInput(char typedChar, int typedKeyIndex) {
						if (typedKeyIndex == Keyboard.KEY_UP) {
							if (CommandLineInterface.prevCommands.size() > 0) {
								setText(CommandLineInterface.prevCommands.get(CommandLineInterface.cmdIndex--));
								CommandLineInterface.cmdIndex = Math.max(0, CommandLineInterface.cmdIndex);
							}
						} else if (typedKeyIndex == Keyboard.KEY_DOWN) {
							if (CommandLineInterface.prevCommands.size() > 0) {
								setText(CommandLineInterface.prevCommands.get(CommandLineInterface.cmdIndex++));
								CommandLineInterface.cmdIndex = Math.min(CommandLineInterface.prevCommands.size(),
										CommandLineInterface.cmdIndex);
							}
							if (CommandLineInterface.cmdIndex == CommandLineInterface.prevCommands.size()) {
								setText("");
								setCursorPosition(0);
								CommandLineInterface.cmdIndex = CommandLineInterface.prevCommands.size() - 1;
							}

						} else if (typedKeyIndex == Keyboard.KEY_RETURN) {
							String textToSend = getText();
							if (textToSend.isEmpty()) {
								CommandLineInterface.indentations--;
							}
							for (int i = 0; i < CommandLineInterface.indentations; i++) {
								textToSend = "    " + textToSend;
							}
							NetworkManager.sendToServer(new MessageRobotTerminalInput(textToSend, robot.getEntityId()));
							CommandLineInterface.lastSent = getText();
							CommandLineInterface.prevCommands.add(CommandLineInterface.lastSent);
							if (CommandLineInterface.prevCommands.size() > 5) {
								CommandLineInterface.prevCommands.remove(0);
							}
							CommandLineInterface.cmdIndex = CommandLineInterface.prevCommands.size() - 1;
							if (CommandLineInterface.lastSent.endsWith(":")) {
								CommandLineInterface.indentations++;
							}
							setText("");
							setCursorPosition(0);
							setIsFocused(true);
							return true;
						}
						return super.handleInput(typedChar, typedKeyIndex);
					}

				}.setBackgroundVisibility(false).setIsFocused(true));

		registerComponent(new Shape(0, (int) (height * .7), width, (int) (height * .31), ShapeType.RECT,
				new Color(80, 80, 80, 128)));
		// registerComponent(new DraggableCamera(0, 0, width, height).setEnabled(true));
	}

	public void updateOutput(String text) {
		text = text.replaceAll("(\\r|\\n|\\r\\n)+", "\n");
		String prevCmd = "";
		if (text.contains("Error")) {
			CommandLineInterface.indentations = 0;
		} else {
			if (!CommandLineInterface.lastSent.isEmpty() || (CommandLineInterface.indentations > 0)) {
				prevCmd = entryStyle.getText() + " " + CommandLineInterface.lastSent;
				for (int i = 0; i < (CommandLineInterface.indentations
						- (CommandLineInterface.lastSent.endsWith(":") ? 1 : 0)); i++) {
					prevCmd = "    " + prevCmd;
				}
			}
		}
		if (text.endsWith(">>> ")) {
			entryStyle.setText(">>>");
			text = text.substring(0, text.lastIndexOf(">>>"));
		} else if (text.endsWith("... ")) {
			entryStyle.setText(". . .");
			text = text.substring(0, text.lastIndexOf("..."));
		}
		output.setText(output.getText() + "\n" + prevCmd + text);
		output.setScrolledAmount(1f);
	}
}
