package com.dyn.rjm.api;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import com.dyn.rjm.RaspberryJamMod;
import com.dyn.rjm.actions.SetBlockNBT;
import com.dyn.rjm.actions.SetBlockStateWithId;
import com.dyn.rjm.actions.SetBlocksNBT;
import com.dyn.rjm.actions.SetBlocksState;
import com.dyn.rjm.events.MCEventHandler;
import com.dyn.rjm.network.SocketEvent;
import com.dyn.rjm.util.Location;
import com.dyn.rjm.util.SetDimension;
import com.dyn.rjm.util.Vec3w;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class Python2MinecraftApi {

	public static class ChatDescription {
		int id;
		String message;

		public ChatDescription(int entityId, String message) {
			id = entityId;
			this.message = message;
		}
	}

	static class HitDescription {
		private String description;

		public HitDescription(World[] worlds, PlayerInteractEvent event) {
			Vec3i pos = Location.encodeVec3i(worlds, event.getEntityPlayer().getEntityWorld(), event.getPos().getX(),
					event.getPos().getY(), event.getPos().getZ());
			description = "" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "," + numericFace(event.getFace())
					+ "," + event.getEntity().getEntityId();
		}

		public String getDescription() {
			return description;
		}

		private int numericFace(EnumFacing face) {
			switch (face) {
			case DOWN:
				return 0;
			case UP:
				return 1;
			case NORTH:
				return 2;
			case SOUTH:
				return 3;
			case WEST:
				return 4;
			case EAST:
				return 5;
			default:
				return 7;
			}
		}
	}

	// world.checkpoint.save/restore, player.setting,
	// world.setting(nametags_visible,*),
	// camera.setFixed() unsupported
	// camera.setNormal(id) and camera.setFollow(id) uses spectating, and so
	// it
	// moves the
	// player along with the entity that was set as camera
	protected static final String AUTHENTICATE = "mcpi.auth";
	protected static final String CLOSESOCKET = "mcpi.close";

	protected static final String CHAT = "chat.post";
	protected static final String SETBLOCK = "world.setBlock";
	protected static final String SETBLOCKS = "world.setBlocks";
	protected static final String GETBLOCK = "world.getBlock";
	protected static final String GETBLOCKWITHDATA = "world.getBlockWithData";
	protected static final String GETBLOCKS = "world.getBlocks";
	protected static final String GETBLOCKSWITHDATA = "world.getBlocksWithData";
	protected static final String GETHEIGHT = "world.getHeight";
	protected static final String WORLDSPAWNENTITY = "world.spawnEntity";
	protected static final String WORLDSPAWNPARTICLE = "world.spawnParticle";
	protected static final String WORLDDELETEENTITY = "world.removeEntity";
	protected static final String WORLDGETPLAYERIDS = "world.getPlayerIds";

	protected static final String WORLDGETPLAYERID = "world.getPlayerId";
	protected static final String WORLDSETTING = "world.setting";

	// EXPERIMENTAL AND UNSUPPORTED
	protected static final String GETLIGHTLEVEL = "block.getLightLevel";
	protected static final String SETLIGHTLEVEL = "block.setLightLevel";

	protected static final String EVENTSBLOCKHITS = "events.block.hits";
	protected static final String EVENTSCHATPOSTS = "events.chat.posts";

	protected static final String EVENTSCLEAR = "events.clear";
	protected static final String EVENTSSETTING = "events.setting";
	// camera.*
	protected static final String SETFOLLOW = "setFollow";
	protected static final String SETNORMAL = "setNormal";
	protected static final String GETENTITYID = "getEntityId";

	// EXPERIMENTAL AND UNSUPPORTED
	protected static final String SETDEBUG = "setDebug";
	protected static final String SETDISTANCE = "setDistance";

	// player.* or entity.*
	protected static final String GETDIRECTION = "getDirection";
	protected static final String GETPITCH = "getPitch";
	protected static final String GETPOS = "getPos";
	protected static final String GETROTATION = "getRotation";
	protected static final String GETTILE = "getTile";

	// EXPERIMENTAL AND UNSUPPORTED
	protected static final String SETDIMENSION = "setDimension";

	protected static final String SETDIRECTION = "setDirection";
	protected static final String SETPITCH = "setPitch";
	protected static final String SETPOS = "setPos";
	protected static final String SETROTATION = "setRotation";

	protected static final String SETTILE = "setTile";

	protected static final String GETNAME = "getNameAndUUID";
	protected static final float TOO_SMALL = (float) 1e-9;
	protected static final int MAX_CHATS = 512;
	protected static final int MAX_HITS = 512;

	protected static boolean useClientMethods = false;

	protected static World[] serverWorlds;

	protected static Minecraft mc;

	protected static PrintWriter writer = null;

	protected static boolean includeNBTWithData = false;
	protected static List<HitDescription> hits = new LinkedList<>();
	protected static List<ChatDescription> chats = new LinkedList<>();
	private volatile static boolean restrictToSword = true;
	private volatile static boolean detectLeftClick = RaspberryJamMod.leftClickToo;

	public static void addChatDescription(ChatDescription cd) {
		synchronized (Python2MinecraftApi.chats) {
			if (Python2MinecraftApi.chats.size() >= Python2MinecraftApi.MAX_CHATS) {
				Python2MinecraftApi.chats.remove(0);
			}
			Python2MinecraftApi.chats.add(cd);
		}
	}

	protected static void chat(String msg) {
		if (!RaspberryJamMod.integrated
				|| (RaspberryJamMod.globalChatMessages && !Python2MinecraftApi.useClientMethods)) {
			Python2MinecraftApi.globalMessage(msg);
		} else {
			Python2MinecraftApi.mc.player.sendMessage(new TextComponentString(msg));
		}
	}

	public static void clearAllEvents() {
		Python2MinecraftApi.hits.clear();
		Python2MinecraftApi.chats.clear();
	}

	public static boolean doesUseClientMethods() {
		return Python2MinecraftApi.useClientMethods;
	}

	protected static void entityGetDirection(int id) {
		Entity e = Python2MinecraftApi.getServerEntityByID(id);
		if (e != null) {
			// sendLine(e.getLookVec());
			double pitch = (e.rotationPitch * Math.PI) / 180.;
			double yaw = (e.rotationYaw * Math.PI) / 180.;
			double x = Math.cos(-pitch) * Math.sin(-yaw);
			double z = Math.cos(-pitch) * Math.cos(-yaw);
			double y = Math.sin(-pitch);
			Python2MinecraftApi.sendLine(new Vec3d(x, y, z));
		}
	}

	protected static void entityGetNameAndUUID(int id) {
		Entity e = Python2MinecraftApi.getServerEntityByID(id);
		if (e == null) {
			Python2MinecraftApi.fail("Unknown entity");
		} else {
			Python2MinecraftApi.sendLine(e.getName() + "," + e.getUniqueID());
		}
	}

	protected static void entityGetPitch(int id) {
		Entity e = Python2MinecraftApi.getServerEntityByID(id);
		if (e != null) {
			Python2MinecraftApi.sendLine(MathHelper.wrapDegrees(e.rotationPitch));
		}
	}

	protected static void entityGetPos(int id) {
		Entity e = Python2MinecraftApi.getServerEntityByID(id);
		if (e != null) {
			World w = e.getEntityWorld();
			Vec3d pos0 = e.getPositionVector();
			while (w != e.getEntityWorld()) {
				// Rare concurrency issue: entity switched worlds between
				// getting w and pos0.
				// To be somewhat safe, let's sleep for approximately a
				// server
				// tick and get
				// everything again.
				try {
					Thread.sleep(50);
				} catch (Exception exc) {
				}
				w = e.getEntityWorld();
				pos0 = e.getPositionVector();
			}

			Vec3d pos = Location.encodeVec3(Python2MinecraftApi.serverWorlds, w, pos0);
			Python2MinecraftApi.sendLine(pos);
		}
	}

	protected static void entityGetRotation(int id) {
		Entity e = Python2MinecraftApi.getServerEntityByID(id);
		if (e != null) {
			Python2MinecraftApi.sendLine(MathHelper.wrapDegrees(e.rotationYaw));
		}
	}

	protected static void entityGetTile(int id) {
		Entity e = Python2MinecraftApi.getServerEntityByID(id);
		if (e != null) {
			World w = e.getEntityWorld();
			e.getPositionVector();

			while (w != e.getEntityWorld()) {
				// Rare concurrency issue: entity switched worlds between
				// getting w and pos0.
				// To be somewhat safe, let's sleep for approximately a
				// server
				// tick and get
				// everything again.
				try {
					Thread.sleep(50);
				} catch (Exception exc) {
				}
				w = e.getEntityWorld();
				e.getPositionVector();
			}

			Vec3d pos = Location.encodeVec3(Python2MinecraftApi.serverWorlds, w, e.getPositionVector());
			Python2MinecraftApi.sendLine("" + Python2MinecraftApi.trunc(pos.x) + "," + Python2MinecraftApi.trunc(pos.y)
					+ "," + Python2MinecraftApi.trunc(pos.z));
		}
	}

	protected static void entitySetDimension(int id, int dimension, MCEventHandler eventHandler) {
		Entity e = Python2MinecraftApi.getServerEntityByID(id);
		if (e != null) {
			eventHandler.queueServerAction(new SetDimension(e, dimension));
		}
	}

	protected static void entitySetDirection(Entity e, double x, double y, double z) {
		double xz = Math.sqrt((x * x) + (z * z));

		if (xz >= Python2MinecraftApi.TOO_SMALL) {
			float yaw = (float) ((Math.atan2(-x, z) * 180) / Math.PI);
			e.setRotationYawHead(yaw);
			e.rotationYaw = yaw;
		}

		if (((x * x) + (y * y) + (z * z)) >= (Python2MinecraftApi.TOO_SMALL * Python2MinecraftApi.TOO_SMALL)) {
			e.rotationPitch = (float) ((Math.atan2(-y, xz) * 180) / Math.PI);
		}
	}

	protected static void entitySetDirection(int id, Scanner scan) {
		double x = scan.nextDouble();
		double y = scan.nextDouble();
		double z = scan.nextDouble();
		Entity e = Python2MinecraftApi.getServerEntityByID(id);
		if (e != null) {
			Python2MinecraftApi.entitySetDirection(e, x, y, z);
		}

		if (!RaspberryJamMod.integrated) {
			return;
		}

		e = Python2MinecraftApi.mc.world.getEntityByID(id);
		if (e != null) {
			Python2MinecraftApi.entitySetDirection(e, x, y, z);
		}
	}

	protected static void entitySetPitch(int id, float angle) {
		Entity e = Python2MinecraftApi.getServerEntityByID(id);
		if (e != null) {
			e.rotationPitch = angle;
		}

		if (!RaspberryJamMod.integrated) {
			return;
		}

		e = Python2MinecraftApi.mc.world.getEntityByID(id);
		if (e != null) {
			e.rotationPitch = angle;
		}
	}

	protected static void entitySetPos(int id, Scanner scan) {
		Entity e = Python2MinecraftApi.getServerEntityByID(id);
		if (e != null) {
			float serverYaw = 0f;
			serverYaw = e.rotationYaw;

			double x = scan.nextDouble();
			double y = scan.nextDouble();
			double z = scan.nextDouble();
			Vec3w pos = Location.decodeVec3w(Python2MinecraftApi.serverWorlds, x, y, z);
			if (pos.getWorld() != e.getEntityWorld()) {
				// e.setWorld(pos.world);
				RaspberryJamMod.logger.info("World change unsupported");
				// TODO: implement moving between worlds
				return;
			}
			e.setPositionAndUpdate(pos.x, pos.y, pos.z);
			e.setRotationYawHead(serverYaw);

			if (!RaspberryJamMod.integrated) {
				return;
			}

			e = Python2MinecraftApi.mc.world.getEntityByID(id);
			if (e != null) {
				e.rotationYaw = serverYaw;
				e.setRotationYawHead(serverYaw);
			}
		}
	}

	protected static void entitySetRotation(int id, float angle) {
		Entity e = Python2MinecraftApi.getServerEntityByID(id);
		if (e != null) {
			e.rotationYaw = angle;
			e.setRotationYawHead(angle);
		}

		if (!RaspberryJamMod.integrated) {
			return;
		}

		e = Python2MinecraftApi.mc.world.getEntityByID(id);
		if (e != null) {
			e.rotationYaw = angle;
			e.setRotationYawHead(angle);
		}
	}

	protected static void entitySetTile(int id, Scanner scan) {
		Entity e = Python2MinecraftApi.getServerEntityByID(id);
		if (e != null) {
			float serverYaw = 0f;
			if (e != null) {
				serverYaw = e.rotationYaw;
				Location pos = Python2MinecraftApi.getBlockLocation(scan);
				if (pos.getWorld() != e.getEntityWorld()) {
					// TODO: implement moving between worlds
					return;
				}
				e.setPositionAndUpdate(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
				e.setRotationYawHead(serverYaw);
			}

			if (!RaspberryJamMod.integrated) {
				return;
			}

			e = Python2MinecraftApi.mc.world.getEntityByID(id);
			if (e != null) {
				e.rotationYaw = serverYaw;
				e.setRotationYawHead(serverYaw);
			}
		}
	}

	protected static void fail(String error) {
		RaspberryJamMod.logger.info("Code Error: " + error);
		Python2MinecraftApi.sendLine("FAIL|" + error);
	}

	protected static Location getBlockLocation(Scanner scan) {
		int x = scan.nextInt();
		int y = scan.nextInt();
		int z = scan.nextInt();
		return Location.decodeLocation(Python2MinecraftApi.serverWorlds, x, y, z);
	}

	protected static BlockPos getBlockPos(Scanner scan) {
		int x = scan.nextInt();
		int y = scan.nextInt();
		int z = scan.nextInt();
		return new BlockPos(x, y, z);
	}

	public static String getChatsAndClear() {
		StringBuilder out = new StringBuilder();

		synchronized (Python2MinecraftApi.chats) {
			Python2MinecraftApi.hits.size();
			for (ChatDescription c : Python2MinecraftApi.chats) {
				if (out.length() > 0) {
					out.append("|");
				}
				out.append(c.id);
				out.append(",");
				out.append(c.message.replace("&", "&amp;").replace("|", "&#124;"));
			}
			Python2MinecraftApi.chats.clear();
		}

		return out.toString();
	}

	public static String getHitsAndClear() {
		String out = "";

		synchronized (Python2MinecraftApi.hits) {
			Python2MinecraftApi.hits.size();
			for (HitDescription e : Python2MinecraftApi.hits) {
				if (out.length() > 0) {
					out += "|";
				}
				out += e.getDescription();
			}
			Python2MinecraftApi.hits.clear();
		}

		return out;
	}

	protected static String getRest(Scanner scan) {
		StringBuilder out = new StringBuilder();

		while (scan.hasNext()) {
			if (out.length() > 0) {
				out.append(",");
			}
			out.append(scan.next());
		}
		return out.toString();
	}

	protected static Entity getServerEntityByID(int id) {
		if (!Python2MinecraftApi.useClientMethods) {
			for (World w : Python2MinecraftApi.serverWorlds) {
				Entity e = w.getEntityByID(id);
				if (e != null) {
					return e;
				}
			}
			Python2MinecraftApi.fail("Cannot find entity " + id);
			return null;
		} else {
			Entity e = Python2MinecraftApi.mc.world.getEntityByID(id);
			if (e == null) {
				Python2MinecraftApi.fail("Cannot find entity " + id);
			}
			return e;
		}
	}

	public static PrintWriter getWriter() {
		return Python2MinecraftApi.writer;
	}

	public static void globalMessage(String message) {
		for (World w : FMLCommonHandler.instance().getMinecraftServerInstance().worlds) {
			for (EntityPlayer p : w.playerEntities) {
				p.sendMessage(new TextComponentString(message));
			}
		}
	}

	protected static boolean holdingSword(EntityPlayer player) {
		ItemStack item = player.getHeldItemMainhand();
		if (item != null) {
			return item.getItem() instanceof ItemSword;
		}
		return false;
	}

	public static void init() {
		APIRegistry.registerCommand(Python2MinecraftApi.AUTHENTICATE,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.sendLine("handshake");
				});
		APIRegistry.registerCommand(Python2MinecraftApi.CLOSESOCKET,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					// dont post socket closing messages with no player id attached
					if (scan.hasNextInt()) {
						EntityPlayerMP player = (EntityPlayerMP) Python2MinecraftApi
								.getServerEntityByID(scan.nextInt());
						MinecraftForge.EVENT_BUS.post(new SocketEvent.Close(player));
						Python2MinecraftApi.sendLine("Closing Socket for Player: " + player.getName());
					}
				});
		APIRegistry.registerCommand(Python2MinecraftApi.SETBLOCK,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Location pos = Python2MinecraftApi.getBlockLocation(scan);
					short id = scan.nextShort();
					short meta = scan.hasNextShort() ? scan.nextShort() : 0;
					String tagString = Python2MinecraftApi.getRest(scan);

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
				});
		APIRegistry.registerCommand(Python2MinecraftApi.GETBLOCK,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Location pos = Python2MinecraftApi.getBlockLocation(scan);
					int id = eventHandler.getBlockId(pos);

					Python2MinecraftApi.sendLine(id);
				});
		APIRegistry.registerCommand(Python2MinecraftApi.GETBLOCKWITHDATA,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					if (Python2MinecraftApi.includeNBTWithData) {
						Python2MinecraftApi
								.sendLine(eventHandler.describeBlockState(Python2MinecraftApi.getBlockLocation(scan)));
					} else {
						Location loc = Python2MinecraftApi.getBlockLocation(scan);
						Python2MinecraftApi
								.sendLine("" + eventHandler.getBlockId(loc) + "," + eventHandler.getBlockMeta(loc));
					}
				});
		APIRegistry.registerCommand(Python2MinecraftApi.GETBLOCKS,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Location pos1 = Python2MinecraftApi.getBlockLocation(scan);
					Location pos2 = Python2MinecraftApi.getBlockLocation(scan);
					StringBuilder out = new StringBuilder();
					int x1 = Math.min(pos1.getX(), pos2.getX());
					int x2 = Math.max(pos1.getX(), pos2.getX());
					int y1 = Math.min(pos1.getY(), pos2.getY());
					int y2 = Math.max(pos1.getY(), pos2.getY());
					int z1 = Math.min(pos1.getZ(), pos2.getZ());
					int z2 = Math.max(pos1.getZ(), pos2.getZ());
					for (int y = y1; y <= y2; y++) {
						for (int x = x1; x <= x2; x++) {
							for (int z = z1; z <= z2; z++) {
								if (out.length() != 0) {
									out.append(",");
								}
								out.append(eventHandler.getBlockId(new Location(pos1.getWorld(), x, y, z)));
							}
						}
					}
					Python2MinecraftApi.sendLine(out.toString());
				});
		APIRegistry.registerCommand(Python2MinecraftApi.GETBLOCKSWITHDATA,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Location pos1 = Python2MinecraftApi.getBlockLocation(scan);
					Location pos2 = Python2MinecraftApi.getBlockLocation(scan);
					StringBuilder out = new StringBuilder();
					int x1 = Math.min(pos1.getX(), pos2.getX());
					int x2 = Math.max(pos1.getX(), pos2.getX());
					int y1 = Math.min(pos1.getY(), pos2.getY());
					int y2 = Math.max(pos1.getY(), pos2.getY());
					int z1 = Math.min(pos1.getZ(), pos2.getZ());
					int z2 = Math.max(pos1.getZ(), pos2.getZ());
					for (int y = y1; y <= y2; y++) {
						for (int x = x1; x <= x2; x++) {
							for (int z = z1; z <= z2; z++) {
								if (out.length() != 0) {
									out.append("|");
								}
								Location pos = new Location(pos1.getWorld(), x, y, z);
								if (Python2MinecraftApi.includeNBTWithData) {
									out.append(eventHandler.describeBlockState(pos).replace("&", "&amp;").replace("|",
											"&#124;"));
								} else {
									out.append(
											"" + eventHandler.getBlockId(pos) + "," + eventHandler.getBlockMeta(pos));
								}
							}
						}
					}
					Python2MinecraftApi.sendLine(out.toString());
				});
		APIRegistry.registerCommand(Python2MinecraftApi.GETHEIGHT,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					int x0 = scan.nextInt();
					int z0 = scan.nextInt();
					Location pos = Location.decodeLocation(Python2MinecraftApi.serverWorlds, x0, 0, z0);
					Chunk chunk = Python2MinecraftApi.serverWorlds[0].getChunkFromBlockCoords(pos);
					int h = chunk.getHeight(pos);
					int x = pos.getX();
					int z = pos.getZ();
					for (int y = Python2MinecraftApi.serverWorlds[0].getHeight(); y >= h; y--) {
						Block b = chunk.getBlockState(x, y, z).getBlock();
						if (b != Blocks.AIR) {
							h = y;
							break;
						}
					}

					h -= Python2MinecraftApi.serverWorlds[0].getSpawnPoint().getY();

					Python2MinecraftApi.sendLine(h);
				});
		APIRegistry.registerCommand(Python2MinecraftApi.GETLIGHTLEVEL,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Block block = Block.getBlockById(scan.nextInt());
					Python2MinecraftApi.sendLine(block.getLightValue(block.getBlockState().getBaseState()) / 15.);
				});
		APIRegistry.registerCommand(Python2MinecraftApi.SETLIGHTLEVEL,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					int id = scan.nextInt();
					float value = scan.nextFloat();
					Block.getBlockById(id).setLightLevel(value);
				});
		APIRegistry.registerCommand(Python2MinecraftApi.SETBLOCKS,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Location pos1 = Python2MinecraftApi.getBlockLocation(scan);
					Location pos2 = Python2MinecraftApi.getBlockLocation(scan);

					short id = scan.nextShort();
					short meta = scan.hasNextShort() ? scan.nextShort() : 0;

					String tagString = Python2MinecraftApi.getRest(scan);

					SetBlocksState setState;

					if (tagString.contains("{")) {
						try {
							setState = new SetBlocksNBT(pos1, pos2, id, meta, JsonToNBT.getTagFromJson(tagString));
						} catch (NBTException e) {
							setState = new SetBlocksState(pos1, pos2, id, meta);
						}
					} else {
						setState = new SetBlocksState(pos1, pos2, id, meta);
					}

					eventHandler.queueServerAction(setState);
				});
		APIRegistry.registerCommand(Python2MinecraftApi.CHAT,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.chat(args);
				});
		APIRegistry.registerCommand(Python2MinecraftApi.WORLDGETPLAYERIDS,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					List<Integer> players = new ArrayList<>();
					for (World w : Python2MinecraftApi.serverWorlds) {
						for (EntityPlayer p : w.playerEntities) {
							players.add(p.getEntityId());
						}
					}
					Collections.sort(players);

					String ids = "";
					for (Integer id : players) {
						if (ids.length() > 0) {
							ids += "|";
						}
						ids += id;
					}
					Python2MinecraftApi.sendLine(ids);
				});
		APIRegistry.registerCommand(Python2MinecraftApi.WORLDGETPLAYERID,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					if (scan.hasNext()) {
						String name = scan.next();
						for (World w : Python2MinecraftApi.serverWorlds) {
							for (EntityPlayer p : w.playerEntities) {
								if (p.getName().equals(name)) {
									Python2MinecraftApi.sendLine(p.getEntityId());
									return;
								}
							}
						}
						Python2MinecraftApi.fail("Unknown player");
					} else {
						Python2MinecraftApi.fail("Requires Player Name");
					}
				});
		APIRegistry.registerCommand(Python2MinecraftApi.WORLDDELETEENTITY,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.removeEntity(scan.nextInt());
				});
		APIRegistry.registerCommand(Python2MinecraftApi.WORLDSPAWNENTITY,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.spawnEntity(scan);
				});
		APIRegistry.registerCommand(Python2MinecraftApi.WORLDSPAWNPARTICLE,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.spawnParticle(scan);
				});
		APIRegistry.registerCommand(Python2MinecraftApi.EVENTSCLEAR,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.clearAllEvents();
				});
		APIRegistry.registerCommand(Python2MinecraftApi.EVENTSBLOCKHITS,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.sendLine(Python2MinecraftApi.getHitsAndClear());
				});
		APIRegistry.registerCommand(Python2MinecraftApi.EVENTSCHATPOSTS,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.sendLine(Python2MinecraftApi.getChatsAndClear());
				});
		APIRegistry.registerCommand(Python2MinecraftApi.WORLDSETTING,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					String setting = scan.next();
					if (setting.equals("world_immutable")) {
						eventHandler.setStopChanges(scan.nextInt() != 0);
					} else if (setting.equals("include_nbt_with_data")) {
						Python2MinecraftApi.includeNBTWithData = (scan.nextInt() != 0);
					} else if (setting.equals("pause_drawing")) {
						eventHandler.setPause(scan.nextInt() != 0);
						// name_tags not supported
					}
				});
		APIRegistry.registerCommand(Python2MinecraftApi.EVENTSSETTING,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					String setting = scan.next();
					if (setting.equals("restrict_to_sword")) {
						Python2MinecraftApi.restrictToSword = (scan.nextInt() != 0);
					} else if (setting.equals("detect_left_click")) {
						Python2MinecraftApi.detectLeftClick = (scan.nextInt() != 0);
					}
				});

		// player
		APIRegistry.registerCommand("player." + Python2MinecraftApi.GETPOS,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entityGetPos(scan.nextInt());
				});
		APIRegistry.registerCommand("player." + Python2MinecraftApi.GETTILE,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entityGetTile(scan.nextInt());
				});
		APIRegistry.registerCommand("player." + Python2MinecraftApi.GETROTATION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entityGetRotation(scan.nextInt());
				});
		APIRegistry.registerCommand("player." + Python2MinecraftApi.SETROTATION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entitySetRotation(scan.nextInt(), scan.nextFloat());
				});
		APIRegistry.registerCommand("player." + Python2MinecraftApi.GETPITCH,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entityGetPitch(scan.nextInt());
				});
		APIRegistry.registerCommand("player." + Python2MinecraftApi.SETPITCH,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entitySetPitch(scan.nextInt(), scan.nextFloat());
				});
		APIRegistry.registerCommand("player." + Python2MinecraftApi.GETDIRECTION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entityGetDirection(scan.nextInt());
				});
		APIRegistry.registerCommand("player." + Python2MinecraftApi.SETDIRECTION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entitySetDirection(scan.nextInt(), scan);
				});
		APIRegistry.registerCommand("player." + Python2MinecraftApi.SETTILE,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entitySetTile(scan.nextInt(), scan);
				});
		APIRegistry.registerCommand("player." + Python2MinecraftApi.SETPOS,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entitySetPos(scan.nextInt(), scan);
				});
		APIRegistry.registerCommand("player." + Python2MinecraftApi.SETDIMENSION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entitySetDimension(scan.nextInt(), scan.nextInt(), eventHandler);
				});
		APIRegistry.registerCommand("player." + Python2MinecraftApi.GETNAME,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entityGetNameAndUUID(scan.nextInt());
				});

		// entity
		APIRegistry.registerCommand("entity." + Python2MinecraftApi.GETPOS,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entityGetPos(scan.nextInt());
				});
		APIRegistry.registerCommand("entity." + Python2MinecraftApi.GETTILE,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entityGetTile(scan.nextInt());
				});
		APIRegistry.registerCommand("entity." + Python2MinecraftApi.GETROTATION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entityGetRotation(scan.nextInt());
				});
		APIRegistry.registerCommand("entity." + Python2MinecraftApi.SETROTATION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entitySetRotation(scan.nextInt(), scan.nextFloat());
				});
		APIRegistry.registerCommand("entity." + Python2MinecraftApi.GETPITCH,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entityGetPitch(scan.nextInt());
				});
		APIRegistry.registerCommand("entity." + Python2MinecraftApi.SETPITCH,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entitySetPitch(scan.nextInt(), scan.nextFloat());
				});
		APIRegistry.registerCommand("entity." + Python2MinecraftApi.GETDIRECTION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entityGetDirection(scan.nextInt());
				});
		APIRegistry.registerCommand("entity." + Python2MinecraftApi.SETDIRECTION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entitySetDirection(scan.nextInt(), scan);
				});
		APIRegistry.registerCommand("entity." + Python2MinecraftApi.SETTILE,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entitySetTile(scan.nextInt(), scan);
				});
		APIRegistry.registerCommand("entity." + Python2MinecraftApi.SETPOS,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entitySetPos(scan.nextInt(), scan);
				});
		APIRegistry.registerCommand("entity." + Python2MinecraftApi.SETDIMENSION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entitySetDimension(scan.nextInt(), scan.nextInt(), eventHandler);
				});
		APIRegistry.registerCommand("entity." + Python2MinecraftApi.GETNAME,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Python2MinecraftApi.entityGetNameAndUUID(scan.nextInt());
				});
		APIRegistry.registerCommand("camera." + Python2MinecraftApi.GETENTITYID,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					int playerid = scan.nextInt();
					EntityPlayerMP player = (EntityPlayerMP) Python2MinecraftApi.getServerEntityByID(playerid);
					Python2MinecraftApi.sendLine(player.getSpectatingEntity().getEntityId());
				});
		APIRegistry.registerCommand("camera." + Python2MinecraftApi.SETFOLLOW,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					if (!RaspberryJamMod.integrated) {
						return;
					}

					Python2MinecraftApi.mc.gameSettings.debugCamEnable = false;

					int playerid = scan.nextInt();
					EntityPlayerMP player = (EntityPlayerMP) Python2MinecraftApi.getServerEntityByID(playerid);

					if (player != null) {
						if (!scan.hasNext()) {
							player.setSpectatingEntity(null);
						} else {
							Entity entity = Python2MinecraftApi.getServerEntityByID(scan.nextInt());
							if (entity != null) {
								player.setSpectatingEntity(entity);
							}
						}
					}
					Python2MinecraftApi.mc.gameSettings.thirdPersonView = 1;
					Python2MinecraftApi.mc.entityRenderer.loadEntityShader((Entity) null);
				});
		APIRegistry.registerCommand("camera." + Python2MinecraftApi.SETNORMAL,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					if (!RaspberryJamMod.integrated) {
						return;
					}

					Python2MinecraftApi.mc.gameSettings.debugCamEnable = false;

					int playerid = scan.nextInt();
					EntityPlayerMP player = (EntityPlayerMP) Python2MinecraftApi.getServerEntityByID(playerid);

					if (player != null) {
						if (!scan.hasNext()) {
							player.setSpectatingEntity(null);
						} else {
							Entity entity = Python2MinecraftApi.getServerEntityByID(scan.nextInt());
							if (entity != null) {
								player.setSpectatingEntity(entity);
							}
						}
					}
					Python2MinecraftApi.mc.gameSettings.thirdPersonView = 0;
					Python2MinecraftApi.mc.entityRenderer
							.loadEntityShader(Python2MinecraftApi.mc.getRenderViewEntity());
				});
		APIRegistry.registerCommand("camera." + Python2MinecraftApi.SETDEBUG,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					if (!RaspberryJamMod.integrated) {
						return;
					}

					Python2MinecraftApi.mc.gameSettings.debugCamEnable = true;
				});
		APIRegistry.registerCommand("camera." + Python2MinecraftApi.SETDISTANCE,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					Float d = scan.nextFloat();
					Class c = net.minecraft.client.renderer.EntityRenderer.class;
					try {
						Field f = c.getDeclaredField("thirdPersonDistance");
						f.setAccessible(true);
						f.set(Python2MinecraftApi.mc.entityRenderer, d);
					} catch (Exception e) {
						RaspberryJamMod.logger.error("" + e);
					}
					try {
						Field f = c.getDeclaredField("thirdPersonDistanceTemp");
						f.setAccessible(true);
						f.set(Python2MinecraftApi.mc.entityRenderer, d);
					} catch (Exception e) {
						RaspberryJamMod.logger.error("" + e);
					}
				});
	}

	public static void onClick(LeftClickBlock event, MCEventHandler eventHandler) {
		if (Python2MinecraftApi.detectLeftClick) {
			if (!Python2MinecraftApi.restrictToSword || Python2MinecraftApi.holdingSword(event.getEntityPlayer())) {
				synchronized (Python2MinecraftApi.hits) {
					if (Python2MinecraftApi.hits.size() >= Python2MinecraftApi.MAX_HITS) {
						Python2MinecraftApi.hits.remove(0);
					}
					Python2MinecraftApi.hits.add(new HitDescription(eventHandler.getWorlds(), event));
				}
			}
		}
		if (eventHandler.stopChanges) {
			event.setCanceled(true);
		}
	}

	public static void onClick(RightClickBlock event, MCEventHandler eventHandler) {
		if (!Python2MinecraftApi.restrictToSword || Python2MinecraftApi.holdingSword(event.getEntityPlayer())) {
			synchronized (Python2MinecraftApi.hits) {
				if (Python2MinecraftApi.hits.size() >= Python2MinecraftApi.MAX_HITS) {
					Python2MinecraftApi.hits.remove(0);
				}
				Python2MinecraftApi.hits.add(new HitDescription(eventHandler.getWorlds(), event));
			}
		}
		if (eventHandler.stopChanges) {
			event.setCanceled(true);
		}
	}

	static boolean refresh() {
		if (!Python2MinecraftApi.useClientMethods) {
			Python2MinecraftApi.serverWorlds = FMLCommonHandler.instance().getMinecraftServerInstance().worlds;

			if (Python2MinecraftApi.serverWorlds == null) {
				Python2MinecraftApi.fail("Worlds not available");
				return false;
			}
			return true;
		} else {
			if (!RaspberryJamMod.integrated) {
				Python2MinecraftApi.fail("This requires the client");
				return false;
			}

			Python2MinecraftApi.mc = Minecraft.getMinecraft();
			if (Python2MinecraftApi.mc == null) {
				Python2MinecraftApi.fail("Minecraft client not yet available");
				return false;
			}

			Python2MinecraftApi.serverWorlds = new World[] { Python2MinecraftApi.mc.world };

			if (Python2MinecraftApi.mc.player == null) {
				Python2MinecraftApi.fail("Client player not available");
				return false;
			}

			return true;
		}
	}

	protected static void removeEntity(int id) {
		Entity e = Python2MinecraftApi.getServerEntityByID(id);
		if (e != null) {
			e.getEntityWorld().removeEntity(e);
		}
	}

	protected static BlockPos rotateVectorAngle(BlockPos pos, float angle) {
		angle = MathHelper.wrapDegrees(angle);
		Vec3d rotated = new Vec3d(pos.getX(), pos.getY(), pos.getZ()).rotateYaw((float) Math.toRadians(angle));
		return new BlockPos(Math.round(rotated.x), Math.round(rotated.y), Math.round(rotated.z));
	}

	protected static BlockPos rotateVectorRadian(BlockPos pos, float radian) {
		Vec3d rotated = new Vec3d(pos.getX(), pos.getY(), pos.getZ()).rotateYaw(radian);
		return new BlockPos(Math.round(rotated.x), Math.round(rotated.y), Math.round(rotated.z));
	}

	protected static void sendLine(BlockPos pos) {
		Python2MinecraftApi.sendLine("" + pos.getX() + "," + pos.getY() + "," + pos.getZ());
	}

	protected static void sendLine(double x) {
		Python2MinecraftApi.sendLine(Double.toString(x));
	}

	protected static void sendLine(int x) {
		Python2MinecraftApi.sendLine(Integer.toString(x));
	}

	protected static void sendLine(String string) {
		try {
			Python2MinecraftApi.getWriter().print(string + "\n");
			Python2MinecraftApi.getWriter().flush();
		} catch (Exception e) {
		}
	}

	protected static void sendLine(Vec3d pos) {
		Python2MinecraftApi.sendLine("" + pos.x + "," + pos.y + "," + pos.z);
	}

	public static void setUseClientMethods(boolean state) {
		Python2MinecraftApi.useClientMethods = state;
	}

	public static void setWriter(PrintWriter p_writer) {
		Python2MinecraftApi.writer = p_writer;
	}

	protected static void spawnEntity(Scanner scan) {
		String entityId = scan.next();
		double x0 = scan.nextDouble();
		double y0 = scan.nextDouble();
		double z0 = scan.nextDouble();
		Vec3w pos = Location.decodeVec3w(Python2MinecraftApi.serverWorlds, x0, y0, z0);
		String tagString = Python2MinecraftApi.getRest(scan);
		Entity entity;
		if (tagString.length() > 0) {
			NBTTagCompound tags;
			try {
				tags = JsonToNBT.getTagFromJson(tagString);
			} catch (NBTException e) {
				Python2MinecraftApi.fail("Cannot parse tags");
				return;
			}
			tags.setString("id", entityId);
			entity = EntityList.createEntityFromNBT(tags, pos.getWorld());
		} else {
			entity = EntityList.createEntityByIDFromName(new ResourceLocation(entityId), pos.getWorld());
		}

		if (entity == null) {
			Python2MinecraftApi.fail("Cannot create entity");
		} else {
			entity.setPositionAndUpdate(pos.x, pos.y, pos.z);
			pos.getWorld().spawnEntity(entity);
			Python2MinecraftApi.sendLine(entity.getEntityId());
		}
	}

	protected static void spawnParticle(Scanner scan) {
		if (!Python2MinecraftApi.useClientMethods) {
			String particleName = scan.next();
			double x0 = scan.nextDouble();
			double y0 = scan.nextDouble();
			double z0 = scan.nextDouble();
			Vec3w pos = Location.decodeVec3w(Python2MinecraftApi.serverWorlds, x0, y0, z0);
			double dx = scan.nextDouble();
			double dy = scan.nextDouble();
			double dz = scan.nextDouble();
			double speed = scan.nextDouble();
			int count = scan.nextInt();

			int[] extras = null;
			EnumParticleTypes particle = null;
			for (EnumParticleTypes e : EnumParticleTypes.values()) {
				if (e.getParticleName().equals(particleName)) {
					particle = e;
					extras = new int[e.getArgumentCount()];
					try {
						for (int i = 0; i < extras.length; i++) {
							extras[i] = scan.nextInt();
						}
					} catch (Exception exc) {
					}
					break;
				}
			}
			if (particle == null) {
				Python2MinecraftApi.fail("Cannot find particle type");
			} else {
				((WorldServer) pos.getWorld()).spawnParticle(particle, false, pos.x, pos.y, pos.z, count, dx, dy, dz,
						speed, extras);
			}
		} else {
			String particleName = scan.next();
			double x0 = scan.nextDouble();
			double y0 = scan.nextDouble();
			double z0 = scan.nextDouble();
			Vec3w pos = Location.decodeVec3w(Python2MinecraftApi.serverWorlds, x0, y0, z0);
			double dx = scan.nextDouble();
			double dy = scan.nextDouble();
			double dz = scan.nextDouble();
			scan.nextDouble();
			int count = scan.nextInt();

			int[] extras = null;
			EnumParticleTypes particle = null;
			for (EnumParticleTypes e : EnumParticleTypes.values()) {
				if (e.getParticleName().equals(particleName)) {
					particle = e;
					extras = new int[e.getArgumentCount()];
					try {
						for (int i = 0; i < extras.length; i++) {
							extras[i] = scan.nextInt();
						}
					} catch (Exception exc) {
					}
					break;
				}
			}
			if (particle == null) {
				Python2MinecraftApi.fail("Cannot find particle type");
			} else {
				for (int i = 0; i < count; i++) {
					pos.getWorld().spawnParticle(particle, false, pos.x, pos.y, pos.z, dx, dy, dz, extras);
				}
			}
		}
	}

	public static int trunc(double x) {
		return (int) Math.floor(x);
	}

	protected static void unknownCommand() {
		Python2MinecraftApi.fail("unknown command");
	}

	public void clearChats() {
		synchronized (Python2MinecraftApi.chats) {
			Python2MinecraftApi.chats.clear();
		}
	}

	public void clearHits() {
		synchronized (Python2MinecraftApi.hits) {
			Python2MinecraftApi.hits.clear();
		}
	}

	public int eventCount() {
		synchronized (Python2MinecraftApi.hits) {
			return Python2MinecraftApi.hits.size();
		}
	}
}
