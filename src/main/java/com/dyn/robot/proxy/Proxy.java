package com.dyn.robot.proxy;

import com.dyn.robot.blocks.BlockDynRobot;
import com.dyn.robot.entity.EntityRobot;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public interface Proxy {

	public void addScheduledTask(Runnable runnable);

	public void createNewProgrammingInterface(EntityRobot robot);

	/**
	 * Returns a side-appropriate EntityPlayer for use during message handling
	 */
	public EntityPlayer getPlayerEntity(MessageContext ctx);

	public String getProgrammingInterfaceText();

	public IThreadListener getThreadFromContext(MessageContext ctx);

	public void handleCodeExecutionEnded();

	public void handleErrorMessage(String error, String code, int line);

	public void init();

	public void openActivationInterface(World world, BlockDynRobot robot, BlockPos pos);

	public void openRobotGui();

	public void openRobotProgrammingWindow();

	public void openRobotProgrammingWindow(EntityRobot robot);

	public void playSound(String sound);

	public void preInit();

	public void registerBlockItem(Block block);

	public void registerItem(Item item, String name, int meta);

	public void toggleRenderRobotProgramInterface(boolean state);
}