package com.dyn.robot.entity.pathing;

import com.dyn.robot.entity.SimpleRobotEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class RobotNodeProcessor extends WalkNodeProcessor {

	// @Override
	// public PathNodeType getPathNodeType(IBlockAccess blockaccessIn, int x, int y,
	// int z) {
	// PathNodeType pathnodetype = getPathNodeTypeRaw(blockaccessIn, x, y, z);
	//
	// if ((pathnodetype == PathNodeType.OPEN) && (y >= 1)) {
	// Block block = blockaccessIn.getBlockState(new BlockPos(x, y - 1,
	// z)).getBlock();
	// PathNodeType pathnodetype1 = getPathNodeTypeRaw(blockaccessIn, x, y - 1, z);
	// pathnodetype = (pathnodetype1 != PathNodeType.WALKABLE) && (pathnodetype1 !=
	// PathNodeType.OPEN)
	// ? PathNodeType.WALKABLE
	// : PathNodeType.OPEN;
	//
	// if ((pathnodetype1 == PathNodeType.DAMAGE_FIRE) || (block == Blocks.MAGMA)
	// || (pathnodetype1 == PathNodeType.LAVA)) {
	// pathnodetype = ((SimpleRobotEntity) entity).getSuit().getMetadata() == 1 ?
	// PathNodeType.WALKABLE
	// : PathNodeType.DAMAGE_FIRE;
	// }
	//
	// if (pathnodetype1 == PathNodeType.WATER) {
	// pathnodetype = ((SimpleRobotEntity) entity).getSuit().getMetadata() == 2 ?
	// PathNodeType.WALKABLE
	// : PathNodeType.OPEN;
	// }
	// if (pathnodetype1 == PathNodeType.DAMAGE_CACTUS) {
	// pathnodetype = PathNodeType.DAMAGE_CACTUS;
	// }
	// }
	//
	// pathnodetype = checkNeighborBlocks(blockaccessIn, x, y, z, pathnodetype);
	// return pathnodetype;
	// }

	@Override
	protected PathNodeType getPathNodeTypeRaw(IBlockAccess p_189553_1_, int p_189553_2_, int p_189553_3_,
			int p_189553_4_) {
		BlockPos blockpos = new BlockPos(p_189553_2_, p_189553_3_, p_189553_4_);
		IBlockState iblockstate = p_189553_1_.getBlockState(blockpos);
		Block block = iblockstate.getBlock();
		Material material = iblockstate.getMaterial();

		PathNodeType type = block.getAiPathNodeType(iblockstate, p_189553_1_, blockpos);
		if (type != null) {
			return type;
		}

		if (material == Material.AIR) {
			return PathNodeType.OPEN;
		} else if ((block != Blocks.TRAPDOOR) && (block != Blocks.IRON_TRAPDOOR) && (block != Blocks.WATERLILY)) {
			if (block == Blocks.FIRE) {
				return ((SimpleRobotEntity) entity).getSuit().getMetadata() == 1 ? PathNodeType.OPEN
						: PathNodeType.DAMAGE_FIRE;
			} else if (block == Blocks.CACTUS) {
				return PathNodeType.DAMAGE_CACTUS;
			} else if ((block instanceof BlockDoor) && (material == Material.WOOD)
					&& !iblockstate.getValue(BlockDoor.OPEN).booleanValue()) {
				return PathNodeType.DOOR_WOOD_CLOSED;
			} else if ((block instanceof BlockDoor) && (material == Material.IRON)
					&& !iblockstate.getValue(BlockDoor.OPEN).booleanValue()) {
				return PathNodeType.DOOR_IRON_CLOSED;
			} else if ((block instanceof BlockDoor) && iblockstate.getValue(BlockDoor.OPEN).booleanValue()) {
				return PathNodeType.DOOR_OPEN;
			} else if (block instanceof BlockRailBase) {
				return PathNodeType.RAIL;
			} else if (!(block instanceof BlockFence) && !(block instanceof BlockWall)
					&& (!(block instanceof BlockFenceGate)
							|| iblockstate.getValue(BlockFenceGate.OPEN).booleanValue())) {
				if (material == Material.WATER) {
					return ((SimpleRobotEntity) entity).getSuit().getMetadata() == 2 ? PathNodeType.OPEN
							: PathNodeType.WATER;
				} else if (material == Material.LAVA) {
					return ((SimpleRobotEntity) entity).getSuit().getMetadata() == 1 ? PathNodeType.OPEN
							: PathNodeType.LAVA;
				} else {
					return block.isPassable(p_189553_1_, blockpos) ? PathNodeType.OPEN : PathNodeType.BLOCKED;
				}
			} else {
				return PathNodeType.FENCE;
			}
		} else {
			return PathNodeType.TRAPDOOR;
		}
	}

	@Override
	public void init(IBlockAccess sourceIn, EntityLiving mob) {
		super.init(sourceIn, mob);
	}
}
