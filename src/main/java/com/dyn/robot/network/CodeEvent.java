package com.dyn.robot.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;

public class CodeEvent extends Event {
	public static class ErrorEvent extends CodeEvent {
		private final String error;
		private final int line;

		public ErrorEvent(String code, String error, int line, EntityPlayer player) {
			super(player, code);
			this.error = error;
			this.line = line;

		}

		public String getError() {
			return error;
		}

		public int getLine() {
			return line;
		}
	}

	public static class FailEvent extends CodeEvent {
		private final int id;

		public FailEvent(String code, int id, EntityPlayer player) {
			super(player, code);
			this.id = id;
		}

		public int getId() {
			return id;
		}
	}

	public static class RobotErrorEvent extends ErrorEvent {

		private final int entityId;

		public RobotErrorEvent(String code, String error, int line, EntityPlayer player, int entityId) {
			super(code, error, line, player);
			this.entityId = entityId;
		}

		public int getEntityId() {
			return entityId;
		}

	}

	public static class RobotSuccessEvent extends SuccessEvent {

		private final int entityId;

		public RobotSuccessEvent(String code, int entityId, EntityPlayer player) {
			super(code, player);
			this.entityId = entityId;
		}

		public int getEntityId() {
			return entityId;
		}

	}

	public static class SuccessEvent extends CodeEvent {
		public SuccessEvent(String code, EntityPlayer player) {
			super(player, code);
		}
	}

	private final EntityPlayer player;

	private final String code;

	CodeEvent(EntityPlayer player, String code) {
		this.code = code;
		this.player = player;
	}

	public String getCode() {
		return code;
	}

	public EntityPlayer getPlayer() {
		return player;
	}

}
