package com.dyn.robot.items;

import java.util.ArrayList;
import java.util.List;

import com.dyn.robot.entity.EntityRobot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class ItemRemote extends Item {

	public ItemRemote() {
		super();
	}

	public List<EntityRobot> getEntitiesInRadius(World world, double x, double y, double z, int radius) {
		List<EntityRobot> list = world.getEntitiesWithinAABB(EntityRobot.class,
				AxisAlignedBB.fromBounds(x, y, z, x + radius, y + radius, z + radius));
		return list;
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
		if (worldIn.isRemote) {
			List<EntityRobot> robotsOwned = new ArrayList<EntityRobot>();
			for (EntityRobot robot : getEntitiesInRadius(worldIn, playerIn.posX, playerIn.posY, playerIn.posZ, 32)) {
				if (robot.getOwner() == playerIn.getName()) {
					robotsOwned.add(robot);
				}
			}
			// System.out.println("opening gui");
			// //since we are on the client side we can probably use rabbit
			// instead of guihandlers
			// playerIn.openGui(RobotMod.instance, 1, worldIn, (int)
			// playerIn.posX, (int) playerIn.posY,
			// (int) playerIn.posZ);
		} else {
			// we need to create the computer and turn it on
			// createServerComputer().turnOn();
		}
		// open gui with list of robot entities
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
