package com.dyn.rjm.actions;

import com.dyn.rjm.util.Location;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public class SetBlockState extends ServerAction {
	Location pos;
	Block block;
	int meta;

	public SetBlockState(Location pos, Block block, int meta) {
		this.pos = pos;
		this.block = block;
		this.meta = meta;
	}

	@Override
	public boolean contains(World w, int x, int y, int z) {
		return (pos.getWorld() == w) && (x == pos.getX()) && (y == pos.getY()) && (z == pos.getZ());
	}

	@Override
	public String describe() {
		return "" + Block.getIdFromBlock(block) + "," + meta;
	}

	@Override
	public void execute() {
		Block oldBlock = pos.getWorld().getBlockState(pos).getBlock();

		if (null != pos.getWorld().getTileEntity(pos)) {
			pos.getWorld().removeTileEntity(pos);
		}

		if ((oldBlock != block)) {
			pos.getWorld().setBlockState(pos, safeGetStateFromMeta(block, meta), 3);
		}
	}

	@Override
	public int getBlockId() {
		return Block.getIdFromBlock(block);
	}

	@Override
	public int getBlockMeta() {
		return meta;
	}

	@Override
	public IBlockState getBlockState() {
		return block.getStateFromMeta(meta);
	}

	public IBlockState safeGetStateFromMeta(Block b, int meta) {
		try {
			return b.getStateFromMeta(meta);
		} catch (Exception e) {
			return b.getStateFromMeta(0);
		}
	}
}
