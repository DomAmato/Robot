package com.dyn.robot.api;

import java.io.PrintWriter;
import java.util.Scanner;

import com.dyn.robot.RobotMod;
import com.dyn.robot.network.SocketEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class Python2MinecraftApi {

	protected static final String AUTHENTICATE = "mcpi.auth";
	protected static final String CLOSESOCKET = "mcpi.close";

	protected static final float TOO_SMALL = (float) 1e-9;

	protected static World[] serverWorlds;

	protected static Minecraft mc;

	protected static PrintWriter writer = null;

	protected static boolean includeNBTWithData = false;

	protected static void chat(String msg) {
		if (!RobotMod.integrated || (RobotMod.globalChatMessages)) {
			Python2MinecraftApi.globalMessage(msg);
		} else {
			Python2MinecraftApi.mc.player.sendMessage(new TextComponentString(msg));
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

	protected static void fail(String error) {
		RobotMod.logger.info("Code Error: " + error);
		Python2MinecraftApi.sendLine("FAIL|" + error);
	}

	protected static BlockPos getBlockPos(Scanner scan) {
		int x = scan.nextInt();
		int y = scan.nextInt();
		int z = scan.nextInt();
		return new BlockPos(x, y, z);
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
		for (World w : Python2MinecraftApi.serverWorlds) {
			Entity e = w.getEntityByID(id);
			if (e != null) {
				return e;
			}
		}
		Python2MinecraftApi.fail("Cannot find entity " + id);
		return null;
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

	public static void init() {
		APIRegistry.registerCommand(Python2MinecraftApi.AUTHENTICATE, (String args, Scanner scan) -> {
			Python2MinecraftApi.sendLine("handshake");
		});
		APIRegistry.registerCommand(Python2MinecraftApi.CLOSESOCKET, (String args, Scanner scan) -> {
			// dont post socket closing messages with no player id
			// attached
			if (scan.hasNextInt()) {
				EntityPlayerMP player = (EntityPlayerMP) Python2MinecraftApi.getServerEntityByID(scan.nextInt());
				MinecraftForge.EVENT_BUS.post(new SocketEvent.Close(player));
				Python2MinecraftApi.sendLine("Closing Socket for Player: " + player.getName());
			}
		});
	}

	static boolean refresh() {
		Python2MinecraftApi.serverWorlds = FMLCommonHandler.instance().getMinecraftServerInstance().worlds;

		if (Python2MinecraftApi.serverWorlds == null) {
			Python2MinecraftApi.fail("Worlds not available");
			return false;
		}
		return true;
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

	public static void setWriter(PrintWriter p_writer) {
		Python2MinecraftApi.writer = p_writer;
	}

	public static int trunc(double x) {
		return (int) Math.floor(x);
	}

	protected static void unknownCommand() {
		Python2MinecraftApi.fail("unknown command");
	}
}
