package com.dom.robot;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class RoboTab extends CreativeTabs {

	public RoboTab() {
		super("robotab");
	}

	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(RobotMod.robot_remote);
	}

}
