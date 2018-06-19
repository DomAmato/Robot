package com.dyn.robot.entity.inventory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class RobotInventory extends InventoryBasic {

	public static final int SDCARD_SLOT = 0;
	public static final int RAM_SLOT = 1;
	public static final int EQUIP_SLOT = 2;
	public static final int SIM_SLOT = 3;
	public static final int METER_SLOT = 4;
	public static final int SUIT_SLOT = 5;
	public static final int START_EXPANSION_SLOT = 6;
	public static final int END_EXPANSION_SLOT = 14;
	public static final int START_INVENTORY = 15;

	public RobotInventory(String name, int slots) {
		super(name, true, slots);
	}

	public ItemStack addItemStackToInventory(ItemStack is) {
		if ((is == ItemStack.EMPTY) || (is.isEmpty())) {
			return ItemStack.EMPTY;
		}

		ItemStack itemstack = is.copy();

		if (containsItem(itemstack)) {
			for (int i = RobotInventory.START_INVENTORY; i < getSizeInventory(); ++i) {
				ItemStack itemstack1 = getStackInSlot(i);

				if (ItemStack.areItemsEqual(itemstack1, itemstack)) {
					int j = Math.min(getInventoryStackLimit(), itemstack1.getMaxStackSize());
					int k = Math.min(itemstack.getCount(), j - itemstack1.getCount());

					if (k > 0) {
						itemstack1.grow(k);
						itemstack.shrink(k);

						if (itemstack.isEmpty()) {
							markDirty();
							return ItemStack.EMPTY;
						}
					}
				}
			}
		}

		for (int i = RobotInventory.START_INVENTORY; i < getSizeInventory(); i++) {
			if ((getStackInSlot(i) == ItemStack.EMPTY) || (getStackInSlot(i).isEmpty())) {
				setInventorySlotContents(i, itemstack);
				markDirty();
				return ItemStack.EMPTY;
			}
		}

		if (itemstack.getCount() != is.getCount()) {
			markDirty();
		}

		return itemstack;
	}

	public boolean canAddToInventory(ItemStack is) {
		if ((is == ItemStack.EMPTY) || (is.isEmpty())) {
			return false;
		}

		int addAmount = is.getCount();

		for (int i = RobotInventory.START_INVENTORY; i < getSizeInventory(); i++) {
			if ((getStackInSlot(i) == ItemStack.EMPTY) || (getStackInSlot(i).isEmpty())) {
				return true;
			}
			ItemStack is2 = getStackInSlot(i);
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
		for (int a = RobotInventory.START_EXPANSION_SLOT; a <= RobotInventory.END_EXPANSION_SLOT; a++) {
			if ((getStackInSlot(a).isEmpty())) {
				return a;
			}
		}
		return RobotInventory.END_EXPANSION_SLOT;
	}

	public int getOpenInventorySlot() {
		for (int a = RobotInventory.START_INVENTORY; a < getSizeInventory(); a++) {
			if ((getStackInSlot(a).isEmpty())) {
				return a;
			}
		}
		return getSizeInventory() - 1;
	}

	public int getQuantityOfItem(Item item) {
		int total = 0;
		for (int i = 0; i < getSizeInventory(); i++) {
			ItemStack is2 = getStackInSlot(i);
			if (!is2.isEmpty()) {
				if (is2.getItem() == item) {
					total += is2.getCount();
				}
			}
		}
		return total;
	}

	public int getQuantityOfItem(ItemStack is) {
		int total = 0;
		for (int i = 0; i < getSizeInventory(); i++) {
			ItemStack is2 = getStackInSlot(i);
			if (!is2.isEmpty()) {
				if (ItemStack.areItemsEqual(is, is2)) {
					total += is2.getCount();
				}
			}
		}
		return total;
	}

	public ItemStack getSDCard() {
		return getStackInSlot(RobotInventory.SDCARD_SLOT);
	}

	public ItemStack getStackOfItem(Item item) {
		if (item != null) {
			for (int i = 0; i < getSizeInventory(); i++) {
				ItemStack is2 = getStackInSlot(i);
				if (!is2.isEmpty()) {
					if (is2.getItem() == item) {
						return is2;
					}
				}
			}
		}
		return null;
	}

	public ItemStack getStackOfItem(ItemStack is) {
		if (!is.isEmpty()) {
			for (int i = 0; i < getSizeInventory(); i++) {
				ItemStack is2 = getStackInSlot(i);
				if (!is2.isEmpty()) {
					if (ItemStack.areItemsEqual(is, is2)) {
						return is2;
					}
				}
			}
		}
		return null;
	}

	public ItemStack getSuit() {
		return getStackInSlot(RobotInventory.SUIT_SLOT);
	}

	public boolean hasExpansionChip(ItemStack chip) {
		if (!chip.isEmpty()) {
			for (int i = 5; i <= RobotInventory.END_EXPANSION_SLOT; i++) {
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
		return !(getStackInSlot(RobotInventory.SDCARD_SLOT).isEmpty());
	}

	public boolean hasSuit() {
		return getStackInSlot(RobotInventory.SUIT_SLOT) != ItemStack.EMPTY;
	}

	public boolean isExpansionSlotsFull() {
		for (int a = RobotInventory.START_EXPANSION_SLOT; a <= RobotInventory.END_EXPANSION_SLOT; a++) {
			if (getStackInSlot(a).isEmpty()) {
				removeStackFromSlot(a);
			}
			if (getStackInSlot(a).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public boolean isInventoryEmpty() {
		for (int a = RobotInventory.START_INVENTORY; a < getSizeInventory(); a++) {
			if (!getStackInSlot(a).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public boolean isInventoryFull() {
		for (int a = RobotInventory.START_INVENTORY; a < getSizeInventory(); a++) {
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

	public List<ItemStack> removeItemTypeFromInventory(Item item, int amount) {
		List<ItemStack> retList = new ArrayList();
		for (int i = 0; i < getSizeInventory(); i++) {
			ItemStack is2 = getStackInSlot(i);
			if (!is2.isEmpty()) {
				if (is2.getItem() == item) {
					int prevAmt = amount;
					amount -= decrStackSize(i, amount).getCount();
					retList.add(
							new ItemStack(is2.getItem(), amount <= 0 ? prevAmt : prevAmt - amount, is2.getMetadata()));
					if (amount <= 0) {
						return retList;
					}
				}
			}
		}
		return retList;
	}
}
