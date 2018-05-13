package com.dyn.robot.entity.inventory;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.items.ItemExpansionChip;
import com.dyn.robot.items.ItemMemoryCard;
import com.dyn.robot.items.ItemMemoryStick;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.EnumHand;

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

		addSlotToContainer(new Slot(robotInventory, slot++, 62, 18) {
			/**
			 * Check if the stack is a valid item for this slot. Always true beside for the
			 * armor slots.
			 */
			@Override
			public boolean isItemValid(ItemStack stack) {
				return super.isItemValid(stack) && (stack.getItem() == RobotMod.card) && !getHasStack();
			}

		});

		addSlotToContainer(new Slot(robotInventory, slot++, 62, 36) {
			/**
			 * Check if the stack is a valid item for this slot. Always true beside for the
			 * armor slots.
			 */
			@Override
			public boolean isItemValid(ItemStack stack) {
				return super.isItemValid(stack) && (stack.getItem() == RobotMod.ram) && !getHasStack();
			}

		});

		addSlotToContainer(new Slot(robotInventory, slot++, 62, 54) {
			/**
			 * Check if the stack is a valid item for this slot. Always true beside for the
			 * armor slots.
			 */
			@Override
			public boolean isItemValid(ItemStack stack) {
				return super.isItemValid(stack) && ((stack.getItem() instanceof ItemSword)
						|| (stack.getItem() instanceof ItemTool) || (stack.getItem() instanceof ItemHoe))
						&& !getHasStack();
			}

			@Override
			public void onSlotChanged() {
				robot.setHeldItem(EnumHand.MAIN_HAND, getStack());
				super.onSlotChanged();
			}

		});

		addSlotToContainer(new Slot(robotInventory, slot++, 80, 18) {
			/**
			 * Check if the stack is a valid item for this slot. Always true beside for the
			 * armor slots.
			 */
			@Override
			public boolean isItemValid(ItemStack stack) {
				return super.isItemValid(stack) && (stack.getItem() == RobotMod.sim_card) && !getHasStack();
			}

		});

		addSlotToContainer(new Slot(robotInventory, slot++, 80, 36) {
			/**
			 * Check if the stack is a valid item for this slot. Always true beside for the
			 * armor slots.
			 */
			@Override
			public boolean isItemValid(ItemStack stack) {
				return super.isItemValid(stack) && (stack.getItem() == RobotMod.meter) && !getHasStack();
			}

		});

		for (int i1 = 0; i1 < 3; ++i1) {
			for (int k1 = 0; k1 < 3; ++k1) {
				addSlotToContainer(new Slot(robotInventory, slot++, 116 + (k1 * 18), 18 + (i1 * 18)) {
					@Override
					public int getSlotStackLimit() {
						return 1;
					}

					/**
					 * Check if the stack is a valid item for this slot. Always true beside for the
					 * armor slots.
					 */
					@Override
					public boolean isItemValid(ItemStack stack) {
						return super.isItemValid(stack) && (stack.getItem() == RobotMod.expChip)
								&& !((RobotInventory) robotInventory).containsItem(stack) && !getHasStack();
					}

				});
			}
		}

		for (int i1 = 0; i1 < 2; ++i1) {
			for (int k1 = 0; k1 < 9; ++k1) {
				addSlotToContainer(new Slot(robotInventory, slot++, 8 + (k1 * 18), 92 + (i1 * 18) + j) {
					@Override
					public boolean isItemValid(ItemStack stack) {
						return super.isItemValid(stack) && !((stack.getItem() instanceof ItemExpansionChip)
								|| (stack.getItem() instanceof ItemMemoryCard)
								|| (stack.getItem() instanceof ItemMemoryStick)) && !getHasStack();
					}

				});
			}
		}

		for (int i1 = 0; i1 < 3; ++i1) {
			for (int k1 = 0; k1 < 9; ++k1) {
				addSlotToContainer(new Slot(playerInventory, k1 + (i1 * 9) + 9, 8 + (k1 * 18), 140 + (i1 * 18) + j));
			}
		}

		for (int j1 = 0; j1 < 9; ++j1) {
			addSlotToContainer(new Slot(playerInventory, j1, 8 + (j1 * 18), 198 + j));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return robotInventory.isUsableByPlayer(playerIn) && robot.isEntityAlive()
				&& (robot.getDistanceSq(playerIn) < 16.0F);
	}

	/**
	 * Called when the container is closed.
	 */
	@Override
	public void onContainerClosed(EntityPlayer playerIn) {
		super.onContainerClosed(playerIn);
		robotInventory.closeInventory(playerIn);
	}

	/**
	 * Take a stack from the specified inventory slot.
	 */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(index);

		if ((slot != null) && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index < robotInventory.getSizeInventory()) {
				if (!mergeItemStack(itemstack1, robotInventory.getSizeInventory(), inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (getSlot(0).isItemValid(itemstack1)) {
				if (!mergeItemStack(itemstack1, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (getSlot(1).isItemValid(itemstack1)) {
				if (!mergeItemStack(itemstack1, 1, 2, false)) {
					return ItemStack.EMPTY;
				}
			} else if (getSlot(2).isItemValid(itemstack1)) {
				if (!mergeItemStack(itemstack1, 2, 3, false)) {
					return ItemStack.EMPTY;
				}
			} else if (getSlot(3).isItemValid(itemstack1)) {
				if (!mergeItemStack(itemstack1, 3, 4, false)) {
					return ItemStack.EMPTY;
				}
			} else if (getSlot(4).isItemValid(itemstack1)) {
				if (!mergeItemStack(itemstack1, 4, 5, false)) {
					return ItemStack.EMPTY;
				}
			} else if (getSlot(((RobotInventory) robotInventory).getOpenExpansionSlot()).isItemValid(itemstack1)) {
				if (!mergeItemStack(itemstack1, 5, 14, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!mergeItemStack(itemstack1, 14, robotInventory.getSizeInventory(), false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}
}