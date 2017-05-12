package com.dyn.robot.items;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import com.dyn.DYNServerMod;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMemoryCard extends Item {

	public ItemMemoryCard() {
		super();
		setMaxStackSize(1);
		setHasSubtypes(true);
	}

	/**
	 * allows items to add custom lines of information to the mouseover
	 * description
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		if (stack.hasTagCompound()) {
			NBTTagCompound nbttagcompound = stack.getTagCompound();
			String s = nbttagcompound.getString("author");

			if (!StringUtils.isNullOrEmpty(s)) {
				tooltip.add(EnumChatFormatting.GRAY
						+ StatCollector.translateToLocalFormatted("book.byAuthor", new Object[] { s }));
			} else {
				tooltip.add(EnumChatFormatting.RED + "Corrputed Card");
			}
		} else {
			tooltip.add(EnumChatFormatting.GRAY + "Empty Card");
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (stack.hasTagCompound()) {
			NBTTagCompound nbttagcompound = stack.getTagCompound();
			String s = nbttagcompound.getString("title");

			if (!StringUtils.isNullOrEmpty(s)) {
				return s;
			}
		}

		return super.getItemStackDisplayName(stack);
	}

	/**
	 * returns a list of items with the same ID, but different meta (eg: dye
	 * returns 16 items)
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		subItems.add(new ItemStack(itemIn, 1, 0));
		// List<String> schematics = Lists.newArrayList();
		for (File script : DYNServerMod.scriptsLoc.listFiles((FilenameFilter) (dir, name) -> name.endsWith("py"))) {
			if (script != null) {
				NBTTagCompound tag = new NBTTagCompound();

				StringBuilder sb = new StringBuilder();

				try {
					Files.lines(script.toPath()).forEach(line -> {
						sb.append(line + "\n");
					});

					tag.setString("author", "DYN Minecraft Team");
					tag.setString("title", script.getName().replace(".py", ""));
					tag.setString("text", sb.toString());

					ItemStack is = new ItemStack(itemIn);
					is.setTagCompound(tag);

					subItems.add(is);
				} catch (IOException e) {
					DYNServerMod.logger.error("Filed parsing contents of python file", e);
				}
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		return stack.hasTagCompound();
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
	public boolean isItemTool(ItemStack stack) {
		return false;
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
