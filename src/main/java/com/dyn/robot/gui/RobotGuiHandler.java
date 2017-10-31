package com.dyn.robot.gui;

import java.util.List;

import com.dyn.robot.RobotMod;
import com.dyn.robot.api.RobotAPI;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.entity.inventory.RobotChipContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
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
	// Gets the client side element for the given gui id this should return a
	// gui
	public List<EntityRobot> getEntitiesInRadius(World world, double x, double y, double z, int radius) {
		List<EntityRobot> list = world.getEntitiesWithinAABB(EntityRobot.class,
				new AxisAlignedBB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius));
		return list;
	}
	
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		EntityRobot robot = (EntityRobot) world.getEntityByID(ID);
		//the problem of checking if this is the owner is on activation that data has not synced yet
		//and causing a crash in the GL context
		if (robot != null) {
			return new RobotInventoryScreen(player.inventory, robot);
		}
		RobotMod.logger.error("<Client> Invalid ID: Could not find robot with id " + ID);
		
		return null;

	}

	// Gets the server side element for the given gui id this should return a
	// container
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		EntityRobot robot = (EntityRobot) world.getEntityByID(ID);
		if (robot != null && robot.getOwner() == player) {
			return new RobotChipContainer(player.inventory, robot.robot_inventory, robot, player);
		}
		RobotMod.logger.error("<Server> Invalid ID: Could not find robot with that id");

		return null;
	}

}