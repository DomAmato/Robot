package com.dyn.robot.proxy;

import com.dyn.robot.blocks.BlockDynRobot;
import com.dyn.robot.entity.EntityRobot;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public interface Proxy {
	public void createNewProgrammingInterface(EntityRobot robot);

	public String getProgrammingInterfaceText();

	public void handleErrorMessage(String error, String code, int line);

	public void init();

	public void openRemoteInterface(EntityRobot robot);

	public void openRemoteInterface(World world, BlockDynRobot robot, BlockPos pos);

	public void openRobotGui();

	public void openRobotProgrammingWindow();

	public void openRobotProgrammingWindow(EntityRobot robot);

	public void preInit();

	public void registerBlockItem(Block block);

	public void registerItem(Item item, String name, int meta);

	public void toggleRenderRobotProgramInterface(boolean state);
}