package com.dyn.robot.gui;

import java.util.List;
import java.util.UUID;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.entity.inventory.RobotChipContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

/**
 * User: brandon3055 Date: 06/01/2015
 *
 * This class is used to get the client and server gui elements when a player
 * opens a gui. There can only be one registered IGuiHandler instance handler
 * per mod.
 */
public class RobotGuiHandler implements IGuiHandler {
	private static final int ACTIVATING = 1;
	private static final int SEARCHING = 2;

	public static int getActivationGuiID() {
		return RobotGuiHandler.ACTIVATING;
	}

	public static int getSearchingGuiID() {
		return RobotGuiHandler.SEARCHING;
	}

	// Gets the client side element for the given gui id this should return a
	// gui
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if ((ID != RobotGuiHandler.getActivationGuiID()) && (ID != RobotGuiHandler.getSearchingGuiID())) {
			RobotMod.logger.error("Invalid ID: expected " + RobotGuiHandler.getActivationGuiID() + " or "
					+ RobotGuiHandler.getSearchingGuiID() + ", received " + ID);
		}

		switch (ID) {
		case ACTIVATING:
			for (EntityRobot robot : getEntitiesInRadius(world, x, y, z, 1)) {
				return new RemoteInterface(player.inventory, robot);
			}
			RobotMod.logger.error("No Robots found in radius");
			break;
		case SEARCHING:
			for (EntityRobot robot : getEntitiesInRadius(world, x, y, z, 32)) {
				if ((robot.getOwner() == player) || UUID.fromString(robot.getOwnerId()).equals(player.getUniqueID())) {
					return new RemoteInterface(player.inventory, robot);
				}
			}
			RobotMod.logger.error("No Robots belonging to player found in radius");
			break;
		}

		return null;

	}

	public List<EntityRobot> getEntitiesInRadius(World world, double x, double y, double z, int radius) {
		List<EntityRobot> list = world.getEntitiesWithinAABB(EntityRobot.class,
				AxisAlignedBB.fromBounds(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius));
		return list;
	}

	// Gets the server side element for the given gui id this should return a
	// container
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if ((ID != RobotGuiHandler.getActivationGuiID()) && (ID != RobotGuiHandler.getSearchingGuiID())) {
			RobotMod.logger.error("Invalid ID: expected " + RobotGuiHandler.getActivationGuiID() + " or "
					+ RobotGuiHandler.getSearchingGuiID() + ", received " + ID);
		}
		switch (ID) {
		case ACTIVATING:
			for (EntityRobot robot : getEntitiesInRadius(world, x, y, z, 1)) {
				if (robot.getOwner() == player) {
					return new RobotChipContainer(player.inventory, robot.robot_inventory, robot, player);
				}
			}
			break;
		case SEARCHING:
			for (EntityRobot robot : getEntitiesInRadius(world, x, y, z, 32)) {
				if (robot.getOwner() == player) {
					return new RobotChipContainer(player.inventory, robot.robot_inventory, robot, player);
				}
			}
			break;
		}
		return null;
	}

}