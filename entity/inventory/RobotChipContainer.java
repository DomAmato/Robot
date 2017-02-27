package com.dyn.robot.entity.inventory;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.items.ItemExpansionChip;
import com.dyn.robot.items.ItemMemoryCard;
import com.dyn.robot.items.ItemMemoryStick;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

public class RobotChipContainer extends Container {

	private IInventory robotInventory;
	private EntityRobot robot;

	public RobotChipContainer(IInventory playerInventory, IInventory robotInventory, final EntityRobot robot,
			EntityPlayer player) {
		this.robot = robot;
		this.robotInventory = robotInventory;
		int i = 3;
		robotInventory.openInventory(player);
		int j = (i - 4) * 18;

		int slot = 0;

		this.addSlotToContainer(new Slot(robotInventory, slot++, 62, 18) {
			/**
			 * Check if the stack is a valid item for this slot. Always true
			 * beside for the armor slots.
			 */
			public boolean isItemValid(ItemStack stack) {
				return super.isItemValid(stack) && stack.getItem() == RobotMod.card && !this.getHasStack();
			}

		});

		this.addSlotToContainer(new Slot(robotInventory, slot++, 62, 36) {
			/**
			 * Check if the stack is a valid item for this slot. Always true
			 * beside for the armor slots.
			 */
			public boolean isItemValid(ItemStack stack) {
				return super.isItemValid(stack) && stack.getItem() == RobotMod.ram && !this.getHasStack();
			}

		});

		this.addSlotToContainer(new Slot(robotInventory, slot++, 62, 54) {
			/**
			 * Check if the stack is a valid item for this slot. Always true
			 * beside for the armor slots.
			 */
			public boolean isItemValid(ItemStack stack) {
				return super.isItemValid(stack) && (stack.getItem() instanceof ItemSword
						|| stack.getItem() instanceof ItemSpade || stack.getItem() instanceof ItemPickaxe
						|| stack.getItem() instanceof ItemAxe || stack.getItem() instanceof ItemHoe)
						&& !this.getHasStack();
			}

			@Override
			public void onSlotChanged() {
				robot.setCurrentItemOrArmor(0, getStack());
				super.onSlotChanged();
			}

		});

		for (int i1 = 0; i1 < 3; ++i1) {
			for (int k1 = 0; k1 < 3; ++k1) {
				this.addSlotToContainer(new Slot(robotInventory, slot++, 116 + k1 * 18, 18 + i1 * 18) {
					/**
					 * Check if the stack is a valid item for this slot. Always
					 * true beside for the armor slots.
					 */
					public boolean isItemValid(ItemStack stack) {
						return super.isItemValid(stack) && stack.getItem() == RobotMod.expChip
								&& !((RobotInventory) robotInventory).containsItem(stack)
								&& !this.getHasStack();
					}

				});
			}
		}

		for (int i1 = 0; i1 < 2; ++i1) {
			for (int k1 = 0; k1 < 9; ++k1) {
				this.addSlotToContainer(new Slot(robotInventory, slot++, 8 + k1 * 18, 92 + i1 * 18 + j) {
					public boolean isItemValid(ItemStack stack) {
						return super.isItemValid(stack) && !(stack.getItem() instanceof ItemExpansionChip
								|| stack.getItem() instanceof ItemMemoryCard
								|| stack.getItem() instanceof ItemMemoryStick || stack.getItem() instanceof ItemSword
								|| stack.getItem() instanceof ItemSpade || stack.getItem() instanceof ItemPickaxe
								|| stack.getItem() instanceof ItemAxe || stack.getItem() instanceof ItemHoe)
								&& !this.getHasStack();
					}

				});
			}
		}

		for (int i1 = 0; i1 < 3; ++i1) {
			for (int k1 = 0; k1 < 9; ++k1) {
				this.addSlotToContainer(new Slot(playerInventory, k1 + i1 * 9 + 9, 8 + k1 * 18, 140 + i1 * 18 + j));
			}
		}

		for (int j1 = 0; j1 < 9; ++j1) {
			this.addSlotToContainer(new Slot(playerInventory, j1, 8 + j1 * 18, 198 + j));
		}
	}

	public boolean canInteractWith(EntityPlayer playerIn) {
		return this.robotInventory.isUseableByPlayer(playerIn) && this.robot.isEntityAlive()
				&& this.robot.getDistanceToEntity(playerIn) < 8.0F;
	}

	/**
	 * Take a stack from the specified inventory slot.
	 */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index < this.robotInventory.getSizeInventory()) {
				if (!this.mergeItemStack(itemstack1, this.robotInventory.getSizeInventory(), this.inventorySlots.size(),
						true)) {
					return null;
				}
			} else if (this.getSlot(0).isItemValid(itemstack1)) {
				if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
					return null;
				}
			} else if (this.getSlot(1).isItemValid(itemstack1)) {
				if (!this.mergeItemStack(itemstack1, 1, 2, false)) {
					return null;
				}
			} else if (this.getSlot(2).isItemValid(itemstack1)) {
				if (!this.mergeItemStack(itemstack1, 2, 3, false)) {
					return null;
				}
			} else if (this.getSlot(((RobotInventory) robotInventory).getOpenExpansionSlot()).isItemValid(itemstack1)) {
				if (!this.mergeItemStack(itemstack1, 3, 12, false)) {
					return null;
				}
			} else if (!this.mergeItemStack(itemstack1, 12, this.robotInventory.getSizeInventory(), false)) {
				return null;
			}

			if (itemstack1.stackSize == 0) {
				slot.putStack((ItemStack) null);
			} else {
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}

	/**
	 * Called when the container is closed.
	 */
	public void onContainerClosed(EntityPlayer playerIn) {
		super.onContainerClosed(playerIn);
		this.robotInventory.closeInventory(playerIn);
	}
}