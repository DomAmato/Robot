package com.dyn.robot.api;

import java.io.PrintWriter;
import java.util.Scanner;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.network.SocketEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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
		RobotMod.logger.debug("Code Error: " + error);
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

	public static void init() {
		APIRegistry.registerCommand(Python2MinecraftApi.AUTHENTICATE, (String args, Scanner scan) -> {
			Python2MinecraftApi.sendLine("handshake");
		});
		APIRegistry.registerCommand(Python2MinecraftApi.CLOSESOCKET, (String args, Scanner scan) -> {
			// dont post socket closing messages with no player id
			// attached
			if (scan.hasNextInt()) {
				Entity entity = Python2MinecraftApi.getServerEntityByID(scan.nextInt());
				if (entity instanceof EntityPlayer) {
					MinecraftForge.EVENT_BUS.post(new SocketEvent.Close((EntityPlayer) entity));
					Python2MinecraftApi.sendLine("Closing Socket for Player: " + entity.getName());
				} else if (entity instanceof EntityRobot) {
					MinecraftForge.EVENT_BUS.post(new SocketEvent.CloseRobot((EntityRobot) entity));
					Python2MinecraftApi.sendLine("Closing Socket for Robot: " + ((EntityRobot) entity).getRobotName());
				}
			}
		});
	}

	public static boolean refresh() {
		Python2MinecraftApi.serverWorlds = FMLCommonHandler.instance().getMinecraftServerInstance().worlds;

		if (Python2MinecraftApi.serverWorlds == null) {
			Python2MinecraftApi.fail("Worlds not available");
			return false;
		}
		return true;
	}

	protected static Rotation rotationFromAngle(float angle) {
		angle = MathHelper.wrapDegrees(angle);
		switch ((int) angle) {
		case 90:
		case -270:
			return Rotation.CLOCKWISE_90;
		case 180:
		case -180:
			return Rotation.CLOCKWISE_180;
		case -90:
		case 270:
			return Rotation.COUNTERCLOCKWISE_90;
		default:
			return Rotation.NONE;
		}
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
