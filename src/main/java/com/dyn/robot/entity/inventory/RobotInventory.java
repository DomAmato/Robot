package com.dyn.robot.entity.inventory;

import com.dyn.robot.entity.EntityRobot;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

public class RobotInventory extends InventoryBasic {

	public RobotInventory(String name, int slots, EntityRobot robot) {
		super(name, true, slots);
	}

	public ItemStack addItemStackToInventory(ItemStack is) {
		if ((is == null) || (is.isEmpty())) {
			return null;
		}

		for (int a = 12; a < getSizeInventory(); a++) {
			if ((getStackInSlot(a) == null) || (getStackInSlot(a).isEmpty())) {
				setInventorySlotContents(a, is);
				return null;
			}
			ItemStack is2 = getStackInSlot(a);
			if ((is2.getItem() == is.getItem()) && (is2.getItemDamage() == is.getItemDamage())) {
				int amount = Math.min(is.getCount(), is2.getMaxStackSize() - is2.getCount());
				is.shrink(amount);
				is2.grow(amount);
				setInventorySlotContents(a, is2);
			}
			if (is.isEmpty()) {
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
			if ((getStackInSlot(a) == null) || (getStackInSlot(a).isEmpty())) {
				return a;
			}
		}
		return 11;
	}

	public int getQuantityOfItem(ItemStack is) {
		int total = 0;
		for (int i = 0; i < getSizeInventory(); i++) {
			ItemStack is2 = getStackInSlot(i);
			if (is2 != null) {
				if (is2.getItem() == is.getItem()) {
					total += is2.getCount();
				}
			}
		}
		return total;
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
			if ((getStackInSlot(a) != null) && (getStackInSlot(a).isEmpty())) {
				removeStackFromSlot(a);
			}
			if ((getStackInSlot(a) == null) || (getStackInSlot(a).isEmpty())) {
				return false;
			}
		}

		return true;
	}

	public boolean removeItemFromInventory(ItemStack is, int amount) {
		for (int i = 0; i < getSizeInventory(); i++) {
			ItemStack is2 = getStackInSlot(i);
			if (is2 != null) {
				if (is2.getItem() == is.getItem()) {
					amount -= decrStackSize(i, amount).getCount();
					if (amount <= 0) {
						break;
					}
				}
			}
		}
		return amount <= 0;
	}
}
