package com.dyn.robot.items;

import java.util.List;

import com.dyn.robot.entity.DynRobotEntity;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemDynRobotSpawner extends Item {

	public ItemDynRobotSpawner() {
		setHasSubtypes(true);
	}

	/**
	 * returns a list of items with the same ID, but different meta (eg: dye
	 * returns 16 items)
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		subItems.add(new ItemStack(itemIn, 1, 0));
		subItems.add(new ItemStack(itemIn, 1, 1));
	}

	/**
	 * Checks isDamagable and if it cannot be stacked
	 */
	@Override
	public boolean isItemTool(ItemStack stack) {
		return false;
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
		if (is.getMetadata() == 0) {
			robot.setTamable(true);
		}
		if (robot != null) {
			return true;
		}
		return false;
	}
}
