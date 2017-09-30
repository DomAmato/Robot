package com.dom.rjm.actions;

import com.dom.rjm.util.Location;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public class SetBlockStateWithId extends ServerAction {
	Location pos;
	short id;
	short meta;

	public SetBlockStateWithId(Location pos, short id, short meta) {
		this.pos = pos;
		this.id = id;
		this.meta = meta;
	}

	public SetBlockStateWithId(short id, short meta) {
		this.id = id;
		this.meta = meta;
	}

	@Override
	public boolean contains(World w, int x, int y, int z) {
		return (pos.getWorld() == w) && (x == pos.getX()) && (y == pos.getY()) && (z == pos.getZ());
	}

	@Override
	public String describe() {
		return "" + id + "," + meta + ",";
	}

	@Override
	public void execute() {
		IBlockState oldState = pos.getWorld().getBlockState(pos);
		Block oldBlock = oldState.getBlock();

		if (null != pos.getWorld().getTileEntity(pos)) {
			pos.getWorld().removeTileEntity(pos);
		}

		if ((Block.getIdFromBlock(oldBlock) != id) || (oldBlock.getMetaFromState(oldState) != meta)) {
			pos.getWorld().setBlockState(pos, safeGetStateFromMeta(Block.getBlockById(id), meta), 3);
		}
	}

	@Override
	public int getBlockId() {
		return id;
	}

	@Override
	public int getBlockMeta() {
		return meta;
	}

	@Override
	public IBlockState getBlockState() {
		return safeGetStateFromMeta(Block.getBlockById(id), meta);
	}

	public IBlockState safeGetStateFromMeta(Block b, int meta) {
		try {
			return b.getStateFromMeta(meta);
		} catch (Exception e) {
			return b.getStateFromMeta(0);
		}
	}
}
