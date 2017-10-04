package com.dyn.robot.network.messages;

import com.dyn.robot.RobotMod;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PlayCustomSoundMessage implements IMessage {
	public static class Handler implements IMessageHandler<PlayCustomSoundMessage, IMessage> {

		@Override
		public IMessage onMessage(PlayCustomSoundMessage message, MessageContext ctx) {
			RobotMod.proxy.addScheduledTask(() -> {
				// the respective proxies should check if it can validly handle
				// the error
				RobotMod.proxy.playSound(message.getSoundName());

			});
			return null;
		}

	}

	private String sound;

	public PlayCustomSoundMessage() {
	}

	public PlayCustomSoundMessage(String sound) {
		this.sound = sound;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		sound = ByteBufUtils.readUTF8String(buf);
	}

	public String getSoundName() {
		return sound;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, sound);
	}
}
