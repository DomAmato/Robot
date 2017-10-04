package com.dyn.robot.network.messages;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageTeleportRobot implements IMessage {

	public static class Handler implements IMessageHandler<MessageTeleportRobot, IMessage> {
		@Override
		public IMessage onMessage(final MessageTeleportRobot message, final MessageContext ctx) {
			RobotMod.proxy.addScheduledTask(() -> {
				EntityPlayerMP player = ctx.getServerHandler().playerEntity;
				World world = player.worldObj;
				EntityRobot robot = (EntityRobot) world.getEntityByID(message.getEntityId());
				BlockPos pos = player.getPosition().offset(player.getHorizontalFacing());

				if (robot.dimension != player.dimension) {
					robot.travelToDimension(player.dimension);
				}
				robot.posX = pos.getX();
				robot.posY = pos.getY();
				robot.posZ = pos.getZ();
				robot.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
				robot.setPosition(pos.getX(), pos.getY(), pos.getZ());
				robot.getNavigator().clearPathEntity();
			});
			return null;
		}
	}

	private int entityId;

	// The basic, no-argument constructor MUST be included for
	// automated handling
	public MessageTeleportRobot() {
	}

	public MessageTeleportRobot(int id) {
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
