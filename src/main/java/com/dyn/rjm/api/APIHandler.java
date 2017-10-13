package com.dyn.rjm.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import com.dyn.rjm.RaspberryJamMod;
import com.dyn.rjm.api.Python2MinecraftApi.ChatDescription;
import com.dyn.rjm.events.MCEventHandler;
import com.dyn.rjm.network.CodeEvent;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
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

	protected MCEventHandler eventHandler;

	public APIHandler(MCEventHandler eventHandler, PrintWriter writer) throws IOException {
		this.eventHandler = eventHandler;
		Python2MinecraftApi.setWriter(writer);
		eventHandler.registerAPIHandler(this);
	}

	public void addChatDescription(ChatDescription cd) {
		Python2MinecraftApi.addChatDescription(cd);
	}

	public void close() {
		eventHandler.unregisterAPIHandler(this);
	}

	protected void fail(String string) {
		Python2MinecraftApi.sendLine("Fail");
	}

	public PrintWriter getWriter() {
		return Python2MinecraftApi.getWriter();
	}

	public void onClick(LeftClickBlock event) {
		Python2MinecraftApi.onClick(event, eventHandler);
	}

	public void onClick(RightClickBlock event) {
		Python2MinecraftApi.onClick(event, eventHandler);
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

			synchronized (eventHandler) {
				if (!APIRegistry.runCommand(cmd, args, scan, eventHandler)) {
					unknownCommand(cmd);
				}
			}

			scan.close();
			scan = null;
		} catch (Exception e) {
			RaspberryJamMod.logger.error("Error during processing" + e);
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
