package com.dyn.robot.api;

import java.util.Scanner;

import com.dyn.DYNServerMod;
import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.server.network.NetworkManager;
import com.dyn.server.network.packets.client.RobotSpeakMessage;
import com.dyn.utils.HelperFunctions;

import mobi.omegacentauri.raspberryjammod.RaspberryJamMod;
import mobi.omegacentauri.raspberryjammod.actions.SetBlockNBT;
import mobi.omegacentauri.raspberryjammod.actions.SetBlockState;
import mobi.omegacentauri.raspberryjammod.actions.SetBlockStateWithId;
import mobi.omegacentauri.raspberryjammod.api.APIRegistry;
import mobi.omegacentauri.raspberryjammod.api.APIRegistry.Python2MinecraftApi;
import mobi.omegacentauri.raspberryjammod.events.MCEventHandler;
import mobi.omegacentauri.raspberryjammod.network.CodeEvent;
import mobi.omegacentauri.raspberryjammod.util.Location;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

public class RobotAPI extends Python2MinecraftApi {

	private static final String GETROBOTID = "robot.id";

	private static final String ROBOTMOVE = "robot.moveTo";
	private static final String ROBOTPLACE = "robot.place";
	private static final String ROBOTBREAK = "robot.break";
	private static final String ROBOTINTERACT = "robot.interact";
	private static final String ROBOTTURN = "robot.turn";
	private static final String ROBOTFORWARD = "robot.forward";
	private static final String ROBOTCLIMB = "robot.climb";
	private static final String ROBOTDESCEND = "robot.descend";
	private static final String ROBOTBACKWARD = "robot.backward";
	private static final String ROBOTINSPECT = "robot.inspect";
	private static final String ROBOTJUMP = "robot.jump";
	private static final String ROBOTSAY = "robot.say";
	private static final String ROBOTNAME = "robot.name";
	private static final String ROBOTFACE = "robot.face";

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
		APIRegistry.registerCommand(ROBOTNAME, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) getServerEntityByID(id);
			sendLine(robot.getName());
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
//			if((robot.getRobotLevel() & 4) == 0){
//				//bitwise and to see if the flag is set
//				fail("Robot does not know the placeBlock command");
//			}
			BlockPos curLoc = robot.getPosition();
			BlockPos placeBlock = curLoc.offset(robot.getProgrammedDirection());

			if (scan.hasNext()) {
				BlockPos temp = rotateVector(getBlockPos(scan),
						HelperFunctions.getAngleFromFacing(robot.getProgrammedDirection()));

				if ((sqVectorLength(temp) == 0) || (sqVectorLength(temp) > 3) || (Math.abs(temp.getX()) > 1)
						|| (Math.abs(temp.getY()) > 1) || (Math.abs(temp.getZ()) > 1)) {
					if (sqVectorLength(temp) == 0) {
						fail("Coordinates cannot equal 0");
					} else {
						fail("Distance is greater than robots reach");
					}
				}
				placeBlock = curLoc.add(temp);
			}
			// only place the block if the block is air
			// canBlockBePlaced(blockIn, pos, p_175716_3_, side, entityIn,
			// itemStackIn)
			if (robot.worldObj.getBlockState(placeBlock).getBlock().canPlaceBlockAt(robot.worldObj, placeBlock)) {
				Location pos = new Location(robot.worldObj, placeBlock.getX(), placeBlock.getY(), placeBlock.getZ());
				if (scan.hasNext()) {
					short id = scan.nextShort();
					short meta = scan.hasNextShort() ? scan.nextShort() : 0;
					String tagString = getRest(scan);

					SetBlockStateWithId setState;

					if (tagString.contains("{")) {
						try {
							setState = new SetBlockNBT(pos, id, meta, JsonToNBT.getTagFromJson(tagString));
						} catch (NBTException e) {
							System.err.println("Cannot parse NBT");
							setState = new SetBlockStateWithId(pos, id, meta);
						}
					} else {
						setState = new SetBlockStateWithId(pos, id, meta);
					}
					eventHandler.queueServerAction(setState);
					RaspberryJamMod.EVENT_BUS
							.post(new CodeEvent.SuccessEvent("Success", robot.getEntityId(), robot.getOwner()));
				} else {
					if (!robot.isInventoryEmpty()) {
						int slot = 0;
						ItemStack inventorySlot = null;
						for (int i = 0; i < robot.m_inventory.getSizeInventory(); i++) {
							if ((robot.m_inventory.getStackInSlot(i) != null) && (Block
									.getBlockFromItem(robot.m_inventory.getStackInSlot(i).getItem()) != null)) {
								inventorySlot = robot.m_inventory.getStackInSlot(i);
								slot = i;
								break;
							}
						}

						if (inventorySlot != null) {
							Block inventoryBlock = Block.getBlockFromItem(inventorySlot.getItem());
							if ((inventoryBlock != null) && inventoryBlock.canPlaceBlockAt(robot.worldObj, pos)) {
								robot.m_inventory.decrStackSize(slot, 1);
								eventHandler.queueServerAction(
										new SetBlockState(pos, inventoryBlock, inventorySlot.getItemDamage()));
								RaspberryJamMod.EVENT_BUS.post(
										new CodeEvent.SuccessEvent("Success", robot.getEntityId(), robot.getOwner()));
							}

						} else {
							fail("No Valid Block Found in Inventory");
						}
					} else {
						fail("No Block in Inventory");
					}
				}
			}
		});
		APIRegistry.registerCommand(ROBOTBREAK, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			// TODO we need to check permission if the robot can break blocks
			int id = scan.nextInt();
			EntityPlayerMP player = havePlayer ? (EntityPlayerMP) RobotMod.robotid2player.get(id) : playerMP;
			if (RobotMod.robotEcho.get(id)) {
				NetworkManager.sendTo(new RobotSpeakMessage("Break", id), player);
			}
			EntityRobot robot = (EntityRobot) getServerEntityByID(id);
			if (!robot.shouldExecuteCode()) {
				fail("Robot is not executing code, it might be out of sync");
			}
//			if((robot.getRobotLevel() & 2) == 0){
//				//bitwise and to see if the flag is set
//				fail("Robot does not know the breakBlock command");
//			}
			BlockPos curLoc = robot.getPosition();
			BlockPos breakBlock = curLoc.offset(robot.getProgrammedDirection());
			if (scan.hasNext()) {
				BlockPos temp = rotateVector(getBlockPos(scan),
						HelperFunctions.getAngleFromFacing(robot.getProgrammedDirection()));

				if ((sqVectorLength(temp) == 0) || (sqVectorLength(temp) > 3) || (Math.abs(temp.getX()) > 1)
						|| (Math.abs(temp.getY()) > 1) || (Math.abs(temp.getZ()) > 1)) {
					if (sqVectorLength(temp) == 0) {
						fail("Coordinates cannot equal 0");
					} else {
						fail("Distance is greater than robots reach");
					}
				}
				breakBlock = curLoc.add(temp);
			}

			if (robot.worldObj.getBlockState(breakBlock).getBlock() != Blocks.air) {
				if (!robot.isInventoryFull()) {
					IBlockState broken = robot.worldObj.getBlockState(breakBlock);
					robot.addItemStackToInventory(
							new ItemStack(broken.getBlock(), 1, broken.getBlock().getMetaFromState(broken)));
				} else {
					robot.worldObj.getBlockState(breakBlock).getBlock().dropBlockAsItem(robot.worldObj, breakBlock,
							robot.worldObj.getBlockState(breakBlock), 1);
				}
				IBlockState state = robot.worldObj.getBlockState(breakBlock);
				BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(robot.worldObj, breakBlock, state,
						robot.getOwner());
				MinecraftForge.EVENT_BUS.post(event);
				robot.worldObj.setBlockToAir(breakBlock);
				// BlockPos loc = robot.getPosition();
				// robot.setPosition(loc.getX() + .5, loc.getY(), loc.getZ() +
				// .5);
				// robot.rotate(HelperFunctions.getAngleFromFacing(robot.getProgrammedDirection()));
				RaspberryJamMod.EVENT_BUS
						.post(new CodeEvent.SuccessEvent("Success", robot.getEntityId(), robot.getOwner()));
			} else {
				fail("Nothing to break");
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
				interactBlock = curLoc.offset(robot.getProgrammedDirection());
			}
			if (robot.worldObj.getBlockState(interactBlock).getBlock() != Blocks.air) {
				robot.worldObj.getBlockState(interactBlock).getBlock().onBlockActivated(robot.worldObj, interactBlock,
						robot.worldObj.getBlockState(interactBlock), robot.getOwner(),
						robot.getProgrammedDirection().getOpposite(), 0, 0, 0);
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
		APIRegistry.registerCommand(ROBOTFACE, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			if (RobotMod.robotEcho.get(id)) {
				EntityPlayerMP player = havePlayer ? (EntityPlayerMP) RobotMod.robotid2player.get(id) : playerMP;
				NetworkManager.sendTo(new RobotSpeakMessage("Facing", id), player);
			}
			EntityRobot robot = (EntityRobot) getServerEntityByID(id);
			robot.rotate(HelperFunctions.getAngleFromFacing(EnumFacing.getFront(scan.nextInt())));
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
		APIRegistry.registerCommand(ROBOTDESCEND, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			if (RobotMod.robotEcho.get(id)) {
				EntityPlayerMP player = havePlayer ? (EntityPlayerMP) RobotMod.robotid2player.get(id) : playerMP;
				NetworkManager.sendTo(new RobotSpeakMessage("Descend", id), player);
			}
			EntityRobot robot = (EntityRobot) getServerEntityByID(id);
			if (!robot.shouldExecuteCode()) {
				fail("Robot is not executing code, it might be out of sync");
			}
			if (!robot.descend(scan.nextInt())) {
				fail("Could not descend block");
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
					HelperFunctions.getAngleFromFacing(robot.getProgrammedDirection()), robot.rotationPitch);
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
			BlockPos curLoc = robot.getPosition();
			BlockPos inspectBlock = curLoc.offset(robot.getProgrammedDirection());
			if (scan.hasNext()) {
				BlockPos temp = rotateVector(getBlockPos(scan),
						HelperFunctions.getAngleFromFacing(robot.getProgrammedDirection()));

				if ((sqVectorLength(temp) > 3) || (Math.abs(temp.getX()) > 1)
						|| (Math.abs(temp.getY()) > 1) || (Math.abs(temp.getZ()) > 1)) {
						fail("Distance is greater than robots sensor");
				}
				inspectBlock = curLoc.add(temp);
			}
			Location loc = new Location(robot.worldObj, inspectBlock);
			sendLine("" + eventHandler.getBlockId(loc) + "," + eventHandler.getBlockMeta(loc));
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
		DYNServerMod.logger.info("Attaching robot " + id + " to player " + player.getName());
		if (RobotMod.robotid2player.containsKey(id) || RobotMod.robotid2player.containsValue(player)) {
			RobotMod.robotid2player.inverse().replace(player, id);
		} else {
			RobotMod.robotid2player.put(id, player);
		}
	}
}
