package com.dyn.robot;

import com.dyn.robot.entity.BlockDynRobot;
import com.dyn.robot.entity.DynRobotEntity;
import com.dyn.robot.gui.RobotGuiHandler;
import com.dyn.robot.items.ItemDynRobotSpawner;
import com.dyn.robot.items.ItemRemote;
import com.dyn.robot.programs.UserProgramLibrary;
import com.dyn.robot.proxy.Proxy;
import com.dyn.robot.reference.MetaData;
import com.dyn.robot.reference.Reference;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, dependencies = "required-after:dyn|server;after:ComputerCraft")
public class RobotMod {
	@Mod.Instance(Reference.MOD_ID)
	public static RobotMod instance;

	@SidedProxy(modId = Reference.MOD_ID, clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static Proxy proxy;

	public static BlockDynRobot dynRobot;
	public static ItemRemote dynRobotRemote;

	public static void registerNewEntity(Class entityClass, String name, int id) {
		EntityRegistry.registerModEntity(entityClass, name, id, instance, 64, 3, false);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init();
		proxy.registerBlockItem(dynRobot);
		proxy.registerItem(dynRobotRemote, dynRobotRemote.getUnlocalizedName(), 0);
	}

	@Mod.EventHandler
	public void onServerStart(FMLServerStartedEvent event) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			UserProgramLibrary.resetLibraryCache();
		}
	}

	@Mod.EventHandler
	public void onServerStopped(FMLServerStoppedEvent event) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			UserProgramLibrary.resetLibraryCache();
		}
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {

	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MetaData.init(event.getModMetadata());

		registerItems();
		registerBlocks();
		registerNewEntity(DynRobotEntity.class, "dynRobotEntity", 0);

		proxy.preInit();

		NetworkRegistry.INSTANCE.registerGuiHandler(this, new RobotGuiHandler());
	}

	private void registerBlocks() {
		dynRobot = (BlockDynRobot) new BlockDynRobot().setUnlocalizedName("dyn_robot")
				.setCreativeTab(CreativeTabs.tabRedstone);
		GameRegistry.registerBlock(dynRobot, ItemDynRobotSpawner.class, "dyn_robot");
	}

	private void registerItems() {
		dynRobotRemote = (ItemRemote) new ItemRemote().setUnlocalizedName("dyn_robot_remote")
				.setCreativeTab(CreativeTabs.tabRedstone);
		GameRegistry.registerItem(dynRobotRemote, "dyn_robot_remote");
	}
}
