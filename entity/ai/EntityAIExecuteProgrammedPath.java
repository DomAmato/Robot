package com.dyn.robot.entity.ai;

import com.dyn.DYNServerMod;
import com.dyn.robot.entity.EntityRobot;

import mobi.omegacentauri.raspberryjammod.RaspberryJamMod;
import mobi.omegacentauri.raspberryjammod.network.CodeEvent;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.BlockPos;

public class EntityAIExecuteProgrammedPath extends EntityAIBase {
	private EntityRobot entity;
	private double speed;
	private boolean notifySuccess = false;
	private double prevDist = 0;
	private int watchDog = 1;

	/** Block to move to */
	private BlockPos destination = BlockPos.ORIGIN;
	private BlockPos prevDestination = destination;

	public EntityAIExecuteProgrammedPath(EntityRobot entity, double speed) {
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
		// the script is finished, if our watch dog fails timeout and fail
		boolean doContinue = (watchDog > 0) && (entity.shouldExecuteCode()
				|| (!entity.getProgramPath().isEmpty() && !entity.getNavigator().noPath()));
		if (!doContinue) {
			if (watchDog <= 0) {
				RaspberryJamMod.EVENT_BUS
						.post(new CodeEvent.FailEvent("Watchdog timed out", entity.getEntityId(), entity.getOwner()));
				notifySuccess = false;
				entity.stopExecutingCode();
				entity.clearProgramPath();
				entity.getNavigator().clearPathEntity();
				entity.setPositionAndUpdate(prevDestination.getX() + .5, prevDestination.getY(),
						prevDestination.getZ() + .5);
			}
			if (notifySuccess) {
				if (entity.getPosition().equals(destination)) {
					RaspberryJamMod.EVENT_BUS
							.post(new CodeEvent.SuccessEvent("Success", entity.getEntityId(), entity.getOwner()));
					notifySuccess = false;
				} else {
					RaspberryJamMod.EVENT_BUS.post(new CodeEvent.FailEvent("Failed to reach destination",
							entity.getEntityId(), entity.getOwner()));
					notifySuccess = false;
					entity.stopExecutingCode();
					entity.clearProgramPath();
					entity.getNavigator().clearPathEntity();
					entity.setPositionAndUpdate(prevDestination.getX() + .5, prevDestination.getY(),
							prevDestination.getZ() + .5);
				}
			}
			DYNServerMod.logger.info("Stop AI Path Execution");
		}
		return doContinue;
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
		DYNServerMod.logger.info("Start AI Path Execution");
		prevDestination = destination = entity.getPosition();
		watchDog = 20;
	}

	/**
	 * Updates the task
	 */
	@Override
	public void updateTask() {
		if (entity.getDistanceSqToCenter(destination) > .35D) {
			if (prevDist != entity.getDistanceSqToCenter(destination)) {
				prevDist = entity.getDistanceSqToCenter(destination);
			} else {
				// the entity made no progress is it stuck?
				watchDog--;
			}
			// DYNServerMod.logger.info(entity.getDistanceSqToCenter(destination));
		} else if (!entity.isCodePaused()) {
			// only update when execution is not paused
			if (!entity.getProgramPath().isEmpty()) {
				notifySuccess = true;
				watchDog = 20;
				prevDestination = destination;
				destination = entity.getProgramPath().iterator().next();
				entity.getProgramPath().remove(destination);
				// heres the problem... the ai isnt smart enough to climb
				if (!entity.getNavigator().tryMoveToXYZ((destination.getX()) + 0.5D, (destination.getY()),
						(destination.getZ()) + 0.5D, speed)) {
					// we are moving up or down
					if (entity.getPosition().getY() != destination.getY()) {
						entity.getMoveHelper().setMoveTo((destination.getX()) + 0.5D, (destination.getY()) + 0.5D,
								(destination.getZ()) + 0.5D, speed);
					} else {
						RaspberryJamMod.EVENT_BUS.post(new CodeEvent.FailEvent("Failed trying to set destination",
								entity.getEntityId(), entity.getOwner()));
						notifySuccess = false;
						entity.setPositionAndUpdate(prevDestination.getX() + .5, prevDestination.getY(),
								prevDestination.getZ() + .5);
						DYNServerMod.logger.info("Stopping Code from path");
						entity.stopExecutingCode();
						entity.clearProgramPath();
						entity.getNavigator().clearPathEntity();
					}
				}
			} else if (notifySuccess) {
				// the program path is empty lets send a 1 time event
				RaspberryJamMod.EVENT_BUS
						.post(new CodeEvent.SuccessEvent("Success", entity.getEntityId(), entity.getOwner()));
				notifySuccess = false;
			}
		}
	}
}