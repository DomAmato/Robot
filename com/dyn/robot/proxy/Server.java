package com.dyn.robot.proxy;

import java.util.List;

import com.dyn.robot.entity.DynRobotEntity;
import com.dyn.robot.entity.EntityRobot;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class Server implements Proxy {

	@Override
	public void init() {
		// MinecraftForge.EVENT_BUS.register(this);

	}

	@Override
	public void openRobotGui() {
		// TODO Auto-generated method stub

	}

	@Override
	public void openRobotProgrammingWindow(World world, BlockPos pos, Entity entity) {
		int radius = 5;
		// hopefully this works... its possible robots will overlap each
		// other
		List<EntityRobot> robots = world.getEntitiesWithinAABB(DynRobotEntity.class, AxisAlignedBB.fromBounds(
				pos.getX(), pos.getY(), pos.getZ(), pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius));
		for (EntityRobot robot : robots) {
			System.out.println("dyn robot owners: " + robot.getOwner());
		}
		if (robots.size() > 0) {
			if (robots.get(0).getServerComputer() == null) {
				robots.get(0).createServerComputer().turnOn();
			} else if (!robots.get(0).getServerComputer().isOn()) {
				robots.get(0).getServerComputer().turnOn();
			}
			System.out.println("Found " + robots.size() + " Robots");

		} else {
			System.out.println("No server robots owned by player found");
		}

	}

	@Override
	public void preInit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerBlockItem(Block block) {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerItem(Item item, String name, int meta) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see forge.reference.proxy.Proxy#renderGUI()
	 */
	@Override
	public void renderGUI() {
		// Actions on render GUI for the server (logging)

	}

}