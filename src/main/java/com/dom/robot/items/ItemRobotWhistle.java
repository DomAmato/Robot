package com.dom.robot.items;

import java.util.List;

import com.dom.robot.RobotMod;
import com.dom.robot.entity.EntityRobot;
import com.dom.robot.reference.Reference;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class ItemRobotWhistle extends Item {

	public ItemRobotWhistle() {
		setMaxStackSize(1);
		setMaxDamage(0);
		setUnlocalizedName("robot_whistle");
		setCreativeTab(RobotMod.roboTab);
		setRegistryName(Reference.MOD_ID, getUnlocalizedName());
	}

	public List<EntityRobot> getEntitiesInRadius(World world, double x, double y, double z, int radius) {
		List<EntityRobot> list = world.getEntitiesWithinAABB(EntityRobot.class,
				new AxisAlignedBB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius));
		return list;
	}

	/**
	 * returns the action that specifies what animation to play when the items is
	 * being used
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
		return 18;
	}

	/**
	 * Checks isDamagable and if it cannot be stacked
	 */
	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	/**
	 * Called whenever this item is equipped and the right mouse button is pressed.
	 * Args: itemStack, world, entityPlayer
	 */
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		// worldIn.playSound(playerIn, "dynrobot:robo.whistle", 1, 1);
		playerIn.setActiveHand(handIn);
		return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase playerIn) {
		if (!worldIn.isRemote) {
			for (EntityRobot robot : getEntitiesInRadius(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ, 32)) {
				if (robot.getOwner() == playerIn) {
					// if (robot.getIsFollowing()) {
					// robot.getNavigator().clearPathEntity();
					// }
					// robot.setIsFollowing(!robot.getIsFollowing());
					robot.setIsFollowing(true);
				}
			}
		}
		return stack;
	}
}
