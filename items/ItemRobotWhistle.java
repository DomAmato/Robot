package com.dyn.robot.items;

import java.util.List;

import com.dyn.robot.entity.EntityRobot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class ItemRobotWhistle extends Item {

	public ItemRobotWhistle() {
		setMaxStackSize(1);
		setMaxDamage(0);
	}

	public List<EntityRobot> getEntitiesInRadius(World world, double x, double y, double z, int radius) {
		List<EntityRobot> list = world.getEntitiesWithinAABB(EntityRobot.class,
				AxisAlignedBB.fromBounds(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius));
		return list;
	}

	/**
	 * returns the action that specifies what animation to play when the items
	 * is being used
	 */
	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.DRINK;
	}

	/**
	 * How long it takes to use or consume an item
	 */
	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 12;
	}

	/**
	 * Checks isDamagable and if it cannot be stacked
	 */
	@Override
	public boolean isItemTool(ItemStack stack) {
		return false;
	}

	/**
	 * Called whenever this item is equipped and the right mouse button is
	 * pressed. Args: itemStack, world, entityPlayer
	 */
	@Override
	public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn) {
		worldIn.playSoundAtEntity(playerIn, "random.bow", 0.5F, 0.4F / ((itemRand.nextFloat() * 0.4F) + 0.8F));
		playerIn.setItemInUse(itemStackIn, getMaxItemUseDuration(itemStackIn));
		return itemStackIn;
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityPlayer playerIn) {
		if (!worldIn.isRemote) {
			for (EntityRobot robot : getEntitiesInRadius(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ, 32)) {
				if (robot.getOwner() == playerIn) {
					if (robot.getIsFollowing()) {
						robot.getNavigator().clearPathEntity();
					}
					robot.setIsFollowing(!robot.getIsFollowing());
				}
			}
		}
		return stack;
	}
}
