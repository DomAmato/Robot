package com.dom.robot.network.messages;

import com.dom.robot.RobotMod;
import com.dom.robot.entity.EntityRobot;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageReplaceSDCardItemStack implements IMessage {

	public static class Handler implements IMessageHandler<MessageReplaceSDCardItemStack, IMessage> {
		@Override
		public IMessage onMessage(final MessageReplaceSDCardItemStack message, final MessageContext ctx) {
			RobotMod.proxy.addScheduledTask(() -> {
				EntityRobot robot = (EntityRobot) ctx.getServerHandler().player.world
						.getEntityByID(message.getRobotId());
				robot.robot_inventory.setInventorySlotContents(0, message.getNewCard());
			});
			return null;
		}
	}

	private int robotId;
	private ItemStack newCard;

	// The basic, no-argument constructor MUST be included for
	// automated handling
	public MessageReplaceSDCardItemStack() {
	}

	public MessageReplaceSDCardItemStack(int id, ItemStack newCard) {
		robotId = id;
		this.newCard = newCard;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		newCard = ByteBufUtils.readItemStack(buf);
		robotId = buf.readInt();
	}

	public ItemStack getNewCard() {
		return newCard;
	}

	public int getRobotId() {
		return robotId;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeItemStack(buf, newCard);
		buf.writeInt(robotId);

	}
}
