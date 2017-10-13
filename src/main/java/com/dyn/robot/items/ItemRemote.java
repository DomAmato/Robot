package com.dyn.robot.items;

import java.util.List;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.gui.RobotGuiHandler;
import com.dyn.robot.reference.Reference;

import net.minecraft.entity.Entity;
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

public class ItemRemote extends Item {

	public ItemRemote() {
		super();
		setMaxStackSize(1);
		setUnlocalizedName("robot_remote");
		setCreativeTab(RobotMod.roboTab);
		setRegistryName(Reference.MOD_ID, "robot_remote");
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
		return EnumAction.BOW;
	}

	/**
	 * Checks isDamagable and if it cannot be stacked
	 */
	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	/**
	 * Returns true if the item can be used on the given entity, e.g. shears on
	 * sheep.
	 */
	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target,
			EnumHand hand) {
		if (target instanceof EntityRobot) {
			return true;
		}
		return false;
	}

	/**
	 * Called whenever this item is equipped and the right mouse button is pressed.
	 * Args: itemStack, world, entityPlayer
	 */
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if (!worldIn.isRemote) {
			if ((RobotMod.currentRobot != null) && !RobotMod.currentRobot.isDead
					&& (playerIn.getPosition().distanceSq(RobotMod.currentRobot.getPosition()) < (64 * 64))) {
				RobotMod.logger.info("Opening current robot window");
				playerIn.openGui(RobotMod.instance, RobotGuiHandler.getActivationGuiID(), playerIn.world,
						(int) RobotMod.currentRobot.posX, (int) RobotMod.currentRobot.posY,
						(int) RobotMod.currentRobot.posZ);
			} else {
				RobotMod.logger.info("Searching for closest robot");
				playerIn.openGui(RobotMod.instance, RobotGuiHandler.getSearchingGuiID(), playerIn.world,
						(int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
			}

		}
		return new ActionResult<>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
	}

	/**
	 * Called when the player Left Clicks (attacks) an entity. Processed before
	 * damage is done, if return value is true further processing is canceled and
	 * the entity is not attacked.
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
