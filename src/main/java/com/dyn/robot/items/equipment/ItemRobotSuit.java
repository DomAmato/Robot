package com.dyn.robot.items.equipment;

import com.dyn.robot.RobotMod;
import com.dyn.robot.reference.Reference;
import com.dyn.robot.utils.EnchantmentUtils;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRobotSuit extends Item {

	public ItemRobotSuit() {
		super();
		setHasSubtypes(true);
		setMaxStackSize(1);
		setUnlocalizedName("robot_suit");
		setRegistryName(Reference.MOD_ID, "robot_suit");
		setCreativeTab(RobotMod.roboTab);
	}

	@Override
	public int getMetadata(int damage) {
		return damage;
	}

	// add a subitem for each item we want to appear in the creative tab
	// in this case - a full bottle of each colour
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (isInCreativeTab(tab)) {
			super.getSubItems(tab, items);
			for (int i = 1; i < 3; i++) {
				items.add(new ItemStack(this, 1, i));
			}
		}
	}

	// Make a unique name for each contents type (lime, orange, etc) so we can
	// name them individually
	// The fullness information is added separately in getItemStackDisplayName()
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "_" + stack.getMetadata();
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
		if ((stack.getItemDamage() == 0)
				&& EnchantmentUtils.hasEnchant(Enchantments.PROTECTION, ItemEnchantedBook.getEnchantments(book))) {
			return true;
		}
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
