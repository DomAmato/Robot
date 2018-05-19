package com.dyn.robot.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class RobotBlockTileEntity extends TileEntity {

	private NBTTagList inventory = new NBTTagList();
	private String robotName = "";
	private String prevScript = "";

	public NBTTagList getInventory() {
		return inventory;
	}

	public String getPrevScript() {
		return prevScript;
	}

	public String getRobotName() {
		return robotName;
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
		inventory = nbttagcompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
		robotName = nbttagcompound.getString("robotName");
		prevScript = nbttagcompound.getString("code");

	}

	public void setInventory(NBTTagList inventory) {
		this.inventory = inventory;
	}

	public void setPrevScript(String prevScript) {
		this.prevScript = prevScript;
	}

	public void setRobotName(String robotName) {
		this.robotName = robotName;
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return (oldState.getBlock() != newSate.getBlock());
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setTag("Items", inventory);

		nbttagcompound.setString("robotName", robotName);
		nbttagcompound.setString("code", prevScript);
		return nbttagcompound;
	}
}
