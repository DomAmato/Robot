package com.dyn.robot.network;

import com.dyn.robot.entity.EntityRobot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;

public class SocketEvent extends Event {

	public static class Close extends SocketEvent {
		public Close(EntityPlayer player) {
			super(player);
		}
	}
	
	public static class CloseRobot extends Close {	
		private final EntityRobot robot;
		public CloseRobot(EntityRobot robot) {
			super(robot.getOwner());
			this.robot = robot;
		}
		
		public EntityRobot getRobot() {
			return robot;
		}
	}

	private final EntityPlayer player;

	public SocketEvent(EntityPlayer player) {
		this.player = player;
	}

	public EntityPlayer getPlayer() {
		return player;
	}
}
