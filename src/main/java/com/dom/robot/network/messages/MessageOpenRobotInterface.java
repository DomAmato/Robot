package com.dom.robot.network.messages;

import com.dom.robot.RobotMod;
import com.dom.robot.entity.EntityRobot;
import com.dom.robot.gui.RobotGuiHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageOpenRobotInterface implements IMessage {

	public static class Handler implements IMessageHandler<MessageOpenRobotInterface, IMessage> {
		@Override
		public IMessage onMessage(final MessageOpenRobotInterface message, final MessageContext ctx) {
			RobotMod.proxy.addScheduledTask(() -> {
				EntityPlayerMP player = ctx.getServerHandler().player;
				EntityRobot robot = (EntityRobot) ctx.getServerHandler().player.world
						.getEntityByID(message.getEntityId());
				player.openGui(RobotMod.instance, RobotGuiHandler.getActivationGuiID(), player.world, (int) robot.posX,
						(int) robot.posY, (int) robot.posZ);
			});
			return null;
		}
	}

	private int entityId;

	// The basic, no-argument constructor MUST be included for
	// automated handling
	public MessageOpenRobotInterface() {
	}

	public MessageOpenRobotInterface(int id) {
		entityId = id;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		entityId = buf.readInt();
	}

	public int getEntityId() {
		return entityId;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(entityId);
	}
}
