package com.dyn.robot.network.messages;

import com.dyn.robot.RobotMod;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class RobotErrorMessage implements IMessage {
	public static class Handler implements IMessageHandler<RobotErrorMessage, IMessage> {

		@Override
		public IMessage onMessage(RobotErrorMessage message, MessageContext ctx) {
			RobotMod.proxy.addScheduledTask(() -> {
				RobotMod.proxy.handleErrorMessage(message.getError(), message.getCode(), message.getLine());
			});
			return null;
		}

	}

	private String code;
	private String error;

	private int line;

	public RobotErrorMessage() {
	}

	public RobotErrorMessage(String code, String error, int line, int robotid) {
		this.code = code;
		this.error = error;
		this.line = line;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		code = ByteBufUtils.readUTF8String(buf);
		error = ByteBufUtils.readUTF8String(buf);
		line = buf.readInt();
	}

	public String getCode() {
		return code;
	}

	public String getError() {
		return error;
	}

	public int getLine() {
		return line;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, code);
		ByteBufUtils.writeUTF8String(buf, error);
		buf.writeInt(line);
	}
}
