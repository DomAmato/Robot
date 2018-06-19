package com.dyn.robot.items.equipment;

import com.dyn.robot.RobotMod;
import com.dyn.robot.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemSIMCard extends Item {

	public ItemSIMCard() {
		super();
		setHasSubtypes(true);
		setMaxStackSize(1);
		setUnlocalizedName("robot_sim");
		setRegistryName(Reference.MOD_ID, "robot_sim");
		setCreativeTab(RobotMod.roboTab);
	}

	/**
	 * Allow or forbid the specific book/item combination as an anvil enchant
	 *
	 * @param stack
	 *            The item
	 * @param book
	 *            The book
	 * @return if the enchantment is allowed
	 */
	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return false;
	}

	/**
	 * Checks isDamagable and if it cannot be stacked
	 */
	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
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
