package com.dyn.rjm.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.dyn.rjm.events.ClientEventHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class NightVisionExternalCommand implements ICommand {
	private ClientEventHandler eventHandler;

	public NightVisionExternalCommand(ClientEventHandler eventHandler2) {
		eventHandler = eventHandler2;
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
		boolean nv;

		if (args.length == 0) {
			nv = !eventHandler.getNightVision();
		} else if (args[0].toLowerCase().equals("on")) {
			nv = true;
		} else if (args[0].toLowerCase().equals("off")) {
			nv = false;
		} else {
			throw new CommandException("Usage: /nightvision [on|off]");
		}

		eventHandler.setNightVision(nv);
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		if (player != null) {
			if (nv) {
				player.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("night_vision"), 4096));
				player.sendMessage(new TextComponentString("Enabled night vision"));
			} else {
				player.removePotionEffect(Potion.getPotionFromResourceLocation("night_vision"));
				player.sendMessage(new TextComponentString("Disabled night vision"));
			}
		}
	}

	@Override
	public List<String> getAliases() {
		List<String> aliases = new ArrayList<>();
		aliases.add(getName());
		aliases.add("nv");
		return aliases;
	}

	@Override
	public String getName() {
		return "nightvision";
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			@Nullable BlockPos targetPos) {

		if (args.length == 1) {
			List<String> options = new ArrayList<>();
			options.add("off");
			options.add("on");
			return options;
		}
		return null;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "nightvision [on|off]";
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}
}
