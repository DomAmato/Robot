package com.dyn.robot.api;

import java.util.Scanner;

import com.dyn.robot.entity.EntityRobot;

import mobi.omegacentauri.raspberryjammod.actions.SetBlockNBT;
import mobi.omegacentauri.raspberryjammod.actions.SetBlockState;
import mobi.omegacentauri.raspberryjammod.api.APIRegistry;
import mobi.omegacentauri.raspberryjammod.api.APIRegistry.Python2MinecraftApi;
import mobi.omegacentauri.raspberryjammod.events.MCEventHandler;
import mobi.omegacentauri.raspberryjammod.util.BlockState;
import mobi.omegacentauri.raspberryjammod.util.Location;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

public class RobotAPI extends Python2MinecraftApi {

	private static final String GETROBOTID = "robot.id";

	private static final String ROBOTMOVE = "robot.moveTo";
	private static final String ROBOTPLACE = "robot.placeBlock";
	private static final String ROBOTBREAK = "robot.breakBlock";
	private static final String ROBOTINTERACT = "robot.interact";
	private static final String ROBOTTURN = "robot.turn";
	private static final String ROBOTFORWARD = "robot.forward";
	private static final String ROBOTINSPECT = "robot.inspect";
	public static int robotId = 0;

	public static int getRobotId() {
		return robotId;
	}

	public static void notifyFailure(String failMessage) {
		fail(failMessage);
	}

	public static void registerCommands() {
		// robot
		APIRegistry.registerCommand("robot." + GETPOS, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
			entityGetPos(robotId);
		});
		APIRegistry.registerCommand("robot." + GETTILE, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
			entityGetTile(robotId);
		});
		APIRegistry.registerCommand("robot." + GETROTATION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
					entityGetRotation(robotId);
				});
		APIRegistry.registerCommand("robot." + SETROTATION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
					entitySetRotation(robotId, scan.nextFloat());
				});
		APIRegistry.registerCommand("robot." + GETDIRECTION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
					entityGetDirection(robotId);
				});
		APIRegistry.registerCommand("robot." + SETDIRECTION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
					entitySetDirection(robotId, scan);
				});
		APIRegistry.registerCommand("robot." + SETTILE, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
			entitySetTile(robotId, scan);
		});
		APIRegistry.registerCommand("robot." + SETPOS, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
			entitySetPos(robotId, scan);
		});
		APIRegistry.registerCommand("robot." + SETDIMENSION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
					entitySetDimension(robotId, scan.nextInt());
				});
		APIRegistry.registerCommand("robot." + GETNAME, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
			entityGetNameAndUUID(robotId);
		});
		APIRegistry.registerCommand(ROBOTMOVE, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
			int x = scan.nextInt();
			int y = scan.nextInt();
			int z = scan.nextInt();
			robot.addToProgramPath(new BlockPos(x, y, z));
		});
		APIRegistry.registerCommand(ROBOTPLACE, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
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
				if (args.length() > 0) {
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
				} else {
					if (!robot.isInventoryEmpty()) {
						for (int i = 0; i < robot.m_inventory.getSizeInventory(); i++) {
							Block inventoryBlock = Block
									.getBlockFromItem(robot.m_inventory.getStackInSlot(i).getItem());
							if ((inventoryBlock != null) && inventoryBlock.canPlaceBlockAt(robot.worldObj, pos)) {
								robot.m_inventory.decrStackSize(i, 1);
								pos.getWorld().setBlockState(pos, inventoryBlock.getBlockState().getBaseState(), 3);
								break;
							}
						}
					} else {
						fail("No Block in Inventory");
						// pos.getWorld().setBlockState(pos, (IBlockState)
						// Blocks.dirt.getDefaultState(), 3);
					}
				}
			}
		});
		APIRegistry.registerCommand(ROBOTBREAK, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
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
						robot.worldObj.getBlockState(interactBlock), robot.getOwner(),
						robot.getHorizontalFacing().getOpposite(), 0, 0, 0);
			}
		});
		APIRegistry.registerCommand(ROBOTTURN, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
			if (args.isEmpty()) {
				fail("Requires an an arguement between -180 and 180");
			}
			float newYaw = MathHelper.wrapAngleTo180_float(robot.rotationYaw + scan.nextFloat());
			robot.rotationYaw = newYaw;
			robot.setRotationYawHead(newYaw);
			robot.setRenderYawOffset(newYaw);
		});
		APIRegistry.registerCommand(ROBOTFORWARD, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
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
			robot.addToProgramPath(dest);
		});
		APIRegistry.registerCommand(GETROBOTID, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
			robot.setIsFollowing(false);
			robot.removeNonEssentialAI();
			BlockPos loc = robot.getPosition();
			// snap the robot to the center of the block and set its facing to
			// the current direction
			if ((robot.rotationYaw % 90) != 0) {
				robot.setPositionAndRotation(loc.getX() + .5, loc.getY(), loc.getZ() + .5,
						getAngleFromFacing(robot.getHorizontalFacing()), robot.rotationPitch);
			}
			sendLine(robotId);
		});
		APIRegistry.registerCommand(ROBOTINSPECT, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			EntityRobot robot = (EntityRobot) getServerEntityByID(robotId);
			
			BlockPos loc = robot.getPosition();
			Location block = new Location(robot.worldObj, loc.offset(robot.getHorizontalFacing()));
			if(!args.isEmpty()){
				block = (Location) block.add(getBlockLocation(scan));
			}
			BlockState state = eventHandler.getBlockState(block);
			sendLine("" + state.id + "," + state.meta);
		});
	}

	public static void setRobotId(int id) {
		robotId = id;
	}
	
	private static float getAngleFromFacing(EnumFacing dir) {
		switch (dir) {
		case SOUTH:
			return 0;
		case NORTH:
			return 180;
		case EAST:
			return 270;
		case WEST:
			return 90;
		default:
			return 0;
		}
	}
}
