package com.dyn.rjm.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.dyn.rjm.RaspberryJamMod;
import com.dyn.rjm.api.APIHandler;
import com.dyn.rjm.util.PathUtility;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public abstract class ScriptExternalCommand implements ICommand {

	private List<Process> runningScripts;
	final String scriptProcessorPath;
	protected boolean clientSide;

	public ScriptExternalCommand(boolean clientSide) {
		this.clientSide = clientSide;
		runningScripts = new LinkedList<>();
		String path = PathUtility.getPythonExecutablePath();
		if (path.contains("/") || path.contains(System.getProperty("file.separator"))) {
			scriptProcessorPath = new File(path).getAbsolutePath().toString();
		} else {
			scriptProcessorPath = path;
		}
	}

	public boolean addMode() {
		return false;
	}

	// returns number of scripts closed
	public int close() {
		int closed = 0;

		for (Process runningScript : runningScripts) {
			try {
				runningScript.exitValue();
			} catch (IllegalThreadStateException e) {
				// script was still running
				closed++;
				runningScript.destroy();
			}
		}
		runningScripts.clear();

		return closed;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (clientSide != RaspberryJamMod.clientOnlyAPI) {
			return;
		}

		if (!clientSide) {
			World serverWorld = server.getEntityWorld();

			if (!RaspberryJamMod.allowRemote && (!RaspberryJamMod.integrated || ((serverWorld.playerEntities.size() > 1)
					&& !sender.getName().equals(Minecraft.getMinecraft().player.getName())))) {
				globalMessage("Blocked possible remote script launch by " + sender.getCommandSenderEntity());
				return;
			}
		}

		boolean addMode = addMode();

		if (!addMode) {
			int c = RaspberryJamMod.closeAllScripts();
			if (0 < c) {
				String message;
				if (1 < c) {
					message = "Stopped the " + c + " running scripts.";
				} else {
					message = "Stopped the running script.";
				}
				globalMessage(message);
			}
		}

		int arg = 0;

		if (args.length <= arg) {
			return;
		}

		if (!sandboxedScriptPath(args[arg])) {
			throw new CommandException("Unacceptable script name");
		}

		File script = getScript(args[arg]);
		if (script == null) {
			throw new CommandException("Cannot find script");
		}

		List<String> cmd = new ArrayList<>();
		cmd.add(scriptProcessorPath);
		cmd.add(script.getName());
		for (int i = 1; (arg + i) < args.length; i++) {
			cmd.add(args[arg + i]);
		}

		ProcessBuilder pb = new ProcessBuilder(cmd);
		// pb.redirectErrorStream(true);
		pb.directory(script.getParentFile());

		Map<String, String> environment = pb.environment();
		EntityPlayer player = null;
		Entity senderEntity = sender.getCommandSenderEntity();
		if (senderEntity instanceof EntityPlayer) {
			environment.put("MINECRAFT_PLAYER_NAME", senderEntity.getName());
			environment.put("MINECRAFT_PLAYER_ID", "" + senderEntity.getEntityId());
		}
		environment.put("MINECRAFT_API_PORT", "" + RaspberryJamMod.currentPortNumber);

		pb.command(cmd);
		try {
			RaspberryJamMod.logger.info("Running " + script);
			Process runningScript = pb.start();
			runningScripts.add(runningScript);
			gobble(runningScript.getInputStream(), player, "");
			gobble(runningScript.getErrorStream(), player, "[ERR] ");
		} catch (IOException e) {
			throw new CommandException("Error " + e);
		}
	}

	protected String extraPath() {
		return "";
	}

	@Override
	public List getAliases() {
		List<String> aliases = new ArrayList<>();
		aliases.add(getName());
		return aliases;
	}

	abstract protected String getExtension();

	protected File getScript(String base) {
		String[] paths = getScriptPaths();

		String ext = getExtension();

		String name = base;

		if (!name.endsWith(ext)) {
			name += getExtension();
		}

		for (String path : paths) {
			try {
				File f = new File(path + name);
				if (f.canRead()) {
					return f;
				}
			} catch (SecurityException e) {
			}
		}
		return null;
	}

	abstract protected String[] getScriptPaths();

	protected List<String> getScripts(String subdir) {
		List<String> scripts = new ArrayList<>();
		String ext = getExtension();

		for (String dir : getScriptPaths()) {
			File[] files = new File(dir + subdir).listFiles();
			if (files != null) {
				for (File f : files) {
					String name = f.getName();
					if (!name.startsWith(".") && f.canRead()) {
						if (name.endsWith(ext) && f.isFile()) {
							scripts.add(subdir + name.substring(0, name.length() - ext.length()));
						} else if (f.isDirectory()) {
							scripts.add(subdir + name + "/");
						}
					}
				}
			}
		}

		Collections.sort(scripts);
		for (int i = scripts.size() - 1; i > 0; i--) {
			if (scripts.get(i).equals(scripts.get(i - 1))) {
				scripts.remove(i);
			}
		}

		return scripts;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			@Nullable BlockPos targetPos) {
		if (args.length == 0) {
			return null;
		}

		if (args.length != 1) {
			return null;
		}

		int arg = 0;

		if (!sandboxedScriptPath(args[arg])) {
			return null;
		}

		int lastSlash = args[arg].lastIndexOf('/');
		String subdir = "";
		if (lastSlash != -1) {
			subdir = args[0].substring(arg, lastSlash + 1);
		}
		List<String> scripts = getScripts(subdir);
		for (int i = scripts.size() - 1; i >= 0; i--) {
			if (!scripts.get(i).toLowerCase().startsWith(args[arg].toLowerCase())) {
				scripts.remove(i);
			}
		}
		return scripts;
	}

	private void globalMessage(String msg) {
		if (clientSide) {
			Minecraft.getMinecraft().player.sendMessage(new TextComponentString(msg));
		} else {
			APIHandler.globalMessage(msg);
		}
	}

	private void gobble(final InputStream stream, final EntityPlayer entity, final String label) {
		Thread t = new Thread() {

			@Override
			public void run() {
				BufferedReader br;

				br = new BufferedReader(new InputStreamReader(stream));

				String line;
				try {
					while (null != (line = br.readLine())) {
						line.trim();
						if (entity == null) {
							globalMessage(label + line);
						} else {
							entity.sendMessage(new TextComponentString(label + line));
						}
					}
				} catch (IOException e) {
				}

				try {
					br.close();
				} catch (IOException e) {
				}
			}
		};
		t.setDaemon(true);
		t.start();
	}

	private boolean sandboxedScriptPath(String path) {
		// only allow scripts from the selected directories, and don't allow
		// leaving them
		// by using .., and also don't allow passing options to the script
		// processor.
		return !path.startsWith(".") && !path.contains("/.")
				&& !(PathUtility.isWindows() && (path.contains(":") || path.contains("\\.")));
	}
}
