package com.dyn.robot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.Logger;

import com.dyn.robot.api.APIHandler;
import com.dyn.robot.api.APIServer;
import com.dyn.robot.api.Python2MinecraftApi;
import com.dyn.robot.api.RobotAPI;
import com.dyn.robot.blocks.BlockRobot;
import com.dyn.robot.blocks.BlockRobotMagnet;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.entity.SimpleRobotEntity;
import com.dyn.robot.items.ItemExpansionChip;
import com.dyn.robot.items.ItemMemoryCard;
import com.dyn.robot.items.ItemMemoryStick;
import com.dyn.robot.items.ItemMemoryWipe;
import com.dyn.robot.items.ItemRedstoneMeter;
import com.dyn.robot.items.ItemReferenceManual;
import com.dyn.robot.items.ItemRemote;
import com.dyn.robot.items.ItemRobotWhistle;
import com.dyn.robot.items.ItemSIMCard;
import com.dyn.robot.items.ItemSimpleRobotSpawner;
import com.dyn.robot.items.ItemWrench;
import com.dyn.robot.items.RoboTab;
import com.dyn.robot.network.CodeEvent;
import com.dyn.robot.network.NetworkManager;
import com.dyn.robot.network.SocketEvent;
import com.dyn.robot.network.messages.RobotErrorMessage;
import com.dyn.robot.proxy.Proxy;
import com.dyn.robot.python.RobotScript;
import com.dyn.robot.reference.Reference;
import com.dyn.robot.utils.FileUtils;
import com.dyn.robot.utils.PathUtility;
import com.dyn.robot.utils.SimpleItemStack;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTippedArrow;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = "@VERSION")
public class RobotMod {

	@Mod.Instance(Reference.MOD_ID)
	public static RobotMod instance;

	@SidedProxy(modId = Reference.MOD_ID, clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static Proxy proxy;

	public static final RoboTab roboTab = new RoboTab();

	public static BlockRobot robot_block = new BlockRobot();
	public static BlockRobotMagnet robot_magent = new BlockRobotMagnet();

	public static final ItemRemote robot_remote = new ItemRemote();
	public static final ItemWrench robot_wrench = new ItemWrench();
	public static final ItemSimpleRobotSpawner robot_spawner = new ItemSimpleRobotSpawner();
	public static final ItemExpansionChip expChip = new ItemExpansionChip();
	public static final ItemMemoryCard card = new ItemMemoryCard();
	public static final ItemSIMCard sim_card = new ItemSIMCard();
	public static final ItemRedstoneMeter meter = new ItemRedstoneMeter();
	public static final ItemMemoryStick ram = new ItemMemoryStick();
	public static final ItemRobotWhistle whistle = new ItemRobotWhistle();
	public static final ItemMemoryWipe neuralyzer = new ItemMemoryWipe();
	public static final ItemReferenceManual manual = new ItemReferenceManual();

	public static final SoundEvent ROBOT_ON = RobotMod.createSoundEvent("robot.on");
	public static final SoundEvent ROBOT_REMOTE = RobotMod.createSoundEvent("robot.remote");
	public static final SoundEvent ROBOT_ERROR = RobotMod.createSoundEvent("robot.error");
	public static final SoundEvent ROBOT_HARSH = RobotMod.createSoundEvent("robot.harsh");
	public static final SoundEvent ROBOT_BEEP = RobotMod.createSoundEvent("robot.beep");
	public static final SoundEvent ROBOT_WHISTLE = RobotMod.createSoundEvent("robot.whistle");

	public static File scriptsLoc;
	public static File apiFileLocation;

	// config options
	public static Configuration configFile;
	public static boolean concurrent = true;
	public static boolean useSystemPath = true;
	public static String pythonInterpreter = "python";
	public static String pythonEmbeddedLocation = "emb-python";
	public static boolean integrated = true;
	public static volatile boolean apiActive = false;
	public static String apiLocation = "roboticraft/api";

	// websocket stuff
	public static int portNumber = 4711;
	public static int wsPort = 14711;
	public static boolean searchForPort = false;
	public static int currentPortNumber;
	public static String serverAddress = null;

	// currently running scripts
	public static Map<Integer, RobotScript> runningProcesses = Maps.newHashMap();

	// an easier way to index recipes
	public static Map<SimpleItemStack, List<IRecipe>> recipeMap = Maps.newHashMap();

	public static Logger logger;

	private static List<APIHandler> apiHandlers = new ArrayList<>();

	// client
	public static List<EntityRobot> currentRobots = new ArrayList();

	public static Pattern pythonImportRegex;

	public static boolean saveScripts;

	public static String scriptFolder;

	/**
	 * Create a {@link SoundEvent}.
	 *
	 * @param soundName
	 *            The SoundEvent's name without the testmod3 prefix
	 * @return The SoundEvent
	 */
	private static SoundEvent createSoundEvent(final String soundName) {
		final ResourceLocation soundID = new ResourceLocation(Reference.MOD_ID, soundName);
		return new SoundEvent(soundID).setRegistryName(soundID);
	}

	public static void registerAPIHandler(APIHandler h) {
		RobotMod.apiHandlers.add(h);
	}

	public static void synchronizeConfig() {
		RobotMod.portNumber = RobotMod.configFile.getInt("Port Number", Configuration.CATEGORY_GENERAL, 4711, 0, 65535,
				"Port number");
		RobotMod.wsPort = RobotMod.configFile.getInt("Websocket Port", Configuration.CATEGORY_GENERAL, 14711, 0, 65535,
				"Websocket port");
		RobotMod.searchForPort = RobotMod.configFile.getBoolean("Port Search if Needed", Configuration.CATEGORY_GENERAL,
				false, "Port search if needed");
		RobotMod.concurrent = RobotMod.configFile.getBoolean("Multiple Connections", Configuration.CATEGORY_GENERAL,
				true, "Multiple connections");
		RobotMod.useSystemPath = RobotMod.configFile.getBoolean("Search System Path", Configuration.CATEGORY_GENERAL,
				true, "Search for python on the system path or use a local embedded version");
		RobotMod.pythonEmbeddedLocation = RobotMod.configFile.getString("Embedded Python Location",
				Configuration.CATEGORY_GENERAL, "emb-python",
				"Relative to .minecraft folder or server jar, only works on Windows");
		RobotMod.pythonInterpreter = RobotMod.configFile.getString("Python Interpreter", Configuration.CATEGORY_GENERAL,
				"python", "Python interpreter");
		RobotMod.apiLocation = RobotMod.configFile.getString("Minecraft Python API Directory",
				Configuration.CATEGORY_GENERAL, "roboticraft/api", "Relative to .minecraft folder or server jar");
		RobotMod.pythonImportRegex = Pattern.compile(
				RobotMod.configFile.getString("A regular expression to filter import statements",
						Configuration.CATEGORY_GENERAL, "^(from|import)\\s+(?!(api).*).*",
						"Instead of requiring a bunch of setup for a sandbox just remove non api imports"),
				Pattern.MULTILINE);
		RobotMod.scriptFolder = RobotMod.configFile.getString("Minecraft Python Saved Script Directory",
				Configuration.CATEGORY_GENERAL, "python_scripts", "Relative to .minecraft folder or server jar");
		RobotMod.saveScripts = RobotMod.configFile.getBoolean("Save Scripts ", Configuration.CATEGORY_GENERAL, true,
				"Save excuted scripts to the script folder, logs by player or robot name");
		if (RobotMod.configFile.hasChanged()) {
			RobotMod.configFile.save();
		}
	}

	public static void unregisterAPIHandler(APIHandler h) {
		RobotMod.apiHandlers.remove(h);
	}

	private APIServer fullAPIServer = null;

	@SubscribeEvent
	public void codeError(CodeEvent.ErrorEvent event) {
		EntityPlayer player = event.getPlayer();
		World world = player.world;
		EntityRobot robot = (EntityRobot) world.getEntityByID(((CodeEvent.RobotErrorEvent) event).getEntityId());
		robot.stopExecutingCode();
		NetworkManager.sendTo(
				new RobotErrorMessage(event.getCode(), event.getError(), event.getLine(), robot.getEntityId()),
				(EntityPlayerMP) event.getPlayer());
	}

	@SubscribeEvent
	public void deathEvent(LivingDeathEvent event) {
		if ((event.getEntity() instanceof SimpleRobotEntity) && ((EntityRobot) event.getEntity()).shouldExecuteCode()) {
			MinecraftForge.EVENT_BUS.post(new CodeEvent.FailEvent("Robot was Destroyed",
					event.getEntity().getEntityId(), ((EntityRobot) event.getEntity()).getOwner()));
		}
	}

	private String generateBlockAPILine(String name, int id, int meta) {
		return String.format("%1$-33s", name.toUpperCase().replaceAll(" ", "_").replaceAll("([^A-Z0-9a-z\\s_])+", ""))
				+ " = Block(" + id + ", " + meta + ", \"" + WordUtils.capitalizeFully(name.replace("_", " ")) + "\")\n";
	}

	private void generateBlocksAPIClass() {
		File file = new File(RobotMod.apiFileLocation, "/core/blocks.py");
		Set<String> names = new HashSet();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write("from .block import Block\n\n");
			for (Block block : Block.REGISTRY) {
				NonNullList<ItemStack> items = NonNullList.create();
				Item.getItemFromBlock(block).getSubItems(CreativeTabs.SEARCH, items);
				if (!items.isEmpty()) {
					for (ItemStack item : items) {
						if (Block.getBlockFromItem(item.getItem()) != Blocks.AIR) {
							if (names.add(generateSetName(item.getDisplayName()))) {
								writer.write(generateBlockAPILine(item.getDisplayName(), Block.getIdFromBlock(block),
										item.getMetadata()));
							} else {
								if (names.add(generateSetName(block.getRegistryName().getResourcePath()))) {
									writer.write(generateBlockAPILine(block.getRegistryName().getResourcePath(),
											Block.getIdFromBlock(block), 0));
								} else {
									if (Block.getIdFromBlock(block) == 80) {
										// Snow blocks
										names.add(generateSetName("SNOW_BLOCK"));
										writer.write(generateBlockAPILine("Snow Block", 80, 0));
									} else {
										RobotMod.logger.info("Found Duplicate name for Block: "
												+ block.getRegistryName().getResourcePath() + " - "
												+ Block.getIdFromBlock(block));

									}
								}
							}
						}
					}
				} else {
					if (names.add(generateSetName(block.getRegistryName().getResourcePath()))) {
						writer.write(generateBlockAPILine(block.getRegistryName().getResourcePath(),
								Block.getIdFromBlock(block), 0));
					} else {
						RobotMod.logger.info("Found Duplicate name for Block: "
								+ block.getRegistryName().getResourcePath() + " - " + Block.getIdFromBlock(block));
					}
				}
			}
			writer.write("\n# This looks weird but it makes indexing blocks way easier\nBLOCKS = {\n");
			for (String name : names) {
				if (name != "AIR") {
					writer.write(name.toUpperCase() + ":" + name.toUpperCase() + ",\n");
				}
			}
			writer.write("AIR : AIR\n}");
		} catch (IOException e) {
			RobotMod.logger.error("Could not write to blocks python file", e);
		}
	}

	private void generateCraftingMappings() {
		for (IRecipe recipe : CraftingManager.REGISTRY) {
			if (recipe.getRecipeOutput() != ItemStack.EMPTY) {
				SimpleItemStack result = new SimpleItemStack(recipe.getRecipeOutput());
				if (RobotMod.recipeMap.containsKey(result)) {
					RobotMod.logger.info("Recipe Map already has " + recipe.getRecipeOutput().getDisplayName());
					RobotMod.recipeMap.get(result).add(recipe);
				} else {
					List<IRecipe> tempList = new ArrayList();
					tempList.add(recipe);
					RobotMod.recipeMap.put(result, tempList);
				}
			}
		}
	}

	private String generateItemAPILine(String name, int id, int meta) {
		return String.format("%1$-33s", name.toUpperCase().replaceAll(" ", "_").replaceAll("([^A-Z0-9a-z\\s_])+", ""))
				+ " = Item(" + id + ", " + meta + ", \"" + WordUtils.capitalizeFully(name.replace("_", " ")) + "\")\n";
	}

	private void generateItemsAPIClass() {
		File file = new File(RobotMod.apiFileLocation, "/core/items.py");
		Set<String> names = new HashSet();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write("from .item import Item\n\n");
			names.add("EMPTY");
			writer.write(generateItemAPILine("EMPTY", 0, 0));
			for (Item entry : Item.REGISTRY) {
				if (!(entry instanceof ItemPotion) && !(entry instanceof ItemEnchantedBook)
						&& !(entry instanceof ItemMonsterPlacer) && !(entry instanceof ItemTippedArrow)) {
					NonNullList<ItemStack> items = NonNullList.create();
					entry.getSubItems(CreativeTabs.SEARCH, items);
					for (ItemStack item : items) {
						if (names.add(generateSetName(item.getDisplayName()))) {
							writer.write(generateItemAPILine(item.getDisplayName(), Item.getIdFromItem(item.getItem()),
									item.getMetadata()));
						} else {
							if (names.add(generateSetName(item.getItem().getRegistryName().getResourcePath()))) {
								writer.write(generateItemAPILine(item.getItem().getRegistryName().getResourcePath(),
										Item.getIdFromItem(item.getItem()), item.getMetadata()));
							} else {
								switch (Item.getIdFromItem(item.getItem())) {
								case 80:
									names.add("SNOW_BLOCK");
									writer.write(
											generateItemAPILine("SNOW_BLOCK", Item.getIdFromItem(item.getItem()), 0));
									break;
								case 322:
									names.add("ENCHANTED_GOLDEN_APPLE");
									writer.write(generateItemAPILine("ENCHANTED_GOLDEN_APPLE",
											Item.getIdFromItem(item.getItem()), item.getMetadata()));
									break;
								case 360:
									names.add("MELON_SLICE");
									writer.write(
											generateItemAPILine("MELON_SLICE", Item.getIdFromItem(item.getItem()), 0));
									break;
								default:
									RobotMod.logger.info("Found Duplicate name for Item: "
											+ item.getItem().getRegistryName().getResourcePath() + " - "
											+ Item.getIdFromItem(item.getItem()));
								}
							}
						}
					}
				}

			}
			writer.write("\n# This looks weird but it makes indexing items way easier\nITEMS = {\n");
			for (String name : names) {
				writer.write(name.toUpperCase() + ":" + name.toUpperCase() + ",\n");
			}
			writer.write("EMPTY : EMPTY\n}");
		} catch (IOException e) {
			RobotMod.logger.error("Could not write to items python file", e);
		}
	}

	private String generateSetName(String input) {
		return input.toUpperCase().replaceAll(" ", "_").replaceAll("([^A-Z0-9a-z\\s_])+", "");
	}

	private File getJarFile(File dir) {
		String path = RobotMod.class.getResource("/assets/roboticraft").getPath();

		try {
			path = path.substring(path.indexOf(dir.toURI().toURL().getPath()));
		} catch (MalformedURLException e) {
			RobotMod.logger.error("Problem getting path to jar file", e);
		}

		path = path.substring(0, path.lastIndexOf('!'));

		try {
			path = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			RobotMod.logger.error("Problem decoding path", e);
		}

		return new File(path);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		RobotMod.proxy.init();
	}

	@SubscribeEvent
	public void onFailEvent(CodeEvent.FailEvent event) {
		for (APIHandler apiHandler : RobotMod.apiHandlers) {
			apiHandler.onFail(event);
		}
	}

	@Mod.EventHandler
	public void onServerStarting(FMLServerStartingEvent event) {
		RobotMod.synchronizeConfig();

		RobotMod.apiActive = true;

		RobotAPI.registerCommands();
		MinecraftForge.EVENT_BUS.register(this);

		try {
			RobotMod.currentPortNumber = -1;
			fullAPIServer = new APIServer(RobotMod.portNumber, RobotMod.searchForPort ? 65535 : RobotMod.portNumber,
					RobotMod.wsPort);
			RobotMod.currentPortNumber = fullAPIServer.getPortNumber();

			new Thread(() -> {
				try {
					fullAPIServer.communicate();
				} catch (IOException e) {
					RobotMod.logger.error("RobotMod error " + e);
				} finally {
					RobotMod.logger.info("Closing RobotMod");
					if (fullAPIServer != null) {
						fullAPIServer.close();
					}
				}
			}).start();

			Python2MinecraftApi.init();

		} catch (IOException e1) {
			RobotMod.logger.error("Threw " + e1);
		}

		if (!RobotMod.useSystemPath && PathUtility.isWindows()) {
			if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
				File localpath = net.minecraft.client.Minecraft.getMinecraft().mcDataDir;
				File path = new File(localpath, RobotMod.pythonEmbeddedLocation);
				if (!path.exists() || (path.list().length == 0)) {
					path.mkdirs();
					File zip = new File(path, "python.zip");
					FileUtils.downloadFile("https://www.python.org/ftp/python/3.6.5/python-3.6.5-embed-amd64.zip", zip);
					FileUtils.unZip(zip.getAbsolutePath(), path.getAbsolutePath());
					zip.delete();
					for (File file : path.listFiles()) {
						if (file.getName().contains("python36._pth")) {
							file.renameTo(new File(path, "python36zz._pth"));
						}
					}
				}
			} else {
				File localpath = FMLCommonHandler.instance().getMinecraftServerInstance().getDataDirectory();
				File path = new File(localpath, RobotMod.pythonEmbeddedLocation);
				if (!path.exists() || (path.list().length == 0)) {
					path.mkdirs();
					File zip = new File(path, "python.zip");
					FileUtils.downloadFile("https://www.python.org/ftp/python/3.6.5/python-3.6.5-embed-amd64.zip", zip);
					FileUtils.unZip(zip.getAbsolutePath(), path.getAbsolutePath());
					zip.delete();
					for (File file : path.listFiles()) {
						if (file.getName().contains("python36._pth")) {
							file.renameTo(new File(path, "python36zz._pth"));
						}
					}
				}
			}
		}
	}

	@Mod.EventHandler
	public void onServerStopping(FMLServerStoppingEvent event) {

		RobotMod.apiActive = false;

		MinecraftForge.EVENT_BUS.unregister(this);

		for (RobotScript script : RobotMod.runningProcesses.values()) {
			script.endScript();
		}

		if (fullAPIServer != null) {
			fullAPIServer.close();
		}
	}

	@SubscribeEvent
	public void onSuccessEvent(CodeEvent.RobotSuccessEvent event) {
		for (APIHandler apiHandler : RobotMod.apiHandlers) {
			apiHandler.onSuccess(event);
		}
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		generateCraftingMappings();
		generateBlocksAPIClass();
		generateItemsAPIClass();
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		RobotMod.logger = event.getModLog();

		RobotMod.integrated = true;
		try {
			Class.forName("net.minecraft.client.Minecraft");
		} catch (ClassNotFoundException e) {
			RobotMod.integrated = false;
		}

		RobotMod.configFile = new Configuration(event.getSuggestedConfigurationFile());
		RobotMod.configFile.load();

		RobotMod.synchronizeConfig();

		NetworkManager.registerMessages();
		NetworkManager.registerPackets();

		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {

			RobotMod.scriptsLoc = new File(net.minecraft.client.Minecraft.getMinecraft().mcDataDir, "python_scripts");

			if (!RobotMod.scriptsLoc.exists()) {
				RobotMod.scriptsLoc.mkdir();
			}

			RobotMod.apiFileLocation = new File(net.minecraft.client.Minecraft.getMinecraft().mcDataDir,
					RobotMod.apiLocation);

			if (!RobotMod.apiFileLocation.exists()) {
				RobotMod.apiFileLocation.mkdirs();
			}
		} else {
			// only do this server side
			RobotMod.scriptsLoc = new File(FMLCommonHandler.instance().getMinecraftServerInstance().getDataDirectory(),
					RobotMod.scriptFolder);

			if (!RobotMod.scriptsLoc.exists()) {
				RobotMod.scriptsLoc.mkdir();
			}

			RobotMod.apiFileLocation = new File(
					FMLCommonHandler.instance().getMinecraftServerInstance().getDataDirectory(), RobotMod.apiLocation);

			if (!RobotMod.apiFileLocation.exists()) {
				RobotMod.apiFileLocation.mkdirs();
			}
		}

		if (!(boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment")) {
			try {
				JarFile roboMod = new JarFile(getJarFile(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT
						? net.minecraft.client.Minecraft.getMinecraft().mcDataDir
						: FMLCommonHandler.instance().getMinecraftServerInstance().getDataDirectory()));
				Enumeration<JarEntry> resources = roboMod.entries();
				while (resources.hasMoreElements()) {
					JarEntry entry = resources.nextElement();
					if (entry.getName().startsWith("assets/roboticraft/api")) {
						File file = new File(RobotMod.apiFileLocation,
								entry.getName().replace("assets/roboticraft/api", ""));
						if (!file.exists()) {
							if (entry.isDirectory()) {
								file.mkdir();
								continue;
							}
							InputStream is = roboMod.getInputStream(entry);
							FileOutputStream fos = new FileOutputStream(file);
							while (is.available() > 0) { // write contents of 'is' to 'fos'
								fos.write(is.read());
							}
							fos.close();
							is.close();
						}
					}
				}
				roboMod.close();
			} catch (IOException e) {
				RobotMod.logger.error("Could not create API folder", e);
			}
		}

		RobotMod.proxy.preInit();
	}

	@SubscribeEvent
	public void socketClose(SocketEvent.CloseRobot event) {
		event.getRobot().stopExecutingCode();
	}

	@SubscribeEvent
	public void worldLoad(WorldEvent.Load event) {
		Python2MinecraftApi.refresh();
	}
}
