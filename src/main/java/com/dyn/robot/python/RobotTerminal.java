package com.dyn.robot.python;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import com.dyn.robot.RobotMod;
import com.dyn.robot.network.CodeEvent;
import com.dyn.robot.network.NetworkManager;
import com.dyn.robot.network.messages.MessageRobotTerminalOutput;
import com.dyn.robot.utils.PathUtility;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;

public class RobotTerminal {

	private String scriptProcessorPath;
	private Process runningScript;

	private BufferedWriter writer;

	public RobotTerminal(EntityPlayer player, int robotId) {
		try {
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
			environment.put("MINECRAFT_ROBOT_ID", "" + robotId);
			environment.put("MINECRAFT_API_PORT", "" + RobotMod.currentPortNumber);

			builder.redirectErrorStream(true);
			runningScript = builder.start();

			createStreamListenerDaemon(runningScript.getInputStream(), player);

			OutputStream in = runningScript.getOutputStream();
			writer = new BufferedWriter(new OutputStreamWriter(in));
			writeLine("from api.robot import *", player);
			writeLine("robot = Robot()", player);
			writer.flush();
		} catch (IOException e) {
			RobotMod.logger.error(e);
			MinecraftForge.EVENT_BUS.post(new CodeEvent.RobotErrorEvent("", e.getMessage(), 0, player, robotId));
		}
	}

	private void createStreamListenerDaemon(final InputStream stream, final EntityPlayer entity) {
		Thread t = new Thread() {

			@Override
			public void run() {
				BufferedReader br;

				br = new BufferedReader(new InputStreamReader(stream));

				int buffer = 0;
				try {
					// dump the first lines so that players dont know everything about the python
					// interpreter
					br.readLine();
					br.readLine();
					Thread.sleep(500); // sleep for a half second to fill the buffer so it doesnt read a single
										// charater
					while (br.ready()) {
						br.read();
					}
					NetworkManager.sendTo(new MessageRobotTerminalOutput(
							"RoboOS v1.2.3 Bootup Complete\nPython Intialized...\n>>> from api.robot import *\n>>> robot = Robot()"),
							(EntityPlayerMP) entity);
					while (-1 != (buffer = br.read())) {
						String line = "" + (char) buffer;
						Thread.sleep(500); // sleep for a half second to fill the buffer so it doesnt read a single
											// charater
						while (br.ready()) {
							line += (char) br.read();
						}
						if (line.contains("RequestError")) {
							line = line.substring(line.lastIndexOf("RequestError") + 7);
						}
						NetworkManager.sendTo(new MessageRobotTerminalOutput(line), (EntityPlayerMP) entity);
						// whatever comes from the session send back to the player
					}
				} catch (IOException | InterruptedException e) {
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

	public void endScript() {
		runningScript.destroy();
	}

	public boolean isProcessAlive() {
		try {
			runningScript.exitValue();
			return false;
		} catch (Exception e) {
			return true;
		}

	}

	public void writeLine(String line, final EntityPlayer entity) {
		try {
			if (RobotMod.pythonImportRegex.matcher(line).find()) {
				NetworkManager.sendTo(
						new MessageRobotTerminalOutput("Command contains blacklisted code, action is not allowed\n>>>"),
						(EntityPlayerMP) entity);
			} else if (writer != null) {
				writer.write(line);
				writer.newLine();
				writer.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
