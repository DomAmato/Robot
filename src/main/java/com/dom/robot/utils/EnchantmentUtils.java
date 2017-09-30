package com.dom.robot.utils;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

//taken from
//https://github.com/SneakingShadow/ShadowCore/blob/master/src/main/java/com/sneakingshadow/core/util/EnchantmentHelper.java

public class EnchantmentUtils {

	// TODO Make sure this class works properly.

	private static void checkEnchanted(NBTTagList tagList, NBTTagCompound stackTagCompound) {
		// stackTagCompound.setTag("ench", null)
		if (tagList.tagCount() == 0) {
			stackTagCompound.removeTag("ench");
		}
	}

	public static Short getEnchant(ItemStack itemStack) {
		return EnchantmentUtils.getEnchant(itemStack.getEnchantmentTagList());
	}

	public static Short getEnchant(NBTTagList tagList) {
		return tagList.getCompoundTagAt(0).getShort("id");
	}

	public static List<Short> getEnchants(ItemStack itemStack) {
		return EnchantmentUtils.getEnchants(itemStack.getEnchantmentTagList());
	}

	public static List<Short> getEnchants(NBTTagList tagList) {
		List<Short> list = new ArrayList<>();
		for (int i = tagList.tagCount() - 1; i >= 0; i--) {
			list.add(tagList.getCompoundTagAt(i).getShort("id"));
		}
		return list;
	}

	public static int getLevel(Enchantment id, ItemStack itemStack) {
		return EnchantmentUtils.getLevel(id, itemStack.getEnchantmentTagList());
	}

	public static int getLevel(Enchantment id, NBTTagList tagList) {
		return EnchantmentUtils.getLevel(Enchantment.getEnchantmentID(id), tagList);
	}

	public static int getLevel(int id, ItemStack itemStack) {
		return EnchantmentUtils.getLevel(id, itemStack.getEnchantmentTagList());
	}

	public static int getLevel(int id, NBTTagList tagList) {
		if (tagList != null) {
			for (int i = 0; i < tagList.tagCount(); i++) {
				if (tagList.getCompoundTagAt(i).getShort("id") == id) {
					return tagList.getCompoundTagAt(i).getShort("lvl");
				}
			}
		}
		return 0;
	}

	public static boolean hasEnchant(Enchantment enchantment, ItemStack itemStack) {
		return EnchantmentUtils.hasEnchant(Enchantment.getEnchantmentID(enchantment),
				itemStack.getEnchantmentTagList());
	}

	public static boolean hasEnchant(Enchantment enchantment, NBTTagList tagList) {
		return EnchantmentUtils.hasEnchant(Enchantment.getEnchantmentID(enchantment), tagList);
	}

	public static boolean hasEnchant(int id, ItemStack itemStack) {
		return EnchantmentUtils.hasEnchant(id, itemStack.getEnchantmentTagList());
	}

	public static boolean hasEnchant(int id, NBTTagList tagList) {
		if (tagList != null) {
			for (int i = 0; i < tagList.tagCount(); i++) {
				if (tagList.getCompoundTagAt(i).getShort("id") == id) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean hasEnchantOtherThan(Enchantment enchantment, ItemStack itemStack) {
		return EnchantmentUtils.hasEnchantOtherThan(Enchantment.getEnchantmentID(enchantment),
				itemStack.getEnchantmentTagList());
	}

	public static boolean hasEnchantOtherThan(Enchantment enchantment, NBTTagList tagList) {
		return EnchantmentUtils.hasEnchantOtherThan(Enchantment.getEnchantmentID(enchantment), tagList);
	}

	public static boolean hasEnchantOtherThan(int id, ItemStack itemStack) {
		return EnchantmentUtils.hasEnchantOtherThan(id, itemStack.getEnchantmentTagList());
	}

	public static boolean hasEnchantOtherThan(int id, NBTTagList tagList) {
		if (tagList != null) {
			for (int i = 0; i < tagList.tagCount(); i++) {
				if (tagList.getCompoundTagAt(i).getShort("id") != id) {
					return true;
				}
			}
		}
		return false;
	}

	public static void remove(Enchantment enchantment, ItemStack itemStack) {
		EnchantmentUtils.remove(Enchantment.getEnchantmentID(enchantment), itemStack.getEnchantmentTagList(),
				itemStack.getTagCompound());
	}

	public static void remove(Enchantment enchantment, NBTTagList tagList, NBTTagCompound stackTagCompound) {
		EnchantmentUtils.remove(Enchantment.getEnchantmentID(enchantment), tagList, stackTagCompound);
	}

	public static void remove(int id, ItemStack itemStack) {
		EnchantmentUtils.remove(id, itemStack.getEnchantmentTagList(), itemStack.getTagCompound());
	}

	public static void remove(int id, NBTTagList tagList, NBTTagCompound stackTagCompound) {

		if (tagList != null) {
			for (int i = 0; i < tagList.tagCount(); i++) {
				if (tagList.getCompoundTagAt(i).getShort("id") == id) {
					tagList.removeTag(i);
				}
			}
		}
		EnchantmentUtils.checkEnchanted(tagList, stackTagCompound);
	}

	public static void removeAll(ItemStack itemStack) {
		itemStack.getTagCompound().removeTag("ench");
	}

	public static void removeAll(NBTTagCompound stackTagCompound) {
		stackTagCompound.removeTag("ench");
	}

	public static void setLevel(Enchantment enchantment, int level, ItemStack itemStack) {
		NBTTagList tagList = itemStack.getEnchantmentTagList();

		if (tagList != null) {
			boolean booly = true;
			for (int i = 0; i < tagList.tagCount(); i++) {
				if (tagList.getCompoundTagAt(i).getShort("id") == Enchantment.getEnchantmentID(enchantment)) {
					tagList.getCompoundTagAt(i).setShort("lvl", (short) level);

					booly = false;
				}
			}
			if (booly) {
				NBTTagCompound tagCompound = new NBTTagCompound();
				tagCompound.setShort("id", (short) Enchantment.getEnchantmentID(enchantment));
				tagCompound.setShort("lvl", (short) level);
				tagList.appendTag(tagCompound);
			}
		} else {
			itemStack.addEnchantment(enchantment, level);
		}
	}

}