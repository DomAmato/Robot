package com.dyn.robot.api;

import java.util.List;
import java.util.Scanner;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.network.NetworkManager;
import com.dyn.robot.network.messages.RobotSpeakMessage;
import com.dyn.robot.util.EnchantmentUtils;
import com.dyn.robot.util.HelperFunctions;

import mobi.omegacentauri.raspberryjammod.RaspberryJamMod;
import mobi.omegacentauri.raspberryjammod.actions.SetBlockNBT;
import mobi.omegacentauri.raspberryjammod.actions.SetBlockStateWithId;
import mobi.omegacentauri.raspberryjammod.api.APIRegistry;
import mobi.omegacentauri.raspberryjammod.api.Python2MinecraftApi;
import mobi.omegacentauri.raspberryjammod.events.MCEventHandler;
import mobi.omegacentauri.raspberryjammod.network.CodeEvent;
import mobi.omegacentauri.raspberryjammod.util.Location;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemReed;
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
	private static final String ROBOTDETECT = "robot.detect";
	private static final String ROBOTATTACK = "robot.attack";
	private static final String ROBOTSCHEM = "robot.schematic";

	public static int robotId = 0;

	public static int getRobotId() {
		return RobotAPI.robotId;
	}

	public static void registerCommands() {
		// robot
		APIRegistry.registerCommand(RobotAPI.ROBOTMOVE, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();

			EntityRobot robot = (EntityRobot) Python2MinecraftApi.getServerEntityByID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Moving", id), player);
					}
				}
				int x = (int) (robot.posX + scan.nextInt());
				int y = (int) (robot.posY + scan.nextInt());
				int z = (int) (robot.posZ + scan.nextInt());
				robot.addToProgramPath(new BlockPos(x, y, z));
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTNAME, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) Python2MinecraftApi.getServerEntityByID(id);
			Python2MinecraftApi.sendLine(robot.getName());
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTPLACE, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) Python2MinecraftApi.getServerEntityByID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (!robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 3))) {
					Python2MinecraftApi.fail("Robot does not know the placeBlock command");
					return;
				}
				if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Place", id), player);
					}
				}
				BlockPos curLoc = robot.getPosition();
				BlockPos placeBlock = curLoc.offset(robot.getProgrammedDirection());

				if (scan.hasNext()) {
					BlockPos temp = Python2MinecraftApi.rotateVectorAngle(Python2MinecraftApi.getBlockPos(scan),
							HelperFunctions.getAngleFromFacing(robot.getProgrammedDirection()));

					if ((RobotAPI.sqVectorLength(temp) == 0) || (RobotAPI.sqVectorLength(temp) > 3)
							|| (Math.abs(temp.getX()) > 1) || (Math.abs(temp.getY()) > 1)
							|| (Math.abs(temp.getZ()) > 1)) {
						if (RobotAPI.sqVectorLength(temp) == 0) {
							Python2MinecraftApi.fail("Coordinates cannot equal 0");
							return;
						} else {
							Python2MinecraftApi.fail("Distance is greater than robots reach");
							return;
						}
					}
					placeBlock = curLoc.add(temp);
				}
				if (robot.worldObj.getBlockState(placeBlock).getBlock().canPlaceBlockAt(robot.worldObj, placeBlock)) {
					if (scan.hasNext()) {
						Location pos = new Location(robot.worldObj, placeBlock.getX(), placeBlock.getY(),
								placeBlock.getZ());
						short blockId = scan.nextShort();
						short meta = scan.hasNextShort() ? scan.nextShort() : 0;
						String tagString = Python2MinecraftApi.getRest(scan);

						SetBlockStateWithId setState;

						if (tagString.contains("{")) {
							try {
								setState = new SetBlockNBT(pos, blockId, meta, JsonToNBT.getTagFromJson(tagString));
							} catch (NBTException e) {
								System.err.println("Cannot parse NBT");
								setState = new SetBlockStateWithId(pos, blockId, meta);
							}
						} else {
							setState = new SetBlockStateWithId(pos, blockId, meta);
						}
						eventHandler.queueServerAction(setState);
						RaspberryJamMod.EVENT_BUS.post(
								new CodeEvent.RobotSuccessEvent("Success", robot.getEntityId(), robot.getOwner()));
					} else {
						if (!robot.robot_inventory.isInventoryEmpty()) {
							int slot = 0;
							ItemStack inventorySlot = null;
							for (int i = 12; i < robot.robot_inventory.getSizeInventory(); i++) {
								if ((robot.robot_inventory.getStackInSlot(i) != null) && ((Block
										.getBlockFromItem(robot.robot_inventory.getStackInSlot(i).getItem()) != null)
										|| (robot.robot_inventory.getStackInSlot(i).getItem() instanceof ItemReed)
										|| (robot.robot_inventory.getStackInSlot(i).getItem() instanceof ItemDoor))) {
									inventorySlot = robot.robot_inventory.getStackInSlot(i);
									slot = i;
									break;
								}
							}

							if (inventorySlot != null) {
								if (!inventorySlot.getItem().onItemUse(inventorySlot, robot.getOwner(), robot.worldObj,
										placeBlock, (inventorySlot.getItem() instanceof ItemDoor) ? EnumFacing.UP
												: robot.getProgrammedDirection().getOpposite(),
										0, 0, 0)) {
									Python2MinecraftApi.fail("Cannot place block at location");
								} else {
									if (robot.robot_inventory.getStackInSlot(slot).stackSize <= 0) {
										robot.robot_inventory.removeStackFromSlot(slot);
									}
									robot.swingItem();
									RaspberryJamMod.EVENT_BUS.post(new CodeEvent.RobotSuccessEvent("Success",
											robot.getEntityId(), robot.getOwner()));
								}
							} else {
								Python2MinecraftApi.fail("No Valid Block Found in Inventory");
								return;
							}
						} else {
							Python2MinecraftApi.fail("No Block in Inventory");
							return;
						}
					}
				} else {
					Python2MinecraftApi.fail("Cannot place block at location");
				}
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTBREAK, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			// TODO we need to check permission if the robot can break blocks
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) Python2MinecraftApi.getServerEntityByID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (!robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 2))) {
					Python2MinecraftApi.fail("Robot does not know the breakBlock command");
					return;
				}
				if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Break", id), player);
					}
				}
				BlockPos curLoc = robot.getPosition();
				BlockPos breakBlock = curLoc.offset(robot.getProgrammedDirection());
				if (scan.hasNext()) {
					// this rotation is the problem
					BlockPos temp = Python2MinecraftApi.rotateVectorAngle(Python2MinecraftApi.getBlockPos(scan),
							HelperFunctions.getAngleFromFacing(robot.getProgrammedDirection()));

					if (RobotAPI.sqVectorLength(temp) == 0) {
						Python2MinecraftApi.fail("Coordinates cannot equal 0");
						return;
					}

					if ((RobotAPI.sqVectorLength(temp) > 3) || (Math.abs(temp.getX()) > 1)
							|| (Math.abs(temp.getY()) > 1) || (Math.abs(temp.getZ()) > 1)) {
						Python2MinecraftApi.fail("Distance is greater than robots reach: " + String.format("(%d, %d, %d) = %d",
								temp.getX(), temp.getY(), temp.getZ(), RobotAPI.sqVectorLength(temp)));
						return;
					}
					breakBlock = curLoc.add(temp);
				}

				if (robot.worldObj.getBlockState(breakBlock).getBlock() != Blocks.air) {
					if (!robot.robot_inventory.isInventoryFull()) {
						IBlockState broken = robot.worldObj.getBlockState(breakBlock);
						robot.robot_inventory.addItemStackToInventory(
								new ItemStack(broken.getBlock(), 1, broken.getBlock().getMetaFromState(broken)));
					} else {
						robot.worldObj.getBlockState(breakBlock).getBlock().dropBlockAsItem(robot.worldObj, breakBlock,
								robot.worldObj.getBlockState(breakBlock), 1);
					}
					IBlockState state = robot.worldObj.getBlockState(breakBlock);
					BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(robot.worldObj, breakBlock, state,
							robot.getOwner());
					MinecraftForge.EVENT_BUS.post(event);
					robot.swingItem();
					robot.worldObj.setBlockToAir(breakBlock);
					RaspberryJamMod.EVENT_BUS
							.post(new CodeEvent.RobotSuccessEvent("Success", robot.getEntityId(), robot.getOwner()));
				} else {
					Python2MinecraftApi.fail("Nothing to break");
				}
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTINTERACT,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					int id = scan.nextInt();
					EntityRobot robot = (EntityRobot) Python2MinecraftApi.getServerEntityByID(id);
					if (robot != null) {
						if (!robot.shouldExecuteCode()) {
							Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
							return;
						}
						if (!robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 5))) {
							Python2MinecraftApi.fail("Robot does not know the interact command");
							return;
						}
						if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 15))) {
							EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
							if (player != null) {
								NetworkManager.sendTo(new RobotSpeakMessage("Interact", id), player);
							}
						}
						BlockPos curLoc = robot.getPosition();
						BlockPos interactBlock = null;
						if (Block.isEqualTo(robot.worldObj.getBlockState(curLoc).getBlock(), Blocks.lever)
								|| Block.isEqualTo(robot.worldObj.getBlockState(curLoc).getBlock(), Blocks.stone_button)
								|| Block.isEqualTo(robot.worldObj.getBlockState(curLoc).getBlock(),
										Blocks.wooden_button)) {
							interactBlock = curLoc;
						} else {
							interactBlock = curLoc.offset(robot.getProgrammedDirection());
						}
						if (robot.worldObj.getBlockState(interactBlock).getBlock() != Blocks.air) {
							robot.swingItem();
							robot.worldObj.getBlockState(interactBlock).getBlock().onBlockActivated(robot.worldObj,
									interactBlock, robot.worldObj.getBlockState(interactBlock), robot.getOwner(),
									robot.getProgrammedDirection().getOpposite(), 0, 0, 0);
						}
					}
				});
		APIRegistry.registerCommand(RobotAPI.ROBOTTURN, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) Python2MinecraftApi.getServerEntityByID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Turning", id), player);
					}
				}
				float rotate = scan.nextFloat();
				float newYaw = MathHelper.wrapAngleTo180_float(robot.rotationYaw + rotate);
				robot.rotate(newYaw);
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTFACE, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) Python2MinecraftApi.getServerEntityByID(id);
			if (robot != null) {
				if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Facing", id), player);
					}
				}
				robot.rotate(HelperFunctions.getAngleFromFacing(EnumFacing.getFront(scan.nextInt())));
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTSCHEM, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) Python2MinecraftApi.getServerEntityByID(id);
			if (robot != null) {
				if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Facing", id), player);
					}
				}
				robot.setBuildSchematic(true);
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTDETECT, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			// TODO: Need to be able to determine the mob type
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) Python2MinecraftApi.getServerEntityByID(id);
			if (robot != null) {
				if (!robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 6))) {
					Python2MinecraftApi.fail("Robot does not know the detect command");
					return;
				}
				if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Detecting", id), player);
					}
				}
				int level = EnchantmentUtils.getLevel(Enchantment.power,
						robot.robot_inventory.getStackOfItem(new ItemStack(RobotMod.expChip, 1, 6)));
				// Can map the entity this way
				// EntityList.stringToClassMapping.get("");
				List<EntityMob> list = robot.worldObj.getEntitiesWithinAABB(EntityMob.class,
						robot.getEntityBoundingBox().expand(5 + (10 * level), 5 + (10 * level), 5 + (10 * level)));

				String entityList = "";
				for (EntityMob entity : list) {
					entityList += entity.getName() + "|" + entity.getEntityId() + "%";
				}
				if (!entityList.isEmpty()) {
					entityList = entityList.substring(0, entityList.length() - 1);
				}
				Python2MinecraftApi.sendLine(entityList);
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTATTACK, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			// TODO:
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) Python2MinecraftApi.getServerEntityByID(id);
			if (robot != null) {
				if (!robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 7))) {
					Python2MinecraftApi.fail("Robot does not know the attack command");
					return;
				}
				if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Attacking", id), player);
					}
				}
				id = scan.nextInt();
				Entity target = Python2MinecraftApi.getServerEntityByID(id);
				if (target instanceof EntityPlayer) {
					Python2MinecraftApi.fail("Cannot go against the first law of robotics");
				}
				robot.setAttackTarget((EntityLivingBase) target);

			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTFORWARD, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) Python2MinecraftApi.getServerEntityByID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Forward", id), player);
					}
				}
				robot.moveForward(scan.nextInt());
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTBACKWARD,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					int id = scan.nextInt();
					EntityRobot robot = (EntityRobot) Python2MinecraftApi.getServerEntityByID(id);
					if (robot != null) {
						if (!robot.shouldExecuteCode()) {
							Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
							return;
						}
						if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 15))) {
							EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
							if (player != null) {
								NetworkManager.sendTo(new RobotSpeakMessage("Backward", id), player);
							}
						}
						robot.moveBackward(scan.nextInt());
					}
				});
		APIRegistry.registerCommand(RobotAPI.ROBOTCLIMB, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) Python2MinecraftApi.getServerEntityByID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (!robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 0))) {
					Python2MinecraftApi.fail("Robot does not know the climb command");
					return;
				}
				if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Climb", id), player);
					}
				}
				if (!robot.climb(scan.nextInt())) {
					Python2MinecraftApi.fail("Could not climb block");
				}
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTDESCEND, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) Python2MinecraftApi.getServerEntityByID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (!robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 0))) {
					Python2MinecraftApi.fail("Robot does not know the descend command");
					return;
				}
				if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Descend", id), player);
					}
				}
				if (!robot.descend(scan.nextInt())) {
					Python2MinecraftApi.fail("Could not descend block");
				}
			}
		});
		APIRegistry.registerCommand(RobotAPI.GETROBOTID, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int playerid = scan.nextInt();
			EntityPlayer player = (EntityPlayer) Python2MinecraftApi.getServerEntityByID(playerid);
			int id = player != null ? RobotMod.robotid2player.inverse().get(player) : RobotAPI.robotId;
			if (player != null) {
				RobotMod.logger.info("Getting Id For Player: " + player.getName());
			} else {
				RobotMod.logger.info("Player Id was null using stored RobotID: " + RobotAPI.robotId);
			}
			EntityRobot robot = (EntityRobot) Python2MinecraftApi.getServerEntityByID(id);
			robot.setIsFollowing(false);
			robot.removeNonEssentialAI();
			Python2MinecraftApi.sendLine(id);
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTINSPECT, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) Python2MinecraftApi.getServerEntityByID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (!robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 4))) {
					Python2MinecraftApi.fail("Robot does not know the inspect command");
					return;
				}

				if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Inspect", id), player);
					}
				}
				BlockPos curLoc = robot.getPosition();
				BlockPos inspectBlock = curLoc.offset(robot.getProgrammedDirection());
				if (scan.hasNext()) {
					BlockPos temp = Python2MinecraftApi.rotateVectorAngle(Python2MinecraftApi.getBlockPos(scan),
							HelperFunctions.getAngleFromFacing(robot.getProgrammedDirection()));

					if ((RobotAPI.sqVectorLength(temp) > 3) || (Math.abs(temp.getX()) > 1)
							|| (Math.abs(temp.getY()) > 1) || (Math.abs(temp.getZ()) > 1)) {
						Python2MinecraftApi.fail("Distance is greater than robots sensor");
						return;
					}
					inspectBlock = curLoc.add(temp);
				}
				Location loc = new Location(robot.worldObj, inspectBlock);
				Python2MinecraftApi.sendLine("" + eventHandler.getBlockId(loc) + "," + eventHandler.getBlockMeta(loc));
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTSAY, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
			EntityRobot robot = (EntityRobot) Python2MinecraftApi.getServerEntityByID(id);
			if (!robot.shouldExecuteCode()) {
				Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
				return;
			}
			// adds a comma to the beginning so we substring
			if (player != null) {
				NetworkManager.sendTo(new RobotSpeakMessage(scan.nextLine().substring(1), id), player);
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTJUMP, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) Python2MinecraftApi.getServerEntityByID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (!robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 1))) {
					Python2MinecraftApi.fail("Robot does not know the jump command");
					return;
				}
				if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Jumping", id), player);
					}
				}
				robot.setShouldJump(true);
			}
		});
	}

	public static void setRobotId(int id, EntityPlayer player) {
		RobotAPI.robotId = id;
		if (RobotMod.robotid2player.containsValue(player) && (RobotMod.robotid2player.inverse().get(player) != id)) {
			int oldId = RobotMod.robotid2player.inverse().remove(player);
			if (RobotMod.robotid2player.containsKey(oldId)) {
				RobotMod.robotid2player.remove(oldId);
			}
			RobotMod.logger
					.info("Replacing robot id " + oldId + " with new id " + id + " for player " + player.getName());
			RobotMod.robotid2player.put(id, player);
		} else {
			RobotMod.logger.info("Attaching robot " + id + " to player " + player.getName());
			RobotMod.robotid2player.put(id, player);
		}
	}

	private static int sqVectorLength(BlockPos temp) {
		return (temp.getX() * temp.getX()) + (temp.getY() * temp.getY()) + (temp.getZ() * temp.getZ());
	}
}
