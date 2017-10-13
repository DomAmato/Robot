package com.dyn.rjm.actions;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public abstract class ServerAction {
	public boolean contains(World w, int x, int y, int z) {
		return false;
	}

	public String describe() {
		return "";
	}

	abstract public void execute();

	public int getBlockId() {
		return 0;
	}

	public int getBlockMeta() {
		return 0;
	}

	public abstract IBlockState getBlockState();
}
