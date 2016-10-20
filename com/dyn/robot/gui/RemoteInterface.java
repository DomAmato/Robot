package com.dyn.robot.gui;

import java.awt.Color;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.BlockDynRobot;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.server.network.NetworkDispatcher;
import com.dyn.server.network.packets.server.ActivateRobotMessage;
import com.forgeessentials.chat.Censor;
import com.rabbit.gui.component.control.Button;
import com.rabbit.gui.component.control.TextBox;
import com.rabbit.gui.component.control.ToggleButton;
import com.rabbit.gui.component.display.Picture;
import com.rabbit.gui.component.display.TextLabel;
import com.rabbit.gui.show.Show;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;

public class RemoteInterface extends Show {
	private BlockDynRobot robotBlock;
	private BlockPos robotBlockPos;
	private EntityPlayer player;
	private String robotName;
	private EntityRobot robot;

	public RemoteInterface(BlockDynRobot robot, EntityPlayer player, BlockPos pos) {
		robotBlock = robot;
		robotBlockPos = pos;
		this.player = player;
		robotName = "";
	}

	public RemoteInterface(EntityRobot robot, EntityPlayer player) {
		this.player = player;
		this.robot = robot;
	}

	@Override
	public void setup() {
		registerComponent(new TextLabel((int) (width * .3), (int) (height * .3), (int) (width * .4),
				(int) (height * .4), Color.black, "Robot Remote"));

		if (RobotMod.currentRobot != null) {
			registerComponent(new TextLabel((int) (width * .3), (int) (height * .4), (int) (width * .4), height / 2,
					Color.black,
					String.format("Your Current Robot:\n\nRobot Name: %s\nLocation: %s",
							RobotMod.currentRobot.getCustomNameTag(), RobotMod.currentRobot.getPosition().toString()))
									.setMultilined(true));
			registerComponent(new ToggleButton((int) (width * .5), (int) (height * .4), (int) (width * .25), 20, "is Following", false));
		} else {
			registerComponent(
					new TextBox((int) (width * .3), (int) (height * .4), (int) (width * .4), 25, "Give Robot a Name")
							.setTextChangedListener((TextBox textbox, String previousText) -> {
								robotName = previousText;
							}));
		}

		if (robotBlock != null) {
			registerComponent(new Button((int) (width * .3), (int) (height * .6), (int) (width * .175), 20, "Activate")
					.setClickListener(btn -> {
						if (RobotMod.currentRobot != null) {
							BlockPos pos = RobotMod.currentRobot.getPosition();
							NetworkDispatcher.sendToServer(new ActivateRobotMessage(player.getName(), pos,
									RobotMod.currentRobot.dimension, false));
							RobotMod.currentRobot.setDead();
							RobotMod.currentRobot = null;
						}
						NetworkDispatcher
								.sendToServer(
										new ActivateRobotMessage(
												(robotName.isEmpty() ? "Robot" + (int) (65535 * Math.random())
														: Censor.filter(robotName)),
												robotBlockPos, player.dimension, true));
						getStage().close();
					}));
		}

		if (RobotMod.currentRobot != null) {
			registerComponent(
					new Button((int) (width * .525), (int) (height * .6), (int) (width * .175), 20, "Deactivate")
							.setClickListener(btn -> {
								BlockPos pos = RobotMod.currentRobot.getPosition();
								NetworkDispatcher.sendToServer(new ActivateRobotMessage(player.getName(), pos,
										RobotMod.currentRobot.dimension, false));
								RobotMod.currentRobot.setDead();
								RobotMod.currentRobot = null;
								getStage().close();
							}));
		}

		registerComponent(new Picture((int) (width * .25), (int) (height * .25), (int) (width * .5), height / 2,
				new ResourceLocation("dyn", "textures/gui/background.png")));
	}
}
