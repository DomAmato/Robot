package com.dyn.robot.gui;

import com.dyn.robot.entity.EntityRobot;
import com.rabbit.gui.show.Show;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class RemoteInterface extends Show {
	protected World world;
	protected final EntityRobot robot;
	EntityPlayer player;

	protected RemoteInterface(EntityPlayer player, World world, EntityRobot robot) {
		this.world = world;
		this.robot = robot;
		this.player = player;
	}
}
