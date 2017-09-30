package com.dom.rjm.api;

import java.util.Map;
import java.util.Scanner;

import com.dom.rjm.RaspberryJamMod;
import com.dom.rjm.events.MCEventHandler;
import com.google.common.collect.Maps;

public class APIRegistry {

	@FunctionalInterface
	public static interface CommandRunnable {
		void execute(String args, Scanner scan, MCEventHandler eventHandler);
	}

	public static boolean initd = false;

	public static Map<String, CommandRunnable> commands = Maps.newHashMap();

	public static CommandRunnable getExectuableCode(String name) {
		return APIRegistry.commands.get(name);
	}

	public static void registerCommand(String name, CommandRunnable executableCode) {
		try {
			APIRegistry.commands.put(name, executableCode);
			RaspberryJamMod.logger.info("Registering Command: " + name);
		} catch (Exception e) {
			RaspberryJamMod.logger.error("Command already registered");
		}
	}

	public static boolean runCommand(String name, String args, Scanner scan, MCEventHandler eventHandler) {
		if (!APIRegistry.commands.containsKey(name)) {
			return false;
		}
		try {
			RaspberryJamMod.logger.info("Running Cmd: " + name + ", " + args);
			APIRegistry.commands.get(name).execute(args, scan, eventHandler);
		} catch (Exception e) {
			RaspberryJamMod.logger.error("Failed Executing Command: " + name, e);
		}
		return true;
	}

	public static void setExectuableCode(String name, CommandRunnable code) {
		APIRegistry.commands.replace(name, code);
	}

}
