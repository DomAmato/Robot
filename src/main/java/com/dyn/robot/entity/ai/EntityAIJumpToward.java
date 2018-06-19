package com.dyn.robot.entity.ai;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.network.CodeEvent;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;

public class EntityAIJumpToward extends EntityAIBase {

	/** The entity that is leaping. */
	EntityRobot robot;
	/** The entity that the robot is leaping towards. */
	BlockPos leapTarget;
	/** The entity's motionY after leaping. */
	float leapMotionY;

	public EntityAIJumpToward(EntityRobot leapingEntity, float leapMotionYIn) {
		robot = leapingEntity;
		leapMotionY = leapMotionYIn;
		setMutexBits(8);
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	@Override
	public boolean shouldContinueExecuting() {
		boolean doContinue = !robot.onGround;
		if (!doContinue) {
			if (robot.getProgramPath().iterator().hasNext()) {
				BlockPos prevLoc = robot.getProgramPath().iterator().next();
				robot.getProgramPath().remove(prevLoc);
			}
			PathPoint[] landPoint = {
					new PathPoint(robot.getPosition().getX(), robot.getPosition().getY(), robot.getPosition().getZ()) };
			robot.getNavigator().setPath(new Path(landPoint), 1.5D);
			final float newYaw = MathHelper.wrapDegrees(robot.getProgrammedDirection().getHorizontalAngle());
			robot.setPosition(robot.getPosition().getX() + .5, robot.getPosition().getY(),
					robot.getPosition().getZ() + .5);
			robot.motionX = 0;
			robot.motionZ = 0;
			robot.motionY = 0;
			robot.rotate(newYaw);
			MinecraftForge.EVENT_BUS
					.post(new CodeEvent.RobotSuccessEvent("Success", robot.getEntityId(), robot.getOwner()));
			robot.resumeExecution();
		}
		return doContinue;
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public boolean shouldExecute() {
		if (robot.getShouldJump()) {
			robot.setShouldJump(false);
			// the path should be clear anyways but on that chance that it isn't we should
			// clear it
			robot.clearProgramPath();
			robot.pauseCodeExecution();
			return true;
		}
		return false;
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void startExecuting() {
		leapTarget = robot.getPosition().offset(robot.getProgrammedDirection(), 2);
		RobotMod.logger
				.debug(robot.getRobotName() + " Jumping Towards: " + leapTarget + " from " + robot.getPosition());
		robot.rotate(MathHelper.wrapDegrees(robot.getProgrammedDirection().getHorizontalAngle()));
		double d0 = (leapTarget.getX() + .5) - robot.posX;
		double d1 = (leapTarget.getZ() + .5) - robot.posZ;
		float f = MathHelper.sqrt((d0 * d0) + (d1 * d1));
		robot.motionX += ((d0 / f) * 0.5D * 0.800000011920929D) + (robot.motionX * 0.20000000298023224D);
		robot.motionZ += ((d1 / f) * 0.5D * 0.800000011920929D) + (robot.motionZ * 0.20000000298023224D);
		robot.motionY = leapMotionY;
	}
}