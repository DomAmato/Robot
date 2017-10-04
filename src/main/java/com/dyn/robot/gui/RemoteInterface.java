package com.dyn.robot.gui;

import java.awt.Color;
import java.io.IOException;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.entity.inventory.RobotChipContainer;
import com.rabbit.gui.utils.ColourHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RemoteInterface extends GuiContainer {
	private static final ResourceLocation guiTex = new ResourceLocation("robot", "textures/gui/robot_container.png");

	private EntityRobot robot;

	/** The mouse x-position recorded during the last rendered frame. */
	private float mousePosx;
	/** The mouse y-position recorded during the last renderered frame. */
	private float mousePosY;

	public RemoteInterface(IInventory playerInv, EntityRobot robot) {
		super(new RobotChipContainer(playerInv, robot.robot_inventory, robot, Minecraft.getMinecraft().thePlayer));
		this.robot = robot;
		allowUserInput = false;
		Minecraft.getMinecraft().getSoundHandler()
				.playSound(PositionedSoundRecord.create(new ResourceLocation("robot:robot.remote"), 1.0F));
	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed for
	 * buttons)
	 */
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled) {
			if (button.id == 1) {
				RobotMod.proxy.openRobotProgrammingWindow(robot);
			}
		}
	}

	/**
	 * Args : renderPartialTicks, mouseX, mouseY
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(RemoteInterface.guiTex);

		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		fontRendererObj.drawString(EnumChatFormatting.GRAY + "Name: " + EnumChatFormatting.WHITE + robot.getRobotName(),
				guiLeft + xSize + 30, 30, ColourHelper.AWTColor2RGBInt(Color.white));
		fontRendererObj.drawString(
				EnumChatFormatting.GRAY + "Location: " + EnumChatFormatting.WHITE
						+ String.format("X: %d Y: %d Z: %d", robot.getPosition().getX(), robot.getPosition().getY(),
								robot.getPosition().getZ()),
				guiLeft + xSize + 30, 40, ColourHelper.AWTColor2RGBInt(Color.white));
		fontRendererObj.drawString(
				EnumChatFormatting.GRAY + "Current Memory: " + EnumChatFormatting.WHITE + robot.getMemorySize(),
				guiLeft + xSize + 30, 50, ColourHelper.AWTColor2RGBInt(Color.gray));
		fontRendererObj.drawString(
				EnumChatFormatting.GRAY + "Can Climb: "
						+ (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 0))
								? EnumChatFormatting.GREEN + "Yes"
								: EnumChatFormatting.RED + "No"),
				guiLeft + xSize + 30, 70, ColourHelper.AWTColor2RGBInt(Color.white));
		fontRendererObj.drawString(
				EnumChatFormatting.GRAY + "Can Jump: "
						+ (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 1))
								? EnumChatFormatting.GREEN + "Yes"
								: EnumChatFormatting.RED + "No"),
				guiLeft + xSize + 30, 80, ColourHelper.AWTColor2RGBInt(Color.white));
		fontRendererObj.drawString(
				EnumChatFormatting.GRAY + "Can Mine: "
						+ (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 2))
								? EnumChatFormatting.GREEN + "Yes"
								: EnumChatFormatting.RED + "No"),
				guiLeft + xSize + 30, 90, ColourHelper.AWTColor2RGBInt(Color.white));
		fontRendererObj.drawString(
				EnumChatFormatting.GRAY + "Can Build: "
						+ (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 3))
								? EnumChatFormatting.GREEN + "Yes"
								: EnumChatFormatting.RED + "No"),
				guiLeft + xSize + 30, 100, ColourHelper.AWTColor2RGBInt(Color.white));
		fontRendererObj.drawString(
				EnumChatFormatting.GRAY + "Can Inspect: "
						+ (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 4))
								? EnumChatFormatting.GREEN + "Yes"
								: EnumChatFormatting.RED + "No")
						+ EnumChatFormatting.RESET,
				guiLeft + xSize + 30, 110, ColourHelper.AWTColor2RGBInt(Color.white));
		fontRendererObj.drawString(
				EnumChatFormatting.GRAY + "Can Interact: "
						+ (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 5))
								? EnumChatFormatting.GREEN + "Yes"
								: EnumChatFormatting.RED + "No")
						+ EnumChatFormatting.RESET,
				guiLeft + xSize + 30, 120, ColourHelper.AWTColor2RGBInt(Color.white));
		fontRendererObj.drawString(
				EnumChatFormatting.GRAY + "Can Detect: "
						+ (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 6))
								? EnumChatFormatting.GREEN + "Yes"
								: EnumChatFormatting.RED + "No")
						+ EnumChatFormatting.RESET,
				guiLeft + xSize + 30, 130, ColourHelper.AWTColor2RGBInt(Color.white));
		fontRendererObj.drawString(
				EnumChatFormatting.GRAY + "Can Attack: "
						+ (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 7))
								? EnumChatFormatting.GREEN + "Yes"
								: EnumChatFormatting.RED + "No")
						+ EnumChatFormatting.RESET,
				guiLeft + xSize + 30, 140, ColourHelper.AWTColor2RGBInt(Color.white));
		int i = (width - xSize) / 5;
		int j = (height - ySize) / 2;
		GuiInventory.drawEntityOnScreen(i + 34, j + 69, 47, (i + 51) - mousePosx, (j + 75) - 50 - mousePosY, robot);

	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the
	 * items). Args : mouseX, mouseY
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRendererObj.drawString(robot.robot_inventory.getDisplayName().getUnformattedText(), 8, 6, 4210752);
		fontRendererObj.drawString("Your Inventory", 8, 112, 4210752);
	}

	/**
	 * Draws the screen and all the components in it. Args : mouseX, mouseY,
	 * renderPartialTicks
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		mousePosx = mouseX;
		mousePosY = mouseY;
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		super.initGui();
		ySize = (int) (height * .85);
		guiTop = (int) (height * .075);
		guiLeft = (width - xSize) / 5;
		buttonList.add(new GuiButton(1, guiLeft + xSize + 20, ySize - 20, 150, 20, "Open Programmer"));
	}
}