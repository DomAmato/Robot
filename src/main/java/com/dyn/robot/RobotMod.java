package com.dyn.robot;

import java.io.File;

import org.apache.logging.log4j.Logger;

import com.dyn.rjm.network.CodeEvent;
import com.dyn.rjm.network.SocketEvent;
import com.dyn.robot.api.RobotAPI;
import com.dyn.robot.blocks.BlockRobot;
import com.dyn.robot.blocks.BlockRobotMagnet;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.entity.SimpleRobotEntity;
import com.dyn.robot.items.ItemExpansionChip;
import com.dyn.robot.items.ItemMemoryCard;
import com.dyn.robot.items.ItemMemoryStick;
import com.dyn.robot.items.ItemMemoryWipe;
import com.dyn.robot.items.ItemRecombobulator;
import com.dyn.robot.items.ItemReferenceManual;
import com.dyn.robot.items.ItemRemote;
import com.dyn.robot.items.ItemRobotWhistle;
import com.dyn.robot.items.ItemSimpleRobotSpawner;
import com.dyn.robot.items.ItemWrench;
import com.dyn.robot.network.NetworkManager;
import com.dyn.robot.network.messages.CodeExecutionEndedMessage;
import com.dyn.robot.network.messages.RawErrorMessage;
import com.dyn.robot.proxy.Proxy;
import com.dyn.robot.reference.MetaData;
import com.dyn.robot.reference.Reference;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION)
public class RobotMod {

	@Mod.Instance(Reference.MOD_ID)
	public static RobotMod instance;

	@SidedProxy(modId = Reference.MOD_ID, clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static Proxy proxy;

	public static final RoboTab roboTab = new RoboTab();
	public static final BlockRobot robot_block = new BlockRobot();

	public static final BlockRobotMagnet robot_magent = new BlockRobotMagnet();
	public static final ItemRemote robot_remote = new ItemRemote();
	public static final ItemWrench robot_wrench = new ItemWrench();
	public static final ItemSimpleRobotSpawner robot_spawner = new ItemSimpleRobotSpawner();
	public static final ItemExpansionChip expChip = new ItemExpansionChip();
	public static final ItemMemoryCard card = new ItemMemoryCard();
	public static final ItemMemoryStick ram = new ItemMemoryStick();
	public static final ItemRobotWhistle whistle = new ItemRobotWhistle();
	public static final ItemMemoryWipe neuralyzer = new ItemMemoryWipe();

	public static final ItemRecombobulator printer = new ItemRecombobulator(Item.ToolMaterial.DIAMOND);
	public static final ItemReferenceManual manual = new ItemReferenceManual();

	public static File scriptsLoc;

	public static Logger logger;

	// server
	public static BiMap<Integer, EntityPlayer> robotid2player = HashBiMap.create();

	// client
	public static EntityRobot currentRobot;

	@SubscribeEvent
	public void codeError(CodeEvent.ErrorEvent event) {
		if (event instanceof CodeEvent.RobotErrorEvent) {
			EntityPlayer player = event.getPlayer();
			World world = player.world;
			EntityRobot robot = (EntityRobot) world.getEntityByID(((CodeEvent.RobotErrorEvent) event).getEntityId());
			robot.stopExecutingCode();
		}
		NetworkManager.sendTo(new RawErrorMessage(event.getCode(), event.getError(), event.getLine()),
				(EntityPlayerMP) event.getPlayer());
	}

	@SubscribeEvent
	public void deathEvent(LivingDeathEvent event) {
		if ((event.getEntity() instanceof SimpleRobotEntity) && ((EntityRobot) event.getEntity()).shouldExecuteCode()) {
			MinecraftForge.EVENT_BUS.post(new CodeEvent.FailEvent("Robot was Destroyed",
					event.getEntity().getEntityId(), ((EntityRobot) event.getEntity()).getOwner()));
		}
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		RobotMod.proxy.init();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {

	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		RobotMod.logger = event.getModLog();
		MetaData.init(event.getModMetadata());

		NetworkManager.registerMessages();
		NetworkManager.registerPackets();

		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {

			RobotMod.scriptsLoc = new File(net.minecraft.client.Minecraft.getMinecraft().mcDataDir, "python_scripts");

			if (!RobotMod.scriptsLoc.exists()) {
				RobotMod.scriptsLoc.mkdir();
			}
		} else {
			// only do this server side
			RobotMod.scriptsLoc = new File(FMLCommonHandler.instance().getMinecraftServerInstance().getDataDirectory(),
					"python_scripts");

			if (!RobotMod.scriptsLoc.exists()) {
				RobotMod.scriptsLoc.mkdir();
			}
		}

		RobotMod.proxy.preInit();
	}

	// this only fires for the client if its internal
	@Mod.EventHandler
	public void serverStarted(FMLServerStartingEvent e) {
		if (e.getSide() == Side.CLIENT) {
			try {
				Class.forName("com.dyn.rjm.RaspberryJamMod");
				RobotAPI.registerCommands();
				MinecraftForge.EVENT_BUS.register(this);
			} catch (ClassNotFoundException er) {
				// this is just to make sure rjm exists
			}
		}
	}

	@SubscribeEvent
	public void socketClose(SocketEvent.Close event) {
		if (RobotMod.robotid2player.inverse().containsKey(event.getPlayer())) {
			World world = event.getPlayer().world;
			EntityRobot robot = (EntityRobot) world
					.getEntityByID(RobotMod.robotid2player.inverse().get(event.getPlayer()));
			RobotMod.logger.info("Stop Executing Code from Socket Message for Player: " + event.getPlayer().getName());
			if (robot != null) {
				robot.stopExecutingCode();
			}
		}
		NetworkManager.sendTo(new CodeExecutionEndedMessage("Complete"), (EntityPlayerMP) event.getPlayer());
	}
}
