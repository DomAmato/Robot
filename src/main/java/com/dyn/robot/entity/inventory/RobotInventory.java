package com.dyn.robot.entity.inventory;

import com.dyn.robot.entity.EntityRobot;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class RobotInventory extends InventoryBasic {

	public RobotInventory(String name, int slots, EntityRobot robot) {
		super(name, true, slots);
	}

	public ItemStack addItemStackToInventory(ItemStack is) {
		if ((is == null) || (is.isEmpty())) {
			return null;
		}

		for (int a = 14; a < getSizeInventory(); a++) {
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

	public boolean canAddToInventory(ItemStack is) {
		if ((is == null) || (is.isEmpty())) {
			return false;
		}

		int addAmount = is.getCount();

		for (int a = 14; a < getSizeInventory(); a++) {
			if ((getStackInSlot(a) == null) || (getStackInSlot(a).isEmpty())) {
				return true;
			}
			ItemStack is2 = getStackInSlot(a);
			if ((is2.getItem() == is.getItem()) && (is2.getItemDamage() == is.getItemDamage())) {
				addAmount -= Math.min(is.getCount(), is2.getMaxStackSize() - is2.getCount());
			}
			if (addAmount <= 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This only checks the item not for specific variants
	 * 
	 * @param item
	 * @return true if item exists in inventory
	 */
	public boolean containsItem(Item item) {
		for (int i = 0; i < getSizeInventory(); i++) {
			ItemStack is2 = getStackInSlot(i);
			if (!is2.isEmpty()) {
				if (is2.getItem() == item) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean containsItem(ItemStack is) {
		if (!is.isEmpty()) {
			for (int i = 0; i < getSizeInventory(); i++) {
				ItemStack is2 = getStackInSlot(i);
				if (!is2.isEmpty()) {
					if ((is2.getItem() == is.getItem())
							&& ((is2.getItemDamage() == is.getItemDamage()) || is.isItemStackDamageable())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean containsItemType(Class itemType) {
		for (int i = 0; i < getSizeInventory(); i++) {
			ItemStack is2 = getStackInSlot(i);
			if (!is2.isEmpty()) {
				if (is2.getItem().getClass().isAssignableFrom(itemType)) {
					return true;
				}
			}
		}
		return false;
	}

	public int getOpenExpansionSlot() {
		for (int a = 5; a < 14; a++) {
			if ((getStackInSlot(a).isEmpty())) {
				return a;
			}
		}
		return 13;
	}

	public int getQuantityOfItem(ItemStack is) {
		int total = 0;
		for (int i = 0; i < getSizeInventory(); i++) {
			ItemStack is2 = getStackInSlot(i);
			if (!is2.isEmpty()) {
				if (is2.getItem() == is.getItem()) {
					total += is2.getCount();
				}
			}
		}
		return total;
	}

	public ItemStack getSDCard() {
		return getStackInSlot(0);
	}

	public ItemStack getStackOfItem(ItemStack is) {
		if (!is.isEmpty()) {
			for (int i = 0; i < getSizeInventory(); i++) {
				ItemStack is2 = getStackInSlot(i);
				if (!is2.isEmpty()) {
					if ((is2.getItem() == is.getItem())
							&& ((is2.getItemDamage() == is.getItemDamage()) || is.isItemStackDamageable())) {
						return is2;
					}
				}
			}
		}
		return null;
	}

	public boolean hasExpansionChip(ItemStack chip) {
		if (!chip.isEmpty()) {
			for (int i = 5; i < 14; i++) {
				ItemStack is2 = getStackInSlot(i);
				if (!is2.isEmpty()) {
					if ((is2.getItem() == chip.getItem()) && ((is2.getItemDamage() == chip.getItemDamage()))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean hasSDCard() {
		return !(getStackInSlot(0).isEmpty());
	}

	public boolean isInventoryEmpty() {
		for (int a = 14; a < getSizeInventory(); a++) {
			if (!getStackInSlot(a).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public boolean isInventoryFull() {
		for (int a = 14; a < getSizeInventory(); a++) {
			if (getStackInSlot(a).isEmpty()) {
				removeStackFromSlot(a);
			}
			if (getStackInSlot(a).isEmpty()) {
				return false;
			}
		}

		return true;
	}

	public boolean removeItemFromInventory(ItemStack is, int amount) {
		for (int i = 0; i < getSizeInventory(); i++) {
			ItemStack is2 = getStackInSlot(i);
			if (!is2.isEmpty()) {
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

	public boolean removeItemTypeFromInventory(Item item, int amount) {
		for (int i = 0; i < getSizeInventory(); i++) {
			ItemStack is2 = getStackInSlot(i);
			if (!is2.isEmpty()) {
				if (is2.getItem() == item) {
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
