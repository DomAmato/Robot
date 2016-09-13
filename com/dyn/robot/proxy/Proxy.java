package com.dyn.robot.proxy;

import net.minecraft.item.Item;

public interface Proxy {
	public void init();

	public void openRobotGui();

	public void renderGUI();
	
	public void registerItem(Item item, String name, int meta);
}