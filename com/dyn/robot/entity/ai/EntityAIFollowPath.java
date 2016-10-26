package com.dyn.robot.entity.ai;

import com.dyn.robot.api.RobotAPI;
import com.dyn.robot.entity.EntityRobot;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class EntityAIFollowPath extends EntityAIBase {
	private EntityRobot entity;
	private double speed;

	/** Block to move to */
	private boolean canReachDestination;
	private BlockPos destinationBlock = BlockPos.ORIGIN;
	private boolean reachedEnd;

	public EntityAIFollowPath(EntityRobot entity, double speed) {
		this.entity = entity;
		this.speed = speed;
		setMutexBits(7);
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	@Override
	public boolean continueExecuting() {
		if (!canReachDestination) {
			entity.clearProgramPath();
			entity.getNavigator().clearPathEntity();
			entity.beginExecuteCode(false);
			RobotAPI.notifyFailure("Cant Reach Destination, Path might be obscured");
			return false;
		} else if (reachedEnd) {
			// center it on top of the block since it never reaches the exact
			// center otherwise
			entity.setPositionAndRotation(destinationBlock.getX() + 0.5D, destinationBlock.getY(),
					destinationBlock.getZ() + .5D, getAngleFromFacing(entity.getHorizontalFacing()),
					entity.rotationPitch);
			entity.clearProgramPath();
			entity.getNavigator().clearPathEntity();
			entity.beginExecuteCode(false);
			return false;
		}
		// execute if path isnt empty and there is more program to execute
		return true;
	}

	private float getAngleFromFacing(EnumFacing dir) {
		switch (dir) {
		case SOUTH:
			return 0;
		case NORTH:
			return 180;
		case EAST:
			return 270;
		case WEST:
			return 90;
		default:
			return 0;
		}
	}

	/**
	 * Resets the task
	 */
	@Override
	public void resetTask() {
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public boolean shouldExecute() {
		if (entity.shouldExecuteCode() && !entity.getProgramPath().isEmpty() && entity.getNavigator().noPath()) {
			canReachDestination = true;
			reachedEnd = false;
			return true;
		}
		return false;
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void startExecuting() {
		destinationBlock = entity.getProgramPath().iterator().next();
		entity.getProgramPath().remove(destinationBlock);
		canReachDestination = entity.getNavigator().tryMoveToXYZ((destinationBlock.getX()) + 0.5D,
				(destinationBlock.getY()), (destinationBlock.getZ()) + 0.5D, speed);
	}

	/**
	 * Updates the task
	 */
	@Override
	public void updateTask() {
		if (entity.getDistanceSqToCenter(destinationBlock) > .35D) {
			// DYNServerMod.logger.info(entity.getDistanceSqToCenter(this.destinationBlock));
		} else {
			if (!entity.getProgramPath().isEmpty()) {
				destinationBlock = entity.getProgramPath().iterator().next();
				entity.getProgramPath().remove(destinationBlock);
				canReachDestination = entity.getNavigator().tryMoveToXYZ((destinationBlock.getX()) + 0.5D,
						(destinationBlock.getY()), (destinationBlock.getZ()) + 0.5D, speed);
			} else {
				// there is no more code left to execute and
				reachedEnd = true;
			}
		}
	}
}