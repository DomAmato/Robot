package com.dyn.robot.items;

import java.util.List;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.gui.RobotGuiHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class ItemRemote extends Item {

	public ItemRemote() {
		super();
		setMaxStackSize(1);
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
		return EnumAction.BOW;
	}

	/**
	 * Checks isDamagable and if it cannot be stacked
	 */
	@Override
	public boolean isItemTool(ItemStack stack) {
		return false;
	}

	/**
	 * Returns true if the item can be used on the given entity, e.g. shears on
	 * sheep.
	 */
	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target) {
		if (target instanceof EntityRobot) {
			return true;
		}
		return false;
	}

	/**
	 * Called whenever this item is equipped and the right mouse button is
	 * pressed. Args: itemStack, world, entityPlayer
	 */
	@Override
	public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn) {
		if (!worldIn.isRemote) {
			if ((RobotMod.currentRobot != null) && !RobotMod.currentRobot.isDead
					&& (playerIn.getPosition().distanceSq(RobotMod.currentRobot.getPosition()) < (64 * 64))) {
				playerIn.openGui(RobotMod.instance, RobotGuiHandler.getActivationGuiID(), playerIn.worldObj,
						(int) RobotMod.currentRobot.posX, (int) RobotMod.currentRobot.posY,
						(int) RobotMod.currentRobot.posZ);
			} else {
				playerIn.openGui(RobotMod.instance, RobotGuiHandler.getSearchingGuiID(), playerIn.worldObj,
						(int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
			}

		} else {
			worldIn.playSoundAtEntity(playerIn, "dynrobot:robot.remote", 1, 1);
		}
		return itemStackIn;
	}

	/**
	 * Called when the player Left Clicks (attacks) an entity. Processed before
	 * damage is done, if return value is true further processing is canceled
	 * and the entity is not attacked.
	 *
	 * @param stack
	 *            The Item being used
	 * @param player
	 *            The player that is attacking
	 * @param entity
	 *            The entity being attacked
	 * @return True to cancel the rest of the interaction.
	 */
	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		return true;
	}
}
