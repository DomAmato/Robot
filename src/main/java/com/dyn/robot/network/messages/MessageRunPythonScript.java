package com.dyn.robot.network.messages;

import java.util.Arrays;
import java.util.regex.Pattern;

import com.dyn.rjm.process.RunPythonShell;
import com.dyn.robot.RobotMod;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageRunPythonScript implements IMessage {

	public static class Handler implements IMessageHandler<MessageRunPythonScript, IMessage> {
		@Override
		public IMessage onMessage(final MessageRunPythonScript message, final MessageContext ctx) {
			RobotMod.proxy.addScheduledTask(() -> RunPythonShell
					.run(Arrays.asList(message.getScript().split(Pattern.quote("\n"))), ctx.getServerHandler().player));
			return null;
		}
	}

	private String script;

	public MessageRunPythonScript() {
	}

	public MessageRunPythonScript(String script) {
		this.script = script;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		script = ByteBufUtils.readUTF8String(buf);
	}

	public String getScript() {
		return script;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, script);
	}
}
