package com.dyn.robot.entity.ai;

import java.util.Map.Entry;
import java.util.Stack;

import com.dyn.rjm.network.CodeEvent;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.schematics.ItemSchematic;
import com.dyn.schematics.Schematic;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class EntiyAIBuildSchematic extends EntityAIBase {
	private class BlockData {
		public BlockPos pos;
		public IBlockState state;
		public NBTTagCompound tile;

		private ItemStack stack;

		public BlockData(BlockPos pos, IBlockState state, NBTTagCompound tile) {
			this.pos = pos;
			this.state = state;
			this.tile = tile;
		}

		public ItemStack getStack() {
			if (stack == null) {
				stack = new ItemStack(state.getBlock(), 1, state.getBlock().damageDropped(state));
			}
			return stack;
		}
	}

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

	private BlockPos getClosePos(BlockPos origin, World world) {
		for (int x = -1; x < 2; ++x) {
			for (int z = -1; z < 2; ++z) {
				for (int y = 2; y >= -2; --y) {
					BlockPos pos = origin.add(x, y, z);
					if (isBlockSurfaceSolid(pos) && world.isAirBlock(pos.up())) {
						return pos.up();
					}
				}
			}
		}
		return world.getTopSolidOrLiquidBlock(origin);
	}

	protected boolean isBlockSurfaceSolid(BlockPos blockpos) {
		IBlockState iblockstate = robot.world.getBlockState(blockpos);
		return (iblockstate.getBlockFaceShape(robot.world, blockpos, EnumFacing.DOWN) == BlockFaceShape.SOLID)
				&& robot.world.isAirBlock(blockpos.up());
	}

	private boolean loadSchematic(int slot) {
		placingList.clear();
		BlockPos firstPos = startingPos.offset(robot.getProgrammedDirection()).down();
		if (!robot.robot_inventory.getStackInSlot(slot).hasTagCompound()) {
			MinecraftForge.EVENT_BUS
					.post(new CodeEvent.FailEvent("Schematic is empty", robot.getEntityId(), robot.getOwner()));
			return false;
		}
		Schematic schematic = ItemSchematic.getSchematic(robot.robot_inventory.getStackInSlot(slot));
		if (schematic == null) {
			MinecraftForge.EVENT_BUS
					.post(new CodeEvent.FailEvent("Schematic is null", robot.getEntityId(), robot.getOwner()));
			return false;
		}
		for (Entry<Block, Integer> block : schematic.getRequiredMaterials().entrySet()) {
			int total = robot.robot_inventory.getQuantityOfItem(new ItemStack(block.getKey()));
			if (total < block.getValue()) {
				MinecraftForge.EVENT_BUS
						.post(new CodeEvent.FailEvent(
								"Not enough " + block.getKey().getLocalizedName() + " found in inventory. Requires: "
										+ block.getValue() + " found: " + total,
								robot.getEntityId(), robot.getOwner()));
				return false;
			}
		}

		for (int i = 0; i < schematic.getSize(); i++) {
			Block b = Block.getBlockById(schematic.getBlockIdAtIndex(i));
			if (b == null) {
				b = Blocks.AIR;
			}
			int meta = schematic.getBlockMetadataAtIndex(i);
			int x = i % schematic.getWidth();
			int z = ((i - x) / schematic.getWidth()) % schematic.getLength();
			int y = (((i - x) / schematic.getWidth()) - z) / schematic.getLength();
			BlockPos blockPos = firstPos.add(schematic.rotatePos(x, y, z, rotation));
			IBlockState original = robot.world.getBlockState(blockPos);
			if (original.getBlock() == b) {
				if (b == Blocks.AIR) {
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
				tile = schematic.getTileEntityTag(x, y, z, blockPos);
			}
			placingList.push(new BlockData(blockPos, state, tile));
		}
		return true;
	}

	public boolean nearPosition(BlockPos pos) {
		BlockPos npcpos = robot.getPosition();
		float x = npcpos.getX() - pos.getX();
		float z = npcpos.getZ() - pos.getZ();
		float y = npcpos.getY() - pos.getY();
		float height = MathHelper.ceil(robot.height + 1.0f) * MathHelper.ceil(robot.height + 1.0f);
		return (((x * x) + (z * z)) < 2.5) && ((y * y) < (height + 2.5));
	}

	public void placeBlock() {
		if (placing == null) {
			return;
		}
		robot.getNavigator().clearPathEntity();
		robot.swingArm(robot.swingingHand);
		robot.robot_inventory.removeItemFromInventory(placing.getStack(), 1);
		robot.world.setBlockState(placing.pos, placing.state, 3);
		if ((placing.state.getBlock() instanceof ITileEntityProvider) && (placing.tile != null)) {
			TileEntity tile = robot.world.getTileEntity(placing.pos);
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
	public boolean shouldContinueExecuting() {
		boolean doContinue = ((placingList != null) && !placingList.isEmpty())
				|| (placingList.isEmpty() && (placing != null));
		if (!doContinue) {
			robot.setHeldItem(robot.swingingHand, robot.robot_inventory.getStackInSlot(2));
			if (nearPosition(startingPos)) {
				MinecraftForge.EVENT_BUS
						.post(new CodeEvent.RobotSuccessEvent("Success", robot.getEntityId(), robot.getOwner()));
			} else {
				return true;
			}
		}
		return doContinue;
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
			MinecraftForge.EVENT_BUS
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
			if (placing.state.getBlock() != Blocks.AIR) {
				robot.setHeldItem(robot.swingingHand, placing.getStack());
			}
		}
		robot.getNavigator().tryMoveToXYZ(placing.pos.getX(), placing.pos.getY() + 1, placing.pos.getZ(), 1.0);
		if ((tryTicks++ > 40) || nearPosition(placing.pos)) {
			BlockPos blockPos = placing.pos;
			placeBlock();
			if (tryTicks > 40) {
				blockPos = getClosePos(blockPos, robot.world);
				robot.setPositionAndUpdate(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
			}
		}
	}
}
