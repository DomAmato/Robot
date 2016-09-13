package com.dyn.robot;

import java.util.Random;

import com.dyn.robot.block.BlockDynRobot;
import com.dyn.robot.block.ItemDynRobot;
import com.dyn.robot.block.TileDynRobot;
import com.dyn.robot.entity.DynRobotEntity;
import com.dyn.robot.entity.render.DynRobotRenderer;
import com.dyn.robot.programs.UserProgramLibrary;
import com.dyn.robot.proxy.Proxy;
import com.dyn.robot.reference.MetaData;
import com.dyn.robot.reference.Reference;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
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

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		registerTileEntities();
		proxy.init();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {

	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MetaData.init(event.getModMetadata());
		registerItems();
		registerNewEntity(DynRobotEntity.class, "dynRobotEntity", 0);
		RenderingRegistry.registerEntityRenderingHandler(DynRobotEntity.class, new DynRobotRenderer());
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

	private void registerItems() {

		dynRobot = (BlockDynRobot) new BlockDynRobot().setUnlocalizedName("dyn_robot").setCreativeTab(CreativeTabs.tabRedstone);
		GameRegistry.registerBlock(dynRobot, ItemDynRobot.class, "dyn_robot");
	}

	private void registerTileEntities() {
		GameRegistry.registerTileEntity(TileDynRobot.class, "dyn_robot_tile");
	}

	public static void registerNewEntity(Class entityClass, String name, int id) {
		long seed = name.hashCode();
		Random rand = new Random(seed);
		int primaryColor = rand.nextInt() * 16777215;
		int secondaryColor = rand.nextInt() * 16777215;

		EntityRegistry.registerModEntity(entityClass, name, id, instance, 64, 3, false, primaryColor, secondaryColor);
	}
}
