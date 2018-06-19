package com.dyn.robot.network.messages;

import com.dyn.robot.RobotMod;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageRobotTerminalInput implements IMessage {

	public static class Handler implements IMessageHandler<MessageRobotTerminalInput, IMessage> {
		@Override
		public IMessage onMessage(final MessageRobotTerminalInput message, final MessageContext ctx) {
			RobotMod.proxy.addScheduledTask(() -> {
				if (RobotMod.openSessions.containsKey(message.getId())) {
					RobotMod.openSessions.get(message.getId()).writeLine(message.getInput(),
							ctx.getServerHandler().player);
				} else {
					// this is an error
				}
			});
			return null;
		}
	}

	private int robotId;
	private String input;

	public MessageRobotTerminalInput() {
	}

	public MessageRobotTerminalInput(String input, int robotId) {
		this.robotId = robotId;
		this.input = input;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		robotId = buf.readInt();
		input = ByteBufUtils.readUTF8String(buf);
	}

	public int getId() {
		return robotId;
	}

	public String getInput() {
		return input;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(robotId);
		ByteBufUtils.writeUTF8String(buf, input);
	}

}
