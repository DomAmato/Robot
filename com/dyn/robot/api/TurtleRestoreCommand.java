package com.dyn.robot.api;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleCommand;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class TurtleRestoreCommand implements ITurtleCommand {
	@Override
	public TurtleCommandResult execute(ITurtleAccess turtle) {
		if ((turtle instanceof IDYNRobotAccess)) {
			IDYNRobotAccess junior = (IDYNRobotAccess) turtle;
			BlockPos position = junior.getSavedPosition();
			EnumFacing direction = junior.getSavedDirection();
			int slot = junior.getSavedSlot();
			ItemStack[] inventory = junior.getSavedInventory();
			if ((position != null) && (direction != null) && (slot >= 0)) {
				World world = turtle.getWorld();
				if (turtle.teleportTo(world, position)) {
					turtle.setDirection(direction);

					turtle.setSelectedSlot(slot);
					if (inventory != null) {
						IInventory inv = turtle.getInventory();
						for (int i = 0; i < inventory.length; i++) {
							inv.setInventorySlotContents(i, inventory[i]);
						}
					}
					junior.clearSavedState();
					return TurtleCommandResult.success();
				}
				return TurtleCommandResult.failure("Teleport failed");
			}
			return TurtleCommandResult.failure("No position saved");
		}
		return TurtleCommandResult.failure("Not a Beginner's turtle");
	}
}
