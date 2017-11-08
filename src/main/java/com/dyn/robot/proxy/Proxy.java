package com.dyn.robot.proxy;

import java.util.List;

import com.dyn.robot.blocks.BlockRobot;
import com.dyn.robot.entity.EntityRobot;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public interface Proxy {
	public void addScheduledTask(Runnable runnable);

	public void createNewProgrammingInterface(EntityRobot robot);

	public EntityPlayer getplayer(MessageContext ctx);

	public String getProgrammingInterfaceText();

	public IThreadListener getThreadFromContext(MessageContext ctx);

	public void handleCodeExecutionEnded();

	public void handleErrorMessage(String error, String code, int line);

	public void init();

	public void openActivationInterface(World world, BlockRobot robot, BlockPos pos);

	public void openMagnetGui(BlockPos pos, IBlockState state, List<EntityRobot> robots);

	public void openRemoteGui();

	public void openRobotGui();

	public void openRobotProgrammingWindow();

	public void openRobotProgrammingWindow(EntityRobot robot);

	public void preInit();

	public void toggleRenderRobotProgramInterface(boolean state);

	public void openActivationInterface(EntityRobot entityRobot);
}