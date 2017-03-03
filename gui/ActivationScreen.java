package com.dyn.robot.gui;

import java.awt.Color;

import com.dyn.robot.RobotMod;
import com.dyn.robot.blocks.BlockDynRobot;
import com.dyn.server.network.NetworkManager;
import com.dyn.server.network.messages.MessageActivateRobot;
import com.rabbit.gui.component.control.Button;
import com.rabbit.gui.component.control.TextBox;
import com.rabbit.gui.component.display.Panel;
import com.rabbit.gui.component.display.Picture;
import com.rabbit.gui.component.display.TextLabel;
import com.rabbit.gui.show.Show;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;

public class ActivationScreen extends Show {
	private BlockPos robotBlockPos;
	private EntityPlayer player;
	private String robotName;

	public ActivationScreen(BlockDynRobot robot, EntityPlayer player, BlockPos pos) {
		robotBlockPos = pos;
		this.player = player;
		if (robot.hasName()) {
			robotName = robot.getRobotName();
		} else {
			robotName = "";
		}
		if ((RobotMod.currentRobot != null) && RobotMod.currentRobot.isDead) {
			RobotMod.currentRobot = null;
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
					if (RobotMod.currentRobot != null) {
						BlockPos pos = RobotMod.currentRobot.getPosition();
						NetworkManager.sendToServer(new MessageActivateRobot(player.getName(), pos,
								RobotMod.currentRobot.dimension, false));
						RobotMod.currentRobot.setDead();
						RobotMod.currentRobot = null;
					}
					NetworkManager.sendToServer(new MessageActivateRobot(
							(robotName.isEmpty() ? "Robot" + (int) (65535 * Math.random()) : robotName), robotBlockPos,
							player.dimension, true));
					getStage().close();
				}));
		if (RobotMod.currentRobot != null) {
			panel.registerComponent(new Button((int) (panel.getWidth() * .55), (int) (panel.getHeight() * .6),
					(int) (panel.getWidth() * .25), 20, "Deactivate").setClickListener(btn -> {
						BlockPos pos = RobotMod.currentRobot.getPosition();
						NetworkManager.sendToServer(new MessageActivateRobot(player.getName(), pos,
								RobotMod.currentRobot.dimension, false));
						RobotMod.currentRobot.setDead();
						RobotMod.currentRobot = null;
						Minecraft.getMinecraft().setIngameFocus();
					}));
		}

		panel.registerComponent(new Picture(0, 0, panel.getWidth(), panel.getHeight(),
				new ResourceLocation("dyn", "textures/gui/background.png")));

		// this is my lazy way of not having to rearrange things
		panel.reverseComponents();

		registerComponent(panel);
	}
}
