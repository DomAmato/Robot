package com.dyn.robot.gui;

import java.awt.Color;

import com.dyn.robot.RobotMod;
import com.dyn.robot.blocks.BlockRobot;
import com.dyn.robot.network.NetworkManager;
import com.dyn.robot.network.messages.MessageActivateRobot;
import com.rabbit.gui.component.control.Button;
import com.rabbit.gui.component.control.TextBox;
import com.rabbit.gui.component.display.Panel;
import com.rabbit.gui.component.display.Picture;
import com.rabbit.gui.component.display.TextLabel;
import com.rabbit.gui.show.Show;
import com.rabbit.gui.utils.DefaultTextures;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class ActivationScreen extends Show {
	private BlockPos robotBlockPos;
	private EntityPlayer player;
	private String robotName;

	public ActivationScreen(BlockRobot robot, EntityPlayer player, BlockPos pos) {
		robotBlockPos = pos;
		this.player = player;
		if (robot.hasName()) {
			robotName = robot.getRobotName();
		} else {
			robotName = "";
		}
	}

	@Override
	public void setup() {
		Panel panel = new Panel((int) (width * .2), (int) (height * .2), (int) (width * .6), (int) (height * .6))
				.setVisible(true).setFocused(true);

		panel.registerComponent(new TextLabel((int) (panel.getWidth() * .05), (int) (panel.getHeight() * .05),
				(int) (panel.getWidth() * .8), 20, Color.black, "Robot Remote"));

		panel.registerComponent(new TextBox((int) (panel.getWidth() * .1), (int) (panel.getHeight() * .2),
				(int) (panel.getWidth() * .8), 25, "Give Robot a Name")
						.setTextChangedListener((TextBox textbox, String previousText) -> {
							robotName = previousText;
						}));

		panel.registerComponent(new Button((int) (panel.getWidth() * .1), (int) (panel.getHeight() * .6),
				(int) (panel.getWidth() * .25), 20, "Activate").setClickListener(btn -> {
					NetworkManager.sendToServer(new MessageActivateRobot(
							(robotName.isEmpty() ? "Robot" + (int) (65535 * Math.random()) : robotName), robotBlockPos,
							player.dimension, true));
					getStage().close();
				}));

		panel.registerComponent(new Picture(0, 0, panel.getWidth(), panel.getHeight(), DefaultTextures.BACKGROUND1));

		// this is my lazy way of not having to rearrange things
		panel.reverseComponents();

		registerComponent(panel);
	}
}
