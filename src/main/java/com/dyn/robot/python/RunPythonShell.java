package com.dyn.robot.python;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.dyn.robot.RobotMod;
import com.dyn.robot.network.CodeEvent;
import com.dyn.robot.utils.PathUtility;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;

public class RunPythonShell {

	private static String scriptProcessorPath;
	private static Process runningScript;

	private static String lineNum = "";

	private static String codeLine = "";

	private static int robotId = 0;

	private static void createErrorListenerDaemon(final InputStream stream, final EntityPlayer entity) {
		Thread t = new Thread() {

			@Override
			public void run() {
				BufferedReader br;

				br = new BufferedReader(new InputStreamReader(stream));

				String line;
				try {
					while (null != (line = br.readLine())) {
						if (!line.contains("copyright") && !line.contains("Python")) {
							if (line.contains(">>>")) {
								RunPythonShell.lineNum = line;
							} else {
								// stop the script from executing
								if (line.contains("Error:")) {
									try {
										RunPythonShell.runningScript.exitValue();
									} catch (IllegalThreadStateException e) {
										// script was still running
										RunPythonShell.runningScript.destroy();
									}
									// send the error to the player and then
									// determine the problem client side
									int lineLoc = 0;
									String tempLine = RunPythonShell.lineNum;
									int index = RunPythonShell.lineNum.indexOf(">>>");
									while (index != -1) {
										lineLoc++;
										tempLine = tempLine.substring(index + 1);
										index = tempLine.indexOf(">>>");
									}

									index = RunPythonShell.lineNum.indexOf("...");
									tempLine = RunPythonShell.lineNum;
									while (index != -1) {
										lineLoc++;
										tempLine = tempLine.substring(index + 1);
										index = tempLine.indexOf("...");
									}
									// posts error to bus which is handled
									// server side and translated to client
									line = line.substring(line.lastIndexOf(".") + 1);

									MinecraftForge.EVENT_BUS.post(new CodeEvent.RobotErrorEvent(RunPythonShell.codeLine,
											line, lineLoc, entity, RunPythonShell.robotId));

									RunPythonShell.lineNum = "";
									RunPythonShell.codeLine = "";
									// kill the script if it hasnt been already
									if ((RunPythonShell.runningScript != null)
											&& RunPythonShell.isProcessAlive(RunPythonShell.runningScript)) {
										RunPythonShell.runningScript.destroy();
									}
									break;
								} else if (!line.contains("<stdin>") && !line.trim().equals("^")) {
									RunPythonShell.codeLine = line;
								}
							}
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

	private static boolean isProcessAlive(Process proc) {
		try {
			proc.exitValue();
			return false;
		} catch (Exception e) {
			return true;
		}

	}

	public static void run(List<String> program, EntityPlayer player, int robotId) {
		try {
			RunPythonShell.robotId = robotId;
			String path = PathUtility.getPythonExecutablePath();
			if (path.contains("/") || path.contains(System.getProperty("file.separator"))) {
				RunPythonShell.scriptProcessorPath = new File(path).getAbsolutePath().toString();
			} else {
				RunPythonShell.scriptProcessorPath = path;
			}

			if ((RunPythonShell.runningScript != null) && RunPythonShell.isProcessAlive(RunPythonShell.runningScript)) {
				RunPythonShell.runningScript.destroy();
			}

			ProcessBuilder builder = new ProcessBuilder(RunPythonShell.scriptProcessorPath, "-i");

			builder.directory(RobotMod.apiFileLocation.getParentFile());

			Map<String, String> environment = builder.environment();
			environment.put("MINECRAFT_PLAYER_NAME", player.getName());
			environment.put("MINECRAFT_PLAYER_ID", "" + player.getEntityId());
			environment.put("MINECRAFT_API_PORT", "" + RobotMod.currentPortNumber);

			RunPythonShell.runningScript = builder.start();

			// we dont have to worry about checking if the script is alive since
			// it gets destroyed earlier
			if (RobotMod.playerProcesses.containsKey(player)) {
				RobotMod.playerProcesses.replace(player, RunPythonShell.runningScript);
			} else {
				RobotMod.playerProcesses.put(player, RunPythonShell.runningScript);
			}

			RunPythonShell.createErrorListenerDaemon(RunPythonShell.runningScript.getErrorStream(), player);

			OutputStream in = RunPythonShell.runningScript.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(in));

			boolean codeBlock = false;
			int blockDepth = 0;
			for (String lines : program) {
				if (codeBlock && ((lines.split(Pattern.quote("    ")).length - 1) < blockDepth)) {
					for (int i = blockDepth - lines.split(Pattern.quote("    ")).length - 1; i > 0; i--) {
						writer.newLine();
					}
					blockDepth = lines.split(Pattern.quote("    ")).length - 1;
					if (blockDepth == 0) {
						codeBlock = false;
					}
				}
				writer.write(lines);
				if (lines.contains("    ")) {
					blockDepth = lines.split(Pattern.quote("    ")).length - 1;
					codeBlock = true;
				}
				writer.newLine();
			}
			if (codeBlock) {
				for (int i = blockDepth; i > 0; i--) {
					writer.newLine();
				}
			}
			writer.write("exit()");
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			RobotMod.logger.error(e);
			MinecraftForge.EVENT_BUS
					.post(new CodeEvent.RobotErrorEvent(RunPythonShell.codeLine, e.getMessage(), 0, player, robotId));
		}
	}
}
