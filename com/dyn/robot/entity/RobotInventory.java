package com.dyn.robot.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

public class RobotInventory implements IInventory {

	private ItemStack[] theInventory = new ItemStack[36];
	private EntityRobot robot;

	RobotInventory(EntityRobot robot) {
		this.robot = robot;
	}

	@Override
	public void clear() {
		for (int i = 0; i < theInventory.length; ++i) {
			theInventory[i] = null;
		}
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		if (theInventory[index] != null) {
			if (index == 2) {
				ItemStack itemstack2 = theInventory[index];
				theInventory[index] = null;
				return itemstack2;
			} else if (theInventory[index].stackSize <= count) {
				ItemStack itemstack1 = theInventory[index];
				theInventory[index] = null;

				return itemstack1;
			} else {
				ItemStack itemstack = theInventory[index].splitStack(count);

				if (theInventory[index].stackSize == 0) {
					theInventory[index] = null;
				}

				return itemstack;
			}
		} else {
			return null;
		}
	}

	@Override
	public IChatComponent getDisplayName() {
		return new ChatComponentTranslation(getName(), new Object[0]);
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public String getName() {
		return "robot.inventory";
	}

	@Override
	public int getSizeInventory() {
		return theInventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		if (index < getSizeInventory()) {
			return theInventory[index];
		}
		return null;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return robot.isOwner(player);
	}

	@Override
	public void markDirty() {
		// TODO Auto-generated method stub

	}

	@Override
	public void openInventory(EntityPlayer player) {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		if (theInventory[index] != null) {
			ItemStack itemstack = theInventory[index];
			theInventory[index] = null;
			return itemstack;
		} else {
			return null;
		}
	}

	@Override
	public void setField(int id, int value) {
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		theInventory[index] = stack;

		if ((stack != null) && (stack.stackSize > getInventoryStackLimit())) {
			stack.stackSize = getInventoryStackLimit();
		}
	}

}
