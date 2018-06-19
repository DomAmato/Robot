package com.dyn.robot.network.messages;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.python.RobotTerminal;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageStartRobotTerminal implements IMessage {

	public static class Handler implements IMessageHandler<MessageStartRobotTerminal, IMessage> {
		@Override
		public IMessage onMessage(final MessageStartRobotTerminal message, final MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			World world = player.world;
			EntityRobot robot = (EntityRobot) world.getEntityByID(message.getId());
			robot.clearProgramPath();
			robot.removeIdleAI();
			robot.startExecutingCode();

			RobotMod.proxy.addScheduledTask(() -> {
				if (RobotMod.openSessions.containsKey(message.getId())) {
					RobotMod.openSessions.get(message.getId()).endScript();
					RobotMod.openSessions.replace(message.getId(),
							new RobotTerminal(ctx.getServerHandler().player, message.getId()));
				} else {
					RobotMod.openSessions.put(message.getId(),
							new RobotTerminal(ctx.getServerHandler().player, message.getId()));
				}
			});
			return null;
		}
	}

	private int robotId;

	public MessageStartRobotTerminal() {
	}

	public MessageStartRobotTerminal(int robotId) {
		this.robotId = robotId;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		robotId = buf.readInt();
	}

	public int getId() {
		return robotId;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(robotId);
	}

}
