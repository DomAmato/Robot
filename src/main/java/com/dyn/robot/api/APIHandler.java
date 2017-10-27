package com.dyn.robot.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import com.dyn.robot.RobotMod;
import com.dyn.robot.network.CodeEvent;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class APIHandler {

	static protected String getRest(Scanner scan) {
		StringBuilder out = new StringBuilder();

		while (scan.hasNext()) {
			if (out.length() > 0) {
				out.append(",");
			}
			out.append(scan.next());
		}
		return out.toString();
	}

	public static void globalMessage(String message) {
		for (World w : FMLCommonHandler.instance().getMinecraftServerInstance().worlds) {
			for (EntityPlayer p : w.playerEntities) {
				p.sendMessage(new TextComponentString(message));
			}
		}
	}

	public APIHandler(PrintWriter writer) throws IOException {
		Python2MinecraftApi.setWriter(writer);
		RobotMod.registerAPIHandler(this);
	}

	public void close() {
		RobotMod.unregisterAPIHandler(this);
	}

	protected void fail(String string) {
		Python2MinecraftApi.sendLine("Fail");
	}

	public PrintWriter getWriter() {
		return Python2MinecraftApi.getWriter();
	}

	public void onFail(CodeEvent.FailEvent event) {
		fail(String.format("Robot with id %d could not complete command: %s", event.getId(), event.getCode()));
	}

	public void onSuccess(CodeEvent.RobotSuccessEvent event) {
		Python2MinecraftApi.sendLine(event.getEntityId() + "," + event.getCode());
	}

	public void process(String clientSentence) {
		if (!Python2MinecraftApi.refresh()) {
			return;
		}

		Scanner scan = null;

		try {
			int paren = clientSentence.indexOf('(');
			if (paren < 0) {
				return;
			}

			String cmd = clientSentence.substring(0, paren);
			String args = clientSentence.substring(paren + 1).replaceAll("[\\s\r\n]+$", "").replaceAll("\\)$", "");

			if (cmd.startsWith("player.")) {
				// Compatibility with the mcpi library included with Juice
				if (args.startsWith("None,")) {
					args = args.substring(5);
				} else if (args.equals("None")) {
					args = "";
				}
			}

			scan = new Scanner(args);
			scan.useDelimiter(",");

			if (!APIRegistry.runCommand(cmd, args, scan)) {
				unknownCommand(cmd);
			}

			scan.close();
			scan = null;
		} catch (Exception e) {
			RobotMod.logger.error("Error during processing" + e);
			e.printStackTrace();
		} finally {
			if (scan != null) {
				scan.close();
			}
		}
	}

	protected void unknownCommand(String command) {
		fail("Unknown Command: " + command);
	}

}
