package com.dyn.robot.entity.pathing;

import com.dyn.robot.entity.EntityRobot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
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
				clearPath();
			}

			ticksAtLastPos = totalTicks;
			lastPosCheck = positionVec3;
		}
	}

	@Override
	protected PathFinder getPathFinder() {
		nodeProcessor = new RobotNodeProcessor();
		nodeProcessor.setCanEnterDoors(true);
		return new PathFinder(nodeProcessor);
	}

	@Override
	public Path getPathToEntityLiving(Entity entityIn) {
		Path path = super.getPathToEntityLiving(entityIn);
		return path;
	}

	@Override
	public Path getPathToPos(BlockPos pos) {
		Path path = super.getPathToPos(pos);
		// the robot really only moves one block at a time during code execution
		// if ((path == null) && ((SimpleRobotEntity) entity).shouldExecuteCode()) {
		// Block block = world.getBlockState(pos).getBlock();
		// Block blockdn = world.getBlockState(pos.down()).getBlock();
		// if (!blockdn.isPassable(world, pos) && ((block == Blocks.LAVA) || (block ==
		// Blocks.FLOWING_LAVA))
		// && ((SimpleRobotEntity) entity).hasSuit()
		// && (((SimpleRobotEntity) entity).getSuit().getMetadata() == 1)) {
		// PathPoint[] points = { new PathPoint((int) entity.posX, (int) entity.posY,
		// (int) entity.posZ),
		// new PathPoint(pos.getX(), pos.getY(), pos.getZ()) };
		// path = new Path(points);
		// }
		// }
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
}
