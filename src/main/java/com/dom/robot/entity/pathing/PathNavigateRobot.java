package com.dom.robot.entity.pathing;

import java.util.Map;

import com.dom.robot.entity.EntityRobot;
import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PathNavigateRobot extends PathNavigateGround {

	/** Time, in number of ticks, following the current path */
	private int totalTicks;
	/**
	 * The time when the last position check was done (to detect successful
	 * movement)
	 */
	private int ticksAtLastPos;

	/**
	 * inates of the entity's position last time a check was done (part of
	 * monitoring getting 'stuck')
	 */
	private Vec3d lastPosCheck = new Vec3d(0.0D, 0.0D, 0.0D);

	public PathNavigateRobot(EntityLiving entityLivingIn, World worldIn) {
		super(entityLivingIn, worldIn);
	}

	/**
	 * Checks if entity haven't been moved when last checked and if so, clears
	 * current {@link net.minecraft.pathfinding.Path}
	 */
	@Override
	protected void checkForStuck(Vec3d positionVec3) {
		if ((totalTicks - ticksAtLastPos) > 100) {
			if (positionVec3.squareDistanceTo(lastPosCheck) < 2.25D) {
				// we probably can adapt this to handle code vs typical AI
				// pathing
				clearPathEntity();
			}

			ticksAtLastPos = totalTicks;
			lastPosCheck = positionVec3;
		}
	}

	// this was supposed to be used for deactivating robots but they could
	// potentially jam up mazes and activities so this isnt actually helpful
	public BlockPos findOpenSpace(int radius) {
		BlockPos curPos = entity.getPosition();
		if (world.isAirBlock(curPos)) {
			return curPos;
		}
		Map<BlockPos, Double> airBlocks = Maps.newTreeMap();
		for (int x = curPos.getX() - (radius / 2); Math.abs(x - curPos.getX()) <= (radius / 2); x++) {
			for (int y = curPos.getY() - (radius / 2); Math.abs(y - curPos.getY()) <= (radius / 2); y++) {
				for (int z = curPos.getZ() - (radius / 2); Math.abs(z - curPos.getZ()) <= (radius / 2); z++) {
					if (world.isAirBlock(new BlockPos(x, y, z))) {
						airBlocks.put(new BlockPos(x, y, z), curPos.distanceSqToCenter(x, y, z));
					}
				}
			}
		}

		double closest = 1000;
		BlockPos retPos = null;
		for (BlockPos key : airBlocks.keySet()) {
			if (super.getPathToPos(key) != null) {
				if (closest > airBlocks.get(key)) {
					System.out.println(key + ", " + airBlocks.get(key));
					retPos = key;
					closest = airBlocks.get(key);
				}
			}
		}

		return retPos;
	}

	@Override
	public Path getPathToEntityLiving(Entity entityIn) {
		Path path = super.getPathToEntityLiving(entityIn);
		return path;
	}

	@Override
	public Path getPathToPos(BlockPos pos) {
		Path path = super.getPathToPos(pos);
		return path;
	}

	@Override
	protected boolean isDirectPathBetweenPoints(Vec3d posVec31, Vec3d posVec32, int sizeX, int sizeY, int sizeZ) {
		if (!((EntityRobot) super.entity).getIsFollowing()) {
			return false;
		} else {
			return super.isDirectPathBetweenPoints(posVec31, posVec32, sizeX, sizeY, sizeZ);
		}
	}

	// @Override
	// protected void pathFollow() {
	// Vec3d Vec3d = getEntityPosition();
	// int i = currentPath.getCurrentPathLength();
	//
	// for (int j = currentPath.getCurrentPathIndex(); j <
	// currentPath.getCurrentPathLength(); ++j) {
	// if (currentPath.getPathPointFromIndex(j).y != (int) vec3.y) {
	// i = j;
	// break;
	// }
	// }
	//
	// float f = entity.width * entity.width * heightRequirement;
	//
	// for (int k = currentPath.getCurrentPathIndex(); k < i; ++k) {
	// Vec3d vec31 = currentPath.getVectorFromIndex(entity, k);
	//
	// if (vec3.squareDistanceTo(vec31) < f) {
	// currentPath.setCurrentPathIndex(k + 1);
	// }
	// }
	//
	// int j1 = MathHelper.ceil(entity.width);
	// int k1 = (int) entity.height + 1;
	// int l = j1;
	//
	// for (int i1 = i - 1; i1 >= currentPath.getCurrentPathIndex(); --i1) {
	// if (isDirectPathBetweenPoints(vec3, currentPath.getVectorFromIndex(entity,
	// i1), j1, k1, l)) {
	// currentPath.setCurrentPathIndex(i1);
	// break;
	// }
	// }
	//
	// checkForStuck(vec3);
	// }
}
