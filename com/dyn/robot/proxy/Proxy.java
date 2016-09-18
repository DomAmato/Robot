package com.dyn.robot.proxy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public interface Proxy {
	public void init();

	public void openRobotGui();

	public void preInit();

	public void registerBlockItem(Block block);

	public void registerItem(Item item, String name, int meta);

	public void renderGUI();
}