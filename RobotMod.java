package com.dyn.robot;

import java.util.Map;

import com.dyn.fixins.tab.RoboTab;
import com.dyn.robot.entity.BlockDynRobot;
import com.dyn.robot.entity.DynRobotEntity;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.items.ItemDynRobotBlock;
import com.dyn.robot.items.ItemDynRobotSpawner;
import com.dyn.robot.items.ItemRemote;
import com.dyn.robot.proxy.Proxy;
import com.dyn.robot.reference.MetaData;
import com.dyn.robot.reference.Reference;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, dependencies = "required-after:dyn|server")
public class RobotMod {
	@Mod.Instance(Reference.MOD_ID)
	public static RobotMod instance;

	@SidedProxy(modId = Reference.MOD_ID, clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static Proxy proxy;

	public static BlockDynRobot dynRobot;
	public static ItemRemote dynRobotRemote;
	public static ItemDynRobotSpawner robotSpawner;

	public static CreativeTabs roboTab = new RoboTab();

	// server
	public static Map<Integer, Boolean> robotEcho = Maps.newHashMap();
	public static BiMap<Integer, EntityPlayer> robotid2player = HashBiMap.create();

	// client
	public static EntityRobot currentRobot;

	public static void registerNewEntity(Class entityClass, String name, int id) {
		EntityRegistry.registerModEntity(entityClass, name, id, instance, 64, 3, false);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init();
		proxy.registerBlockItem(dynRobot);
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
	}

	private void registerBlocks() {
		dynRobot = (BlockDynRobot) new BlockDynRobot().setUnlocalizedName("dyn_robot").setCreativeTab(roboTab);
		GameRegistry.registerBlock(dynRobot, ItemDynRobotBlock.class, "dyn_robot");
	}

	private void registerItems() {
		dynRobotRemote = (ItemRemote) new ItemRemote().setUnlocalizedName("dyn_robot_remote").setCreativeTab(roboTab);
		GameRegistry.registerItem(dynRobotRemote, "dyn_robot_remote");
		proxy.registerItem(dynRobotRemote, dynRobotRemote.getUnlocalizedName(), 0);

		robotSpawner = (ItemDynRobotSpawner) new ItemDynRobotSpawner().setUnlocalizedName("dyn_robot_spawn");
		GameRegistry.registerItem(robotSpawner, "dyn_robot_spawn");
		proxy.registerItem(robotSpawner, robotSpawner.getUnlocalizedName(), 0);
	}
}
