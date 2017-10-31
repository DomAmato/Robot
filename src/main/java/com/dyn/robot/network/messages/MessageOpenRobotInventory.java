package com.dyn.robot.network.messages;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.gui.RobotGuiHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageOpenRobotInventory implements IMessage {

	public static class Handler implements IMessageHandler<MessageOpenRobotInventory, IMessage> {
		@Override
		public IMessage onMessage(final MessageOpenRobotInventory message, final MessageContext ctx) {
			RobotMod.proxy.addScheduledTask(() -> {
				EntityPlayerMP player = ctx.getServerHandler().player;
				EntityRobot robot = (EntityRobot) ctx.getServerHandler().player.world
						.getEntityByID(message.getEntityId());
				player.openGui(RobotMod.instance, robot.getEntityId(), robot.world, (int) robot.posX,
						(int) robot.posY, (int) robot.posZ);
			});
			return null;
		}
	}

	private int entityId;

	// The basic, no-argument constructor MUST be included for
	// automated handling
	public MessageOpenRobotInventory() {
	}

	public MessageOpenRobotInventory(int id) {
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
