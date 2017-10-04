package com.dyn.robot.items;

import com.dyn.robot.RobotMod;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class RoboTab extends CreativeTabs {

	public RoboTab() {
		super("robotab");
	}

	@Override
	public Item getTabIconItem() {
		return RobotMod.dynRobotRemote;
	}

}
