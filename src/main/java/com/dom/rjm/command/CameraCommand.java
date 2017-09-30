package com.dom.rjm.command;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.dom.rjm.RaspberryJamMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class CameraCommand implements ICommand {
	static public void setField(Class c, String field, Object object, Object value) {
		try {
			Field f = c.getDeclaredField(field);
			f.setAccessible(true);
			f.set(object, value);
		} catch (Exception e) {
			RaspberryJamMod.logger.error("" + e);
		}
	}

	public CameraCommand() {
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		Minecraft mc = Minecraft.getMinecraft();

		if ((args.length >= 1) && args[0].equals("debug")) {
			if ((args.length == 1) || args[1].equals("toggle")) {
				mc.gameSettings.debugCamEnable = !mc.gameSettings.debugCamEnable;
			} else if (args.length == 2) {
				mc.gameSettings.debugCamEnable = args[1].equals("on") || args[1].equals("1");
			} else {
				usage(sender);
			}
		} else if ((args.length >= 2) && args[0].equals("distance")) {
			try {
				setThirdPersonDistance(Float.parseFloat(args[1]));
			} catch (NumberFormatException e) {
			}
		} else {
			usage(sender);
		}
	}

	@Override
	public List<String> getAliases() {
		List<String> aliases = new ArrayList<>();
		aliases.add(getName());
		return aliases;
	}

	@Override
	public String getName() {
		return "camera";
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			@Nullable BlockPos targetPos) {

		if (args.length == 1) {
			List<String> options = new ArrayList<>();
			if ("distance".startsWith(args[0])) {
				options.add("distance");
			}
			if ("debug".startsWith(args[0])) {
				options.add("debug");
			}
			return options;
		} else if ((args.length == 2) && args[0].equals("debug")) {
			List<String> options = new ArrayList<>();
			options.add("on");
			options.add("off");
			options.add("toggle");
			return options;
		}
		return null;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "camera debug [on|off]\ncamera distance length";
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}

	private void setThirdPersonDistance(float x) {
		Class c = net.minecraft.client.renderer.EntityRenderer.class;
		EntityRenderer r = Minecraft.getMinecraft().entityRenderer;
		CameraCommand.setField(c, "thirdPersonDistance", r, x);
		CameraCommand.setField(c, "thirdPersonDistanceTemp", r, x);
		CameraCommand.setField(c, "field_78490_B", r, x);
		CameraCommand.setField(c, "field_78491_C", r, x);
	}

	public void usage(ICommandSender sender) {
		Minecraft.getMinecraft().player.sendMessage(new TextComponentString(getUsage(sender)));
	}
}
