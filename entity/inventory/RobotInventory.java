package com.dyn.robot.entity.inventory;

import com.dyn.robot.entity.EntityRobot;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

public class RobotInventory extends InventoryBasic {

	public RobotInventory(String name, int slots, EntityRobot robot) {
		super(name, true, slots);
	}

	public ItemStack addItemStackToInventory(ItemStack is) {
		if ((is == null) || (is.stackSize <= 0)) {
			return null;
		}

		for (int a = 12; a < getSizeInventory(); a++) {
			if ((getStackInSlot(a) == null) || (getStackInSlot(a).stackSize <= 0)) {
				setInventorySlotContents(a, is);
				return null;
			}
			ItemStack is2 = getStackInSlot(a);
			if ((is2.getItem() == is.getItem()) && (is2.getItemDamage() == is.getItemDamage())) {
				int amount = Math.min(is.stackSize, is2.getMaxStackSize() - is2.stackSize);
				is.stackSize -= amount;
				is2.stackSize += amount;
				setInventorySlotContents(a, is2);
			}
			if (is.stackSize <= 0) {
				return null;
			}
		}
		return is;
	}

	public boolean containsItem(ItemStack is) {
		if (is != null) {
			for (int i = 0; i < getSizeInventory(); i++) {
				ItemStack is2 = getStackInSlot(i);
				if (is2 != null) {
					if ((is2.getItem() == is.getItem())
							&& ((is2.getItemDamage() == is.getItemDamage()) || is.isItemStackDamageable())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public int getOpenExpansionSlot() {
		for (int a = 3; a < 12; a++) {
			if ((getStackInSlot(a) == null) || (getStackInSlot(a).stackSize == 0)) {
				return a;
			}
		}
		return 11;
	}

	public ItemStack getStackOfItem(ItemStack is) {
		if (is != null) {
			for (int i = 0; i < getSizeInventory(); i++) {
				ItemStack is2 = getStackInSlot(i);
				if (is2 != null) {
					if ((is2.getItem() == is.getItem())
							&& ((is2.getItemDamage() == is.getItemDamage()) || is.isItemStackDamageable())) {
						return is2;
					}
				}
			}
		}
		return null;
	}

	public boolean isInventoryEmpty() {
		for (int a = 12; a < getSizeInventory(); a++) {
			if (getStackInSlot(a) != null) {
				return false;
			}
		}
		return true;
	}

	public boolean isInventoryFull() {
		for (int a = 12; a < getSizeInventory(); a++) {
			if ((getStackInSlot(a) != null) && (getStackInSlot(a).stackSize == 0)) {
				removeStackFromSlot(a);
			}
			if ((getStackInSlot(a) == null) || (getStackInSlot(a).stackSize == 0)) {
				return false;
			}
		}

		return true;
	}
}
