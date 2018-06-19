package com.dyn.robot.network.messages;

import com.dyn.robot.gui.CommandLineInterface;
import com.rabbit.gui.RabbitGui;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageRobotTerminalOutput implements IMessage {

	public static class Handler implements IMessageHandler<MessageRobotTerminalOutput, IMessage> {
		@Override
		public IMessage onMessage(final MessageRobotTerminalOutput message, final MessageContext ctx) {
			if ((RabbitGui.proxy.getCurrentStage() != null)
					&& (RabbitGui.proxy.getCurrentStage().getShow() instanceof CommandLineInterface)) {
				((CommandLineInterface) RabbitGui.proxy.getCurrentStage().getShow()).updateOutput(message.getOutput());
			}

			return null;
		}
	}

	private String output;

	public MessageRobotTerminalOutput() {
	}

	public MessageRobotTerminalOutput(String output) {
		this.output = output;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		output = ByteBufUtils.readUTF8String(buf);
	}

	public String getOutput() {
		return output;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, output);
	}

}
