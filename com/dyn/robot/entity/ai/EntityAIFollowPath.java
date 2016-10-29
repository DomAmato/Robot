package com.dyn.robot.entity.ai;

import com.dyn.robot.entity.EntityRobot;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.BlockPos;

public class EntityAIFollowPath extends EntityAIBase {
	private EntityRobot entity;
	private double speed;

	/** Block to move to */
	private BlockPos destinationBlock = BlockPos.ORIGIN;

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
		// run as long as commands exist in the buffer and keep running until
		// the script is finished
		return entity.shouldExecuteCode() || (!entity.getProgramPath().isEmpty() && !entity.getNavigator().noPath());
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
		return entity.shouldExecuteCode();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void startExecuting() {
		destinationBlock = entity.getPosition();
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
				if (!entity.getNavigator().tryMoveToXYZ((destinationBlock.getX()) + 0.5D, (destinationBlock.getY()),
						(destinationBlock.getZ()) + 0.5D, speed)) {
					entity.getMoveHelper().setMoveTo((destinationBlock.getX()) + 0.5D, (destinationBlock.getY()),
							(destinationBlock.getZ()) + 0.5D, speed);
				}
			}
		}
	}
}