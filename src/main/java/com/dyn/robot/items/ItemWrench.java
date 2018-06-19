package com.dyn.robot.items;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.reference.Reference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemWrench extends Item {

	EntityRobot entity;

	public ItemWrench() {
		super();
		setMaxStackSize(1);
		setUnlocalizedName("robot_wrench");
		setRegistryName(Reference.MOD_ID, "robot_wrench");
		setCreativeTab(RobotMod.roboTab);
	}

	public EntityRobot getEntity() {
		return entity;
	}

	/**
	 * returns the action that specifies what animation to play when the items is
	 * being used
	 */
	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BOW;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 15;
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

	// called when the player starts holding right click;
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if ((entity != null) && !entity.isDead) {
			playerIn.setActiveHand(handIn);
		}
		return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
	}

	/**
	 * Called when the player finishes using this Item (E.g. finishes eating.). Not
	 * called when the player stops using the Item before the action is complete.
	 */
	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase playerIn) {
		if (!worldIn.isRemote) {
			if ((entity != null) && (entity instanceof EntityRobot)) {
				EntityRobot robot = (EntityRobot) worldIn.getEntityByID(entity.getEntityId());
				if (robot != null) {
					ItemStack robotStack = new ItemStack(RobotMod.robot_block, 1);
					robotStack.setTagCompound(robot.getNBTforItemStack());
					robotStack.setStackDisplayName(robot.getRobotName());

					robot.setDead();
					worldIn.spawnEntity(new EntityItem(worldIn, robot.posX, robot.posY + 0.3, robot.posZ, robotStack));
				}
			}
		}
		entity = null;
		return stack;
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

	/**
	 * Called when the player stops using an Item (stops holding the right mouse
	 * button).
	 */
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase playerIn, int timeLeft) {
		if (timeLeft > 0) {
			entity = null;
		}
	}

	public void setEntity(EntityRobot entity) {
		if ((entity != null) && !entity.isDead) {
			this.entity = entity;
		}
	}
}
