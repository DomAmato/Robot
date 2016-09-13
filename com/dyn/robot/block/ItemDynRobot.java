package com.dyn.robot.block;

import java.util.List;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.turtle.items.ItemTurtleNormal;
import dan200.computercraft.shared.turtle.items.TurtleItemFactory;
import dan200.computercraft.shared.util.Colour;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ItemDynRobot extends ItemTurtleNormal {
	public ItemDynRobot(Block block) {
		super(block);
		setUnlocalizedName("dyn_robot");
	}

	@Override
	public ComputerFamily getFamily(int damage) {
		return ComputerFamily.Normal;
	}

	@Override
	public void getSubItems(Item itemID, CreativeTabs tabs, List list) {
		list.add(TurtleItemFactory.create(-1, (String) null, (Colour) null, ComputerFamily.Normal,
				(ITurtleUpgrade) null, (ITurtleUpgrade) null, 0, (ResourceLocation) null));
	}
}
