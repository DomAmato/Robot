package com.dyn.robot.proxy;

import com.dyn.robot.api.RobotAPI;
import com.dyn.robot.entity.BlockDynRobot;
import com.dyn.robot.entity.DynRobotEntity;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.gui.RobotProgrammingInterface;

import mobi.omegacentauri.raspberryjammod.RaspberryJamMod;
import mobi.omegacentauri.raspberryjammod.network.CodeEvent;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Server implements Proxy {

	@Override
	public void createNewProgrammingInterface(EntityRobot robot) {
		// TODO Auto-generated method stub

	}

	@SubscribeEvent
	public void deathEvent(LivingDeathEvent event) {
		if ((event.entity instanceof DynRobotEntity) && ((EntityRobot) event.entity).shouldExecuteCode()) {
			RaspberryJamMod.EVENT_BUS.post(new CodeEvent.FailEvent("Robot was Destroyed", event.entity.getEntityId(),
					((EntityRobot) event.entity).getOwner()));
		}
	}

//	@Override
//	public RobotProgrammingInterface getProgrammingInterface() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public void handleErrorMessage(String error, String code, int line) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {
//		RaspberryJamMod.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(this);
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

	@Override
	public String getProgrammingInterfaceText() {
		// TODO Auto-generated method stub
		return null;
	}

}