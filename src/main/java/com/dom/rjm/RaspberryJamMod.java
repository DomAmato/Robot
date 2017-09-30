package com.dom.rjm;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import com.dom.rjm.api.APIRegistry;
import com.dom.rjm.api.APIServer;
import com.dom.rjm.api.Python2MinecraftApi;
import com.dom.rjm.command.AddPythonExternalCommand;
import com.dom.rjm.command.CameraCommand;
import com.dom.rjm.command.NightVisionExternalCommand;
import com.dom.rjm.command.PythonExternalCommand;
import com.dom.rjm.command.ScriptExternalCommand;
import com.dom.rjm.events.ClientEventHandler;
import com.dom.rjm.events.MCEventHandler;
import com.dom.rjm.events.MCEventHandlerServer;
import com.dom.rjm.util.FileUtils;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = RaspberryJamMod.MODID, version = RaspberryJamMod.VERSION, name = RaspberryJamMod.NAME)
public class RaspberryJamMod {
	public static final String MODID = "raspberryjammod";
	public static final String VERSION = "0.9.12";
	public static final String NAME = "Raspberry Jam Mod";

	public static ScriptExternalCommand[] scriptExternalCommands = null;

	// config options
	public static Configuration configFile;
	public static boolean concurrent = true;
	public static boolean leftClickToo = true;
	public static boolean useSystemPath = true;
	public static boolean allowRemote = true;
	public static boolean globalChatMessages = true;
	public static String pythonInterpreter = "python";
	public static String pythonEmbeddedLocation = "rjm-python";
	public static boolean integrated = true;
	public static volatile boolean apiActive = false;
	public static boolean clientOnlyAPI = false;

	// websocket stuff
	public static int portNumber = 4711;
	public static int wsPort = 14711;
	public static boolean searchForPort = false;
	public static int currentPortNumber;
	public static String serverAddress = null;

	// player scripts so admins/mentors can destroy running scripts
	public static Map<EntityPlayer, Process> playerProcesses = Maps.newHashMap();

	public static Logger logger;

	public static int closeAllScripts() {
		if (RaspberryJamMod.scriptExternalCommands == null) {
			return 0;
		}
		int count = 0;
		for (ScriptExternalCommand c : RaspberryJamMod.scriptExternalCommands) {
			count += c.close();
		}
		return count;
	}

	static public Field findField(Class c, String name) throws NoSuchFieldException {
		do {
			try {
				// for (Field f : c.getDeclaredFields()) {
				// RaspberryJamMod.logger.info(f.getName()+" "+f.getType());
				// }
				return c.getDeclaredField(name);
			} catch (Exception e) {
				// System.out.println(""+e);
			}
		} while (null != (c = c.getSuperclass()));
		throw new NoSuchFieldException(name);
	}

	public static void synchronizeConfig() {
		RaspberryJamMod.portNumber = RaspberryJamMod.configFile.getInt("Port Number", Configuration.CATEGORY_GENERAL,
				4711, 0, 65535, "Port number");
		RaspberryJamMod.wsPort = RaspberryJamMod.configFile.getInt("Websocket Port", Configuration.CATEGORY_GENERAL,
				14711, 0, 65535, "Websocket port");
		RaspberryJamMod.searchForPort = RaspberryJamMod.configFile.getBoolean("Port Search if Needed",
				Configuration.CATEGORY_GENERAL, false, "Port search if needed");
		RaspberryJamMod.concurrent = RaspberryJamMod.configFile.getBoolean("Multiple Connections",
				Configuration.CATEGORY_GENERAL, true, "Multiple connections");
		RaspberryJamMod.allowRemote = RaspberryJamMod.configFile.getBoolean("Remote Connections",
				Configuration.CATEGORY_GENERAL, true, "Remote connections");
		RaspberryJamMod.leftClickToo = RaspberryJamMod.configFile.getBoolean("Detect Sword Left-Click",
				Configuration.CATEGORY_GENERAL, false, "Detect sword left-click");
		RaspberryJamMod.useSystemPath = RaspberryJamMod.configFile.getBoolean("Search System Path",
				Configuration.CATEGORY_GENERAL, true,
				"Search for python on the system path or use a local embedded version");
		RaspberryJamMod.pythonEmbeddedLocation = RaspberryJamMod.configFile.getString("Embedded Python Location",
				Configuration.CATEGORY_GENERAL, "rjm-python", "Relative to .minecraft folder or server jar");
		RaspberryJamMod.pythonInterpreter = RaspberryJamMod.configFile.getString("Python Interpreter",
				Configuration.CATEGORY_GENERAL, "python", "Python interpreter");
		RaspberryJamMod.globalChatMessages = RaspberryJamMod.configFile.getBoolean("Messages Go To All",
				Configuration.CATEGORY_GENERAL, true, "Messages go to all");
		RaspberryJamMod.clientOnlyAPI = RaspberryJamMod.configFile.getBoolean("Read-Only Client-Based API",
				Configuration.CATEGORY_GENERAL, false, "Read-only API");
		// clientOnlyPortNumber = configFile.getInt("Port Number for Client-Only
		// API", Configuration.CATEGORY_GENERAL, 0, 0, 65535, "Client-only API
		// port number (normally 0)");

		if (RaspberryJamMod.configFile.hasChanged()) {
			RaspberryJamMod.configFile.save();
		}
	}

	static public void unregisterCommand(CommandHandler ch, ICommand c) {
		try {
			Map commandMap = ch.getCommands();
			for (String alias : c.getAliases()) {
				try {
					commandMap.remove(alias);
				} catch (Exception e) {
				}
			}

			try {
				commandMap.remove(c.getName());
			} catch (Exception e) {
			}

			Field commandSetField;
			try {
				commandSetField = RaspberryJamMod.findField(ch.getClass(), "commandSet");
			} catch (NoSuchFieldException e) {
				commandSetField = RaspberryJamMod.findField(ch.getClass(), "field_71561_b");
			}
			commandSetField.setAccessible(true);
			Set commandSet = (Set) commandSetField.get(ch);
			commandSet.remove(c);
		} catch (Exception e) {
			System.err.println("Oops " + e);
		}
	}

	private APIServer fullAPIServer = null;

	private NightVisionExternalCommand nightVisionExternalCommand = null;

	private CameraCommand cameraCommand = null;

	private ClientEventHandler clientEventHandler = null;

	private MCEventHandler serverEventHandler = null;

	private MinecraftServer s;

	@Mod.EventHandler
	@SideOnly(Side.CLIENT)
	public void Init(FMLInitializationEvent event) {
		clientEventHandler = new ClientEventHandler();
		MinecraftForge.EVENT_BUS.register(clientEventHandler);
		nightVisionExternalCommand = new NightVisionExternalCommand(clientEventHandler);
		net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(nightVisionExternalCommand);
		cameraCommand = new CameraCommand();
		net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(cameraCommand);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onConfigChanged(net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent event) {
		RaspberryJamMod.logger.info("config changed");
	}

	@Mod.EventHandler
	public void onServerStarting(FMLServerStartingEvent event) {
		RaspberryJamMod.synchronizeConfig();

		if (RaspberryJamMod.clientOnlyAPI) {
			return;
		}

		if (clientEventHandler != null) {
			clientEventHandler.closeAPI();
		}

		RaspberryJamMod.apiActive = true;

		serverEventHandler = new MCEventHandlerServer();
		MinecraftForge.EVENT_BUS.register(serverEventHandler);
		try {
			RaspberryJamMod.currentPortNumber = -1;
			fullAPIServer = new APIServer(serverEventHandler, RaspberryJamMod.portNumber,
					RaspberryJamMod.searchForPort ? 65535 : RaspberryJamMod.portNumber, RaspberryJamMod.wsPort, false);
			RaspberryJamMod.currentPortNumber = fullAPIServer.getPortNumber();

			new Thread(() -> {
				try {
					fullAPIServer.communicate();
				} catch (IOException e) {
					RaspberryJamMod.logger.error("RaspberryJamMod error " + e);
				} finally {
					RaspberryJamMod.logger.info("Closing RaspberryJamMod");
					if (fullAPIServer != null) {
						fullAPIServer.close();
					}
				}
			}).start();
		} catch (IOException e1) {
			RaspberryJamMod.logger.error("Threw " + e1);
		}

		if (!RaspberryJamMod.useSystemPath) {
			if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
				File localpath = Minecraft.getMinecraft().mcDataDir;
				File path = new File(localpath, RaspberryJamMod.pythonEmbeddedLocation);
				if (!path.exists()) {
					path.mkdirs();
					File zip = new File(path, "python.zip");
					FileUtils.downloadFile("https://www.python.org/ftp/python/3.5.2/python-3.5.2-embed-amd64.zip", zip);
					FileUtils.unZip(zip.getAbsolutePath(), path.getAbsolutePath());
					zip.delete();
				}
			} else {
				File localpath = FMLCommonHandler.instance().getMinecraftServerInstance().getDataDirectory();
				File path = new File(localpath, RaspberryJamMod.pythonEmbeddedLocation);
				if (!path.exists()) {
					path.mkdirs();
					File zip = new File(path, "python.zip");
					FileUtils.downloadFile("https://www.python.org/ftp/python/3.5.2/python-3.5.2-embed-amd64.zip", zip);
					FileUtils.unZip(zip.getAbsolutePath(), path.getAbsolutePath());
					zip.delete();
				}
			}
		}

		RaspberryJamMod.scriptExternalCommands = new ScriptExternalCommand[] { new PythonExternalCommand(false),
				new AddPythonExternalCommand(false) };
		for (ScriptExternalCommand c : RaspberryJamMod.scriptExternalCommands) {
			event.registerServerCommand(c);
		}
	}

	@Mod.EventHandler
	public void onServerStopping(FMLServerStoppingEvent event) {
		if (RaspberryJamMod.clientOnlyAPI) {
			return;
		}

		RaspberryJamMod.apiActive = false;

		if (serverEventHandler != null) {
			MinecraftForge.EVENT_BUS.unregister(serverEventHandler);
			serverEventHandler = null;
		}

		if (fullAPIServer != null) {
			fullAPIServer.close();
		}
		RaspberryJamMod.closeAllScripts();
		if (RaspberryJamMod.scriptExternalCommands != null) {
			s = FMLCommonHandler.instance().getMinecraftServerInstance();
			if (s != null) {
				for (ICommand c : RaspberryJamMod.scriptExternalCommands) {
					RaspberryJamMod.unregisterCommand((CommandHandler) s.getCommandManager(), c);
				}
			}
			RaspberryJamMod.scriptExternalCommands = null;
		}
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		RaspberryJamMod.logger = event.getModLog();

		Python2MinecraftApi.init();

		RaspberryJamMod.integrated = true;
		try {
			Class.forName("net.minecraft.client.Minecraft");
		} catch (ClassNotFoundException e) {
			RaspberryJamMod.integrated = false;
		}

		RaspberryJamMod.configFile = new Configuration(event.getSuggestedConfigurationFile());
		RaspberryJamMod.configFile.load();
		// KeyBindings.init();

		RaspberryJamMod.synchronizeConfig();
	}
}
