package com.dyn.robot.proxy;

import com.dyn.robot.api.RobotAPI;
import com.dyn.robot.entity.BlockDynRobot;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.gui.RobotProgrammingInterface;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class Server implements Proxy {

	@Override
	public void createNewProgrammingInterface(EntityRobot robot) {
		// TODO Auto-generated method stub

	}

	@Override
	public RobotProgrammingInterface getProgrammingInterface() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleErrorMessage(String error, String code, int line) {
		// TODO Auto-generated method stub

	}

	
	@Override
	public void init() {
		
	}

	@Override
	public void openRemoteInterface(EntityRobot robot) {
		// TODO Auto-generated method stub

	}

	@Override
	public void openRemoteInterface(World world, BlockDynRobot robot, BlockPos pos) {
		// TODO Auto-generated method stub

	}

	@Override
	public void openRobotGui() {
		// TODO Auto-generated method stub

	}

	@Override
	public void openRobotInterface() {
		// TODO Auto-generated method stub

	}

	@Override
	public void openRobotProgrammingWindow(EntityRobot robot) {

	}

	@Override
	public void preInit() {
		RobotAPI.registerCommands();
	}

	@Override
	public void registerBlockItem(Block block) {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerItem(Item item, String name, int meta) {
		// TODO Auto-generated method stub

	}

	@Override
	public void toggleRenderRobotProgramInterface(boolean state) {
		// TODO Auto-generated method stub

	}

}