package com.dyn.robot.proxy;

import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.gui.RobotProgrammingInterface;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public interface Proxy {
	public void createNewProgrammingInterface(EntityRobot robot);

	public RobotProgrammingInterface getProgrammingInterface();

	public void init();

	public void openRobotGui();

	public void openRobotInterface();

	public void openRobotProgrammingWindow(EntityRobot robot);

	public void preInit();

	public void registerBlockItem(Block block);

	public void registerItem(Item item, String name, int meta);

	public void toggleRenderRobotProgramInterface(boolean state);
}