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

public class RobotScript {

	private String scriptProcessorPath;
	public RobotScript(List<String> program, EntityPlayer player, int robotId) {
		try {
			this.robotId = robotId;
			String path = PathUtility.getPythonExecutablePath();
			if (path.contains("/") || path.contains(System.getProperty("file.separator"))) {
				scriptProcessorPath = new File(path).getCanonicalPath().toString();
			} else {
				scriptProcessorPath = path;
			}

			ProcessBuilder builder = new ProcessBuilder(scriptProcessorPath, "-i");

			builder.directory(RobotMod.apiFileLocation.getParentFile());

			Map<String, String> environment = builder.environment();
			environment.put("MINECRAFT_PLAYER_ID", "" + player.getEntityId());
			environment.put("MINECRAFT_ROBOT_ID", ""+robotId);
			environment.put("MINECRAFT_API_PORT", "" + RobotMod.currentPortNumber);

			runningScript = builder.start();

			createErrorListenerDaemon(runningScript.getErrorStream(), player);

			OutputStream in = runningScript.getOutputStream();
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
					.post(new CodeEvent.RobotErrorEvent("", e.getMessage(), 0, player, robotId));
		}
	}
	
	public void endScript() {
		runningScript.destroy();
	}

	private Process runningScript;

	private int robotId = 0;

	private void createErrorListenerDaemon(final InputStream stream, final EntityPlayer entity) {
		Thread t = new Thread() {

			@Override
			public void run() {
				BufferedReader br;

				br = new BufferedReader(new InputStreamReader(stream));

				String line;
				String lineNum = "";
				String codeLine = "";
				try {
					while (null != (line = br.readLine())) {
						if (!line.contains("copyright") && !line.contains("Python")) {
							if (line.contains(">>>")) {
								lineNum = line;
							} else {
								
								// stop the script from executing
								if (line.contains("Error:")) {
									try {
										runningScript.exitValue();
									} catch (IllegalThreadStateException e) {
										// script was still running
										runningScript.destroy();
									}
									// send the error to the player and then
									// determine the problem client side
									int lineLoc = 0;
									String tempLine = lineNum;
									int index = lineNum.indexOf(">>>");
									while (index != -1) {
										lineLoc++;
										tempLine = tempLine.substring(index + 1);
										index = tempLine.indexOf(">>>");
									}

									index = lineNum.indexOf("...");
									tempLine = lineNum;
									while (index != -1) {
										lineLoc++;
										tempLine = tempLine.substring(index + 1);
										index = tempLine.indexOf("...");
									}
									// posts error to bus which is handled
									// server side and translated to client
									line = line.substring(line.lastIndexOf(".") + 1);

									MinecraftForge.EVENT_BUS.post(new CodeEvent.RobotErrorEvent(codeLine,
											line, lineLoc, entity, robotId));

									lineNum = "";
									codeLine = "";
									// kill the script if it hasnt been already
									if ((runningScript != null)
											&& isProcessAlive()) {
										runningScript.destroy();
									}
									break;
								} else if (!line.contains("<stdin>") && !line.trim().equals("^")) {
									codeLine = line;
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

	public boolean isProcessAlive() {
		try {
			runningScript.exitValue();
			return false;
		} catch (Exception e) {
			return true;
		}

	}
}
