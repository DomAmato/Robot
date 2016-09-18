package com.dyn.robot.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.dyn.robot.api.IDYNRobotAccess;
import com.dyn.robot.entity.EntityRobot;

import dan200.computercraft.client.gui.widgets.WidgetTerminal;
import dan200.computercraft.shared.computer.core.IComputer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class RemoteInterface extends GuiScreen {
	private static final ResourceLocation backgroundAdvanced = new ResourceLocation("computercraft",
			"textures/gui/corners2.png");
	protected World m_world;
	// protected ContainerTurtle m_container;
	protected final IDYNRobotAccess m_robot;
	protected final IComputer m_computer;
	protected WidgetTerminal m_terminal;

	protected RemoteInterface(EntityPlayer player, World world, EntityRobot robot) {
		m_world = world;
		m_robot = (IDYNRobotAccess) robot.getAccess();
		m_computer = robot.getComputer();

		if (!m_computer.isOn()) {
			System.out.println("Turning Computer On");
			m_computer.turnOn();
		}

		setGuiSize(254, 217);
	}

	/**
	 * Returns true if this GUI should pause the game when it is displayed in
	 * single-player
	 */
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
	}

	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		int startX = (width - m_terminal.getWidth()) / 2;
		int startY = (height - m_terminal.getHeight()) / 2;
		int endX = startX + m_terminal.getWidth();
		int endY = startY + m_terminal.getHeight();

		drawDefaultBackground();

		m_terminal.draw(mc, startX, startY, mouseX, mouseY);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(backgroundAdvanced);

		drawTexturedModalRect(startX - 12, startY - 12, 12, 28, 12, 12);
		drawTexturedModalRect(startX - 12, endY, 12, 40, 12, 16);
		drawTexturedModalRect(endX, startY - 12, 24, 28, 12, 12);
		drawTexturedModalRect(endX, endY, 24, 40, 12, 16);

		drawTexturedModalRect(startX, startY - 12, 0, 0, endX - startX, 12);
		drawTexturedModalRect(startX, endY, 0, 12, endX - startX, 16);

		drawTexturedModalRect(startX - 12, startY, 0, 28, 12, endY - startY);
		drawTexturedModalRect(endX, startY, 36, 28, 12, endY - startY);
	}

	@Override
	public void handleKeyboardInput() throws IOException {
		super.handleKeyboardInput();
		m_terminal.handleKeyboardInput();
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();

		int x = (Mouse.getEventX() * width) / mc.displayWidth;
		int y = height - ((Mouse.getEventY() * height) / mc.displayHeight) - 1;
		int startX = (width - m_terminal.getWidth()) / 2;
		int startY = (height - m_terminal.getHeight()) / 2;
		m_terminal.handleMouseInput(x - startX, y - startY);
	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		m_terminal = new WidgetTerminal(0, 0, 51, 19, () -> m_computer, 2, 2, 2, 2);
		m_terminal.setAllowFocusLoss(false);
	}

	@Override
	protected void keyTyped(char c, int k) throws IOException {
		if (k == 1) {
			super.keyTyped(c, k);
		} else {
			m_terminal.keyTyped(c, k);
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int button) {
		int startX = (width - m_terminal.getWidth()) / 2;
		int startY = (height - m_terminal.getHeight()) / 2;
		m_terminal.mouseClicked(x - startX, y - startY, button);
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		m_terminal.update();
	}

	/*
	 * protected void keyTyped(char c, int k) throws IOException { if (k == 1) {
	 * super.keyTyped(c, k); } else { this.m_terminalGui.keyTyped(c, k); } }
	 *
	 * protected void mouseClicked(int x, int y, int button) throws IOException
	 * { super.mouseClicked(x, y, button); this.m_terminalGui.mouseClicked(x, y,
	 * button); }
	 *
	 * public void handleMouseInput() throws IOException {
	 * super.handleMouseInput(); int x = Mouse.getEventX() * this.width /
	 * this.mc.displayWidth; int y = this.height - Mouse.getEventY() *
	 * this.height / this.mc.displayHeight - 1;
	 * this.m_terminalGui.handleMouseInput(x, y); }
	 *
	 * public void handleKeyboardInput() throws IOException {
	 * super.handleKeyboardInput(); this.m_terminalGui.handleKeyboardInput(); }
	 *
	 * // protected void drawSelectionSlot(boolean advanced) // { // int x =
	 * (this.width - this.xSize) / 2; // int y = (this.height - this.ySize) / 2;
	 * // // int slot = this.m_container.getSelectedSlot(); // if (slot >= 0) //
	 * { // GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); // int slotX = slot %
	 * 4; // int slotY = slot / 4; //
	 * this.mc.getTextureManager().bindTexture(advanced ? backgroundAdvanced :
	 * // background); // drawTexturedModalRect(x +
	 * this.m_container.m_turtleInvStartX - 2 + slotX // * 18, y +
	 * this.m_container.m_playerInvStartY - 2 + slotY * 18, 0, 217, // 24, 24);
	 * // } // }
	 *
	 * protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int
	 * mouseY) { this.m_terminalGui.draw(Minecraft.getMinecraft(), 0, 0, mouseX,
	 * mouseY);
	 *
	 * GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	 * this.mc.getTextureManager().bindTexture(backgroundAdvanced); int x =
	 * (this.width - this.xSize) / 2; int y = (this.height - this.ySize) / 2;
	 * drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
	 *
	 * // drawSelectionSlot(advanced); }
	 *
	 * @Override public void drawScreen(int mouseX, int mouseY, float
	 * partialTicks) { super.drawScreen(mouseX, mouseY, partialTicks);
	 * this.m_terminalGui.draw(Minecraft.getMinecraft(), 0, 0, mouseX, mouseY);
	 *
	 * GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	 * this.mc.getTextureManager().bindTexture(backgroundAdvanced); int x =
	 * (this.width - this.xSize) / 2; int y = (this.height - this.ySize) / 2;
	 * drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize); }
	 */
}
