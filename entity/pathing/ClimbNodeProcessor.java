package com.dyn.robot.entity.pathing;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.pathfinder.NodeProcessor;

public class ClimbNodeProcessor extends NodeProcessor {

	public static int func_176170_a(IBlockAccess blockaccessIn, Entity entityIn, int x, int y, int z, int sizeX,
			int sizeY, int sizeZ, boolean avoidWater, boolean breakDoors, boolean enterDoors) {
		boolean flag = false;
		BlockPos blockpos = new BlockPos(entityIn);
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

		for (int i = x; i < (x + sizeX); ++i) {
			for (int j = y; j < (y + sizeY); ++j) {
				for (int k = z; k < (z + sizeZ); ++k) {
					blockpos$mutableblockpos.set(i, j, k);
					Block block = blockaccessIn.getBlockState(blockpos$mutableblockpos).getBlock();

					if (block.getMaterial() != Material.air) {
						if ((block != Blocks.trapdoor) && (block != Blocks.iron_trapdoor)) {
							if ((block == Blocks.ladder) || (block == Blocks.vine)) {
								return 3;
							}
							if ((block != Blocks.flowing_water) && (block != Blocks.water)) {
								if (!enterDoors && (block instanceof BlockDoor)
										&& (block.getMaterial() == Material.wood)) {
									return 0;
								}
							} else {
								if (avoidWater) {
									return -1;
								}

								flag = true;
							}
						} else {
							flag = true;
						}

						if (entityIn.worldObj.getBlockState(blockpos$mutableblockpos)
								.getBlock() instanceof BlockRailBase) {
							if (!(entityIn.worldObj.getBlockState(blockpos).getBlock() instanceof BlockRailBase)
									&& !(entityIn.worldObj.getBlockState(blockpos.down())
											.getBlock() instanceof BlockRailBase)) {
								return -3;
							}
						} else if (!block.isPassable(blockaccessIn, blockpos$mutableblockpos) && (!breakDoors
								|| !(block instanceof BlockDoor) || (block.getMaterial() != Material.wood))) {
							if ((block instanceof BlockFence) || (block instanceof BlockFenceGate)
									|| (block instanceof BlockWall)) {
								return -3;
							}

							if ((block == Blocks.trapdoor) || (block == Blocks.iron_trapdoor)) {
								return -4;
							}

							Material material = block.getMaterial();

							if (material != Material.lava) {
								return 0;
							}

							if (!entityIn.isInLava()) {
								return -2;
							}
						}
					}
				}
			}
		}

		return flag ? 2 : 1;
	}

	private boolean canEnterDoors;
	private boolean canBreakDoors;
	private boolean avoidsWater;
	private boolean canSwim;
	private boolean shouldAvoidWater;

	private boolean canUseLadders;

	@Override
	public int findPathOptions(PathPoint[] pathOptions, Entity entityIn, PathPoint currentPoint, PathPoint targetPoint,
			float maxDistance) {
		int i = 0;
		int j = 0;
		int ladderHeightUp = 0;
		int ladderHeightDown = 0;

		if (getVerticalOffset(entityIn, currentPoint.xCoord, currentPoint.yCoord + 1, currentPoint.zCoord) == 1) {
			j = 1;
		}
		if (getVerticalOffset(entityIn, currentPoint.xCoord, currentPoint.yCoord - 1, currentPoint.zCoord) == 3) {
			ladderHeightDown = getLadderHeight(entityIn.worldObj, currentPoint.xCoord, currentPoint.yCoord - 1,
					currentPoint.zCoord, -1);
			j = 1;
			final PathPoint pathpoint_m3 = getSafePoint(entityIn, currentPoint.xCoord,
					currentPoint.yCoord - ladderHeightDown, currentPoint.zCoord, j);
			if ((pathpoint_m3 != null) && !pathpoint_m3.visited
					&& (pathpoint_m3.distanceTo(targetPoint) < maxDistance)) {
				pathOptions[i++] = pathpoint_m3;
			}
		}
		if (getVerticalOffset(entityIn, currentPoint.xCoord, currentPoint.yCoord, currentPoint.zCoord) == 3) {
			ladderHeightUp = getLadderHeight(entityIn.worldObj, currentPoint.xCoord, currentPoint.yCoord,
					currentPoint.zCoord, 1);
			j = 1;
			if (ladderHeightUp > 0) {
				int topX = 0;
				int topZ = 0;
				final IBlockState face = entityIn.worldObj
						.getBlockState(new BlockPos(currentPoint.xCoord, currentPoint.yCoord, currentPoint.zCoord));
				if (face.getValue(BlockLadder.FACING) == EnumFacing.NORTH) {
					topZ = 1;
				} else if (face.getValue(BlockLadder.FACING) == EnumFacing.SOUTH) {
					topZ = -1;
				} else if (face.getValue(BlockLadder.FACING) == EnumFacing.WEST) {
					topX = 1;
				} else if (face.getValue(BlockLadder.FACING) == EnumFacing.EAST) {
					topX = -1;
				}
				final PathPoint pathpoint_m4 = getSafePoint(entityIn, currentPoint.xCoord + topX,
						currentPoint.yCoord + ladderHeightUp, currentPoint.zCoord + topZ, j);
				if ((pathpoint_m4 != null) && !pathpoint_m4.visited
						&& (pathpoint_m4.distanceTo(targetPoint) < maxDistance)) {
					pathOptions[i++] = pathpoint_m4;
				}
			}
		}

		PathPoint pathpoint = getSafePoint(entityIn, currentPoint.xCoord, currentPoint.yCoord, currentPoint.zCoord + 1,
				j);
		PathPoint pathpoint1 = getSafePoint(entityIn, currentPoint.xCoord - 1, currentPoint.yCoord, currentPoint.zCoord,
				j);
		PathPoint pathpoint2 = getSafePoint(entityIn, currentPoint.xCoord + 1, currentPoint.yCoord, currentPoint.zCoord,
				j);
		PathPoint pathpoint3 = getSafePoint(entityIn, currentPoint.xCoord, currentPoint.yCoord, currentPoint.zCoord - 1,
				j);

		if ((pathpoint != null) && !pathpoint.visited && (pathpoint.distanceTo(targetPoint) < maxDistance)) {
			pathOptions[i++] = pathpoint;
		}

		if ((pathpoint1 != null) && !pathpoint1.visited && (pathpoint1.distanceTo(targetPoint) < maxDistance)) {
			pathOptions[i++] = pathpoint1;
		}

		if ((pathpoint2 != null) && !pathpoint2.visited && (pathpoint2.distanceTo(targetPoint) < maxDistance)) {
			pathOptions[i++] = pathpoint2;
		}

		if ((pathpoint3 != null) && !pathpoint3.visited && (pathpoint3.distanceTo(targetPoint) < maxDistance)) {
			pathOptions[i++] = pathpoint3;
		}

		return i;
	}

	public boolean getAvoidsWater() {
		return avoidsWater;
	}

	public boolean getCanSwim() {
		return canSwim;
	}

	public boolean getCanUseLadders() {
		return canUseLadders;
	}

	public boolean getEnterDoors() {
		return canEnterDoors;
	}

	private int getLadderHeight(final World worldObj, final int l, final int i1, final int j1, final int direction) {
		Block k1;
		int ladderHeight;
		for (k1 = worldObj.getBlockState(new BlockPos(l, i1, j1))
				.getBlock(), ladderHeight = 0; k1 == Blocks.ladder; k1 = worldObj
						.getBlockState(new BlockPos(l, i1 + ladderHeight, j1)).getBlock()) {
			ladderHeight += direction;
		}
		return ladderHeight;
	}

	/**
	 * Returns given entity's position as PathPoint
	 */
	@Override
	public PathPoint getPathPointTo(Entity entityIn) {
		int i;

		if (canSwim && entityIn.isInWater()) {
			i = (int) entityIn.getEntityBoundingBox().minY;
			BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(
					MathHelper.floor_double(entityIn.posX), i, MathHelper.floor_double(entityIn.posZ));

			for (Block block = blockaccess.getBlockState(blockpos$mutableblockpos)
					.getBlock(); (block == Blocks.flowing_water)
							|| (block == Blocks.water); block = blockaccess.getBlockState(blockpos$mutableblockpos)
									.getBlock()) {
				++i;
				blockpos$mutableblockpos.set(MathHelper.floor_double(entityIn.posX), i,
						MathHelper.floor_double(entityIn.posZ));
			}

			avoidsWater = false;
		} else {
			i = MathHelper.floor_double(entityIn.getEntityBoundingBox().minY + 0.5D);
		}

		return openPoint(MathHelper.floor_double(entityIn.getEntityBoundingBox().minX), i,
				MathHelper.floor_double(entityIn.getEntityBoundingBox().minZ));
	}

	/**
	 * Returns PathPoint for given coordinates
	 */
	@Override
	public PathPoint getPathPointToCoords(Entity entityIn, double x, double y, double target) {
		return openPoint(MathHelper.floor_double(x - (entityIn.width / 2.0F)), MathHelper.floor_double(y),
				MathHelper.floor_double(target - (entityIn.width / 2.0F)));
	}

	/**
	 * Returns a point that the entity can safely move to
	 */
	public PathPoint getSafePoint(Entity entityIn, int x, int y, int z, int p_176171_5_) {
		PathPoint pathpoint = null;
		int i = getVerticalOffset(entityIn, x, y, z);

		if (i == 2) {
			return openPoint(x, y, z);
		} else {
			if ((i == 1) || (i == 3)) // clear or ladder
			{
				pathpoint = openPoint(x, y, z);
			}

			if ((pathpoint == null) && (p_176171_5_ > 0) && (i != -3) && (i != -4)
					&& (getVerticalOffset(entityIn, x, y + p_176171_5_, z) == 1)) {
				pathpoint = openPoint(x, y + p_176171_5_, z);
				y += p_176171_5_;
			}

			if (pathpoint != null) {
				int j = 0;
				int k;

				for (k = 0; y > 0; pathpoint = openPoint(x, y, z)) {
					k = getVerticalOffset(entityIn, x, y - 1, z);

					if (avoidsWater && (k == -1)) {
						return null;
					}

					if (k != 1) {
						break;
					}

					if (j++ >= entityIn.getMaxFallHeight()) {
						return null;
					}

					--y;

					if (y <= 0) {
						return null;
					}
				}

				if (k == -2) {
					return null;
				}
			}

			return pathpoint;
		}
	}

	/**
	 * Checks if an entity collides with blocks at a position. Returns 1 if
	 * clear, 3 if ladder, 0 for colliding with any solid block, -1 for water(if
	 * avoids water), -2 for lava, -3 for fence and wall, -4 for closed
	 * trapdoor, 2 if otherwise clear except for open trapdoor or water(if not
	 * avoiding)
	 */
	private int getVerticalOffset(Entity entityIn, int x, int y, int z) {
		return func_176170_a(blockaccess, entityIn, x, y, z, entitySizeX, entitySizeY, entitySizeZ, avoidsWater,
				canBreakDoors, canEnterDoors);
	}

	@Override
	public void initProcessor(IBlockAccess iblockaccessIn, Entity entityIn) {
		super.initProcessor(iblockaccessIn, entityIn);
		shouldAvoidWater = avoidsWater;
	}

	/**
	 * This method is called when all nodes have been processed and PathEntity
	 * is created. {@link net.minecraft.world.pathfinder.WalkNodeProcessor
	 * WalkNodeProcessor} uses this to change its field
	 * {@link net.minecraft.world.pathfinder.WalkNodeProcessor#avoidsWater
	 * avoidsWater}
	 */
	@Override
	public void postProcess() {
		super.postProcess();
		avoidsWater = shouldAvoidWater;
	}

	public void setAvoidsWater(boolean avoidsWaterIn) {
		avoidsWater = avoidsWaterIn;
	}

	public void setBreakDoors(boolean canBreakDoorsIn) {
		canBreakDoors = canBreakDoorsIn;
	}

	public void setCanSwim(boolean canSwimIn) {
		canSwim = canSwimIn;
	}

	public void setCanUseLadders(boolean canUseLadders) {
		this.canUseLadders = canUseLadders;
	}

	public void setEnterDoors(boolean canEnterDoorsIn) {
		canEnterDoors = canEnterDoorsIn;
	}
}
