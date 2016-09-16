package com.dyn.robot.items;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;

public class ItemDynRobotSpawner extends ItemBlock {

	public ItemDynRobotSpawner(Block block) {
		super(block);
		setCreativeTab(CreativeTabs.tabRedstone);
	}

	// @Override
	// public boolean onItemUse(ItemStack is, EntityPlayer p, World worldIn,
	// BlockPos pos, EnumFacing side, float hitX,
	// float hitY, float hitZ) {
	// if (worldIn.isRemote) {
	// return super.onItemUse(is, p, worldIn, pos, side, hitX, hitY, hitZ);
	// }
	//
	// DynRobotEntity robot = (DynRobotEntity)
	// ItemMonsterPlacer.spawnCreature(worldIn,
	// EntityList.classToStringMapping.get(DynRobotEntity.class), pos.getX() +
	// 0.5, pos.getY() + 1.3, pos.getZ() + 0.5);
	// if (robot != null) {
	// robot.setTamer(p);
	// return true;
	// }
	// return false;
	// }

}
