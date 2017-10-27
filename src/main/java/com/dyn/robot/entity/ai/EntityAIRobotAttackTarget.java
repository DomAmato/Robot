package com.dyn.robot.entity.ai;

import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.network.CodeEvent;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class EntityAIRobotAttackTarget extends EntityAIBase {
	World world;
	protected EntityRobot robot;
	/**
	 * An amount of decrementing ticks that allows the entity to attack once the
	 * tick reaches 0.
	 */
	int attackTick;
	/** The speed with which the mob will approach the target */
	double speedTowardsTarget;
	/**
	 * When true, the mob will continue chasing its target, even if it can't find a
	 * path to them right now.
	 */
	boolean longMemory;
	/** The PathEntity of our entity. */
	Path entityPathEntity;
	Class<? extends Entity> classTarget;
	private int delayCounter;
	private double targetX;
	private double targetY;
	private double targetZ;
	private int failedPathFindingPenalty = 0;
	private boolean canPenalize = false;

	public EntityAIRobotAttackTarget(EntityRobot robot, Class<? extends Entity> targetClass, double speedIn,
			boolean useLongMemory) {
		this(robot, speedIn, useLongMemory);
		classTarget = targetClass;
	}

	public EntityAIRobotAttackTarget(EntityRobot robot, double speedIn, boolean useLongMemory) {
		this.robot = robot;
		world = robot.world;
		speedTowardsTarget = speedIn;
		longMemory = useLongMemory;
		setMutexBits(8);
	}

	protected double func_179512_a(EntityLivingBase attackTarget) {
		return (robot.width * 2.0F * robot.width * 2.0F) + attackTarget.width;
	}

	/**
	 * Resets the task
	 */
	@Override
	public void resetTask() {
		robot.getNavigator().clearPathEntity();
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	@Override
	public boolean shouldContinueExecuting() {
		EntityLivingBase target = robot.getAttackTarget();
		boolean doContinue = target == null ? false
				: (!target.isEntityAlive() ? false
						: (!longMemory ? !robot.getNavigator().noPath()
								: robot.isWithinHomeDistanceFromPosition(new BlockPos(target))));

		if (!doContinue) {
			if ((target != null) && target.isEntityAlive()) {
				MinecraftForge.EVENT_BUS
						.post(new CodeEvent.FailEvent("Failed to kill target", robot.getEntityId(), robot.getOwner()));
			} else {
				MinecraftForge.EVENT_BUS
						.post(new CodeEvent.RobotSuccessEvent("Success", robot.getEntityId(), robot.getOwner()));
			}
			robot.resumeExecution();
		}

		return doContinue;
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public boolean shouldExecute() {
		EntityLivingBase entitylivingbase = robot.getAttackTarget();

		if (entitylivingbase == null) {
			return false;
		} else if (!entitylivingbase.isEntityAlive()) {
			return false;
		} else if ((classTarget != null) && !classTarget.isAssignableFrom(entitylivingbase.getClass())) {
			return false;
		} else {
			if (canPenalize) {
				if (--delayCounter <= 0) {
					entityPathEntity = robot.getNavigator().getPathToEntityLiving(entitylivingbase);
					delayCounter = 4 + robot.getRNG().nextInt(7);
					return entityPathEntity != null;
				} else {
					return true;
				}
			}
			entityPathEntity = robot.getNavigator().getPathToEntityLiving(entitylivingbase);
			return entityPathEntity != null;
		}
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void startExecuting() {
		robot.getNavigator().setPath(entityPathEntity, speedTowardsTarget);
		delayCounter = 0;
		robot.pauseCodeExecution();
	}

	/**
	 * Updates the task
	 */
	@Override
	public void updateTask() {
		EntityLivingBase entitylivingbase = robot.getAttackTarget();
		robot.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);
		double d0 = robot.getDistanceSq(entitylivingbase.posX, entitylivingbase.getEntityBoundingBox().minY,
				entitylivingbase.posZ);
		double d1 = func_179512_a(entitylivingbase);
		--delayCounter;

		if ((longMemory || robot.getEntitySenses().canSee(entitylivingbase)) && (delayCounter <= 0)
				&& (((targetX == 0.0D) && (targetY == 0.0D) && (targetZ == 0.0D))
						|| (entitylivingbase.getDistanceSq(targetX, targetY, targetZ) >= 1.0D)
						|| (robot.getRNG().nextFloat() < 0.05F))) {
			targetX = entitylivingbase.posX;
			targetY = entitylivingbase.getEntityBoundingBox().minY;
			targetZ = entitylivingbase.posZ;
			delayCounter = 4 + robot.getRNG().nextInt(7);

			if (canPenalize) {
				delayCounter += failedPathFindingPenalty;
				if (robot.getNavigator().getPath() != null) {
					net.minecraft.pathfinding.PathPoint finalPathPoint = robot.getNavigator().getPath()
							.getFinalPathPoint();
					if ((finalPathPoint != null) && (entitylivingbase.getDistanceSq(finalPathPoint.x, finalPathPoint.y,
							finalPathPoint.z) < 1)) {
						failedPathFindingPenalty = 0;
					} else {
						failedPathFindingPenalty += 10;
					}
				} else {
					failedPathFindingPenalty += 10;
				}
			}

			if (d0 > 1024.0D) {
				delayCounter += 10;
			} else if (d0 > 256.0D) {
				delayCounter += 5;
			}

			if (!robot.getNavigator().tryMoveToEntityLiving(entitylivingbase, speedTowardsTarget)) {
				delayCounter += 15;
			}
		}

		attackTick = Math.max(attackTick - 1, 0);

		if ((d0 <= d1) && (attackTick <= 0)) {
			attackTick = 20;

			if (robot.getHeldItemMainhand() != null) {
				robot.swingArm(robot.swingingHand);
			}

			robot.attackEntityAsMob(entitylivingbase);
		}
	}
}