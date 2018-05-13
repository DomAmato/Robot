package com.dyn.robot.entity.ai;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.network.CodeEvent;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;

public class EntityAIExecuteProgrammedPath extends EntityAIBase {
	private EntityRobot robot;
	private double speed;
	private boolean notifySuccess = false;
	private double prevDist = 0;
	private int watchDog = 30;
	private PathNavigate entityPath;
	private boolean clampX, clampZ, isVertical;

	/** Block to move to */
	private BlockPos destination = BlockPos.ORIGIN;
	private BlockPos prevDestination = destination;

	public EntityAIExecuteProgrammedPath(EntityRobot robot, double speed) {
		this.robot = robot;
		this.speed = speed;
		entityPath = robot.getNavigator();
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
		boolean doContinue = (watchDog > 0) && (robot.shouldExecuteCode()
				|| (!robot.getProgramPath().isEmpty() && !robot.getNavigator().noPath()));
		if (!doContinue) {
			if (watchDog <= 0) {
				MinecraftForge.EVENT_BUS
						.post(new CodeEvent.FailEvent("Watchdog timed out", robot.getEntityId(), robot.getOwner()));
				notifySuccess = false;
				robot.stopExecutingCode();
				robot.clearProgramPath();
				robot.setPosition(prevDestination.getX() + .5, prevDestination.getY(), prevDestination.getZ() + .5);
				robot.rotate(robot.getProgrammedDirection().getHorizontalAngle());
			}
			if (notifySuccess) {
				if (robot.getPosition().equals(destination)) {
					RobotMod.logger.info("Notifying Success End");
					MinecraftForge.EVENT_BUS
							.post(new CodeEvent.RobotSuccessEvent("Success", robot.getEntityId(), robot.getOwner()));
					notifySuccess = false;
					entityPath.clearPath();
					robot.setPosition(prevDestination.getX() + .5, prevDestination.getY(),
							prevDestination.getZ() + .5);
					robot.rotate(robot.getProgrammedDirection().getHorizontalAngle());
				} else {
					MinecraftForge.EVENT_BUS.post(new CodeEvent.FailEvent("Failed to reach destination",
							robot.getEntityId(), robot.getOwner()));
					notifySuccess = false;
					robot.stopExecutingCode();
					robot.clearProgramPath();
					robot.setPosition(prevDestination.getX() + .5, prevDestination.getY(),
							prevDestination.getZ() + .5);
					robot.rotate(robot.getProgrammedDirection().getHorizontalAngle());
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
		return robot.shouldExecuteCode();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void startExecuting() {
		RobotMod.logger.info("Start AI Path Execution");
		prevDestination = destination = robot.getPosition();
		watchDog = 30;
	}

	/**
	 * Updates the task
	 */
	@Override
	public void updateTask() {
		if (!robot.isCodePaused()) {
			if (!entityPath.noPath()) {
				if (!destination.equals(new BlockPos(entityPath.getPath().getFinalPathPoint().x,
						entityPath.getPath().getFinalPathPoint().y, entityPath.getPath().getFinalPathPoint().z))) {
					destination = new BlockPos(entityPath.getPath().getFinalPathPoint().x,
							entityPath.getPath().getFinalPathPoint().y, entityPath.getPath().getFinalPathPoint().z);
				}
			}
			double dist = robot.getDistanceSqToCenter(destination);
			// double dist = getClampedDistance();
			if (!entityPath.noPath() || (dist > .365D)) {
				if (clampX) {
					// moving in the Z or Y plane
					robot.motionX = 0;
				}
				if (clampZ) {
					// moving in the X or Y plane
					robot.motionZ = 0;
				}
				if (isVertical) {
					robot.motionY = robot.posY < destination.getY() ? .3 : -.3;
					if (!clampX || !clampZ) {
						robot.getMoveHelper().setMoveTo((destination.getX()) + 0.5D, destination.getY(),
								(destination.getZ()) + 0.5D, speed);
					}
				}
				if (entityPath.noPath()) {
					robot.getMoveHelper().setMoveTo((destination.getX()) + 0.5D, destination.getY(),
							(destination.getZ()) + 0.5D, speed);
				}
				if (prevDist - dist < 0.1) {
					prevDist = dist;
				} else {
					// the entity made no progress is it stuck?
					watchDog--;
				}
			} else {
				// only update when execution is not paused
				if (!robot.getProgramPath().isEmpty()) {
					RobotMod.logger.info("Executing next part of Program Path");
					clampX = clampZ = isVertical = false;
					notifySuccess = true;
					watchDog = 30;
					prevDestination = destination;

					destination = robot.getProgramPath().iterator().next();
					robot.getProgramPath().remove(destination);
					// heres the problem... the ai isnt smart enough to climb
					if (robot.getPosition().getX() == destination.getX()) {
						clampX = true;
					}
					if (robot.getPosition().getZ() == destination.getZ()) {
						clampZ = true;
					}
					if (clampX && clampZ) {
						isVertical = true;
					}

					if (!entityPath.tryMoveToXYZ((destination.getX()), (destination.getY()), (destination.getZ()),
							speed)) {
						RobotMod.logger.debug("Could not get path to: " + destination);
						if (robot.getPosition().getY() != destination.getY()) {
							robot.getMoveHelper().setMoveTo((destination.getX()) + 0.5D, destination.getY() + 0.5D,
									(destination.getZ()) + 0.5D, speed);
							isVertical = true;
						} else {
							MinecraftForge.EVENT_BUS.post(new CodeEvent.FailEvent("Failed trying to set destination",
									robot.getEntityId(), robot.getOwner()));
							notifySuccess = false;
							robot.setPosition(prevDestination.getX() + .5, prevDestination.getY(),
									prevDestination.getZ() + .5);
							robot.rotate(robot.getProgrammedDirection().getHorizontalAngle());
							RobotMod.logger.debug("Stopping Code from path");
							robot.stopExecutingCode();
							robot.clearProgramPath();
						}
					}
				} else if (notifySuccess) {
					RobotMod.logger.info("Notifying Success");
					// the program path is empty lets send a 1 time event
					MinecraftForge.EVENT_BUS
							.post(new CodeEvent.RobotSuccessEvent("Success", robot.getEntityId(), robot.getOwner()));
					robot.rotate(robot.getProgrammedDirection().getHorizontalAngle());

					entityPath.clearPath();
					notifySuccess = false;
				}
			}
		}
	}
}