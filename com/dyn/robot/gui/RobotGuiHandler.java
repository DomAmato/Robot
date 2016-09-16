package com.dyn.robot.gui;

import java.util.List;

import com.dyn.robot.entity.DynRobotEntity;
import com.dyn.robot.entity.EntityRobot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class RobotGuiHandler implements IGuiHandler {

	public static final int ROBOT_REMOTE_GUI = 0;

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == ROBOT_REMOTE_GUI) {
			int radius = 5;
			// hopefully this works... its possible robots will overlap each
			// other
			List<EntityRobot> robots = world.getEntitiesWithinAABB(EntityRobot.class,
					AxisAlignedBB.fromBounds(x, y, z, x + radius, y + radius, z + radius));
			robots.addAll(world.getEntitiesWithinAABB(DynRobotEntity.class,
					AxisAlignedBB.fromBounds(x, y, z, x + radius, y + radius, z + radius)));
			for (EntityRobot robot : robots) {
				System.out.println("dyn robot owners: " + robot.getOwner());
			}
			if (robots.size() > 0) {
				if (robots.get(0).getClientComputer() == null) {
					robots.get(0).createClientComputer().turnOn();
				} else if (!robots.get(0).getClientComputer().isOn()) {
					robots.get(0).getClientComputer().turnOn();
				}
				return new RemoteInterface(player, world, robots.get(0));
			} else {
				System.out.println("No robots owned by player found");
			}
		}
		return null;
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == ROBOT_REMOTE_GUI) {
			int radius = 1;
			// hopefully this works... its possible robots will overlap each
			// other
			List<EntityRobot> robots = world.getEntitiesWithinAABB(EntityRobot.class,
					AxisAlignedBB.fromBounds(x, y, z, x + radius, y + radius, z + radius));
			robots.addAll(world.getEntitiesWithinAABB(DynRobotEntity.class,
					AxisAlignedBB.fromBounds(x, y, z, x + radius, y + radius, z + radius)));
			if (robots.size() > 0) {
				if (robots.get(0).getServerComputer() == null) {
					robots.get(0).createServerComputer().turnOn();
				} else if (!robots.get(0).getServerComputer().isOn()) {
					robots.get(0).getServerComputer().turnOn();
				}
			} else {
				System.out.println("No server robots owned by player found");
			}
		}
		return null;
	}

}
