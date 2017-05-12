package com.dyn.robot.entity.ai;

import java.util.Map.Entry;
import java.util.Stack;

import com.dyn.fixins.items.ItemSchematic;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.schematics.BlockData;
import com.dyn.schematics.Schematic;

import mobi.omegacentauri.raspberryjammod.RaspberryJamMod;
import mobi.omegacentauri.raspberryjammod.network.CodeEvent;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntiyAIBuildSchematic extends EntityAIBase {
	private Stack<BlockData> placingList;
	private BlockData placing;
	private int tryTicks;
	private int ticks;
	private EntityRobot robot;

	private BlockPos startingPos;
	private int rotation;

	public EntiyAIBuildSchematic(EntityRobot robot) {
		this.robot = robot;
		placingList = new Stack<>();
		placing = null;
		tryTicks = 0;
		ticks = 0;
		rotation = 0;
		setMutexBits(8);
	}

	@Override
	public boolean continueExecuting() {
		boolean doContinue = ((placingList != null) && !placingList.isEmpty())
				|| (placingList.isEmpty() && (placing != null));
		if (!doContinue) {
			robot.setCurrentItemOrArmor(0, robot.robot_inventory.getStackInSlot(2));
			if (nearPosition(startingPos)) {
				RaspberryJamMod.EVENT_BUS
						.post(new CodeEvent.SuccessEvent("Success", robot.getEntityId(), robot.getOwner()));
			} else {
				return true;
			}
		}
		return doContinue;
	}

	private BlockPos getClosePos(BlockPos origin, World world) {
		for (int x = -1; x < 2; ++x) {
			for (int z = -1; z < 2; ++z) {
				for (int y = 2; y >= -2; --y) {
					BlockPos pos = origin.add(x, y, z);
					if (World.doesBlockHaveSolidTopSurface(world, pos) && world.isAirBlock(pos.up())
							&& world.isAirBlock(pos.up(2))) {
						return pos.up();
					}
				}
			}
		}
		return world.getTopSolidOrLiquidBlock(origin);
	}

	private boolean loadSchematic(int slot) {
		placingList.clear();
		BlockPos firstPos = startingPos.offset(robot.getProgrammedDirection()).down();
		if (!robot.robot_inventory.getStackInSlot(slot).hasTagCompound()) {
			RaspberryJamMod.EVENT_BUS
					.post(new CodeEvent.FailEvent("Schematic is empty", robot.getEntityId(), robot.getOwner()));
			return false;
		}
		Schematic schematic = ItemSchematic.getSchematic(robot.robot_inventory.getStackInSlot(slot));
		if (schematic == null) {
			RaspberryJamMod.EVENT_BUS
					.post(new CodeEvent.FailEvent("Schematic is null", robot.getEntityId(), robot.getOwner()));
			return false;
		}
		for (Entry<Block, Integer> block : schematic.getMaterialCosts().entrySet()) {
			int total = robot.robot_inventory.getQuantityOfItem(new ItemStack(block.getKey()));
			if (total < block.getValue()) {
				RaspberryJamMod.EVENT_BUS
						.post(new CodeEvent.FailEvent(
								"Not enough " + block.getKey().getLocalizedName() + " found in inventory. Requires: "
										+ block.getValue() + " found: " + total,
								robot.getEntityId(), robot.getOwner()));
				return false;
			}
		}

		for (int i = 0; i < schematic.blockArray.length; i++) {
			Block b = Block.getBlockById(schematic.blockArray[i]);
			if (b == null) {
				b = Blocks.air;
			}
			int meta = schematic.blockDataArray[i];
			int x = i % schematic.width;
			int z = ((i - x) / schematic.width) % schematic.length;
			int y = (((i - x) / schematic.width) - z) / schematic.length;
			BlockPos blockPos = firstPos.add(schematic.rotatePos(x, y, z, rotation));
			IBlockState original = robot.worldObj.getBlockState(blockPos);
			if (original.getBlock() == b) {
				if (b == Blocks.air) {
					continue;
				}
				if (b.getMetaFromState(original) == meta) {
					continue;
				}
			}
			IBlockState state = b.getStateFromMeta(meta);
			state = schematic.rotationState(state, rotation);
			NBTTagCompound tile = null;
			if (b instanceof ITileEntityProvider) {
				tile = schematic.getTileEntity(x, y, z, blockPos);
			}
			placingList.add(0, new BlockData(blockPos, state, tile));
		}
		return true;
	}

	public boolean nearPosition(BlockPos pos) {
		BlockPos npcpos = robot.getPosition();
		float x = npcpos.getX() - pos.getX();
		float z = npcpos.getZ() - pos.getZ();
		float y = npcpos.getY() - pos.getY();
		float height = MathHelper.ceiling_double_int(robot.height + 1.0f)
				* MathHelper.ceiling_double_int(robot.height + 1.0f);
		return (((x * x) + (z * z)) < 2.5) && ((y * y) < (height + 2.5));
	}

	public void placeBlock() {
		if (placing == null) {
			return;
		}
		robot.getNavigator().clearPathEntity();
		robot.swingItem();
		robot.robot_inventory.removeItemFromInventory(placing.getStack(), 1);
		robot.worldObj.setBlockState(placing.pos, placing.state, 3);
		if ((placing.state.getBlock() instanceof ITileEntityProvider) && (placing.tile != null)) {
			TileEntity tile = robot.worldObj.getTileEntity(placing.pos);
			if (tile != null) {
				tile.readFromNBT(placing.tile);
			}
		}
		placing = null;
	}

	@Override
	public void resetTask() {

	}

	@Override
	public boolean shouldExecute() {
		if (robot.shouldBuildSchematic()) {
			robot.setBuildSchematic(false);
			for (int slot = 0; slot < robot.robot_inventory.getSizeInventory(); slot++) {
				if ((robot.robot_inventory.getStackInSlot(slot) != null)
						&& (robot.robot_inventory.getStackInSlot(slot).getItem() instanceof ItemSchematic)) {
					startingPos = robot.getPosition();
					if (loadSchematic(slot)) {
						return true;
					}
					return false;
				}
			}
			RaspberryJamMod.EVENT_BUS
					.post(new CodeEvent.FailEvent("No Schematic Found", robot.getEntityId(), robot.getOwner()));
			return false;
		}
		return false;
	}

	@Override
	public void updateTask() {
		if ((placingList.isEmpty() && (placing == null))) {
			if (!robot.getNavigator().tryMoveToXYZ(startingPos.getX() + .5, startingPos.getY(), startingPos.getZ() + .5,
					1.0)) {
				robot.setPosition(startingPos.getX() + .5, startingPos.getY(), startingPos.getZ() + .5);
			}
			return;
		}
		if (ticks++ < 10) {
			return;
		}
		ticks = 0;
		if (placing == null) {
			placing = placingList.pop();
			tryTicks = 0;
			if (placing.state.getBlock() != Blocks.air) {
				robot.setCurrentItemOrArmor(0, placing.getStack());
			}
		}
		robot.getNavigator().tryMoveToXYZ(placing.pos.getX(), placing.pos.getY() + 1, placing.pos.getZ(), 1.0);
		if ((tryTicks++ > 40) || nearPosition(placing.pos)) {
			BlockPos blockPos = placing.pos;
			placeBlock();
			if (tryTicks > 40) {
				blockPos = getClosePos(blockPos, robot.worldObj);
				robot.setPositionAndUpdate(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
			}
		}
	}
}
