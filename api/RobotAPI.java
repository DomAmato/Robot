package com.dyn.robot.api;

import java.util.Scanner;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.server.network.NetworkManager;
import com.dyn.server.network.packets.client.RobotSpeakMessage;
import com.dyn.utils.HelperFunctions;

import mobi.omegacentauri.raspberryjammod.actions.SetBlockNBT;
import mobi.omegacentauri.raspberryjammod.actions.SetBlockState;
import mobi.omegacentauri.raspberryjammod.api.APIRegistry;
import mobi.omegacentauri.raspberryjammod.api.APIRegistry.Python2MinecraftApi;
import mobi.omegacentauri.raspberryjammod.events.MCEventHandler;
import mobi.omegacentauri.raspberryjammod.util.BlockState;
import mobi.omegacentauri.raspberryjammod.util.Location;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public class RobotAPI extends Python2MinecraftApi {

	private static final String GETROBOTID = "robot.id";

	private static final String ROBOTMOVE = "robot.moveTo";
	private static final String ROBOTPLACE = "robot.place";
	private static final String ROBOTBREAK = "robot.break";
	private static final String ROBOTINTERACT = "robot.interact";
	private static final String ROBOTTURN = "robot.turn";
	private static final String ROBOTFORWARD = "robot.forward";
	private static final String ROBOTCLIMB = "robot.climb";
	private static final String ROBOTBACKWARD = "robot.backward";
	private static final String ROBOTINSPECT = "robot.inspect";
	private static final String ROBOTJUMP = "robot.jump";
	private static final String ROBOTSAY = "robot.say";
	public static int robotId = 0;

	// its likely that we might get some concurrency issues with this if
	// players simultaneously run code where the robot id is not set
	// to the correct robot... we

	public static int getRobotId() {
		return robotId;
	}

	public static void notifyFailure(String failMessage) {
		fail(failMessage);
	}

	public static void registerCommands() {
		// robot
		APIRegistry.registerCommand("robot." + GETPOS, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			entityGetPos(scan.nextInt());
		});
		APIRegistry.registerCommand("robot." + GETTILE, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			entityGetTile(scan.nextInt());
		});
		APIRegistry.registerCommand("robot." + GETROTATION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					entityGetRotation(scan.nextInt());
				});
		APIRegistry.registerCommand("robot." + SETROTATION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					entitySetRotation(scan.nextInt(), scan.nextFloat());
				});
		APIRegistry.registerCommand("robot." + GETDIRECTION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					entityGetDirection(scan.nextInt());
				});
		APIRegistry.registerCommand("robot." + SETDIRECTION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					entitySetDirection(scan.nextInt(), scan);
				});
		APIRegistry.registerCommand("robot." + SETTILE, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			entitySetTile(scan.nextInt(), scan);
		});
		APIRegistry.registerCommand("robot." + SETPOS, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			entitySetPos(scan.nextInt(), scan);
		});
		APIRegistry.registerCommand("robot." + SETDIMENSION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					entitySetDimension(scan.nextInt(), scan.nextInt(), eventHandler);
				});
		APIRegistry.registerCommand("robot." + GETNAME, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			entityGetNameAndUUID(scan.nextInt());
		});
		APIRegistry.registerCommand(ROBOTMOVE, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			if (RobotMod.robotEcho.get(id)) {
				EntityPlayerMP player = havePlayer ? (EntityPlayerMP) RobotMod.robotid2player.get(id) : playerMP;
				NetworkManager.sendTo(new RobotSpeakMessage("Moving", id), player);
			}
			EntityRobot robot = (EntityRobot) getServerEntityByID(id);
			if (!robot.shouldExecuteCode()) {
				fail("Robot is not executing code, it might be out of sync");
			}
			int x = (int) (robot.posX + scan.nextInt());
			int y = (int) (robot.posY + scan.nextInt());
			int z = (int) (robot.posZ + scan.nextInt());
			robot.addToProgramPath(new BlockPos(x, y, z));
		});
		APIRegistry.registerCommand(ROBOTPLACE, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int robId = scan.nextInt();
			if (RobotMod.robotEcho.get(robId)) {
				EntityPlayerMP player = havePlayer ? (EntityPlayerMP) RobotMod.robotid2player.get(robId) : playerMP;
				NetworkManager.sendTo(new RobotSpeakMessage("Place", robId), player);
			}
			EntityRobot robot = (EntityRobot) getServerEntityByID(robId);
			if (!robot.shouldExecuteCode()) {
				fail("Robot is not executing code, it might be out of sync");
			}
			BlockPos curLoc = robot.getPosition();
			BlockPos placeBlock = new Location(robot.worldObj, curLoc.offset(robot.getHorizontalFacing()));
			if (scan.hasNext()) {
				placeBlock = placeBlock.add(getBlockLocation(scan));
			}

			if (curLoc.distanceSq(placeBlock.getX(), placeBlock.getY(), placeBlock.getZ()) > 3) {
				fail("Distance is greater than robots reach");
			}
			// only place the block if the block is air
			// canBlockBePlaced(blockIn, pos, p_175716_3_, side, entityIn,
			// itemStackIn)
			if (robot.worldObj.getBlockState(placeBlock).getBlock() == Blocks.air) {
				Location pos = new Location(robot.worldObj, placeBlock.getX(), placeBlock.getY(), placeBlock.getZ());
				if (scan.hasNext()) {
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
							ItemStack slot = robot.m_inventory.getStackInSlot(i);
							if (slot != null) {
								Block inventoryBlock = Block.getBlockFromItem(slot.getItem());
								if ((inventoryBlock != null) && inventoryBlock.canPlaceBlockAt(robot.worldObj, pos)) {
									robot.m_inventory.decrStackSize(i, 1);
									pos.getWorld().setBlockState(pos, inventoryBlock.getBlockState().getBaseState(), 3);
									break;
								}
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
			// TODO we need to check permission if the robot can break blocks
			int id = scan.nextInt();
			if (RobotMod.robotEcho.get(id)) {
				EntityPlayerMP player = havePlayer ? (EntityPlayerMP) RobotMod.robotid2player.get(id) : playerMP;
				NetworkManager.sendTo(new RobotSpeakMessage("Break", id), player);
			}
			EntityRobot robot = (EntityRobot) getServerEntityByID(id);
			if (!robot.shouldExecuteCode()) {
				fail("Robot is not executing code, it might be out of sync");
			}
			BlockPos curLoc = robot.getPosition();
			Location breakBlock = new Location(robot.worldObj, curLoc.offset(robot.getHorizontalFacing()));
			if (scan.hasNext()) {
				breakBlock = (Location) breakBlock.add(getBlockLocation(scan));
			}

			if (curLoc.distanceSq(breakBlock.getX(), breakBlock.getY(), breakBlock.getZ()) > 3) {
				fail("Distance is greater than robots reach");
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
			int id = scan.nextInt();
			if (RobotMod.robotEcho.get(id)) {
				EntityPlayerMP player = havePlayer ? (EntityPlayerMP) RobotMod.robotid2player.get(id) : playerMP;
				NetworkManager.sendTo(new RobotSpeakMessage("Interacting", id), player);
			}
			EntityRobot robot = (EntityRobot) getServerEntityByID(id);
			if (!robot.shouldExecuteCode()) {
				fail("Robot is not executing code, it might be out of sync");
			}
			BlockPos curLoc = robot.getPosition();
			BlockPos interactBlock = null;
			if (Block.isEqualTo(robot.worldObj.getBlockState(curLoc).getBlock(), Blocks.lever)
					|| Block.isEqualTo(robot.worldObj.getBlockState(curLoc).getBlock(), Blocks.stone_button)
					|| Block.isEqualTo(robot.worldObj.getBlockState(curLoc).getBlock(), Blocks.wooden_button)) {
				interactBlock = curLoc;
			} else {
				interactBlock = curLoc.offset(robot.getHorizontalFacing());
			}
			if (robot.worldObj.getBlockState(interactBlock).getBlock() != Blocks.air) {
				robot.worldObj.getBlockState(interactBlock).getBlock().onBlockActivated(robot.worldObj, interactBlock,
						robot.worldObj.getBlockState(interactBlock), robot.getOwner(),
						robot.getHorizontalFacing().getOpposite(), 0, 0, 0);
			}
		});
		APIRegistry.registerCommand(ROBOTTURN, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			if (RobotMod.robotEcho.get(id)) {
				EntityPlayerMP player = havePlayer ? (EntityPlayerMP) RobotMod.robotid2player.get(id) : playerMP;
				NetworkManager.sendTo(new RobotSpeakMessage("Turning", id), player);
			}
			EntityRobot robot = (EntityRobot) getServerEntityByID(id);
			if (!robot.shouldExecuteCode()) {
				fail("Robot is not executing code, it might be out of sync");
			}
			float rotate = scan.nextFloat();
			float newYaw = MathHelper.wrapAngleTo180_float(robot.rotationYaw + rotate);
			robot.rotate(newYaw);
		});
		APIRegistry.registerCommand(ROBOTFORWARD, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			if (RobotMod.robotEcho.get(id)) {
				EntityPlayerMP player = havePlayer ? (EntityPlayerMP) RobotMod.robotid2player.get(id) : playerMP;
				NetworkManager.sendTo(new RobotSpeakMessage("Forward", id), player);
			}
			EntityRobot robot = (EntityRobot) getServerEntityByID(id);
			if (!robot.shouldExecuteCode()) {
				fail("Robot is not executing code, it might be out of sync");
			}
			robot.moveForward(scan.nextInt());
		});
		APIRegistry.registerCommand(ROBOTBACKWARD, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			if (RobotMod.robotEcho.get(id)) {
				EntityPlayerMP player = havePlayer ? (EntityPlayerMP) RobotMod.robotid2player.get(id) : playerMP;
				NetworkManager.sendTo(new RobotSpeakMessage("Backward", id), player);
			}
			EntityRobot robot = (EntityRobot) getServerEntityByID(id);
			if (!robot.shouldExecuteCode()) {
				fail("Robot is not executing code, it might be out of sync");
			}
			robot.moveBackward(scan.nextInt());
		});
		APIRegistry.registerCommand(ROBOTCLIMB, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			if (RobotMod.robotEcho.get(id)) {
				EntityPlayerMP player = havePlayer ? (EntityPlayerMP) RobotMod.robotid2player.get(id) : playerMP;
				NetworkManager.sendTo(new RobotSpeakMessage("Climb", id), player);
			}
			EntityRobot robot = (EntityRobot) getServerEntityByID(id);
			if (!robot.shouldExecuteCode()) {
				fail("Robot is not executing code, it might be out of sync");
			}
			if (!robot.climb(scan.nextInt())) {
				fail("Could not climb block");
			}
		});
		APIRegistry.registerCommand(GETROBOTID, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = havePlayer ? RobotMod.robotid2player.inverse().get(playerMP) : robotId;
			EntityRobot robot = (EntityRobot) getServerEntityByID(id);
			robot.setIsFollowing(false);
			robot.removeNonEssentialAI();
			BlockPos loc = robot.getPosition();
			// snap the robot to the center of the block and set its facing to
			// the current direction
			robot.setPositionAndRotation(loc.getX() + .5, loc.getY(), loc.getZ() + .5,
					HelperFunctions.getAngleFromFacing(robot.getHorizontalFacing()), robot.rotationPitch);
			sendLine(id);
		});
		APIRegistry.registerCommand(ROBOTINSPECT, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			if (RobotMod.robotEcho.get(id)) {
				EntityPlayerMP player = havePlayer ? (EntityPlayerMP) RobotMod.robotid2player.get(id) : playerMP;
				NetworkManager.sendTo(new RobotSpeakMessage("Inspect", id), player);
			}
			EntityRobot robot = (EntityRobot) getServerEntityByID(id);
			if (!robot.shouldExecuteCode()) {
				fail("Robot is not executing code, it might be out of sync");
			}
			BlockPos loc = robot.getPosition();
			Location block = new Location(robot.worldObj, loc.offset(robot.getHorizontalFacing()));
			if (scan.hasNext()) {
				block = (Location) block.add(getBlockLocation(scan));
			}
			if (loc.distanceSq(block.getX(), block.getY(), block.getZ()) > 3) {
				fail("Distance is greater than robots ability");
			}
			BlockState state = eventHandler.getBlockState(block);
			sendLine("" + state.id + "," + state.meta);
		});
		APIRegistry.registerCommand(ROBOTSAY, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			EntityPlayerMP player = havePlayer ? (EntityPlayerMP) RobotMod.robotid2player.get(id) : playerMP;
			EntityRobot robot = (EntityRobot) getServerEntityByID(id);
			if (!robot.shouldExecuteCode()) {
				fail("Robot is not executing code, it might be out of sync");
			}
			// adds a comma to the beginning so we substring
			NetworkManager.sendTo(new RobotSpeakMessage(scan.nextLine().substring(1), id), player);
		});
		APIRegistry.registerCommand(ROBOTJUMP, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			if (RobotMod.robotEcho.get(id)) {
				EntityPlayerMP player = havePlayer ? (EntityPlayerMP) RobotMod.robotid2player.get(id) : playerMP;
				NetworkManager.sendTo(new RobotSpeakMessage("Jumping", id), player);
			}
			EntityRobot robot = (EntityRobot) getServerEntityByID(id);
			if (!robot.shouldExecuteCode()) {
				fail("Robot is not executing code, it might be out of sync");
			}
			robot.setShouldJump(true);
		});
	}

	public static void setRobotEcho(int id, boolean echo) {
		if (RobotMod.robotEcho.containsKey(id)) {
			RobotMod.robotEcho.replace(id, echo);
		} else {
			RobotMod.robotEcho.put(id, echo);
		}
	}

	public static void setRobotId(int id, EntityPlayer player) {
		robotId = id;
		if (RobotMod.robotid2player.containsKey(id) || RobotMod.robotid2player.containsValue(player)) {
			RobotMod.robotid2player.inverse().replace(player, id);
		} else {
			RobotMod.robotid2player.put(id, player);
		}
	}
}
