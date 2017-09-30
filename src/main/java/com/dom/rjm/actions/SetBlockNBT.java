package com.dom.rjm.actions;

import com.dom.rjm.util.Location;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class SetBlockNBT extends SetBlockStateWithId {
	static public void scrubNBT(NBTTagCompound tag) {
		tag.removeTag("x");
		tag.removeTag("y");
		tag.removeTag("z");
	}

	NBTTagCompound nbt;

	// Note: This modifies the nbt tag compound
	public SetBlockNBT(Location pos, short id, short meta, NBTTagCompound nbt) {
		super(pos, id, meta);
		this.nbt = nbt;
	}

	@Override
	public String describe() {
		SetBlockNBT.scrubNBT(nbt);
		return super.describe() + nbt.toString();
	}

	@Override
	public void execute() {
		pos.getWorld().setBlockState(pos, safeGetStateFromMeta(Block.getBlockById(id), meta), 2);
		TileEntity tileEntity = pos.getWorld().getTileEntity(pos);
		if (tileEntity != null) {
			nbt.setInteger("x", pos.getX());
			nbt.setInteger("y", pos.getY());
			nbt.setInteger("z", pos.getZ());
			try {
				tileEntity.readFromNBT(nbt);
			} catch (Exception e) {
			}
			tileEntity.markDirty();
		}
	}
}
