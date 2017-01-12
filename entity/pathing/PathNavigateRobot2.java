package com.dyn.robot.entity.pathing;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class PathNavigateRobot2 extends PathNavigateGround {
	/** Current path navigation target */
	private Vec3 targetPosition;
	private ClimbNodeProcessor nodeProcessor;

	public PathNavigateRobot2(EntityLiving entityLivingIn, World worldIn) {
		super(entityLivingIn, worldIn);
	}

	@Override
	public boolean getAvoidsWater() {
		return nodeProcessor.getAvoidsWater();
	}

	@Override
	public boolean getCanSwim() {
		return nodeProcessor.getCanSwim();
	}

	public boolean getCanUseLadders() {
		return nodeProcessor.getCanUseLadders();
	}

	@Override
	public boolean getEnterDoors() {
		return nodeProcessor.getEnterDoors();
	}

	@Override
	protected PathFinder getPathFinder() {
		nodeProcessor = new ClimbNodeProcessor();
		return new PathFinder(nodeProcessor);
	}

	@Override
	/**
	 * Returns the path to the given EntityLiving. Args : entity
	 */
	public PathEntity getPathToEntityLiving(Entity entityIn) {
		targetPosition = new Vec3(entityIn.posX, entityIn.posY, entityIn.posZ);
		return super.getPathToEntityLiving(entityIn);
	}

	@Override
	/**
	 * Returns path to given BlockPos
	 */
	public PathEntity getPathToPos(BlockPos pos) {
		targetPosition = new Vec3(pos);
		return super.getPathToPos(pos);
	}

	@Override
	public void onUpdateNavigation() {
		if (!noPath()) {
			theEntity.width = 1;
			super.onUpdateNavigation();
			theEntity.width = 0.5f;
		} else {
			if (targetPosition != null) {
				double d0 = Math.max(1, (double) (theEntity.width * theEntity.width));
				Math.max(1, (double) (theEntity.height * theEntity.height));

				if (theEntity.getDistanceSqToCenter(new BlockPos(targetPosition)) > d0) {
					if ((getPathToPos(new BlockPos(targetPosition)) == null) && !theEntity.isOnLadder()) {

					}
					PathPoint newTarget = nodeProcessor.getSafePoint(theEntity, (int) targetPosition.xCoord,
							(int) targetPosition.yCoord, (int) targetPosition.zCoord, 1);
					if (newTarget != null) {
						theEntity.getMoveHelper().setMoveTo(newTarget.xCoord, newTarget.yCoord, newTarget.zCoord,
								speed);
					} else {
						targetPosition = null;
					}
				} else {
					targetPosition = null;
				}
			}
		}

		// if (!this.noPath()) {
		// if (this.canNavigate()) {
		// this.pathFollow();
		// }
		// if (!this.noPath()) {
		// final Vec3 vec3 = this.currentPath.getPosition(this.theEntity);
		// if (vec3 != null) {
		// this.theEntity.getMoveHelper().setMoveTo(vec3.xCoord, vec3.yCoord,
		// vec3.zCoord, this.speed);
		// }
		// }
		// }
		//
	}

	@Override
	public void setAvoidsWater(boolean avoidsWater) {
		nodeProcessor.setAvoidsWater(avoidsWater);
	}

	@Override
	public void setBreakDoors(boolean canBreakDoors) {
		nodeProcessor.setBreakDoors(canBreakDoors);
	}

	@Override
	public void setCanSwim(boolean canSwim) {
		nodeProcessor.setCanSwim(canSwim);
	}

	public void setCanUseLadders(boolean canUseLadders) {
		nodeProcessor.setCanUseLadders(canUseLadders);
	}

	@Override
	public void setEnterDoors(boolean par1) {
		nodeProcessor.setEnterDoors(par1);
	}
}
