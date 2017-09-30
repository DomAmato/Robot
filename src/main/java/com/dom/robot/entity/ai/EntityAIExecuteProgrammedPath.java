package com.dom.robot.entity.ai;

import com.dom.rjm.network.CodeEvent;
import com.dom.robot.RobotMod;
import com.dom.robot.entity.EntityRobot;
import com.dom.robot.utils.HelperFunctions;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;

public class EntityAIExecuteProgrammedPath extends EntityAIBase {
	private EntityRobot entity;
	private double speed;
	private boolean notifySuccess = false;
	private double prevDist = 0;
	private int watchDog = 30;
	private PathNavigate entityPath;
	private boolean clampX, clampZ, isVertical;

	/** Block to move to */
	private BlockPos destination = BlockPos.ORIGIN;
	private BlockPos prevDestination = destination;

	public EntityAIExecuteProgrammedPath(EntityRobot entity, double speed) {
		this.entity = entity;
		this.speed = speed;
		entityPath = entity.getNavigator();
		setMutexBits(7);
	}

	/**
	 * Resets the task
	 */
	@Override
	public void resetTask() {
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	@Override
	public boolean shouldContinueExecuting() {
		// run as long as commands exist in the buffer and keep running until
		// the script is finished, if our watch dog fails timeout and fail
		boolean doContinue = (watchDog > 0) && (entity.shouldExecuteCode()
				|| (!entity.getProgramPath().isEmpty() && !entity.getNavigator().noPath()));
		if (!doContinue) {
			if (watchDog <= 0) {
				MinecraftForge.EVENT_BUS
						.post(new CodeEvent.FailEvent("Watchdog timed out", entity.getEntityId(), entity.getOwner()));
				notifySuccess = false;
				entity.stopExecutingCode();
				entity.clearProgramPath();
				entity.setPosition(prevDestination.getX() + .5, prevDestination.getY(), prevDestination.getZ() + .5);
				entity.rotate(HelperFunctions.getAngleFromFacing(entity.getProgrammedDirection()));
			}
			if (notifySuccess) {
				if (entity.getPosition().equals(destination)) {
					MinecraftForge.EVENT_BUS
							.post(new CodeEvent.RobotSuccessEvent("Success", entity.getEntityId(), entity.getOwner()));
					notifySuccess = false;
					entityPath.clearPathEntity();
					entity.setPosition(prevDestination.getX() + .5, prevDestination.getY(),
							prevDestination.getZ() + .5);
					entity.rotate(HelperFunctions.getAngleFromFacing(entity.getProgrammedDirection()));
				} else {
					MinecraftForge.EVENT_BUS.post(new CodeEvent.FailEvent("Failed to reach destination",
							entity.getEntityId(), entity.getOwner()));
					notifySuccess = false;
					entity.stopExecutingCode();
					entity.clearProgramPath();
					entity.setPosition(prevDestination.getX() + .5, prevDestination.getY(),
							prevDestination.getZ() + .5);
					entity.rotate(HelperFunctions.getAngleFromFacing(entity.getProgrammedDirection()));
				}
			}
			RobotMod.logger.info("Stop AI Path Execution");
		}
		return doContinue;
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
		RobotMod.logger.info("Start AI Path Execution");
		prevDestination = destination = entity.getPosition();
		watchDog = 30;
	}

	/**
	 * Updates the task
	 */
	@Override
	public void updateTask() {
		if (!entity.isCodePaused()) {
			if (!entityPath.noPath()) {
				if (!destination.equals(new BlockPos(entityPath.getPath().getFinalPathPoint().x,
						entityPath.getPath().getFinalPathPoint().y, entityPath.getPath().getFinalPathPoint().z))) {
					destination = new BlockPos(entityPath.getPath().getFinalPathPoint().x,
							entityPath.getPath().getFinalPathPoint().y, entityPath.getPath().getFinalPathPoint().z);
				}
			}
			double dist = entity.getDistanceSqToCenter(destination);
			// double dist = getClampedDistance();
			if (!entityPath.noPath() || (dist > .365D)) {
				if (clampX) {
					// moving in the Z or Y plane
					entity.motionX = 0;
				}
				if (clampZ) {
					// moving in the X or Y plane
					entity.motionZ = 0;
				}
				if (isVertical) {
					entity.motionY = entity.posY < destination.getY() ? .3 : -.3;
					if (!clampX || !clampZ) {
						entity.getMoveHelper().setMoveTo((destination.getX()) + 0.5D, destination.getY(),
								(destination.getZ()) + 0.5D, speed);
					}
				}
				if (entityPath.noPath()) {
					entity.getMoveHelper().setMoveTo((destination.getX()) + 0.5D, destination.getY(),
							(destination.getZ()) + 0.5D, speed);
				}
				if (prevDist != dist) {
					prevDist = dist;
				} else {
					// the entity made no progress is it stuck?
					watchDog--;
				}
			} else {
				// only update when execution is not paused
				if (!entity.getProgramPath().isEmpty()) {
					RobotMod.logger.info("Executing next part of Program Path");
					clampX = clampZ = isVertical = false;
					notifySuccess = true;
					watchDog = 30;
					prevDestination = destination;

					destination = entity.getProgramPath().iterator().next();
					entity.getProgramPath().remove(destination);
					// heres the problem... the ai isnt smart enough to climb
					if (entity.getPosition().getX() == destination.getX()) {
						clampX = true;
					}
					if (entity.getPosition().getZ() == destination.getZ()) {
						clampZ = true;
					}
					if (clampX && clampZ) {
						isVertical = true;
					}

					if (!entityPath.tryMoveToXYZ((destination.getX()), (destination.getY()), (destination.getZ()),
							speed)) {
						RobotMod.logger.info("Could not get path to: " + destination);
						if (entity.getPosition().getY() != destination.getY()) {
							entity.getMoveHelper().setMoveTo((destination.getX()) + 0.5D, destination.getY() + 0.5D,
									(destination.getZ()) + 0.5D, speed);
							isVertical = true;
						} else {
							MinecraftForge.EVENT_BUS.post(new CodeEvent.FailEvent("Failed trying to set destination",
									entity.getEntityId(), entity.getOwner()));
							notifySuccess = false;
							entity.setPosition(prevDestination.getX() + .5, prevDestination.getY(),
									prevDestination.getZ() + .5);
							entity.rotate(HelperFunctions.getAngleFromFacing(entity.getProgrammedDirection()));
							RobotMod.logger.info("Stopping Code from path");
							entity.stopExecutingCode();
							entity.clearProgramPath();
						}
					}
				} else if (notifySuccess) {
					RobotMod.logger.info("Notifying Success");
					// the program path is empty lets send a 1 time event
					MinecraftForge.EVENT_BUS
							.post(new CodeEvent.RobotSuccessEvent("Success", entity.getEntityId(), entity.getOwner()));
					entity.rotate(HelperFunctions.getAngleFromFacing(entity.getProgrammedDirection()));

					entityPath.clearPathEntity();
					notifySuccess = false;
				}
			}
		}
	}
}