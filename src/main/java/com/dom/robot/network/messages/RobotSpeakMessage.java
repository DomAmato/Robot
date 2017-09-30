package com.dom.robot.network.messages;

import java.io.IOException;

import com.dom.robot.entity.EntityRobot;
import com.dom.robot.network.AbstractMessage.AbstractClientMessage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

public class RobotSpeakMessage extends AbstractClientMessage<RobotSpeakMessage> {

	private String command;
	private int robotId;

	// The basic, no-argument constructor MUST be included for
	// automated handling
	public RobotSpeakMessage() {
	}

	// We need to initialize our data, so provide a suitable constructor:
	public RobotSpeakMessage(String command, int robotid) {
		this.command = command;
		robotId = robotid;
	}

	@Override
	public void process(EntityPlayer player, Side side) {
		if (side.isClient()) {
			World world = player.world;
			EntityRobot robot = (EntityRobot) world.getEntityByID(robotId);
			if ((robot != null) && !robot.isDead) {
				robot.addMessage(command);
			}
		}
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		robotId = buffer.readInt();
		command = buffer.readString(buffer.readableBytes());
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeInt(robotId);
		buffer.writeString(command);
	}
}
