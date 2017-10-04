package com.dyn.robot.network.messages;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageToggleRobotFollow implements IMessage {

	public static class Handler implements IMessageHandler<MessageToggleRobotFollow, IMessage> {
		@Override
		public IMessage onMessage(final MessageToggleRobotFollow message, final MessageContext ctx) {
			RobotMod.proxy.addScheduledTask(() -> {
				World world = ctx.getServerHandler().playerEntity.worldObj;
				EntityRobot robot = (EntityRobot) world.getEntityByID(message.getEntityId());
				robot.setIsFollowing(message.shouldFollow());
			});
			return null;
		}
	}

	private boolean toggle;

	private int entityId;

	// The basic, no-argument constructor MUST be included for
	// automated handling
	public MessageToggleRobotFollow() {
	}

	public MessageToggleRobotFollow(int id, boolean toggle) {
		this.toggle = toggle;
		entityId = id;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		toggle = buf.readBoolean();
		entityId = buf.readInt();
	}

	public int getEntityId() {
		return entityId;
	}

	public boolean shouldFollow() {
		return toggle;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(toggle);
		buf.writeInt(entityId);
	}
}
