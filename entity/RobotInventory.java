package com.dyn.robot.entity;

import java.util.Arrays;

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
		Arrays.fill(theInventory, null);
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	/**
	 * Removes some of the units from itemstack in the given slot, and returns
	 * as a separate itemstack
	 * 
	 * @param slotIndex
	 *            the slot number to remove the items from
	 * @param count
	 *            the number of units to remove
	 * @return a new itemstack containing the units removed from the slot
	 */
	@Override
	public ItemStack decrStackSize(int slotIndex, int count) {
		ItemStack itemStackInSlot = getStackInSlot(slotIndex);
		if (itemStackInSlot == null) {
			return null;
		}

		ItemStack itemStackRemoved;
		if (itemStackInSlot.stackSize <= count) {
			itemStackRemoved = itemStackInSlot;
			setInventorySlotContents(slotIndex, null);
		} else {
			itemStackRemoved = itemStackInSlot.splitStack(count);
			if (itemStackInSlot.stackSize == 0) {
				setInventorySlotContents(slotIndex, null);
			}
		}
		return itemStackRemoved;
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
		return !robot.isDead && robot.isOwner(player);
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
		}
		return null;
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
