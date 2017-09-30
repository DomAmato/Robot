package com.dom.robot.proxy;

import com.dom.robot.blocks.BlockRobot;
import com.dom.robot.entity.EntityRobot;

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

	public void openRobotGui();

	public void openRobotProgrammingWindow();

	public void openRobotProgrammingWindow(EntityRobot robot);

	public void preInit();

	public void toggleRenderRobotProgramInterface(boolean state);
}