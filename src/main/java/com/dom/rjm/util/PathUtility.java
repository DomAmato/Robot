package com.dom.rjm.util;

import java.io.File;

import com.dom.rjm.RaspberryJamMod;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class PathUtility {
	private static String extraPath() {
		if (PathUtility.isWindows()) {
			return "\\python27\\;python27\\";
		} else {
			return "";
		}
	}

	public static String getPythonExecutablePath() {
		String base = RaspberryJamMod.pythonInterpreter;

		String pathSep = System.getProperty("path.separator");
		String fileSep = System.getProperty("file.separator");
		if (base.contains("/") || base.contains(fileSep)) {
			return base;
		}

		String pathVar = "";

		if (RaspberryJamMod.useSystemPath) {
			pathVar = System.getenv("PATH");
		} else {
			if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
				File localpath = Minecraft.getMinecraft().mcDataDir;
				pathVar = new File(localpath, RaspberryJamMod.pythonEmbeddedLocation).getAbsolutePath();
			} else {
				File localpath = FMLCommonHandler.instance().getMinecraftServerInstance().getDataDirectory();
				pathVar = new File(localpath, RaspberryJamMod.pythonEmbeddedLocation).getAbsolutePath();
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
