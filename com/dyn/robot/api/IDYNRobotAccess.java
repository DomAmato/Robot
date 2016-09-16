package com.dyn.robot.api;

import com.dyn.robot.programs.Program;
import com.dyn.robot.programs.ProgramState;
import com.dyn.robot.programs.UserProgramLibrary;

import dan200.computercraft.api.turtle.ITurtleAccess;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public abstract interface IDYNRobotAccess extends ITurtleAccess {
	public abstract void clearSavedState();

	public abstract int createProgram(String paramString);

	public abstract void deleteProgram(int paramInt);

	public abstract void exportProgramToAllPlayers(int paramInt);

	public abstract void exportProgramToPlayerInventory(int paramInt, EntityPlayer paramEntityPlayer);

	public abstract String getOwnerName();

	public abstract Program getProgram();

	public abstract UserProgramLibrary getProgramLibrary();

	public abstract int getProgramSlot();

	public abstract ProgramState getProgramState();

	public abstract EnumFacing getSavedDirection();

	public abstract ItemStack[] getSavedInventory();

	public abstract BlockPos getSavedPosition();

	public abstract int getSavedSlot();

	public abstract int getSelectedProgramIndex();

	public abstract boolean isLocked();

	public abstract void renameProgram(int paramInt, String paramString);

	public abstract void selectProgram(int paramInt);

	public abstract void setLocked(boolean paramBoolean);

	public abstract void setOwnerName(String paramString);

	public abstract void setProgramErrored(String paramString);

	public abstract void setProgramPaused();

	public abstract void setProgramRunning();

	public abstract void setProgramSlot(int paramInt);

	public abstract void setProgramStopped();

	public abstract void setSavedState(BlockPos paramBlockPos, EnumFacing paramEnumFacing, int paramInt,
			ItemStack[] paramArrayOfItemStack);

	public abstract void setVariable(String paramString1, String paramString2);
}
