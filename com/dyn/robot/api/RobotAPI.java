package com.dyn.robot.api;

import java.util.Scanner;

import mobi.omegacentauri.raspberryjammod.api.APIRegistry;
import mobi.omegacentauri.raspberryjammod.api.APIRegistry.Python2MinecraftApi;
import mobi.omegacentauri.raspberryjammod.events.MCEventHandler;

public class RobotAPI extends Python2MinecraftApi {

	public static int robotId = 0;

	public static void registerCommands() {
		// robot
		APIRegistry.registerCommand("robot." + GETPOS, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			entityGetPos(robotId);
		});
		APIRegistry.registerCommand("robot." + GETTILE, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			entityGetTile(robotId);
		});
		APIRegistry.registerCommand("robot." + GETROTATION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					entityGetRotation(robotId);
				});
		APIRegistry.registerCommand("robot." + SETROTATION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					entitySetRotation(robotId, scan.nextFloat());
				});
		APIRegistry.registerCommand("robot." + GETPITCH, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			entityGetPitch(robotId);
		});
		APIRegistry.registerCommand("robot." + SETPITCH, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			entitySetPitch(robotId, scan.nextFloat());
		});
		APIRegistry.registerCommand("robot." + GETDIRECTION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					entityGetDirection(robotId);
				});
		APIRegistry.registerCommand("robot." + SETDIRECTION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					entitySetDirection(robotId, scan);
				});
		APIRegistry.registerCommand("robot." + SETTILE, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			entitySetTile(robotId, scan);
		});
		APIRegistry.registerCommand("robot." + SETPOS, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			entitySetPos(robotId, scan);
		});
		APIRegistry.registerCommand("robot." + SETDIMENSION,
				(String args, Scanner scan, MCEventHandler eventHandler) -> {
					entitySetDimension(robotId, scan.nextInt());
				});
		APIRegistry.registerCommand("robot." + GETNAME, (String args, Scanner scan, MCEventHandler eventHandler) -> {
			entityGetNameAndUUID(robotId);
		});
	}
	
	public static int getRobotId(){
		return robotId;
	}
	
	public static void setRobotId(int id) {
		robotId = id;
	}
}
