package com.dom.robot;

import java.io.File;

import org.apache.logging.log4j.Logger;

import com.dom.rjm.network.CodeEvent;
import com.dom.rjm.network.SocketEvent;
import com.dom.robot.api.RobotAPI;
import com.dom.robot.blocks.BlockRobot;
import com.dom.robot.blocks.BlockRobotMagnet;
import com.dom.robot.entity.EntityRobot;
import com.dom.robot.entity.SimpleRobotEntity;
import com.dom.robot.items.ItemExpansionChip;
import com.dom.robot.items.ItemMemoryCard;
import com.dom.robot.items.ItemMemoryStick;
import com.dom.robot.items.ItemMemoryWipe;
import com.dom.robot.items.ItemRecombobulator;
import com.dom.robot.items.ItemReferenceManual;
import com.dom.robot.items.ItemRemote;
import com.dom.robot.items.ItemRobotWhistle;
import com.dom.robot.items.ItemSimpleRobotBlock;
import com.dom.robot.items.ItemSimpleRobotSpawner;
import com.dom.robot.items.ItemWrench;
import com.dom.robot.network.NetworkManager;
import com.dom.robot.network.messages.CodeExecutionEndedMessage;
import com.dom.robot.network.messages.RawErrorMessage;
import com.dom.robot.proxy.Proxy;
import com.dom.robot.reference.MetaData;
import com.dom.robot.reference.Reference;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION)
public class RobotMod {
	@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
	public static class RegistrationHandler {
		private static int entityID = 0;

		private static <E extends Entity> EntityEntryBuilder<E> createBuilder(final String name) {
			final EntityEntryBuilder<E> builder = EntityEntryBuilder.create();
			final ResourceLocation registryName = new ResourceLocation(Reference.MOD_ID, name);
			return builder.id(registryName, RegistrationHandler.entityID++).name(name);

		}

		@SubscribeEvent
		public static void registerEntities(final RegistryEvent.Register<EntityEntry> event) {
			EntityEntry result = RegistrationHandler.createBuilder("robot").entity(SimpleRobotEntity.class)
					.tracker(64, 3, false).build();
			event.getRegistry().register(result);
		}

		@SubscribeEvent
		public static void registerItemBlocks(final RegistryEvent.Register<Item> event) {
			ItemBlock roblock = new ItemSimpleRobotBlock(RobotMod.robot_block);
			final Block block = roblock.getBlock();
			final ResourceLocation registryName = Preconditions.checkNotNull(block.getRegistryName(),
					"Block %s has null registry name", block);
			event.getRegistry().register(roblock.setRegistryName(registryName));

			ItemBlock robmag = new ItemBlock(RobotMod.robot_magent);
			final Block robmagblock = robmag.getBlock();
			final ResourceLocation robmagregistryName = Preconditions.checkNotNull(robmagblock.getRegistryName(),
					"Block %s has null registry name", robmagblock);
			event.getRegistry().register(robmag.setRegistryName(robmagregistryName));
		}

		@SubscribeEvent
		public static void registerItems(final RegistryEvent.Register<Item> event) {
			event.getRegistry().register(RobotMod.robot_remote);
			event.getRegistry().register(RobotMod.robot_wrench);
			event.getRegistry().register(RobotMod.robot_spawner);
			event.getRegistry().register(RobotMod.expChip);
			event.getRegistry().register(RobotMod.card);
			event.getRegistry().register(RobotMod.ram);
			event.getRegistry().register(RobotMod.whistle);
			event.getRegistry().register(RobotMod.neuralyzer);
			event.getRegistry().register(RobotMod.printer);
			event.getRegistry().register(RobotMod.manual);
		}

		@SubscribeEvent
		public void registerBlocks(RegistryEvent.Register<Block> event) {
			event.getRegistry().register(RobotMod.robot_block);
			event.getRegistry().register(RobotMod.robot_magent);
		}
	}

	@Mod.Instance(Reference.MOD_ID)
	public static RobotMod instance;

	@SidedProxy(modId = Reference.MOD_ID, clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static Proxy proxy;

	public static CreativeTabs roboTab = new RoboTab();
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
				Class.forName("com.dom.rjm.RaspberryJamMod");
				RobotAPI.registerCommands();
				MinecraftForge.EVENT_BUS.register(this);
			} catch (ClassNotFoundException er) {
				// this is just to make sure rjm exists
			}
		}
	}

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
