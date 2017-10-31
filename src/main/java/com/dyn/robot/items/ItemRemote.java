package com.dyn.robot.items;

import java.util.List;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.gui.RobotGuiHandler;
import com.dyn.robot.network.NetworkManager;
import com.dyn.robot.network.messages.MessageOpenRobotInventory;
import com.dyn.robot.reference.Reference;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
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
		if (worldIn.isRemote) {
			if ((RobotMod.currentRobots.size() == 1) && !RobotMod.currentRobots.get(0).isDead
					&& (playerIn.getPosition().distanceSq(RobotMod.currentRobots.get(0).getPosition()) < (64 * 64))) {
				RobotMod.logger.info("Opening current robot window");
				NetworkManager.sendToServer(new MessageOpenRobotInventory(RobotMod.currentRobots.get(0).getEntityId()));
			} else {
				RobotMod.proxy.openRemoteGui();
			}
		}
		worldIn.playSound(Minecraft.getMinecraft().player, Minecraft.getMinecraft().player.getPosition(),
				RobotMod.ROBOT_REMOTE, SoundCategory.PLAYERS, 0.2f, 1);
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
