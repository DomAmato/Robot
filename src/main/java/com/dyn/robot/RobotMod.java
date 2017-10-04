package com.dyn.robot;

import java.io.File;

import org.apache.logging.log4j.Logger;

import com.dyn.robot.api.RobotAPI;
import com.dyn.robot.blocks.BlockDynRobot;
import com.dyn.robot.blocks.BlockRobotMagnet;
import com.dyn.robot.entity.DynRobotEntity;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.items.ItemDynRobotBlock;
import com.dyn.robot.items.ItemDynRobotSpawner;
import com.dyn.robot.items.ItemExpansionChip;
import com.dyn.robot.items.ItemMemoryCard;
import com.dyn.robot.items.ItemMemoryStick;
import com.dyn.robot.items.ItemMemoryWipe;
import com.dyn.robot.items.ItemRecombobulator;
import com.dyn.robot.items.ItemRemote;
import com.dyn.robot.items.ItemRobotWhistle;
import com.dyn.robot.items.ItemWrench;
import com.dyn.robot.items.RoboTab;
import com.dyn.robot.network.NetworkManager;
import com.dyn.robot.proxy.Proxy;
import com.dyn.robot.reference.MetaData;
import com.dyn.robot.reference.Reference;
import com.dyn.robot.util.PlayerAccessLevel;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, dependencies = "after:raspberryjammod")
public class RobotMod {
	@Mod.Instance(Reference.MOD_ID)
	public static RobotMod instance;

	@SidedProxy(modId = Reference.MOD_ID, clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static Proxy proxy;

	public static BlockDynRobot dynRobot;
	public static BlockRobotMagnet dynRobotMagnet;

	public static ItemRemote dynRobotRemote;
	public static ItemWrench dynRobotWrench;
	public static ItemDynRobotSpawner robotSpawner;
	public static ItemExpansionChip expChip;
	public static ItemMemoryCard card;
	public static ItemMemoryStick ram;
	public static ItemRobotWhistle whistle;
	public static ItemMemoryWipe neuralyzer;
	public static ItemRecombobulator printer;

	public static CreativeTabs roboTab = new RoboTab();

	public static File scriptsLoc;

	// server
	public static BiMap<Integer, EntityPlayer> robotid2player = HashBiMap.create();

	// client
	public static EntityRobot currentRobot;

	public static PlayerAccessLevel accessLevel = PlayerAccessLevel.ADMIN;

	public static Logger logger;

	public static void registerNewEntity(Class entityClass, String name, int id) {
		EntityRegistry.registerModEntity(entityClass, name, id, RobotMod.instance, 64, 3, false);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		RobotMod.proxy.init();

		RobotMod.proxy.registerBlockItem(RobotMod.dynRobot);
		RobotMod.proxy.registerBlockItem(RobotMod.dynRobotMagnet);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {

	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MetaData.init(event.getModMetadata());

		RobotMod.logger = event.getModLog();

		NetworkManager.registerPackets();
		NetworkManager.registerMessages();

		registerItems();
		registerBlocks();

		RobotMod.registerNewEntity(DynRobotEntity.class, "dynRobotEntity", 0);

		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {

			RobotMod.scriptsLoc = new File(net.minecraft.client.Minecraft.getMinecraft().mcDataDir, "python_scripts");

			if (!RobotMod.scriptsLoc.exists()) {
				RobotMod.scriptsLoc.mkdir();
			}
		} else {
			// only do this server side
			RobotMod.scriptsLoc = new File(net.minecraft.server.MinecraftServer.getServer().getDataDirectory(),
					"python_scripts");

			if (!RobotMod.scriptsLoc.exists()) {
				RobotMod.scriptsLoc.mkdir();
			}
		}

		RobotMod.proxy.preInit();
	}

	private void registerBlocks() {
		RobotMod.dynRobot = (BlockDynRobot) new BlockDynRobot().setUnlocalizedName("dyn_robot")
				.setCreativeTab(RobotMod.roboTab);
		GameRegistry.registerBlock(RobotMod.dynRobot, ItemDynRobotBlock.class, "dyn_robot");

		RobotMod.dynRobotMagnet = (BlockRobotMagnet) new BlockRobotMagnet().setUnlocalizedName("dyn_robot_magnet")
				.setCreativeTab(RobotMod.roboTab);
		GameRegistry.registerBlock(RobotMod.dynRobotMagnet, ItemBlock.class, "dyn_robot_magnet");
	}

	private void registerItems() {
		RobotMod.dynRobotRemote = (ItemRemote) new ItemRemote().setUnlocalizedName("dyn_robot_remote")
				.setCreativeTab(RobotMod.roboTab);
		GameRegistry.registerItem(RobotMod.dynRobotRemote, "dyn_robot_remote");
		RobotMod.proxy.registerItem(RobotMod.dynRobotRemote, RobotMod.dynRobotRemote.getUnlocalizedName(), 0);

		RobotMod.dynRobotWrench = (ItemWrench) new ItemWrench().setUnlocalizedName("dyn_robot_wrench");
		GameRegistry.registerItem(RobotMod.dynRobotWrench, "dyn_robot_wrench");
		RobotMod.proxy.registerItem(RobotMod.dynRobotWrench, RobotMod.dynRobotWrench.getUnlocalizedName(), 0);

		RobotMod.robotSpawner = (ItemDynRobotSpawner) new ItemDynRobotSpawner().setUnlocalizedName("dyn_robot_spawn");
		GameRegistry.registerItem(RobotMod.robotSpawner, "dyn_robot_spawn");
		RobotMod.proxy.registerItem(RobotMod.robotSpawner, RobotMod.robotSpawner.getUnlocalizedName() + "_plus", 0);
		RobotMod.proxy.registerItem(RobotMod.robotSpawner, RobotMod.robotSpawner.getUnlocalizedName(), 1);

		RobotMod.expChip = (ItemExpansionChip) new ItemExpansionChip().setUnlocalizedName("expansion_chip");
		GameRegistry.registerItem(RobotMod.expChip, "expansion_chip");
		for (int i = 0; i < 16; i++) {
			RobotMod.proxy.registerItem(RobotMod.expChip, RobotMod.expChip.getUnlocalizedName() + "_" + i, i);
		}

		RobotMod.card = (ItemMemoryCard) new ItemMemoryCard().setUnlocalizedName("dyn_robot_card");
		GameRegistry.registerItem(RobotMod.card, "dyn_robot_card");
		RobotMod.proxy.registerItem(RobotMod.card, RobotMod.card.getUnlocalizedName(), 0);

		RobotMod.ram = (ItemMemoryStick) new ItemMemoryStick().setUnlocalizedName("dyn_robot_memory");
		GameRegistry.registerItem(RobotMod.ram, "dyn_robot_memory");
		for (int i = 0; i < 8; i++) {
			RobotMod.proxy.registerItem(RobotMod.ram, RobotMod.ram.getUnlocalizedName() + "_" + i, i);
		}

		RobotMod.whistle = (ItemRobotWhistle) new ItemRobotWhistle().setUnlocalizedName("dyn_robot_whistle")
				.setCreativeTab(RobotMod.roboTab);
		GameRegistry.registerItem(RobotMod.whistle, "dyn_robot_whistle");
		RobotMod.proxy.registerItem(RobotMod.whistle, RobotMod.whistle.getUnlocalizedName(), 0);

		RobotMod.neuralyzer = (ItemMemoryWipe) new ItemMemoryWipe().setUnlocalizedName("dyn_robot_wipe")
				.setCreativeTab(RobotMod.roboTab);
		GameRegistry.registerItem(RobotMod.neuralyzer, "dyn_robot_wipe");
		RobotMod.proxy.registerItem(RobotMod.neuralyzer, RobotMod.neuralyzer.getUnlocalizedName(), 0);

		RobotMod.printer = (ItemRecombobulator) new ItemRecombobulator(Item.ToolMaterial.EMERALD)
				.setUnlocalizedName("dyn_robot_printer").setCreativeTab(RobotMod.roboTab);
		GameRegistry.registerItem(RobotMod.printer, "dyn_robot_printer");
		RobotMod.proxy.registerItem(RobotMod.printer, RobotMod.printer.getUnlocalizedName(), 0);

		if (RobotMod.accessLevel != PlayerAccessLevel.STUDENT) {
			RobotMod.dynRobotWrench.setCreativeTab(RobotMod.roboTab);
			RobotMod.robotSpawner.setCreativeTab(RobotMod.roboTab);
			RobotMod.expChip.setCreativeTab(RobotMod.roboTab);
			RobotMod.card.setCreativeTab(RobotMod.roboTab);
			RobotMod.ram.setCreativeTab(RobotMod.roboTab);
		}
	}

	// this only fires for the client if its internal
	@Mod.EventHandler
	public void serverStarted(FMLServerStartingEvent e) {
		try {
			Class.forName("mobi.omegacentauri.raspberryjammod.RaspberryJamMod");
			RobotAPI.registerCommands();
		} catch (ClassNotFoundException er) {
			// this is just to make sure rjm exists
		}
	}
}
