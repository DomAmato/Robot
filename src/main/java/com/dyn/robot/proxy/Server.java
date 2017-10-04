package com.dyn.robot.proxy;

import com.dyn.robot.RobotMod;
import com.dyn.robot.api.RobotAPI;
import com.dyn.robot.blocks.BlockDynRobot;
import com.dyn.robot.entity.DynRobotEntity;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.gui.RobotGuiHandler;
import com.dyn.robot.network.NetworkManager;
import com.dyn.robot.network.messages.RawErrorMessage;

import mobi.omegacentauri.raspberryjammod.RaspberryJamMod;
import mobi.omegacentauri.raspberryjammod.network.CodeEvent;
import mobi.omegacentauri.raspberryjammod.network.CodeEvent.RobotErrorEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class Server implements Proxy {

	@Override
	public void addScheduledTask(Runnable runnable) {
		FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(runnable);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void chatEvent(ServerChatEvent event) {
		if (event.player != null) {
			event.setCanceled(true);
			// only send to players in the same dimension
			for (EntityPlayer player : event.player.worldObj.playerEntities) {
				player.addChatComponentMessage(event.getComponent());
			}
		}
	}

	@SubscribeEvent
	public void codeError(CodeEvent.ErrorEvent event) {
		if (event instanceof RobotErrorEvent) {
			EntityPlayer player = event.getPlayer();
			World world = player.worldObj;
			EntityRobot robot = (EntityRobot) world.getEntityByID(((RobotErrorEvent) event).getEntityId());
			robot.stopExecutingCode();
		}
		NetworkManager.sendTo(new RawErrorMessage(event.getCode(), event.getError(), event.getLine()),
				(EntityPlayerMP) event.getPlayer());
	}

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

	/**
	 * Returns a side-appropriate EntityPlayer for use during message handling
	 */
	@Override
	public EntityPlayer getPlayerEntity(MessageContext ctx) {
		return ctx.getServerHandler().playerEntity;
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
		return ctx.getServerHandler().playerEntity.getServerForPlayer();
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
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void openActivationInterface(World world, BlockDynRobot robot, BlockPos pos) {
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
	public void playSound(String sound) {
		// TODO Auto-generated method stub

	}

	@Override
	public void preInit() {
		RobotAPI.registerCommands();
		NetworkRegistry.INSTANCE.registerGuiHandler(RobotMod.instance, new RobotGuiHandler());
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