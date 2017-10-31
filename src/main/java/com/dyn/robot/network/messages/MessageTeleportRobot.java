package com.dyn.robot.network.messages;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.utils.HelperFunctions;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageTeleportRobot implements IMessage {

	public static class Handler implements IMessageHandler<MessageTeleportRobot, IMessage> {
		@Override
		public IMessage onMessage(final MessageTeleportRobot message, final MessageContext ctx) {
			RobotMod.proxy.addScheduledTask(() -> {
				EntityPlayerMP player = ctx.getServerHandler().player;
				World world = player.world;
				EntityRobot robot = (EntityRobot) world.getEntityByID(message.getEntityId());
				BlockPos pos = message.getPos();

				if (robot.dimension != player.dimension) {
					robot.changeDimension(player.dimension);
				}
				
				robot.setPosition(pos.getX(), pos.getY(), pos.getZ());
				robot.rotate(HelperFunctions.getAngleFromFacing(message.getFacing() ));
				robot.getNavigator().clearPathEntity();
			});
			return null;
		}
	}

	private int entityId;
	private BlockPos pos;
	EnumFacing facing;

	// The basic, no-argument constructor MUST be included for
	// automated handling
	public MessageTeleportRobot() {
	}

	public MessageTeleportRobot(int id, BlockPos pos, EnumFacing facing) {
		entityId = id;
		this.pos = pos;
		this.facing = facing;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		entityId = buf.readInt();
		pos = BlockPos.fromLong(buf.readLong());
		facing = EnumFacing.getHorizontal(buf.readInt());
	}

	public BlockPos getPos() {
		return pos;
	}

	public EnumFacing getFacing() {
		return facing;
	}

	public int getEntityId() {
		return entityId;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(entityId);
		buf.writeLong(pos.toLong());
		buf.writeInt(facing.getHorizontalIndex());
	}
}
