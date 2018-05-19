package com.dyn.robot.network.messages;

import java.util.List;

import com.dyn.robot.RobotMod;
import com.dyn.robot.blocks.BlockRobot;
import com.dyn.robot.blocks.RobotBlockTileEntity;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.entity.SimpleRobotEntity;
import com.dyn.robot.reference.Reference;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
				EntityPlayerMP player = ctx.getServerHandler().player;
				World world = player.world;

				RobotBlockTileEntity robotTile = (RobotBlockTileEntity) world.getTileEntity(message.getPosition());
				dir = world.getBlockState(message.getPosition()).getValue(BlockRobot.FACING);
				world.setBlockToAir(message.getPosition());
				SimpleRobotEntity new_robot = (SimpleRobotEntity) EntityList
						.createEntityByIDFromName(new ResourceLocation(Reference.MOD_ID, "robot"), world);
				new_robot.setLocationAndAngles(message.getPosition().getX() + 0.5, message.getPosition().getY(),
						message.getPosition().getZ() + 0.5, dir.getHorizontalAngle(), 0.0F);
				new_robot.rotationYawHead = new_robot.rotationYaw;
				new_robot.renderYawOffset = new_robot.rotationYaw;
				new_robot.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(new_robot)),
						(IEntityLivingData) null);
				new_robot.setOwner(player);
				new_robot.setRobotName(message.getName());
				new_robot.setLastExecutedScript(robotTile.getPrevScript());
				NBTTagList inventory = robotTile.getInventory();
				if(inventory.hasNoTags()) {
					new_robot.robot_inventory.setInventorySlotContents(new_robot.robot_inventory.getOpenExpansionSlot(),
							new ItemStack(RobotMod.expChip, 1, 15));	
				} else {
					for (int i = 0; i < inventory.tagCount(); i++) {
						NBTTagCompound itemtag = inventory.getCompoundTagAt(i);
						int slot = itemtag.getByte("Slot") & 0xFF;
						if ((slot >= 0) && (slot < 32)) {
							new_robot.robot_inventory.setInventorySlotContents(slot, new ItemStack(itemtag));
						}
					}
				}
				world.spawnEntity(new_robot);
				new_robot.setIsFollowing(true);

				// this currently doesnt play the sound
				// world.playSound(player, message.getPosition(), RobotMod.ROBOT_ON,
				// SoundCategory.AMBIENT, 1, 1);
				player.openGui(RobotMod.instance, new_robot.getEntityId(), world, (int) new_robot.posX,
						(int) new_robot.posY, (int) new_robot.posZ);

			});
			return null;
		}
	}

	private String robotName;
	private BlockPos pos;

	private int dim;

	// The basic, no-argument constructor MUST be included for
	// automated handling
	public MessageActivateRobot() {
	}

	public MessageActivateRobot(String robotName, BlockPos pos, int dim) {
		this.robotName = robotName;
		this.pos = pos;
		this.dim = dim;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
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

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(pos.toLong());
		ByteBufUtils.writeUTF8String(buf, robotName);
		buf.writeInt(dim);

	}
}
