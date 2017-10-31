package com.dyn.robot.api;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.network.CodeEvent;
import com.dyn.robot.network.NetworkManager;
import com.dyn.robot.network.messages.RobotSpeakMessage;
import com.dyn.robot.utils.EnchantmentUtils;
import com.dyn.robot.utils.HelperFunctions;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemBlockSpecial;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

public class RobotAPI extends Python2MinecraftApi {

	private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

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

	public static int robotId = 0;

	public static EntityRobot getRobotEntityFromID(int id) {
		EntityRobot robot = (EntityRobot) Python2MinecraftApi.getServerEntityByID(id);
		if ((robot == null) || robot.isDead) {
			Python2MinecraftApi.fail("Robot is dead or no longer exists");
		}
		return robot;
	}

	public static int getRobotId() {
		return RobotAPI.robotId;
	}

	protected static Entity getServerEntityByID(int id) {
		Entity entity = Python2MinecraftApi.getServerEntityByID(id);
		if ((entity == null) || entity.isDead) {
			Python2MinecraftApi.fail("Entity is dead or no longer exists");
		}
		return entity;
	}

	public static void notifyFailure(String failMessage) {
		Python2MinecraftApi.fail(failMessage);
	}

	public static void registerCommands() {
		APIRegistry.registerCommand(RobotAPI.ROBOTMOVE, (String args, Scanner scan) -> {
			int id = scan.nextInt();

			EntityRobot robot = (EntityRobot) RobotAPI.getRobotEntityFromID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 15))) {
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
		APIRegistry.registerCommand(RobotAPI.ROBOTNAME, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) RobotAPI.getRobotEntityFromID(id);
			Python2MinecraftApi.sendLine(robot.getName());
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTPLACE, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) RobotAPI.getRobotEntityFromID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (!robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 3))) {
					Python2MinecraftApi.fail("Robot does not know the place command");
					return;
				}
				if (robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 15))) {
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
				if (robot.world.getBlockState(placeBlock).getBlock().canPlaceBlockAt(robot.world, placeBlock)) {
					if (scan.hasNext()) {
						short blockId = scan.nextShort();
						short meta = scan.hasNextShort() ? scan.nextShort() : 0;

						if (!robot.robot_inventory.containsItem(new ItemStack(Block.getBlockById(blockId), 1, meta))) {
							Python2MinecraftApi.fail("Block not Found in Inventory");
							return;
						}

						final BlockPos immutablePlaceBlock = placeBlock;
						RobotMod.proxy.addScheduledTask(() -> {
							IBlockState oldState = robot.world.getBlockState(immutablePlaceBlock);
							Block oldBlock = oldState.getBlock();

							if ((Block.getIdFromBlock(oldBlock) != blockId)
									|| (oldBlock.getMetaFromState(oldState) != meta)) {
								if (null != robot.world.getTileEntity(immutablePlaceBlock)) {
									robot.world.removeTileEntity(immutablePlaceBlock);
								}
								robot.world.setBlockState(immutablePlaceBlock,
										RobotAPI.safeGetStateFromMeta(Block.getBlockById(blockId), meta), 3);
							}
						});
						MinecraftForge.EVENT_BUS.post(
								new CodeEvent.RobotSuccessEvent("Success", robot.getEntityId(), robot.getOwner()));
					} else {
						if (!robot.robot_inventory.isInventoryEmpty()) {
							int slot = 0;
							ItemStack inventorySlot = null;
							for (int i = 12; i < robot.robot_inventory.getSizeInventory(); i++) {
								if ((robot.robot_inventory.getStackInSlot(i) != null) && ((Block
										.getBlockFromItem(robot.robot_inventory.getStackInSlot(i).getItem()) != null)
										|| (robot.robot_inventory.getStackInSlot(i)
												.getItem() instanceof ItemBlockSpecial)
										|| (robot.robot_inventory.getStackInSlot(i).getItem() instanceof ItemDoor))) {
									inventorySlot = robot.robot_inventory.getStackInSlot(i);
									slot = i;
									break;
								}
							}

							if (inventorySlot != null) {
								if (inventorySlot.getItem().onItemUse(robot.getOwner(), robot.world, placeBlock,
										robot.getActiveHand(),
										(inventorySlot.getItem() instanceof ItemDoor) ? EnumFacing.UP
												: robot.getProgrammedDirection().getOpposite(),
										0, 0, 0) == EnumActionResult.FAIL) {
									Python2MinecraftApi.fail("Cannot place block at location");
								} else {
									if (robot.robot_inventory.getStackInSlot(slot).isEmpty()) {
										robot.robot_inventory.removeStackFromSlot(slot);
									}
									robot.swingArm(robot.getActiveHand());
									MinecraftForge.EVENT_BUS.post(new CodeEvent.RobotSuccessEvent("Success",
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
		APIRegistry.registerCommand(RobotAPI.ROBOTBREAK, (String args, Scanner scan) -> {
			// TODO we need to check permission if the robot can break blocks
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) RobotAPI.getRobotEntityFromID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (!robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 2))) {
					Python2MinecraftApi.fail("Robot does not know the breakBlock command");
					return;
				}
				if (robot.robot_inventory.getStackInSlot(2).isEmpty()
						|| (robot.robot_inventory.getStackInSlot(2).getItem() instanceof ItemSword)
						|| (robot.robot_inventory.getStackInSlot(2).getItem() instanceof ItemHoe)) {
					Python2MinecraftApi.fail("Robot has no tool to break block with");
					return;
				}
				if (robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Mining", id), player);
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
						Python2MinecraftApi
								.fail("Distance is greater than robots reach: " + String.format("(%d, %d, %d) = %d",
										temp.getX(), temp.getY(), temp.getZ(), RobotAPI.sqVectorLength(temp)));
						return;
					}
					breakBlock = curLoc.add(temp);
				}

				if (robot.world.getBlockState(breakBlock).getBlock() != Blocks.AIR) {
					if(robot.getPosition().down().equals(breakBlock)) {
						//we are mining straight down so update the robots position since he will fall at least one space
						robot.InsertToProgramPath(0, breakBlock);
					}
					IBlockState state = robot.world.getBlockState(breakBlock);
					float speed = Math
							.min(state.getBlockHardness(robot.world, breakBlock) / robot.getDigSpeed(state) / 10f, 2);
					final BlockPos immutableBreakBlock = breakBlock;
					robot.makeSwingArm(true);
					RobotAPI.scheduler.schedule(() -> {
						robot.makeSwingArm(false);
						if (!robot.robot_inventory.isInventoryFull()) {
							IBlockState broken = robot.world.getBlockState(immutableBreakBlock);
							robot.robot_inventory.addItemStackToInventory(
									new ItemStack(broken.getBlock(), 1, broken.getBlock().getMetaFromState(broken)));
						} else {
							robot.world.getBlockState(immutableBreakBlock).getBlock().dropBlockAsItem(robot.world,
									immutableBreakBlock, robot.world.getBlockState(immutableBreakBlock), 1);
						}
						BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(robot.world, immutableBreakBlock, state,
								robot.getOwner());
						MinecraftForge.EVENT_BUS.post(event);
						robot.world.setBlockToAir(immutableBreakBlock);
						MinecraftForge.EVENT_BUS.post(
								new CodeEvent.RobotSuccessEvent("Success", robot.getEntityId(), robot.getOwner()));
					}, (long) Math.max(100, 1000 * speed), TimeUnit.MILLISECONDS);

				} else {
					Python2MinecraftApi.fail("Nothing to break");
				}
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTINTERACT, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) RobotAPI.getRobotEntityFromID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (!robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 5))) {
					Python2MinecraftApi.fail("Robot does not know the interact command");
					return;
				}
				if (robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Interact", id), player);
					}
				}
				BlockPos curLoc = robot.getPosition();
				BlockPos interactBlock = null;
				if (Block.isEqualTo(robot.world.getBlockState(curLoc).getBlock(), Blocks.LEVER)
						|| Block.isEqualTo(robot.world.getBlockState(curLoc).getBlock(), Blocks.STONE_BUTTON)
						|| Block.isEqualTo(robot.world.getBlockState(curLoc).getBlock(), Blocks.WOODEN_BUTTON)) {
					interactBlock = curLoc;
				} else {
					interactBlock = curLoc.offset(robot.getProgrammedDirection());
				}
				if (robot.world.getBlockState(interactBlock).getBlock() != Blocks.AIR) {
					robot.swingArm(robot.getActiveHand());
					robot.world.getBlockState(interactBlock).getBlock().onBlockActivated(robot.world, interactBlock,
							robot.world.getBlockState(interactBlock), robot.getOwner(), robot.getActiveHand(),
							robot.getProgrammedDirection().getOpposite(), 0, 0, 0);
				}
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTTURN, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) RobotAPI.getRobotEntityFromID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Turning", id), player);
					}
				}
				float rotate = scan.nextFloat();
				float newYaw = MathHelper.wrapDegrees(robot.rotationYaw + rotate);
				robot.rotate(newYaw);
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTFACE, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) RobotAPI.getRobotEntityFromID(id);
			if (robot != null) {
				if (robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Facing", id), player);
					}
				}
				robot.rotate(HelperFunctions.getAngleFromFacing(EnumFacing.getFront(scan.nextInt())));
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTDETECT, (String args, Scanner scan) -> {
			// TODO: Need to be able to determine the mob type
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) RobotAPI.getRobotEntityFromID(id);
			if (robot != null) {
				if (!robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 6))) {
					Python2MinecraftApi.fail("Robot does not know the detect command");
					return;
				}
				if (robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Detecting", id), player);
					}
				}
				int level = EnchantmentUtils.getLevel(Enchantments.POWER,
						robot.robot_inventory.getStackOfItem(new ItemStack(RobotMod.expChip, 1, 6)));
				// Can map the entity this way
				// EntityList.stringToClassMapping.get("");
				List<EntityMob> list = robot.world.getEntitiesWithinAABB(EntityMob.class,
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
		APIRegistry.registerCommand(RobotAPI.ROBOTATTACK, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) RobotAPI.getRobotEntityFromID(id);
			if (robot != null) {
				if (!robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 7))) {
					Python2MinecraftApi.fail("Robot does not know the attack command");
					return;
				}
				if (robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Attacking", id), player);
					}
				}
				id = scan.nextInt();
				Entity target = RobotAPI.getServerEntityByID(id);
				if (target instanceof EntityPlayer) {
					Python2MinecraftApi.fail("Cannot go against the first law of robotics");
				}
				robot.setAttackTarget((EntityLivingBase) target);

			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTFORWARD, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) RobotAPI.getRobotEntityFromID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Forward", id), player);
					}
				}
				robot.moveForward(scan.nextInt());
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTBACKWARD, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) RobotAPI.getRobotEntityFromID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Backward", id), player);
					}
				}
				robot.moveBackward(scan.nextInt());
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTCLIMB, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) RobotAPI.getRobotEntityFromID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (!robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 0))) {
					Python2MinecraftApi.fail("Robot does not know the climb command");
					return;
				}
				if (robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 15))) {
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
		APIRegistry.registerCommand(RobotAPI.ROBOTDESCEND, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) RobotAPI.getRobotEntityFromID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (!robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 0))) {
					Python2MinecraftApi.fail("Robot does not know the descend command");
					return;
				}
				if (robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 15))) {
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
		APIRegistry.registerCommand(RobotAPI.GETROBOTID, (String args, Scanner scan) -> {
			int playerid = scan.nextInt();
			EntityPlayer player = (EntityPlayer) RobotAPI.getServerEntityByID(playerid);
			int id = player != null ? RobotMod.robotid2player.inverse().get(player) : RobotAPI.robotId;
			if (player != null) {
				RobotMod.logger.info("Getting Id For Player: " + player.getName());
			} else {
				RobotMod.logger.info("Player Id was null using stored RobotID: " + RobotAPI.robotId);
			}
			EntityRobot robot = (EntityRobot) RobotAPI.getRobotEntityFromID(id);
			robot.setIsFollowing(false);
			robot.removeIdleAI();
			Python2MinecraftApi.sendLine(id);
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTINSPECT, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) RobotAPI.getRobotEntityFromID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (!robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 4))) {
					Python2MinecraftApi.fail("Robot does not know the inspect command");
					return;
				}

				if (robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 15))) {
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

				int blockId = Block.getIdFromBlock(robot.world.getBlockState(inspectBlock).getBlock());

				IBlockState state = robot.world.getBlockState(inspectBlock);
				int meta = state.getBlock().getMetaFromState(state);

				Python2MinecraftApi.sendLine("" + blockId + "," + meta);
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTSAY, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
			EntityRobot robot = (EntityRobot) RobotAPI.getRobotEntityFromID(id);
			if (!robot.shouldExecuteCode()) {
				Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
				return;
			}
			// adds a comma to the beginning so we substring
			if (!robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 15))) {
				Python2MinecraftApi.fail("Robot does not know the say command");
				return;
			}
			if (player != null) {
				NetworkManager.sendTo(new RobotSpeakMessage(scan.nextLine().substring(1), id), player);
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTJUMP, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = (EntityRobot) RobotAPI.getRobotEntityFromID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (!robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 1))) {
					Python2MinecraftApi.fail("Robot does not know the jump command");
					return;
				}
				if (robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Jumping", id), player);
					}
				}
				robot.setShouldJump(true);
			}
		});
	}

	private static IBlockState safeGetStateFromMeta(Block b, int meta) {
		try {
			return b.getStateFromMeta(meta);
		} catch (Exception e) {
			return b.getStateFromMeta(0);
		}
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
