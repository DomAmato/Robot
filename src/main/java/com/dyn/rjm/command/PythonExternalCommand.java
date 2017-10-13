package com.dyn.rjm.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class PythonExternalCommand extends ScriptExternalCommand {

	public PythonExternalCommand(boolean clientSide) {
		super(clientSide);
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
	public List<String> getAliases() {
		List<String> aliases = new ArrayList<>();
		aliases.add(getName());
		aliases.add("py");
		return aliases;
	}

	@Override
	protected String getExtension() {
		return ".py";
	}

	@Override
	public String getName() {
		return "python";
	}

	@Override
	protected String[] getScriptPaths() {
		return new String[] { "mcpy/" };
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "python <script> [arguments]: run script, stopping old one(s) (omit script to stop previous script)";
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}
}
