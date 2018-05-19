package com.dyn.robot.gui;

import java.awt.Color;

import com.dyn.robot.RobotMod;
import com.dyn.robot.blocks.BlockRobot;
import com.dyn.robot.blocks.RobotBlockTileEntity;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.network.NetworkManager;
import com.dyn.robot.network.messages.MessageActivateRobot;
import com.dyn.robot.network.messages.MessageClaimRobot;
import com.rabbit.gui.component.control.Button;
import com.rabbit.gui.component.control.TextBox;
import com.rabbit.gui.component.display.Panel;
import com.rabbit.gui.component.display.Picture;
import com.rabbit.gui.component.display.TextLabel;
import com.rabbit.gui.show.Show;
import com.rabbit.gui.utils.DefaultTextures;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class ActivationScreen extends Show {
	private BlockPos robotBlockPos;
	private EntityPlayer player;
	private String robotName;
	EntityRobot entityRobot;

	public ActivationScreen(BlockRobot robot, EntityPlayer player, BlockPos pos) {
		robotBlockPos = pos;
		this.player = player;
		RobotBlockTileEntity tile = (RobotBlockTileEntity) player.world.getTileEntity(pos);
		robotName = tile.getRobotName();
		if (robotName.isEmpty()) {
			robotName = "Robot" + (int) (65535 * Math.random());
		}
	}

	public ActivationScreen(EntityRobot entityRobot, EntityPlayer player) {
		this.entityRobot = entityRobot;
		this.player = player;
		robotName = entityRobot.getRobotName();
		if (robotName.isEmpty()) {
			robotName = "Robot" + (int) (65535 * Math.random());
		}
	}

	@Override
	public void setup() {
		Panel panel = new Panel((int) (width * .2), (int) (height * .2), (int) (width * .6), (int) (height * .6))
				.setVisible(true).setFocused(true);

		panel.registerComponent(new TextLabel((int) (panel.getWidth() * .05), (int) (panel.getHeight() * .05),
				(int) (panel.getWidth() * .8), 20, Color.black, "Robot Remote"));

		panel.registerComponent(new TextLabel((int) (panel.getWidth() * .1), (int) (panel.getHeight() * .2),
				(int) (panel.getWidth() * .8), 20, Color.black, "Give Robot a Name:"));

		panel.registerComponent(new TextBox((int) (panel.getWidth() * .1), (int) (panel.getHeight() * .3),
				(int) (panel.getWidth() * .8), 25).setText(robotName)
						.setTextChangedListener((TextBox textbox, String previousText) -> {
							robotName = previousText;
						}));

		panel.registerComponent(new Button((int) (panel.getWidth() * .6), (int) (panel.getHeight() * .6),
				(int) (panel.getWidth() * .25), 20, "Activate").setClickListener(btn -> {
					if ((entityRobot != null) && !entityRobot.isDead) {
						RobotMod.currentRobots.add(entityRobot);
						NetworkManager.sendToServer(new MessageClaimRobot(
								(((robotName == null) || robotName.isEmpty()) ? "Robot" + (int) (65535 * Math.random())
										: robotName),
								entityRobot.getEntityId()));
					} else {
						NetworkManager.sendToServer(new MessageActivateRobot(
								(robotName.isEmpty() ? "Robot" + (int) (65535 * Math.random()) : robotName),
								robotBlockPos, player.dimension));
					}
					getStage().close();
				}));

		panel.registerComponent(new Picture(0, 0, panel.getWidth(), panel.getHeight(), DefaultTextures.BACKGROUND1));

		// this is my lazy way of not having to rearrange things
		panel.reverseComponents();

		registerComponent(panel);
	}
}
