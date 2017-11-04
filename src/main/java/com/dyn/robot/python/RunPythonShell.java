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
import com.dyn.robot.api.APIHandler;
import com.dyn.robot.network.CodeEvent;
import com.dyn.robot.utils.PathUtility;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class RunPythonShell {

	private static String scriptProcessorPath;
	private static Process runningScript;

	private static String lineNum = "";

	private static String codeLine = "";

	private static boolean isRobot = false;
	private static int robotId = 0;

	private static void globalMessage(String msg) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			Minecraft.getMinecraft().player.sendMessage(new TextComponentString(msg));
		} else {
			APIHandler.globalMessage(msg);
		}
	}

	private static void gobble(final InputStream stream, final EntityPlayer entity, final String label) {
		Thread t = new Thread() {

			@Override
			public void run() {
				BufferedReader br;

				br = new BufferedReader(new InputStreamReader(stream));

				String line;
				try {
					while (null != (line = br.readLine())) {
						line = line.replaceAll(Pattern.quote(">>>"), "");
						line = line.replaceAll(Pattern.quote("..."), "");
						line = line.trim();
						if (!line.contains("copyright") && !line.contains("Python") && !line.isEmpty()) {
							if (entity == null) {
								RunPythonShell.globalMessage(label + line);
							} else {
								entity.sendMessage(new TextComponentString(label + line));
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

	private static void gobbleError(final InputStream stream, final EntityPlayer entity, final String label) {
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
									// server side and  translated to client
									line = line.substring(line.lastIndexOf(".") + 1);
									
									RobotMod.logger.info(line);
									
									if (!RunPythonShell.isRobot) {
										MinecraftForge.EVENT_BUS.post(new CodeEvent.ErrorEvent(RunPythonShell.codeLine,
												line, lineLoc, entity));
									} else {
										MinecraftForge.EVENT_BUS
												.post(new CodeEvent.RobotErrorEvent(RunPythonShell.codeLine, line,
														lineLoc, entity, RunPythonShell.robotId));
									}
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

	public static void run(List<String> program, EntityPlayer player) {
		RunPythonShell.run(program, player, false, 0);
	}

	public static void run(List<String> program, EntityPlayer player, boolean isRobot, int robotId) {
		try {
			RunPythonShell.robotId = robotId;
			RunPythonShell.isRobot = isRobot;
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

			builder.directory(new File(RobotMod.apiLocation));
			//this only works in dev environments the build process cannot
			//access the jar directory
//			builder.directory(new File(RunPythonShell.class.getResource("/assets/robot").getPath()));

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

			RunPythonShell.gobble(RunPythonShell.runningScript.getInputStream(), player, "");
			RunPythonShell.gobbleError(RunPythonShell.runningScript.getErrorStream(), player, "[ERR] ");

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
			RobotMod.logger.info(e);
			MinecraftForge.EVENT_BUS
					.post(new CodeEvent.RobotErrorEvent(RunPythonShell.codeLine, e.getMessage(), 0, player, robotId));
		}
	}
}
