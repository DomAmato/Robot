package com.dom.robot.entity.ai;

import com.dom.robot.entity.EntityRobot;

import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityAIFollowsOwnerEX extends EntityAIBase {
	private EntityRobot follower;
	World world;
	private double followSpeed;
	private PathNavigate entityPath;
	private int ticks;
	float maxDist;
	float minDist;

	public EntityAIFollowsOwnerEX(EntityRobot follower, double followSpeedIn, float minDistIn, float maxDistIn) {
		this.follower = follower;
		world = follower.world;
		followSpeed = followSpeedIn;
		entityPath = follower.getNavigator();
		minDist = minDistIn;
		maxDist = maxDistIn;
		setMutexBits(3);

		if (!(follower.getNavigator() instanceof PathNavigateGround)) {
			throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
		}
	}

	protected boolean isTeleportFriendlyBlock(int x, int p_192381_2_, int y, int p_192381_4_, int p_192381_5_) {
		BlockPos blockpos = new BlockPos(x + p_192381_4_, y - 1, p_192381_2_ + p_192381_5_);
		IBlockState iblockstate = world.getBlockState(blockpos);
		return (iblockstate.getBlockFaceShape(world, blockpos, EnumFacing.DOWN) == BlockFaceShape.SOLID)
				&& world.isAirBlock(blockpos.up());
	}

	/**
	 * Resets the task
	 */
	@Override
	public void resetTask() {
		entityPath.clearPathEntity();
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	@Override
	public boolean shouldContinueExecuting() {
		return !entityPath.noPath() && (follower.getDistanceSqToEntity(follower.getOwner()) > (maxDist * maxDist));
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public boolean shouldExecute() {
		if ((follower.getOwner() == null)) {
			return false;
		} else if (!follower.getIsFollowing()) {
			return false;
		} else if ((follower.getOwner() instanceof EntityPlayer) && follower.getOwner().isSpectator()) {
			return false;
		} else if (follower.getDistanceSqToEntity(follower.getOwner()) < (minDist * minDist)) {
			return false;
		}
		return true;
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void startExecuting() {
		ticks = 0;
	}

	/**
	 * Updates the task
	 */
	@Override
	public void updateTask() {
		follower.getLookHelper().setLookPositionWithEntity(follower.getOwner(), 10.0F, follower.getVerticalFaceSpeed());

		if (--ticks <= 0) {
			ticks = 10;
			if (!entityPath.tryMoveToEntityLiving(follower.getOwner(), followSpeed)) {
				if (follower.getDistanceSqToEntity(follower.getOwner()) >= 144.0D) {
					int i = MathHelper.floor(follower.getOwner().posX) - 2;
					int j = MathHelper.floor(follower.getOwner().posZ) - 2;
					int k = MathHelper.floor(follower.getOwner().getEntityBoundingBox().minY);

					for (int l = 0; l <= 4; ++l) {
						for (int i1 = 0; i1 <= 4; ++i1) {
							if (((l < 1) || (i1 < 1) || (l > 3) || (i1 > 3))
									&& isTeleportFriendlyBlock(i, j, k, l, i1)) {
								follower.setLocationAndAngles(i + l + 0.5F, k, j + i1 + 0.5F, follower.rotationYaw,
										follower.rotationPitch);
								entityPath.clearPathEntity();
								return;
							}
						}
					}
				}
			}
		}
	}
}
