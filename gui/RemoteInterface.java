package com.dyn.robot.gui;

import java.awt.Color;
import java.io.IOException;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.entity.inventory.RobotChipContainer;
import com.rabbit.gui.utils.ColourHelper;

import net.minecraft.client.Minecraft;
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
	private static final ResourceLocation guiTex = new ResourceLocation("dynrobot", "textures/gui/robot_container.png");

	private IInventory playerInventory;
	private EntityRobot robot;

	/** The mouse x-position recorded during the last rendered frame. */
	private float mousePosx;
	/** The mouse y-position recorded during the last renderered frame. */
	private float mousePosY;

	public RemoteInterface(IInventory playerInv, EntityRobot robot) {
		super(new RobotChipContainer(playerInv, robot.robot_inventory, robot, Minecraft.getMinecraft().thePlayer));
		this.playerInventory = playerInv;
		this.robot = robot;
		this.allowUserInput = false;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.ySize = (int) (this.height *.85);
		this.guiTop = (int) (this.height *.075);
		this.guiLeft = (this.width - this.xSize) / 5;
		// this.buttonList.add(new GuiButton(1, (this.width + this.xSize) / 2 -
		// 150, ySize +46, 150, 20, "Open Programmer"));
		this.buttonList.add(new GuiButton(1, guiLeft + this.xSize + 20, ySize-20, 150, 20, "Open Programmer"));
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of
	 * the items). Args : mouseX, mouseY
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRendererObj.drawString(this.robot.robot_inventory.getDisplayName().getUnformattedText(), 8, 6, 4210752);
		this.fontRendererObj.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8,
				112, 4210752);
	}

	/**
	 * Args : renderPartialTicks, mouseX, mouseY
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(guiTex);

		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);

		this.fontRendererObj.drawString(
				EnumChatFormatting.GRAY + "Name: " + EnumChatFormatting.WHITE + robot.getRobotName(),
				guiLeft + this.xSize + 30, 40, ColourHelper.AWTColor2RGBInt(Color.white));
		this.fontRendererObj.drawString(
				EnumChatFormatting.GRAY + "Location: " + EnumChatFormatting.WHITE
						+ String.format("X: %d Y: %d Z: %d", robot.getPosition().getX(), robot.getPosition().getY(),
								robot.getPosition().getZ()),
				guiLeft + this.xSize + 30, 50, ColourHelper.AWTColor2RGBInt(Color.white));
		if (robot.getOwner() != null) {
			this.fontRendererObj.drawString(
					EnumChatFormatting.GRAY + "Owner Name: " + EnumChatFormatting.WHITE
							+ robot.getOwner().getDisplayNameString(),
					guiLeft + this.xSize + 30, 60, ColourHelper.AWTColor2RGBInt(Color.gray));
		}
		this.fontRendererObj.drawString(
				EnumChatFormatting.GRAY + "Current Memory: " + EnumChatFormatting.WHITE
						+ robot.getMemorySize(),
				guiLeft + this.xSize + 30, 70, ColourHelper.AWTColor2RGBInt(Color.gray));
		this.fontRendererObj.drawString(
				EnumChatFormatting.GRAY + "Can Climb: " + (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 0)) ? EnumChatFormatting.GREEN + "Yes" :  EnumChatFormatting.RED + "No"),
				guiLeft + this.xSize + 30, 80, ColourHelper.AWTColor2RGBInt(Color.white));
		this.fontRendererObj.drawString(
				EnumChatFormatting.GRAY + "Can Jump: " + (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 1)) ? EnumChatFormatting.GREEN + "Yes" :  EnumChatFormatting.RED + "No"),
				guiLeft + this.xSize + 30, 90, ColourHelper.AWTColor2RGBInt(Color.white));
		this.fontRendererObj.drawString(
				EnumChatFormatting.GRAY + "Can Mine: " + (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 2)) ? EnumChatFormatting.GREEN + "Yes" :  EnumChatFormatting.RED + "No"),
				guiLeft + this.xSize + 30, 100, ColourHelper.AWTColor2RGBInt(Color.white));
		this.fontRendererObj.drawString(
				EnumChatFormatting.GRAY + "Can Build: " + (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 3)) ? EnumChatFormatting.GREEN + "Yes" :  EnumChatFormatting.RED + "No"),
				guiLeft + this.xSize + 30, 110, ColourHelper.AWTColor2RGBInt(Color.white));
		this.fontRendererObj.drawString(
				EnumChatFormatting.GRAY + "Can Inspect: " + (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 4)) ? EnumChatFormatting.GREEN + "Yes" :  EnumChatFormatting.RED + "No") + EnumChatFormatting.RESET,
				guiLeft + this.xSize + 30, 120, ColourHelper.AWTColor2RGBInt(Color.white));
		int i = (this.width - this.xSize) / 5;
		int j = (this.height - this.ySize) / 2;
		GuiInventory.drawEntityOnScreen(i + 34, j + 69, 47, (float) (i + 51) - this.mousePosx,
				(float) (j + 75 - 50) - this.mousePosY, this.robot);

	}

	/**
	 * Called by the controls from the buttonList when activated. (Mouse pressed
	 * for buttons)
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
	 * Draws the screen and all the components in it. Args : mouseX, mouseY,
	 * renderPartialTicks
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.mousePosx = (float) mouseX;
		this.mousePosY = (float) mouseY;
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}