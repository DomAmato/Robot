package com.dyn.robot.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.network.CodeEvent;
import com.dyn.robot.network.NetworkManager;
import com.dyn.robot.network.messages.RobotSpeakMessage;
import com.dyn.robot.utils.EnchantmentUtils;
import com.dyn.robot.utils.SimpleItemStack;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlockSpecial;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.oredict.OreIngredient;

public class RobotAPI extends Python2MinecraftApi {

	private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	private static final String GETROBOTID = "robot.id";

	// Movement
	private static final String ROBOTMOVE = "robot.moveTo";
	private static final String ROBOTORIENT = "robot.reorient";
	private static final String ROBOTTURN = "robot.turn";
	private static final String ROBOTFORWARD = "robot.forward";
	private static final String ROBOTCLIMB = "robot.climb";
	private static final String ROBOTDESCEND = "robot.descend";
	private static final String ROBOTBACKWARD = "robot.backward";
	private static final String ROBOTJUMP = "robot.jump";
	private static final String ROBOTFACE = "robot.face";

	// Interactions
	private static final String ROBOTPLACE = "robot.place";
	private static final String ROBOTBREAK = "robot.break";
	private static final String ROBOTINTERACT = "robot.interact";
	private static final String ROBOTINSPECT = "robot.inspect";
	private static final String ROBOTSAY = "robot.say";
	private static final String ROBOTNAME = "robot.name";
	private static final String ROBOTDETECT = "robot.detect";
	private static final String ROBOTATTACK = "robot.attack";
	private static final String ROBOTUSEITEM = "robot.useItem";
	private static final String ROBOTUSETOOL = "robot.useTool";

	// Inventory
	private static final String ROBOTCRAFT = "robot.craft";
	private static final String ROBOTEQUIP = "robot.equip";
	private static final String ROBOTDEPOSIT = "robot.deposit";
	private static final String ROBOTTAKE = "robot.take";
	private static final String ROBOTHAS = "robot.contains";

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

			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
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
		APIRegistry.registerCommand(RobotAPI.ROBOTORIENT, (String args, Scanner scan) -> {
			int id = scan.nextInt();

			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Reorienting", id), player);
					}
				}
				robot.clearProgramPath();
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTNAME, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
			Python2MinecraftApi.sendLine(robot.getName());
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTPLACE, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
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

				if (scan.nextBoolean()) {
					BlockPos temp = Python2MinecraftApi.rotateVectorAngle(Python2MinecraftApi.getBlockPos(scan),
							robot.getProgrammedDirection().getHorizontalAngle());

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
					int slot = 0;
					ItemStack inventorySlot = null;
					if (scan.nextBoolean()) {
						short blockId = scan.nextShort();
						short meta = scan.hasNextShort() ? scan.nextShort() : 0;

						if (Block.getBlockById(blockId) == Blocks.AIR) {
							inventorySlot = new ItemStack(Item.getItemById(blockId), 1, meta);
						} else {
							inventorySlot = new ItemStack(Block.getBlockById(blockId), 1, meta);
						}
						if (!robot.robot_inventory.containsItem(inventorySlot)) {
							Python2MinecraftApi.fail("Block not Found in Inventory");
							return;
						}
						for (int i = 14; i < robot.robot_inventory.getSizeInventory(); i++) {
							if ((robot.robot_inventory.getStackInSlot(i) != ItemStack.EMPTY) && (robot.robot_inventory
									.getStackInSlot(i).isItemEqualIgnoreDurability(inventorySlot))) {
								slot = i;
								break;
							}
						}

					} else {
						if (!robot.robot_inventory.isInventoryEmpty()) {
							for (int i = 14; i < robot.robot_inventory.getSizeInventory(); i++) {
								if ((robot.robot_inventory.getStackInSlot(i) != ItemStack.EMPTY) && ((Block
										.getBlockFromItem(
												robot.robot_inventory.getStackInSlot(i).getItem()) != Blocks.AIR)
										|| (robot.robot_inventory.getStackInSlot(i)
												.getItem() instanceof ItemBlockSpecial)
										|| (robot.robot_inventory.getStackInSlot(i).getItem() instanceof ItemDoor))) {
									inventorySlot = robot.robot_inventory.getStackInSlot(i);
									slot = i;
									break;
								}
							}
						} else {
							Python2MinecraftApi.fail("No Block in Inventory");
							return;
						}
					}

					if (inventorySlot != null) {
						FakePlayer fakeplayer = FakePlayerFactory
								.getMinecraft(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0));
						fakeplayer.setHeldItem(robot.getActiveHand(), inventorySlot);
						if (inventorySlot.getItem().onItemUse(fakeplayer, robot.world, placeBlock,
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
							MinecraftForge.EVENT_BUS.post(
									new CodeEvent.RobotSuccessEvent("Success", robot.getEntityId(), robot.getOwner()));
						}
					} else {
						Python2MinecraftApi.fail("No Valid Block Found in Inventory");
						return;
					}
				} else {
					Python2MinecraftApi.fail("Cannot place block at location");
				}
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTBREAK, (String args, Scanner scan) -> {
			// TODO we need to check permission if the robot can break blocks
			int id = scan.nextInt();
			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (!robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 2))) {
					Python2MinecraftApi.fail("Robot does not know the mine command");
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
							robot.getProgrammedDirection().getHorizontalAngle());

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

				// blocks with hardness less than 0 are unbreakable
				if ((robot.world.getBlockState(breakBlock).getBlock() != Blocks.AIR)
						&& (robot.world.getBlockState(breakBlock).getBlockHardness(robot.world, breakBlock) >= 0)) {
					if (robot.getPosition().down().equals(breakBlock)) {
						// we are mining straight down so update the robots position since it will fall
						// at least one space
						robot.InsertToProgramPath(0, breakBlock);
					}
					final IBlockState state = robot.world.getBlockState(breakBlock);
					final float speed = Math
							.min(state.getBlockHardness(robot.world, breakBlock) / robot.getDigSpeed(state) / 10f, 2);
					final BlockPos immutableBreakBlock = breakBlock;
					robot.makeSwingArm(true);
					RobotAPI.scheduler.schedule(() -> {
						// we need this to happen on the server not client
						FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
							robot.makeSwingArm(false);
							NonNullList<ItemStack> drops = NonNullList.create();
							state.getBlock().getDrops(drops, robot.world, immutableBreakBlock, state, 1);
							boolean dropBlock = false;
							for (ItemStack is : drops) {
								if (!robot.robot_inventory.canAddToInventory(is)) {
									dropBlock = true;
								}
							}
							if (!dropBlock) {
								for (ItemStack is : drops) {
									robot.robot_inventory.addItemStackToInventory(is);
								}
							}
							final boolean immutableDrop = dropBlock;

							BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(robot.world, immutableBreakBlock,
									state, robot.getOwner());
							MinecraftForge.EVENT_BUS.post(event);
							robot.world.destroyBlock(immutableBreakBlock, immutableDrop);
							MinecraftForge.EVENT_BUS.post(
									new CodeEvent.RobotSuccessEvent("Success", robot.getEntityId(), robot.getOwner()));
						});
					}, (long) Math.max(100, 1000 * speed), TimeUnit.MILLISECONDS);

				} else {
					Python2MinecraftApi.fail("Nothing to break or block is unbreakable");
				}
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTINTERACT, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
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
					ItemStack inventorySlot = null;
					for (int i = 14; i < robot.robot_inventory.getSizeInventory(); i++) {
						if ((robot.robot_inventory.getStackInSlot(i) != ItemStack.EMPTY)
								&& ((Block.getBlockFromItem(robot.robot_inventory.getStackInSlot(i).getItem()) != null)
										|| (robot.robot_inventory.getStackInSlot(i)
												.getItem() instanceof ItemBlockSpecial)
										|| (robot.robot_inventory.getStackInSlot(i).getItem() instanceof ItemDoor))) {
							inventorySlot = robot.robot_inventory.getStackInSlot(i);
							break;
						}
					}
					FakePlayer fakeplayer = FakePlayerFactory
							.getMinecraft(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0));
					fakeplayer.setHeldItem(robot.getActiveHand(), inventorySlot);
					robot.world.getBlockState(interactBlock).getBlock().onBlockActivated(robot.world, interactBlock,
							robot.world.getBlockState(interactBlock), fakeplayer, robot.getActiveHand(),
							robot.getProgrammedDirection().getOpposite(), 0, 0, 0);
				}
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTTURN, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
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
			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
			if (robot != null) {
				if (robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Facing", id), player);
					}
				}
				robot.rotate(EnumFacing.getFront(scan.nextInt()).getHorizontalAngle());
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTDETECT, (String args, Scanner scan) -> {
			// TODO: Need to be able to determine the mob type
			int id = scan.nextInt();
			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
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
			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
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
			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
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
			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
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
			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
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
			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
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
				RobotMod.logger.debug("Getting Id For Player: " + player.getName());
			} else {
				RobotMod.logger.debug("Player Id was null using stored RobotID: " + RobotAPI.robotId);
			}
			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
			robot.setIsFollowing(false);
			robot.removeIdleAI();
			Python2MinecraftApi.sendLine(id);
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTINSPECT, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
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
							robot.getProgrammedDirection().getHorizontalAngle());

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
			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
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
			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
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
		APIRegistry.registerCommand(RobotAPI.ROBOTUSEITEM, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
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
						NetworkManager.sendTo(new RobotSpeakMessage("Using Item", id), player);
					}
				}
				BlockPos curLoc = robot.getPosition();
				BlockPos interactBlock = curLoc.offset(robot.getProgrammedDirection()).down();
				if (scan.nextBoolean()) { // we were given a location
					// this rotation is the problem
					interactBlock = interactBlock.up();
					BlockPos temp = Python2MinecraftApi.rotateVectorAngle(Python2MinecraftApi.getBlockPos(scan),
							robot.getProgrammedDirection().getHorizontalAngle());

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
					interactBlock = curLoc.add(temp);
				}
				ItemStack inventorySlot = null;
				int slot = 0;
				short blockId = scan.nextShort();
				short meta = scan.hasNextShort() ? scan.nextShort() : 0;

				inventorySlot = new ItemStack(Item.getItemById(blockId), 1, meta);

				if (!robot.robot_inventory.containsItem(inventorySlot)) {
					Python2MinecraftApi.fail("Item not found in Inventory");
					return;
				}
				for (int i = 14; i < robot.robot_inventory.getSizeInventory(); i++) {
					if ((robot.robot_inventory.getStackInSlot(i) != ItemStack.EMPTY)
							&& (robot.robot_inventory.getStackInSlot(i).isItemEqualIgnoreDurability(inventorySlot))) {
						slot = i;
						break;
					}
				}

				robot.swingArm(robot.getActiveHand());

				FakePlayer fakeplayer = FakePlayerFactory
						.getMinecraft(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0));
				fakeplayer.setHeldItem(robot.getActiveHand(), inventorySlot);
				if (inventorySlot
						.onItemUse(fakeplayer, robot.world, interactBlock, robot.getActiveHand(),
								interactBlock.getY() < robot.getPosition().getY() ? EnumFacing.UP
										: robot.getProgrammedDirection().getOpposite(),
								0, 0, 0) == EnumActionResult.PASS) {
					robot.robot_inventory.decrStackSize(slot, 1);
					MinecraftForge.EVENT_BUS
							.post(new CodeEvent.RobotSuccessEvent("Success", robot.getEntityId(), robot.getOwner()));
				} else {
					Python2MinecraftApi.fail("Was unable to use item " + inventorySlot.getDisplayName()
							+ " at location " + interactBlock);
				}
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTUSETOOL, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
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
						NetworkManager.sendTo(new RobotSpeakMessage("Using Tool", id), player);
					}
				}
				BlockPos curLoc = robot.getPosition();
				BlockPos interactBlock = curLoc.offset(robot.getProgrammedDirection()).down();
				if (scan.hasNext()) {
					// this rotation is the problem
					BlockPos temp = Python2MinecraftApi.rotateVectorAngle(Python2MinecraftApi.getBlockPos(scan),
							robot.getProgrammedDirection().getHorizontalAngle());

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
					interactBlock = curLoc.add(temp);
				}

				ItemStack heldItem = robot.getHeldItemMainhand();
				robot.swingArm(robot.getActiveHand());

				FakePlayer fakeplayer = FakePlayerFactory
						.getMinecraft(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0));
				fakeplayer.setHeldItem(robot.getActiveHand(), heldItem);
				heldItem.onItemUse(fakeplayer, robot.world, interactBlock, robot.getActiveHand(),
						interactBlock.getY() < robot.getPosition().getY() ? EnumFacing.UP
								: robot.getProgrammedDirection().getOpposite(),
						0, 0, 0);
				MinecraftForge.EVENT_BUS
						.post(new CodeEvent.RobotSuccessEvent("Success", robot.getEntityId(), robot.getOwner()));
			}
		});
		APIRegistry.registerCommand(RobotAPI.ROBOTEQUIP, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Equipping Item", id), player);
					}
				}
				ItemStack inventorySlot = null;
				short itemId = scan.nextShort();
				short meta = scan.hasNextShort() ? scan.nextShort() : 0;

				inventorySlot = new ItemStack(Item.getItemById(itemId), 1, meta);

				if (inventorySlot == ItemStack.EMPTY || !(robot.robot_inventory.isItemValidForSlot(2, inventorySlot))) {
					Python2MinecraftApi.fail("Cannot Equip passed in Item");
				}
				if (!robot.robot_inventory.containsItem(inventorySlot)) {
					Python2MinecraftApi.fail("Item not found in Inventory");
					return;
				}
				int slot = 0;
				for (int i = 14; i < robot.robot_inventory.getSizeInventory(); i++) {
					if ((robot.robot_inventory.getStackInSlot(i) != ItemStack.EMPTY)
							&& (robot.robot_inventory.getStackInSlot(i).isItemEqualIgnoreDurability(inventorySlot))) {
						slot = i;
						break;
					}
				}

				ItemStack equippingItem = robot.robot_inventory.removeStackFromSlot(slot);
				ItemStack equippedItem = robot.robot_inventory.removeStackFromSlot(2);

				robot.robot_inventory.setInventorySlotContents(2, equippingItem);
				robot.robot_inventory.setInventorySlotContents(slot, equippedItem);

				robot.setHeldItem(EnumHand.MAIN_HAND, equippingItem);

				MinecraftForge.EVENT_BUS
						.post(new CodeEvent.RobotSuccessEvent("Success", robot.getEntityId(), robot.getOwner()));
			}
		});

		APIRegistry.registerCommand(RobotAPI.ROBOTCRAFT, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Crafting Item", id), player);
					}
				}
				short itemId = scan.nextShort();
				short meta = scan.hasNextShort() ? scan.nextShort() : 0;

				ItemStack inventorySlot = new ItemStack(Item.getItemById(itemId), 1, meta);

				if (inventorySlot == ItemStack.EMPTY) {
					Python2MinecraftApi.fail("Item is not valid");
					return;
				}

				if (!RobotMod.recipeMap.containsKey(new SimpleItemStack(inventorySlot))) {
					Python2MinecraftApi.fail(inventorySlot.getDisplayName() + " is not craftable");
					return;
				}
				
				List<ItemStack> ingredients = new ArrayList();
				List<Set<Item>> oreingredients = new ArrayList();
				for (IRecipe recipe : RobotMod.recipeMap.get(new SimpleItemStack(inventorySlot))) {
					for (Ingredient ingredient : recipe.getIngredients()) {
						if (ingredient instanceof OreIngredient) {
							if (ingredient != Ingredient.EMPTY) {
								boolean hasItem = false;
								Set<Item> oreItems = new HashSet();
								for (ItemStack stack : ingredient.getMatchingStacks()) {
									//things like planks will return every variant which gives us a ton of extra ingredients
									RobotMod.logger.info("Recipe requires: " + stack.getDisplayName());
									if(robot.robot_inventory.containsItem(stack.getItem())) {
										oreItems.add(stack.getItem());
										hasItem = true;
									}
								}
								if(!hasItem) {
									ingredients.clear();
									oreingredients.clear();
									break;
								} else {
									oreingredients.add(oreItems);
								}
							}
						} else {
							if (ingredient != Ingredient.EMPTY) {
								boolean hasItem = false;
								for (ItemStack stack : ingredient.getMatchingStacks()) {
									RobotMod.logger.info("Recipe requires: " + stack.getDisplayName());
									if(robot.robot_inventory.containsItem(stack)) {
										ingredients.add(stack);
										hasItem = true;
									}
								}
								if(!hasItem) {
									ingredients.clear();
									oreingredients.clear();
									break;
								}
							}
						}
					}
					if(!ingredients.isEmpty() || !oreingredients.isEmpty()) {
						for(ItemStack ingredient : ingredients) {
							robot.robot_inventory.removeItemFromInventory(ingredient, 1);
						}
						for(Set<Item> ingredient : oreingredients) {
							for(Item i_item : ingredient) {
								if(robot.robot_inventory.removeItemTypeFromInventory(i_item, 1)) {
									break;
								}
							}
						}
						
						robot.robot_inventory.addItemStackToInventory(recipe.getRecipeOutput());
						
						MinecraftForge.EVENT_BUS
						.post(new CodeEvent.RobotSuccessEvent("Success", robot.getEntityId(), robot.getOwner()));
						return;
					}
				}
				Python2MinecraftApi.fail("Cannot Craft, missing required items in inventory");
			}
		});

		APIRegistry.registerCommand(RobotAPI.ROBOTHAS, (String args, Scanner scan) -> {
			int id = scan.nextInt();
			EntityRobot robot = RobotAPI.getRobotEntityFromID(id);
			if (robot != null) {
				if (!robot.shouldExecuteCode()) {
					Python2MinecraftApi.fail("Robot is not executing code, it might be out of sync");
					return;
				}
				if (robot.robot_inventory.hasExpansionChip(new ItemStack(RobotMod.expChip, 1, 15))) {
					EntityPlayerMP player = (EntityPlayerMP) RobotMod.robotid2player.get(id);
					if (player != null) {
						NetworkManager.sendTo(new RobotSpeakMessage("Checking Inventory", id), player);
					}
				}
				short itemId = scan.nextShort();
				short meta = scan.hasNextShort() ? scan.nextShort() : 0;
				
				short amount = scan.nextShort();

				ItemStack inventorySlot = new ItemStack(Item.getItemById(itemId), 1, meta);

				if (!robot.robot_inventory.containsItem(inventorySlot) || robot.robot_inventory.getQuantityOfItem(inventorySlot) < amount) {
					MinecraftForge.EVENT_BUS
							.post(new CodeEvent.RobotSuccessEvent("False", robot.getEntityId(), robot.getOwner()));
				}

				MinecraftForge.EVENT_BUS
						.post(new CodeEvent.RobotSuccessEvent("True", robot.getEntityId(), robot.getOwner()));
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
					.debug("Replacing robot id " + oldId + " with new id " + id + " for player " + player.getName());
			RobotMod.robotid2player.put(id, player);
		} else {
			RobotMod.logger.debug("Attaching robot " + id + " to player " + player.getName());
			RobotMod.robotid2player.put(id, player);
		}
	}

	private static int sqVectorLength(BlockPos temp) {
		return (temp.getX() * temp.getX()) + (temp.getY() * temp.getY()) + (temp.getZ() * temp.getZ());
	}
}
