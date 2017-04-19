package com.dyn.robot.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemDynRobotBlock extends ItemBlock {

	public ItemDynRobotBlock(Block block) {
		super(block);
	}

	/**
	 * Checks isDamagable and if it cannot be stacked
	 */
	@Override
	public boolean isItemTool(ItemStack stack) {
		return false;
	}
}
