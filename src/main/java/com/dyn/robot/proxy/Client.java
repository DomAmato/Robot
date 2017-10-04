package com.dyn.robot.proxy;

import org.lwjgl.input.Keyboard;

import com.dyn.robot.RobotMod;
import com.dyn.robot.blocks.BlockDynRobot;
import com.dyn.robot.entity.DynRobotEntity;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.entity.render.ModelDynRobot;
import com.dyn.robot.entity.render.RenderDynRobot;
import com.dyn.robot.gui.ActivationScreen;
import com.dyn.robot.gui.RobotGuiHandler;
import com.dyn.robot.gui.RobotProgrammingInterface;
import com.dyn.robot.reference.Reference;
import com.rabbit.gui.RabbitGui;
import com.rabbit.gui.component.display.tabs.PictureTab;
import com.rabbit.gui.component.display.tabs.Tab;
import com.rabbit.gui.component.notification.NotificationsManager;
import com.rabbit.gui.component.notification.types.GenericNotification;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Client implements Proxy {

	private RobotProgrammingInterface robotProgramInterface;

	private Tab programTab;

	private KeyBinding scriptKey;

	private boolean showRobotProgrammer = false;

	private int windowWidth;

	@Override
	public void addScheduledTask(Runnable runnable) {
		Minecraft.getMinecraft().addScheduledTask(runnable);
	}

	@Override
	public void createNewProgrammingInterface(EntityRobot robot) {
		if (robotProgramInterface.getRobot() != robot) {
			robotProgramInterface = new RobotProgrammingInterface(robot);
		}
	}

	@Override
	public EntityPlayer getPlayerEntity(MessageContext ctx) {
		// Note that if you simply return 'Minecraft.getMinecraft().thePlayer',
		// your packets will not work as expected because you will be getting a
		// client player even when you are on the server!
		// Sounds absurd, but it's true.

		// Solution is to double-check side before returning the player:
		return ctx.side.isClient() ? Minecraft.getMinecraft().thePlayer : ctx.getServerHandler().playerEntity;
	}

	@Override
	public String getProgrammingInterfaceText() {
		if (robotProgramInterface != null) {
			return robotProgramInterface.getConsoleText();
		}
		return null;
	}

	@Override
	public IThreadListener getThreadFromContext(MessageContext ctx) {
		// this causes null pointers in single player...
		return Minecraft.getMinecraft();
	}

	@Override
	public void handleCodeExecutionEnded() {
		if ((RabbitGui.proxy.getCurrentStage() != null)
				&& (RabbitGui.proxy.getCurrentStage().getShow() instanceof RobotProgrammingInterface)) {
			robotProgramInterface.handleCompletion();
		}
	}

	@Override
	public void handleErrorMessage(String error, String code, int line) {
		if (showRobotProgrammer) {
			NotificationsManager.addNotification(new GenericNotification(Minecraft.getMinecraft(),
					new ResourceLocation("Minecraft", "textures/items/barrier.png"), error.split(":")[0],
					error.split(":")[1].trim()));
			if ((RabbitGui.proxy.getCurrentStage() != null)
					&& (RabbitGui.proxy.getCurrentStage().getShow() instanceof RobotProgrammingInterface)) {
				robotProgramInterface.handleErrorMessage(error, code, line);
			}
		} else if ((RabbitGui.proxy.getCurrentStage() != null)
				&& (RabbitGui.proxy.getCurrentStage().getShow() instanceof RobotProgrammingInterface)) {
			robotProgramInterface.handleErrorMessage(error, code, line);
		}
	}

	@Override
	public void init() {
		MinecraftForge.EVENT_BUS.register(this);
		robotProgramInterface = new RobotProgrammingInterface();

		scriptKey = new KeyBinding("key.toggle.scriptui", Keyboard.KEY_P, "key.categories.toggle");

		ClientRegistry.registerKeyBinding(scriptKey);
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if ((Minecraft.getMinecraft().currentScreen instanceof GuiChat)) {
			return;
		}

		if (scriptKey.isPressed() && showRobotProgrammer) {
			RobotMod.logger.info("Program Gui Toggled");
			showRobotProgrammer = false;
			RabbitGui.proxy.display(robotProgramInterface);
		}
	}

	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		if (Minecraft.getMinecraft().inGameHasFocus || ((RabbitGui.proxy.getCurrentStage() != null)
				&& (RabbitGui.proxy.getCurrentStage().getShow() instanceof RobotProgrammingInterface))) {
			if (Minecraft.getMinecraft().inGameHasFocus) {
				if (showRobotProgrammer && !robotProgramInterface.getRobot().isDead) {
					ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
					if (windowWidth != scaledresolution.getScaledWidth()) {
						windowWidth = scaledresolution.getScaledWidth();
						programTab = new PictureTab(windowWidth, 0, 45, 50, "(P)", 90,
								new ResourceLocation("robot",
										robotProgramInterface.getRobot().getIsFollowing()
												? "textures/gui/robot_follow.png"
												: "textures/gui/robot_stand.png")).setHidden(false);
						;
					}
					programTab.onDraw(0, 0, event.renderTickTime);
				}
			} else if (robotProgramInterface.getRobot().isDead) {
				RabbitGui.proxy.getCurrentStage().close();
			}
		}
	}

	@Override
	public void openActivationInterface(World world, BlockDynRobot robot, BlockPos pos) {
		RabbitGui.proxy.display(new ActivationScreen(robot, Minecraft.getMinecraft().thePlayer, pos));
	}

	@Override
	public void openRobotGui() {
		// TODO Auto-generated method stub

	}

	@Override
	public void openRobotProgrammingWindow() {
		if (!robotProgramInterface.getRobot().isDead) {
			RabbitGui.proxy.display(robotProgramInterface);
		}
	}

	@Override
	public void openRobotProgrammingWindow(EntityRobot robot) {
		createNewProgrammingInterface(robot);

		openRobotProgrammingWindow();
	}

	@Override
	public void playSound(String sound) {
		Minecraft.getMinecraft().getSoundHandler()
				.playSound(PositionedSoundRecord.create(new ResourceLocation(sound), 1.0F));
	}

	@Override
	public void preInit() {
		RenderingRegistry.registerEntityRenderingHandler(DynRobotEntity.class,
				manager -> new RenderDynRobot(manager, new ModelDynRobot(), 0.3F));

		NetworkRegistry.INSTANCE.registerGuiHandler(RobotMod.instance, new RobotGuiHandler());
	}

	@Override
	public void registerBlockItem(Block block) {
		String blockName = block.getUnlocalizedName().replace("tile.", "");
		Item blockItem = GameRegistry.findItem(Reference.MOD_ID, blockName);
		ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation(Reference.MOD_ID + ":" + blockName,
				"inventory");
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(blockItem, 0, itemModelResourceLocation);
	}

	@Override
	public void registerItem(Item item, String name, int meta) {
		if (name.contains("item.")) {
			name = name.replace("item.", "");
		}
		ModelResourceLocation location = new ModelResourceLocation(Reference.MOD_ID + ":" + name, "inventory");
		ModelLoader.setCustomModelResourceLocation(item, meta, location);
	}

	@Override
	public void toggleRenderRobotProgramInterface(boolean state) {
		if (!robotProgramInterface.getRobot().isDead) {
			showRobotProgrammer = state;
			if (state) {
				ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
				windowWidth = scaledresolution.getScaledWidth();
				programTab = new PictureTab(windowWidth, 0, 50, 50, "(P)", 90,
						new ResourceLocation("robot",
								robotProgramInterface.getRobot().getIsFollowing() ? "textures/gui/robot_follow.png"
										: "textures/gui/robot_stand.png")).setHidden(false);
				;
			}
		} else {
			showRobotProgrammer = false;
		}
	}
}