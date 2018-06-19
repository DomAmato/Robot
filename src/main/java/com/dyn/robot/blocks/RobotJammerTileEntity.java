package com.dyn.robot.blocks;

import java.util.List;

import com.dyn.robot.entity.EntityRobot;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class RobotJammerTileEntity extends TileEntity implements ITickable {

	protected static final AxisAlignedBB JAMMER_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.4D, 1.0D);
	private int radius = 5;

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return RobotJammerTileEntity.JAMMER_AABB;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound tagCompound = new NBTTagCompound();
		writeToNBT(tagCompound);
		return new SPacketUpdateTileEntity(pos, getBlockMetadata(), tagCompound);
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	public void markForUpdate() {
		world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
		markDirty();
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		radius = nbttagcompound.getInteger("radius");

	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return (oldState.getBlock() != newSate.getBlock());
	}

	@Override
	public void update() {
		if (world.getBlockState(getPos()).getValue(BlockRobotJammer.POWERED)) {
			List<EntityRobot> robots = getWorld().getEntitiesWithinAABB(EntityRobot.class,
					new AxisAlignedBB(getPos()).grow(radius));
			for (EntityRobot robot : robots) {
				robot.stopExecutingCode();
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("radius", radius);
		return nbttagcompound;
	}
}
