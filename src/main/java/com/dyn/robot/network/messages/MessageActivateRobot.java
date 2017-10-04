package com.dyn.robot.network.messages;

import java.util.List;

import com.dyn.robot.RobotMod;
import com.dyn.robot.blocks.BlockDynRobot;
import com.dyn.robot.entity.DynRobotEntity;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.gui.RobotGuiHandler;
import com.dyn.robot.network.NetworkManager;
import com.dyn.robot.util.HelperFunctions;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageActivateRobot implements IMessage {

	public static class Handler implements IMessageHandler<MessageActivateRobot, IMessage> {
		@Override
		public IMessage onMessage(final MessageActivateRobot message, final MessageContext ctx) {
			RobotMod.proxy.addScheduledTask(() -> {
				EnumFacing dir = EnumFacing.NORTH;
				EntityPlayerMP player = ctx.getServerHandler().playerEntity;
				World world = player.worldObj;

				if (message.isActivating()) {
					dir = world.getBlockState(message.getPosition()).getValue(BlockDynRobot.FACING);
					world.setBlockToAir(message.getPosition());
					DynRobotEntity new_robot = (DynRobotEntity) ItemMonsterPlacer.spawnCreature(world,
							EntityList.classToStringMapping.get(DynRobotEntity.class),
							message.getPosition().getX() + 0.5, message.getPosition().getY(),
							message.getPosition().getZ() + 0.5);
					new_robot.setOwner(player);
					new_robot.setRobotName(message.getName());
					new_robot.rotate(HelperFunctions.getAngleFromFacing(dir));
					new_robot.setIsFollowing(true);
					player.openGui(RobotMod.instance, RobotGuiHandler.getActivationGuiID(), world, (int) new_robot.posX,
							(int) new_robot.posY, (int) new_robot.posZ);

					NetworkManager.sendTo(new PlayCustomSoundMessage("robot:robot.on"), player);

				} else {
					List<EntityRobot> robots = world.getEntitiesWithinAABB(EntityRobot.class,
							AxisAlignedBB.fromBounds(message.getPosition().getX() - 1, message.getPosition().getY() - 1,
									message.getPosition().getZ() - 1, message.getPosition().getX() + 1,
									message.getPosition().getY() + 1, message.getPosition().getZ() + 1));
					String robotName = RobotMod.dynRobot.getLocalizedName();
					for (EntityRobot robot : robots) {
						if (robot.isOwner(player)) {
							dir = robot.getHorizontalFacing();
							robotName = robot.getRobotName();
							robot.setDead();
						}
					}
					ItemStack robotStack = new ItemStack(RobotMod.dynRobot, 1);
					robotStack.setStackDisplayName(robotName);
					player.inventory.addItemStackToInventory(robotStack);
				}
			});
			return null;
		}
	}

	private String robotName;
	private boolean activate;
	private BlockPos pos;

	private int dim;

	// The basic, no-argument constructor MUST be included for
	// automated handling
	public MessageActivateRobot() {
	}

	public MessageActivateRobot(String robotName, BlockPos pos, int dim, boolean activate) {
		this.robotName = robotName;
		this.activate = activate;
		this.pos = pos;
		this.dim = dim;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		activate = buf.readBoolean();
		pos = BlockPos.fromLong(buf.readLong());
		robotName = ByteBufUtils.readUTF8String(buf);
		dim = buf.readInt();
	}

	public int getDimension() {
		return dim;
	}

	public String getName() {
		return robotName;
	}

	public BlockPos getPosition() {
		return pos;
	}

	public boolean isActivating() {
		return activate;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(activate);
		buf.writeLong(pos.toLong());
		ByteBufUtils.writeUTF8String(buf, robotName);
		buf.writeInt(dim);

	}
}
