package com.dom.rjm.util;

import com.dom.rjm.actions.ServerAction;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class SetDimension extends ServerAction {
	Entity entity;
	int dimension;

	public SetDimension(Entity e, int dim) {
		entity = e;
		dimension = dim;
	}

	@Override
	public void execute() {
		if (null == FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dimension)) {
			return;
		}
		entity.changeDimension(dimension);
	}

	@Override
	public IBlockState getBlockState() {
		// TODO Auto-generated method stub
		return null;
	}
}
