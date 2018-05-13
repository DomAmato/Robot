package com.dyn.robot.utils;

import java.io.File;

import com.dyn.robot.RobotMod;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class PathUtility {
	private static String extraPath() {
		if (PathUtility.isWindows()) {
			return "\\python36\\;python36\\";
		} else {
			return "";
		}
	}

	public static String getPythonExecutablePath() {
		String base = RobotMod.pythonInterpreter;

		String pathSep = System.getProperty("path.separator");
		String fileSep = System.getProperty("file.separator");
		if (base.contains("/") || base.contains(fileSep)) {
			return base;
		}

		String pathVar = "";

		if (RobotMod.useSystemPath) {
			pathVar = System.getenv("PATH");
		} else {
			if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
				File localpath = Minecraft.getMinecraft().mcDataDir;
				pathVar = new File(localpath, RobotMod.pythonEmbeddedLocation).getAbsolutePath();
			} else {
				File localpath = FMLCommonHandler.instance().getMinecraftServerInstance().getDataDirectory();
				pathVar = new File(localpath, RobotMod.pythonEmbeddedLocation).getAbsolutePath();
			}
		}

		String extra = PathUtility.extraPath();

		if (pathVar == null) {
			if (extra.length() == 0) {
				return base;
			} else {
				pathVar = extra;
			}
		} else if (extra.length() > 0) {
			pathVar = pathVar + pathSep + extra;
		}

		String exeExt = PathUtility.isWindows() ? ".exe" : "";

		String[] paths = pathVar.split(pathSep);

		for (String dir : paths) {
			String p = dir + fileSep + base + exeExt;
			if (new File(p).canExecute()) {
				return p;
			}
		}
		return base;
	}

	public static boolean isWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}
}
