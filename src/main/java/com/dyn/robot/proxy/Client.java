package com.dyn.robot.proxy;

import java.util.List;

import org.lwjgl.input.Keyboard;

import com.dyn.robot.RobotMod;
import com.dyn.robot.blocks.BlockRobot;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.entity.SimpleRobotEntity;
import com.dyn.robot.entity.render.ModelSimpleRobot;
import com.dyn.robot.entity.render.RenderSimpleRobot;
import com.dyn.robot.gui.ActivationScreen;
import com.dyn.robot.gui.MagnetScreen;
import com.dyn.robot.gui.RemoteScreen;
import com.dyn.robot.gui.RobotGuiHandler;
import com.dyn.robot.gui.RobotProgrammingInterface;
import com.rabbit.gui.RabbitGui;
import com.rabbit.gui.component.display.tabs.PictureTab;
import com.rabbit.gui.component.display.tabs.Tab;
import com.rabbit.gui.component.notification.NotificationsManager;
import com.rabbit.gui.component.notification.types.GenericNotification;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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

	@SubscribeEvent
	public void clientWorldEvent(EntityJoinWorldEvent event) {
		if ((event.getEntity() instanceof EntityRobot) && event.getWorld().isRemote) {
			if (((EntityRobot) event.getEntity()).isOwner(Minecraft.getMinecraft().player)) {
				RobotMod.currentRobots.add((EntityRobot) event.getEntity());
			}
		}
	}

	@Override
	public void createNewProgrammingInterface(EntityRobot robot) {
		if (robotProgramInterface.getRobot() != robot) {
			robotProgramInterface = new RobotProgrammingInterface(robot);
		}
	}

	@Override
	public EntityPlayer getplayer(MessageContext ctx) {
		// Note that if you simply return 'Minecraft.getMinecraft().thePlayer',
		// your packets will not work as expected because you will be getting a
		// client player even when you are on the server!
		// Sounds absurd, but it's true.

		// Solution is to double-check side before returning the player:
		return ctx.side.isClient() ? Minecraft.getMinecraft().player : ctx.getServerHandler().player;
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
		if (showRobotProgrammer || ((RabbitGui.proxy.getCurrentStage() != null)
				&& (RabbitGui.proxy.getCurrentStage().getShow() instanceof RobotProgrammingInterface))) {
			robotProgramInterface.handleCompletion();
		}
	}

	@Override
	public void handleErrorMessage(String error, String code, int line) {
		if (showRobotProgrammer) {
			RobotMod.logger.info(error);
			NotificationsManager.addNotification(
					new GenericNotification(new ResourceLocation("Minecraft", "textures/items/barrier.png"),
							error.split(":")[0], error.split(":")[1].trim()));
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
	public void openActivationInterface(World world, BlockRobot robot, BlockPos pos) {
		RabbitGui.proxy.display(new ActivationScreen(robot, Minecraft.getMinecraft().player, pos));
	}

	@Override
	public void openMagnetGui(BlockPos pos, IBlockState state, List<EntityRobot> robots) {
		RabbitGui.proxy.display(new MagnetScreen(pos, state, robots));
	}

	@Override
	public void openRemoteGui() {
		RabbitGui.proxy.display(new RemoteScreen());
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
	public void preInit() {
		RenderingRegistry.registerEntityRenderingHandler(SimpleRobotEntity.class,
				manager -> new RenderSimpleRobot(manager, new ModelSimpleRobot(), 0.3F));

		NetworkRegistry.INSTANCE.registerGuiHandler(RobotMod.instance, new RobotGuiHandler());
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