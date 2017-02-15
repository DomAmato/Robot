package com.dyn.robot.entity.pathing;

import java.util.List;
import java.util.Map;

import com.dyn.DYNServerMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.server.network.NetworkManager;
import com.dyn.server.network.packets.client.RobotPathMessage;
import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class PathNavigateRobot extends PathNavigateGround {

	/** Time, in number of ticks, following the current path */
	private int totalTicks;
	/**
	 * The time when the last position check was done (to detect successful
	 * movement)
	 */
	private int ticksAtLastPos;

	/**
	 * Coordinates of the entity's position last time a check was done (part of
	 * monitoring getting 'stuck')
	 */
	private Vec3 lastPosCheck = new Vec3(0.0D, 0.0D, 0.0D);
	private float heightRequirement = 1.0F;

	public PathNavigateRobot(EntityLiving entityLivingIn, World worldIn) {
		super(entityLivingIn, worldIn);
	}

	/**
	 * Checks if entity haven't been moved when last checked and if so, clears
	 * current {@link net.minecraft.pathfinding.PathEntity}
	 */
	@Override
	protected void checkForStuck(Vec3 positionVec3) {
		if ((totalTicks - ticksAtLastPos) > 100) {
			if (positionVec3.squareDistanceTo(lastPosCheck) < 2.25D) {
				// we probably can adapt this to handle code vs typical AI
				// pathing
				clearPathEntity();
			}

			ticksAtLastPos = totalTicks;
			lastPosCheck = positionVec3;
		}
	}

	// this was supposed to be used for deactivating robots but they could
	// potentially jam up mazes and activities so this isnt actually helpful
	public BlockPos findOpenSpace(int radius) {
		BlockPos curPos = theEntity.getPosition();
		if (worldObj.isAirBlock(curPos)) {
			return curPos;
		}
		Map<BlockPos, Double> airBlocks = Maps.newTreeMap();
		for (int x = curPos.getX() - (radius / 2); Math.abs(x - curPos.getX()) <= (radius / 2); x++) {
			for (int y = curPos.getY() - (radius / 2); Math.abs(y - curPos.getY()) <= (radius / 2); y++) {
				for (int z = curPos.getZ() - (radius / 2); Math.abs(z - curPos.getZ()) <= (radius / 2); z++) {
					if (worldObj.isAirBlock(new BlockPos(x, y, z))) {
						airBlocks.put(new BlockPos(x, y, z), curPos.distanceSqToCenter(x, y, z));
					}
				}
			}
		}

		for (BlockPos key : airBlocks.keySet()) {
			System.out.println(key + ", " + airBlocks.get(key));
		}

		double closest = 1000;
		BlockPos retPos = null;
		for (BlockPos key : airBlocks.keySet()) {
			if (super.getPathToPos(key) != null) {
				if (closest > airBlocks.get(key)) {
					System.out.println(key + ", " + airBlocks.get(key));
					retPos = key;
					closest = airBlocks.get(key);
				}
			}
		}

		return retPos;
	}

	@Override
	public PathEntity getPathToEntityLiving(Entity entityIn) {
		PathEntity path = super.getPathToEntityLiving(entityIn);
		if ((path != null) && DYNServerMod.developmentEnvironment) {
			NetworkManager.sendToDimension(new RobotPathMessage(path, super.theEntity.getEntityId()),
					entityIn.dimension);
		}
		return path;
	}

	@Override
	public PathEntity getPathToPos(BlockPos pos) {
		PathEntity path = super.getPathToPos(pos);
		if ((path != null) && DYNServerMod.developmentEnvironment) {
			NetworkManager.sendToDimension(new RobotPathMessage(path, super.theEntity.getEntityId()),
					super.theEntity.dimension);
		}
		return path;
	}

	@Override
	protected boolean isDirectPathBetweenPoints(Vec3 posVec31, Vec3 posVec32, int sizeX, int sizeY, int sizeZ) {
		if (!((EntityRobot) super.theEntity).getIsFollowing()) {
			return false;
		} else {
			return super.isDirectPathBetweenPoints(posVec31, posVec32, sizeX, sizeY, sizeZ);
		}
	}

	@Override
	public void onUpdateNavigation() {
		++totalTicks;

		if (!noPath()) {
			if (canNavigate()) {
				pathFollow();
			} else if ((currentPath != null)
					&& (currentPath.getCurrentPathIndex() < currentPath.getCurrentPathLength())) {
				Vec3 vec3 = getEntityPosition();
				Vec3 vec31 = currentPath.getVectorFromIndex(theEntity, currentPath.getCurrentPathIndex());

				if ((vec3.yCoord > vec31.yCoord) && !theEntity.onGround
						&& (MathHelper.floor_double(vec3.xCoord) == MathHelper.floor_double(vec31.xCoord))
						&& (MathHelper.floor_double(vec3.zCoord) == MathHelper.floor_double(vec31.zCoord))) {
					currentPath.setCurrentPathIndex(currentPath.getCurrentPathIndex() + 1);
				}
			}

			if (!noPath()) {
				Vec3 vec32 = currentPath.getPosition(theEntity);

				if (vec32 != null) {
					AxisAlignedBB axisalignedbb1 = (new AxisAlignedBB(vec32.xCoord, vec32.yCoord, vec32.zCoord,
							vec32.xCoord, vec32.yCoord, vec32.zCoord)).expand(0.5D, 0.5D, 0.5D);
					List<AxisAlignedBB> list = worldObj.getCollidingBoundingBoxes(theEntity,
							axisalignedbb1.addCoord(0.0D, -1.0D, 0.0D));
					double d0 = -1.0D;
					axisalignedbb1 = axisalignedbb1.offset(0.0D, 1.0D, 0.0D);

					for (AxisAlignedBB axisalignedbb : list) {
						d0 = axisalignedbb.calculateYOffset(axisalignedbb1, d0);
					}

					theEntity.getMoveHelper().setMoveTo(vec32.xCoord, vec32.yCoord + d0, vec32.zCoord, speed);
				}
			}
		}
	}

	@Override
	protected void pathFollow() {
		Vec3 vec3 = getEntityPosition();
		int i = currentPath.getCurrentPathLength();

		for (int j = currentPath.getCurrentPathIndex(); j < currentPath.getCurrentPathLength(); ++j) {
			if (currentPath.getPathPointFromIndex(j).yCoord != (int) vec3.yCoord) {
				i = j;
				break;
			}
		}

		float f = theEntity.width * theEntity.width * heightRequirement;

		for (int k = currentPath.getCurrentPathIndex(); k < i; ++k) {
			Vec3 vec31 = currentPath.getVectorFromIndex(theEntity, k);

			if (vec3.squareDistanceTo(vec31) < f) {
				currentPath.setCurrentPathIndex(k + 1);
			}
		}

		int j1 = MathHelper.ceiling_float_int(theEntity.width);
		int k1 = (int) theEntity.height + 1;
		int l = j1;

		for (int i1 = i - 1; i1 >= currentPath.getCurrentPathIndex(); --i1) {
			if (isDirectPathBetweenPoints(vec3, currentPath.getVectorFromIndex(theEntity, i1), j1, k1, l)) {
				currentPath.setCurrentPathIndex(i1);
				break;
			}
		}

		checkForStuck(vec3);
	}

	/**
	 * Sets vertical space requirement for path
	 */
	@Override
	public void setHeightRequirement(float jumpHeight) {
		heightRequirement = jumpHeight;
	}

	/**
	 * Sets a new path. If it's diferent from the old path. Checks to adjust
	 * path for sun avoiding, and stores start coords. Args : path, speed
	 */
	@Override
	public boolean setPath(PathEntity pathentityIn, double speedIn) {
		if (pathentityIn == null) {
			currentPath = null;
			return false;
		} else {
			if (!pathentityIn.isSamePath(currentPath)) {
				currentPath = pathentityIn;
			}

			if (currentPath.getCurrentPathLength() == 0) {
				return false;
			} else {
				speed = speedIn;
				Vec3 vec3 = getEntityPosition();
				ticksAtLastPos = totalTicks;
				lastPosCheck = vec3;
				return true;
			}
		}
	}
}
