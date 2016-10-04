package com.dyn.robot.proxy;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public interface Proxy {
	public void init();

	public void openRobotGui();

	public void openRobotProgrammingWindow(World world, BlockPos pos, Entity entity);

	public void preInit();

	public void registerBlockItem(Block block);

	public void registerItem(Item item, String name, int meta);

	public void renderGUI();
}