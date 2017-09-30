package com.dom.rjm.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommandSender;

public class AddPythonExternalCommand extends PythonExternalCommand {

	public AddPythonExternalCommand(boolean clientSide) {
		super(clientSide);
	}

	@Override
	public boolean addMode() {
		return true;
	}

	@Override
	public List getAliases() {
		List<String> aliases = new ArrayList<>();
		aliases.add(getName());
		aliases.add("apy");
		return aliases;
	}

	@Override
	public String getName() {
		return "addpython";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "addpython script arguments: run a new script without stopping old one(s)";
	}
}
