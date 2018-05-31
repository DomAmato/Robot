package com.rabbit.gui.component.display;

import org.lwjgl.opengl.GL11;

import com.rabbit.gui.component.GuiWidget;
import com.rabbit.gui.render.Renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class Compass extends GuiWidget {

	private final ResourceLocation EAST = new ResourceLocation("roboticraft", "textures/gui/East Direction.png");
	private final ResourceLocation NORTH = new ResourceLocation("roboticraft", "textures/gui/North Direction.png");
	private final ResourceLocation SOUTH = new ResourceLocation("roboticraft", "textures/gui/South Direction.png");
	private final ResourceLocation WEST = new ResourceLocation("roboticraft", "textures/gui/West Direction.png");

	private EnumFacing currentDir;
	private Entity trackingEntity;

	public Compass(int x, int y, int width, int height) {
		this(x, y, width, height, EnumFacing.NORTH);
	}

	public Compass(int x, int y, int width, int height, Entity entity) {
		super(x, y, width, height);
		currentDir = entity.getHorizontalFacing();
		trackingEntity = entity;
	}

	public Compass(int x, int y, int width, int height, EnumFacing dir) {
		super(x, y, width, height);
		currentDir = dir;
	}

	public EnumFacing getCurrentDir() {
		return currentDir;
	}

	public Entity getTrackingEntity() {
		return trackingEntity;
	}

	@Override
	public void onDraw(int mouseX, int mouseY, float partialTicks) {
		if (trackingEntity != null) {
			currentDir = trackingEntity.getHorizontalFacing();
		}
		switch (currentDir) {
		case EAST:
			renderPicture(EAST);
			break;
		case SOUTH:
			renderPicture(SOUTH);
			break;
		case WEST:
			renderPicture(WEST);
			break;
		default:
			renderPicture(NORTH);
			break;
		}
	}

	private void renderPicture(ResourceLocation img) {
		GlStateManager.pushMatrix();
		{
			GlStateManager.resetColor();
			GlStateManager.color(1, 1, 1, 1);
			GlStateManager.enableTexture2D();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			Minecraft.getMinecraft().renderEngine.bindTexture(img);
			Renderer.drawScaledTexturedRect(getX(), getY(), getWidth(), getHeight());
		}
		GlStateManager.popMatrix();
	}

	public void setCurrentDir(EnumFacing currentDir) {
		this.currentDir = currentDir;
	}

	public void setTrackingEntity(Entity trackingEntity) {
		this.trackingEntity = trackingEntity;
	}

}
