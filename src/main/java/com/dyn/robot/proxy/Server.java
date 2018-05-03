package com.dyn.robot.proxy;

import java.util.List;

import com.dyn.robot.RobotMod;
import com.dyn.robot.blocks.BlockRobot;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.gui.RobotGuiHandler;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class Server implements Proxy {

	@Override
	public void addScheduledTask(Runnable runnable) {
		FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(runnable);
	}

	@Override
	public void createNewProgrammingInterface(EntityRobot robot) {
		// TODO Auto-generated method stub

	}

	/**
	 * Returns a side-appropriate EntityPlayer for use during message handling
	 */
	@Override
	public EntityPlayer getplayer(MessageContext ctx) {
		return ctx.getServerHandler().player;
	}

	@Override
	public String getProgrammingInterfaceText() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Returns the current thread based on side during message handling, used for
	 * ensuring that the message is being handled by the main thread
	 */
	@Override
	public IThreadListener getThreadFromContext(MessageContext ctx) {
		return ctx.getServerHandler().player.getServer();
	}

	@Override
	public void handleCodeExecutionEnded() {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleErrorMessage(String error, String code, int line) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {

	}

	@Override
	public void openActivationInterface(EntityRobot entityRobot) {
		// TODO Auto-generated method stub

	}

	@Override
	public void openActivationInterface(World world, BlockRobot robot, BlockPos pos) {
		// TODO Auto-generated method stub

	}

	@Override
	public void openMagnetGui(BlockPos pos, IBlockState state, List<EntityRobot> robots) {
		// TODO Auto-generated method stub

	}

	@Override
	public void openRemoteGui() {
		// TODO Auto-generated method stub

	}

	@Override
	public void openRobotGui() {
		// TODO Auto-generated method stub

	}

	@Override
	public void openRobotProgrammingWindow() {
		// TODO Auto-generated method stub

	}

	@Override
	public void openRobotProgrammingWindow(EntityRobot robot) {

	}

	@Override
	public void preInit() {
		NetworkRegistry.INSTANCE.registerGuiHandler(RobotMod.instance, new RobotGuiHandler());
	}

	@Override
	public void toggleRenderRobotProgramInterface(boolean state) {
		// TODO Auto-generated method stub

	}

}