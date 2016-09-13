package com.dyn.robot.api;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleCommand;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class TurtleSaveCommand implements ITurtleCommand {
	private ItemStack[] copyInventory(IInventory inv) {
		ItemStack[] results = new ItemStack[inv.getSizeInventory()];
		for (int i = 0; i < results.length; i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack != null) {
				results[i] = stack.copy();
			}
		}
		return results;
	}

	@Override
	public TurtleCommandResult execute(ITurtleAccess turtle) {
		if ((turtle instanceof IDYNRobotAccess)) {
			IDYNRobotAccess junior = (IDYNRobotAccess) turtle;
			BlockPos position = turtle.getPosition();
			EnumFacing direction = turtle.getDirection();
			int slot = turtle.getSelectedSlot();
			ItemStack[] inventory = copyInventory(turtle.getInventory());
			junior.setSavedState(position, direction, slot, inventory);
			return TurtleCommandResult.success();
		}
		return TurtleCommandResult.failure("Not a Beginner's turtle");
	}
}
