package com.dyn.robot.gui;

import com.dyn.robot.entity.EntityRobot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class RobotContainer extends Container {

	/** Instance of Merchant. */
	private EntityRobot theRobot;
	/** Instance of World. */
	private final World theWorld;

	public RobotContainer(InventoryPlayer playerInventory, EntityRobot robot, World worldIn) {
		theRobot = robot;
		theWorld = worldIn;
		addSlotToContainer(new Slot(robot.m_inventory, 0, 36, 53));
		addSlotToContainer(new Slot(robot.m_inventory, 1, 62, 53));

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				addSlotToContainer(new Slot(playerInventory, j + (i * 9) + 9, 8 + (j * 18), 84 + (i * 18)));
			}
		}

		for (int k = 0; k < 9; ++k) {
			addSlotToContainer(new Slot(playerInventory, k, 8 + (k * 18), 142));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return theRobot.getOwner() == playerIn.getName();
	}

	/**
	 * Looks for changes made in the container, sends them to every listener.
	 */
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
	}

	/**
	 * Called when the container is closed.
	 */
	@Override
	public void onContainerClosed(EntityPlayer playerIn) {
		super.onContainerClosed(playerIn);
		super.onContainerClosed(playerIn);

		if (!theWorld.isRemote) {
			ItemStack itemstack = theRobot.m_inventory.removeStackFromSlot(0);

			if (itemstack != null) {
				playerIn.dropPlayerItemWithRandomChoice(itemstack, false);
			}

			itemstack = theRobot.m_inventory.removeStackFromSlot(1);

			if (itemstack != null) {
				playerIn.dropPlayerItemWithRandomChoice(itemstack, false);
			}
		}
	}

	@Override
	public void onCraftGuiOpened(ICrafting listener) {
		super.onCraftGuiOpened(listener);
	}

	/**
	 * Callback for when the crafting matrix is changed.
	 */
	@Override
	public void onCraftMatrixChanged(IInventory inventoryIn) {
		super.onCraftMatrixChanged(inventoryIn);
	}

	/**
	 * Take a stack from the specified inventory slot.
	 */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		ItemStack itemstack = null;
		Slot slot = inventorySlots.get(index);

		if ((slot != null) && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index == 2) {
				if (!mergeItemStack(itemstack1, 3, 39, true)) {
					return null;
				}

				slot.onSlotChange(itemstack1, itemstack);
			} else if ((index != 0) && (index != 1)) {
				if ((index >= 3) && (index < 30)) {
					if (!mergeItemStack(itemstack1, 30, 39, false)) {
						return null;
					}
				} else if ((index >= 30) && (index < 39) && !mergeItemStack(itemstack1, 3, 30, false)) {
					return null;
				}
			} else if (!mergeItemStack(itemstack1, 3, 39, false)) {
				return null;
			}

			if (itemstack1.stackSize == 0) {
				slot.putStack((ItemStack) null);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.stackSize == itemstack.stackSize) {
				return null;
			}

			slot.onPickupFromSlot(playerIn, itemstack1);
		}

		return itemstack;
	}

}