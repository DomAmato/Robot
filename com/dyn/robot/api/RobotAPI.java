package com.dyn.robot.api;

import java.util.Scanner;

import com.dyn.robot.entity.EntityRobot;

import mobi.omegacentauri.raspberryjammod.actions.SetBlockNBT;
import mobi.omegacentauri.raspberryjammod.actions.SetBlockState;
import mobi.omegacentauri.raspberryjammod.api.APIRegistry;
import mobi.omegacentauri.raspberryjammod.api.APIRegistry.Python2MinecraftApi;
import mobi.omegacentauri.raspberryjammod.events.MCEventHandler;
import mobi.omegacentauri.raspberryjammod.util.Location;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.BlockPos;

public class RobotAPI extends Python2MinecraftApi {

	private static final String GETROBOTID = "robot.id";

	private static final String ROBOTMOVE = "robot.moveTo";
	private static final String ROBOTPLACE = "robot.placeBlock";
	private static final String ROBOTBREAK = "robot.breakBlock";
	private static final String ROBOTPICKUP = "robot.pickup";
	private static final String ROBOTINTERACT = "robot.interact";
	private static final String ROBOTTURN = "robot.turn";
	private static final String ROBOTFORWARD = "robot.forward";
	private static final String ROBOTBACKWARD = "robot.backward";

	public static int robotId = 0;

	public static void registerCommands() {
		// robot
		APIRegistry.registerCommand("robot." + GETPOS, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
			robot.followTask.setIsFollowing(false);
			entityGetPos(robotId);
		});
		APIRegistry.registerCommand("robot." + GETTILE, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
			robot.followTask.setIsFollowing(false);
			entityGetTile(robotId);
		});
		APIRegistry.registerCommand("robot." + GETROTATION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
					robot.followTask.setIsFollowing(false);
					entityGetRotation(robotId);
				});
		APIRegistry.registerCommand("robot." + SETROTATION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
					robot.followTask.setIsFollowing(false);
					entitySetRotation(robotId, scan.nextFloat());
				});
		APIRegistry.registerCommand("robot." + GETDIRECTION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
					robot.followTask.setIsFollowing(false);
					entityGetDirection(robotId);
				});
		APIRegistry.registerCommand("robot." + SETDIRECTION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
					robot.followTask.setIsFollowing(false);
					entitySetDirection(robotId, scan);
				});
		APIRegistry.registerCommand("robot." + SETTILE, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
			robot.followTask.setIsFollowing(false);
			entitySetTile(robotId, scan);
		});
		APIRegistry.registerCommand("robot." + SETPOS, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
			robot.followTask.setIsFollowing(false);
			entitySetPos(robotId, scan);
		});
		APIRegistry.registerCommand("robot." + SETDIMENSION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
					robot.followTask.setIsFollowing(false);
					entitySetDimension(robotId, scan.nextInt());
				});
		APIRegistry.registerCommand("robot." + GETNAME, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
			robot.followTask.setIsFollowing(false);
			entityGetNameAndUUID(robotId);
		});
		APIRegistry.registerCommand(ROBOTMOVE, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
			robot.followTask.setIsFollowing(false);
			int x = scan.nextInt();
			int y = scan.nextInt();
			int z = scan.nextInt();
			BlockPos dest = new BlockPos(x, y, z);
			BlockPos curLoc = robot.getPosition();
			float speed = (float) (curLoc.distanceSq(dest) / (robot.getAIMoveSpeed() * robot.getAIMoveSpeed()));
			if (!robot.setMoveTo(dest, speed)) {
				fail("Cannot move to location");
			}
		});
		APIRegistry.registerCommand(ROBOTPLACE, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
			robot.followTask.setIsFollowing(false);
			BlockPos curLoc = robot.getPosition();
			BlockPos placeBlock = null;
			switch (robot.getHorizontalFacing()) {
			case NORTH:
				placeBlock = curLoc.north();
				break;
			case SOUTH:
				placeBlock = curLoc.south();
				break;
			case EAST:
				placeBlock = curLoc.east();
				break;
			case WEST:
				placeBlock = curLoc.west();
				break;
			default:
				break;
			}
			// only place the block if the block is air
			if (robot.worldObj.getBlockState(placeBlock).getBlock() == Blocks.air) {
				Location pos = new Location(robot.worldObj, placeBlock.getX(), placeBlock.getY(), placeBlock.getZ());
				short id = scan.nextShort();
				short meta = scan.hasNextShort() ? scan.nextShort() : 0;
				String tagString = getRest(scan);

				SetBlockState setState;

				if (tagString.contains("{")) {
					try {
						setState = new SetBlockNBT(pos, id, meta, JsonToNBT.getTagFromJson(tagString));
					} catch (NBTException e) {
						System.err.println("Cannot parse NBT");
						setState = new SetBlockState(pos, id, meta);
					}
				} else {
					setState = new SetBlockState(pos, id, meta);
				}
				eventHandler.queueServerAction(setState);
			}
		});
		APIRegistry.registerCommand(ROBOTBREAK, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
			robot.followTask.setIsFollowing(false);
			BlockPos curLoc = robot.getPosition();
			BlockPos breakBlock = null;
			switch (robot.getHorizontalFacing()) {
			case NORTH:
				breakBlock = curLoc.north();
				break;
			case SOUTH:
				breakBlock = curLoc.south();
				break;
			case EAST:
				breakBlock = curLoc.east();
				break;
			case WEST:
				breakBlock = curLoc.west();
				break;
			default:
				break;
			}
			if (robot.worldObj.getBlockState(breakBlock).getBlock() != Blocks.air) {
				if (!robot.isInventoryFull()) {
					robot.addItemStackToInventory(
							new ItemStack(robot.worldObj.getBlockState(breakBlock).getBlock(), 1));
				} else {
					robot.worldObj.getBlockState(breakBlock).getBlock().dropBlockAsItem(robot.worldObj, breakBlock,
							robot.worldObj.getBlockState(breakBlock), 1);
				}
				robot.worldObj.setBlockToAir(breakBlock);
			}
		});
		APIRegistry.registerCommand(ROBOTINTERACT, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
			robot.followTask.setIsFollowing(false);
			BlockPos curLoc = robot.getPosition();
			BlockPos interactBlock = null;
			if (Block.isEqualTo(robot.worldObj.getBlockState(curLoc).getBlock(), Blocks.lever)
					|| Block.isEqualTo(robot.worldObj.getBlockState(curLoc).getBlock(), Blocks.stone_button)
					|| Block.isEqualTo(robot.worldObj.getBlockState(curLoc).getBlock(), Blocks.wooden_button)) {
				interactBlock = curLoc;
			} else {
				switch (robot.getHorizontalFacing()) {
				case NORTH:
					interactBlock = curLoc.north();
					break;
				case SOUTH:
					interactBlock = curLoc.south();
					break;
				case EAST:
					interactBlock = curLoc.east();
					break;
				case WEST:
					interactBlock = curLoc.west();
					break;
				default:
					break;
				}
			}
			if (robot.worldObj.getBlockState(interactBlock).getBlock() != Blocks.air) {
				robot.worldObj.getBlockState(interactBlock).getBlock().onBlockActivated(robot.worldObj, interactBlock,
						robot.worldObj.getBlockState(interactBlock), (EntityPlayer) robot.getOwner(),
						robot.getHorizontalFacing().getOpposite(), 0, 0, 0);
			}
		});
		APIRegistry.registerCommand(ROBOTTURN, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
			robot.followTask.setIsFollowing(false);
			if (args.isEmpty()) {
				fail("Requires an an arguement between -180 and 180");
			}
			robot.rotationYaw += scan.nextFloat();
		});
		APIRegistry.registerCommand(ROBOTFORWARD, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
			robot.followTask.setIsFollowing(false);
			BlockPos curLoc = robot.getPosition();
			BlockPos dest = null;
			switch (robot.getHorizontalFacing()) {
			case NORTH:
				if (!args.isEmpty()) {
					int rep = scan.nextInt();
					if (rep < 0) {
						fail("Must be a positive number");
						return;
					}
					dest = curLoc;
					for (int i = 0; i < rep; i++) {
						dest = dest.north();
					}
				} else {
					dest = curLoc.north();
				}
				break;
			case SOUTH:
				if (!args.isEmpty()) {
					int rep = scan.nextInt();
					if (rep < 0) {
						fail("Must be a positive number");
						return;
					}
					dest = curLoc;
					for (int i = 0; i < rep; i++) {
						dest = dest.south();
					}
				} else {
					dest = curLoc.south();
				}
				break;
			case EAST:
				if (!args.isEmpty()) {
					int rep = scan.nextInt();
					if (rep < 0) {
						fail("Must be a positive number");
						return;
					}
					dest = curLoc;
					for (int i = 0; i < rep; i++) {
						dest = dest.east();
					}
				} else {
					dest = curLoc.east();
				}
				break;
			case WEST:
				if (!args.isEmpty()) {
					int rep = scan.nextInt();
					if (rep < 0) {
						fail("Must be a positive number");
						return;
					}
					dest = curLoc;
					for (int i = 0; i < rep; i++) {
						dest = dest.west();
					}
				} else {
					dest = curLoc.west();
				}
				break;
			default:
				dest = curLoc;
				break;
			}
			float speed = (float) (curLoc.distanceSq(dest) / (robot.getAIMoveSpeed() * robot.getAIMoveSpeed()));
			if (!robot.setMoveTo(dest, speed)) {
				fail("Cannot move to location");
			}
		});
		APIRegistry.registerCommand(GETROBOTID, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			sendLine(robotId);
		});
	}

	public static int getRobotId() {
		return robotId;
	}

	public static void setRobotId(int id) {
		robotId = id;
	}
}
