package com.dyn.robot.gui;

import java.util.List;

import com.dyn.DYNServerMod;
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
	private static final int GUIID = 1;

	public static int getGuiID() {
		return GUIID;
	}

	// Gets the client side element for the given gui id this should return a
	// gui
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID != getGuiID()) {
			DYNServerMod.logger.error("Invalid ID: expected " + getGuiID() + ", received " + ID);
		}

		for (EntityRobot robot : getEntitiesInRadius(world, x, y, z, 32)) {
			return new RemoteInterface(player.inventory, robot);
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
		if (ID != getGuiID()) {
			DYNServerMod.logger.error("Invalid ID: expected " + getGuiID() + ", received " + ID);
		}

		for (EntityRobot robot : getEntitiesInRadius(world, x, y, z, 32)) {
			if (robot.getOwner() == player) {
				return new RobotChipContainer(player.inventory, robot.robot_inventory, robot, player);
			}
		}
		return null;
	}

}