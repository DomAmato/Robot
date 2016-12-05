package com.dyn.robot.proxy;

import org.lwjgl.input.Keyboard;

import com.dyn.DYNServerMod;
import com.dyn.robot.entity.BlockDynRobot;
import com.dyn.robot.entity.DynRobotEntity;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.entity.render.DynRobotRenderer;
import com.dyn.robot.gui.RemoteInterface;
import com.dyn.robot.gui.RobotProgrammingInterface;
import com.dyn.robot.reference.Reference;
import com.rabbit.gui.RabbitGui;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Client implements Proxy {

	private RobotProgrammingInterface robotProgramInterface;

	private KeyBinding scriptKey;

	private boolean showRobotProgrammer = false;

	@Override
	public void createNewProgrammingInterface(EntityRobot robot) {
		robotProgramInterface = new RobotProgrammingInterface(robot);
	}

	@Override
	public RobotProgrammingInterface getProgrammingInterface() {
		return robotProgramInterface;
	}

	@Override
	public void handleErrorMessage(String error, String code, int line) {
		if (showRobotProgrammer || ((RabbitGui.proxy.getCurrentStage() != null)
				&& (RabbitGui.proxy.getCurrentStage().getShow() instanceof RobotProgrammingInterface))) {
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
			DYNServerMod.logger.info("Program Gui Toggled");
			showRobotProgrammer = false;
			RabbitGui.proxy.display(robotProgramInterface);
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			showRobotProgrammer = false;
		}
	}

	@SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		if (Minecraft.getMinecraft().inGameHasFocus || ((RabbitGui.proxy.getCurrentStage() != null)
				&& (RabbitGui.proxy.getCurrentStage().getShow() instanceof RobotProgrammingInterface))) {
			if (Minecraft.getMinecraft().inGameHasFocus && showRobotProgrammer
					&& !robotProgramInterface.getRobot().isDead) {
				robotProgramInterface.onDraw(0, 0, event.renderTickTime);
			}
		}
	}

	@Override
	public void openRemoteInterface(EntityRobot robot) {
		if ((robot != null) && !robot.isDead) {
			RabbitGui.proxy.display(new RemoteInterface(robot, Minecraft.getMinecraft().thePlayer));
		}
	}

	@Override
	public void openRemoteInterface(World world, BlockDynRobot robot, BlockPos pos) {
		RabbitGui.proxy.display(new RemoteInterface(robot, Minecraft.getMinecraft().thePlayer, pos));
	}

	@Override
	public void openRobotGui() {
		// TODO Auto-generated method stub

	}

	@Override
	public void openRobotInterface() {
		if (!robotProgramInterface.getRobot().isDead) {
			RabbitGui.proxy.display(robotProgramInterface);
		}
	}

	@Override
	public void openRobotProgrammingWindow(EntityRobot robot) {
		if (getProgrammingInterface().getRobot() != robot) {
			createNewProgrammingInterface(robot);
		}

		openRobotInterface();
	}

	@Override
	public void preInit() {
		RenderingRegistry.registerEntityRenderingHandler(DynRobotEntity.class, new DynRobotRenderer());
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
		} else {
			showRobotProgrammer = false;
		}
	}
}