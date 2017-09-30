package com.dom.robot.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemSimpleRobotBlock extends ItemBlock {

	public ItemSimpleRobotBlock(Block block) {
		super(block);
	}

	/**
	 * Checks isDamagable and if it cannot be stacked
	 */
	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}
}
