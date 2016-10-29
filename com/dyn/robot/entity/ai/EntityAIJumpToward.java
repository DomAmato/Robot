package com.dyn.robot.entity.ai;

import com.dyn.robot.entity.EntityRobot;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

public class EntityAIJumpToward extends EntityAIBase {
	/** The entity that is leaping. */
	EntityRobot leaper;
	/** The entity that the leaper is leaping towards. */
	BlockPos leapTarget;
	/** The entity's motionY after leaping. */
	float leapMotionY;

	public EntityAIJumpToward(EntityRobot leapingEntity, float leapMotionYIn) {
		leaper = leapingEntity;
		leapMotionY = leapMotionYIn;
		setMutexBits(5);
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	@Override
	public boolean continueExecuting() {
		return !leaper.onGround;
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public boolean shouldExecute() {
		if (leaper.getShouldJump()) {
			leaper.setShouldJump(false);
			return true;
		}
		return false;
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void startExecuting() {
		leapTarget = leaper.getPosition().offset(leaper.getHorizontalFacing(), 2);
		double d0 = (leapTarget.getX() + .5) - leaper.posX;
		double d1 = (leapTarget.getZ() + .5) - leaper.posZ;
		float f = MathHelper.sqrt_double((d0 * d0) + (d1 * d1));
		leaper.motionX += ((d0 / f) * 0.5D * 0.800000011920929D) + (leaper.motionX * 0.20000000298023224D);
		leaper.motionZ += ((d1 / f) * 0.5D * 0.800000011920929D) + (leaper.motionZ * 0.20000000298023224D);
		leaper.motionY = leapMotionY;
	}
}