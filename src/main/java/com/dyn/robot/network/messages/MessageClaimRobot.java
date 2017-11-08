package com.dyn.robot.network.messages;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageClaimRobot implements IMessage {

	public static class Handler implements IMessageHandler<MessageClaimRobot, IMessage> {
		@Override
		public IMessage onMessage(final MessageClaimRobot message, final MessageContext ctx) {
			RobotMod.proxy.addScheduledTask(() -> {
				EntityPlayerMP player = ctx.getServerHandler().player;
				World world = player.world;

				Entity robot = world.getEntityByID(message.getId());
				((EntityRobot) robot).setRobotName(message.getName());
				((EntityRobot) robot).setOwner(player);
			});
			return null;
		}
	}

	private String robotName;
	private int robotId;

	// The basic, no-argument constructor MUST be included for
	// automated handling
	public MessageClaimRobot() {
	}

	public MessageClaimRobot(String robotName, int id) {
		this.robotName = robotName;
		this.robotId = id;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		robotName = ByteBufUtils.readUTF8String(buf);
		robotId = buf.readInt();
	}

	public int getId() {
		return robotId;
	}

	public String getName() {
		return robotName;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, robotName);
		buf.writeInt(robotId);

	}
}
