package com.dyn.robot.items;

import com.dyn.robot.entity.DynRobotEntity;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemDynRobotSpawner extends Item{

	public ItemDynRobotSpawner() {
	}

	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer p, World worldIn, BlockPos pos, EnumFacing side, float hitX,
			float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return super.onItemUse(is, p, worldIn, pos, side, hitX, hitY, hitZ);
		}

		DynRobotEntity robot = (DynRobotEntity) ItemMonsterPlacer.spawnCreature(worldIn,
				EntityList.classToStringMapping.get(DynRobotEntity.class), pos.getX() + 0.5, pos.getY() + 1.3,
				pos.getZ() + 0.5);
		if (robot != null) {
			return true;
		}
		return false;
	}

}
