package com.dom.rjm.actions;

import com.dom.rjm.RaspberryJamMod;
import com.dom.rjm.util.Location;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SetBlocksState extends SetBlockStateWithId {
	int x2;
	int y2;
	int z2;

	public SetBlocksState(Location corner1, Location corner2, short id, short meta) {
		super(id, meta);

		int x1 = corner1.getX();
		int y1 = corner1.getY();
		int z1 = corner1.getZ();
		int x2 = corner2.getX();
		int y2 = corner2.getY();
		int z2 = corner2.getZ();

		pos = new Location(corner1.getWorld(), Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2));
		this.x2 = Math.max(x1, x2);
		this.y2 = Math.max(y1, y2);
		this.z2 = Math.max(z1, z2);
	}

	@Override
	public boolean contains(World w, int x, int y, int z) {
		return (x <= x2) && (y <= y2) && (z <= z2) && (pos.getX() <= x) && (pos.getY() <= y) && (pos.getZ() <= z)
				&& (w == pos.getWorld());
	}

	@Override
	public void execute() {
		int y1 = pos.getY();
		int z1 = pos.getZ();
		int intId = id;
		int intMeta = meta;
		IBlockState state = safeGetStateFromMeta(Block.getBlockById(intId), intMeta);

		for (int x = pos.getX(); x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				for (int z = z1; z <= z2; z++) {

					// TODO: fix in client-only mode
					if (!RaspberryJamMod.apiActive) {
						break;
					}

					BlockPos here = new BlockPos(x, y, z);
					IBlockState oldState = pos.getWorld().getBlockState(here);
					Block oldBlock = oldState.getBlock();

					if (pos.getWorld().getTileEntity(here) != null) {
						pos.getWorld().removeTileEntity(here);
					}

					if ((Block.getIdFromBlock(oldBlock) != intId) || (oldBlock.getMetaFromState(oldState) != intMeta)) {
						pos.getWorld().setBlockState(here, state, 3);
					}
				}
			}
		}

	}
}
