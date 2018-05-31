package com.rabbit.gui.component.display.tabs;

import java.awt.Color;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.rabbit.gui.component.display.Compass;
import com.rabbit.gui.render.TextAlignment;
import com.rabbit.gui.render.TextRenderer;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;

public class CompassTab extends Tab {

	private Compass compass;
	private String title;

	public CompassTab(int x, int y, int width, int height) {
		this(x, y, width, height, EnumFacing.NORTH);
	}

	public CompassTab(int x, int y, int width, int height, Entity entity) {
		this(x, y, width, height, 0, entity);
	}

	public CompassTab(int x, int y, int width, int height, EnumFacing dir) {
		this(x, y, width, height, 0, dir);
	}

	public CompassTab(int x, int y, int width, int height, int angle, Entity entity) {
		this(x, y, width, height, "", 0, entity);
	}

	public CompassTab(int x, int y, int width, int height, int angle, EnumFacing dir) {
		this(x, y, width, height, "", 0, dir);
	}

	public CompassTab(int x, int y, int width, int height, String title, int angle, Entity entity) {
		super(x, y, width, height, angle);
		this.title = title;
		if ((angle % 180) == 0) {
			if ((title != null) && !title.isEmpty()) {
				double size = Math.min((width * .9), (height - (2 + TextRenderer.getFontRenderer().FONT_HEIGHT)) * .75);
				compass = new Compass(0, 0, (int) (size), (int) (size), entity);
			} else {
				double size = Math.min(width * .75, height * .75);
				compass = new Compass(0, 0, (int) (size), (int) (size), entity);
			}
		} else {
			if ((title != null) && !title.isEmpty()) {
				double size = Math.min((width - (2 + TextRenderer.getFontRenderer().FONT_HEIGHT)) * .75, (height * .9));
				compass = new Compass(0, 0, (int) (size), (int) (size), entity);
			} else {
				double size = Math.min(width * .75, height * .75);
				compass = new Compass(0, 0, (int) (size), (int) (size), entity);
			}
		}
	}

	public CompassTab(int x, int y, int width, int height, String title, int angle, EnumFacing dir) {
		super(x, y, width, height, angle);
		this.title = title;
		if ((angle % 180) == 0) {
			if ((title != null) && !title.isEmpty()) {
				double size = Math.min((width * .9), (height - (2 + TextRenderer.getFontRenderer().FONT_HEIGHT)) * .75);
				compass = new Compass(0, 0, (int) (size), (int) (size), dir);
			} else {
				double size = Math.min(width * .75, height * .75);
				compass = new Compass(0, 0, (int) (size), (int) (size), dir);
			}
		} else {
			if ((title != null) && !title.isEmpty()) {
				double size = Math.min((width - (2 + TextRenderer.getFontRenderer().FONT_HEIGHT)) * .75, (height * .9));
				compass = new Compass(0, 0, (int) (size), (int) (size), dir);
			} else {
				double size = Math.min(width * .75, height * .75);
				compass = new Compass(0, 0, (int) (size), (int) (size), dir);
			}
		}
	}

	public Compass getCompass() {
		return compass;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public void onDraw(int mouseX, int mouseY, float partialTicks) {
		beginDrawingTab(mouseX, mouseY, partialTicks);
		if (!isHidden) {
			if (angle == 180) {
				if ((title != null) && !title.isEmpty()) {
					GlStateManager.translate(width, 0, 1);
					TextRenderer.renderString((int) (width / 2.0) - 2, 5, title, Color.white, TextAlignment.CENTER);
					GlStateManager.translate(-compass.getWidth() / 4,
							(height / 9) + (2 + TextRenderer.getFontRenderer().FONT_HEIGHT), 0);
				} else {
					GlStateManager.translate(width - (compass.getWidth() / 4), height / 9, 1);
				}
				compass.onDraw(mouseX, mouseY, partialTicks);
			} else if (angle == 90) {
				if ((title != null) && !title.isEmpty()) {
					GlStateManager.translate(-height, 0, 1);
					TextRenderer.renderString((int) (height / 2.0) + 2, 5, title, Color.white, TextAlignment.CENTER);
					GlStateManager.translate(compass.getWidth() / 4,
							(width / 9) + (2 + TextRenderer.getFontRenderer().FONT_HEIGHT), 0);
				} else {
					GlStateManager.translate(-height + (compass.getWidth() / 4), width / 9, 1);
				}
				compass.onDraw(mouseX, mouseY, partialTicks);
			} else if (angle == 270) {
				if ((title != null) && !title.isEmpty()) {
					GlStateManager.translate(height, 0, 0);
					TextRenderer.renderString((int) (height / 2.0) - 2, 5, title, Color.white, TextAlignment.CENTER);
					GlStateManager.translate(-compass.getWidth() / 4,
							(width / 9) + (2 + TextRenderer.getFontRenderer().FONT_HEIGHT), 0);
				} else {
					GlStateManager.translate(height - (compass.getWidth() / 4), width / 9, 1);
				}
				compass.onDraw(mouseX, mouseY, partialTicks);
			} else {

				if ((title != null) && !title.isEmpty()) {
					GlStateManager.translate(-width, 0, 0);
					TextRenderer.renderString((int) (width / 2.0) - 2, 5, title, Color.white, TextAlignment.CENTER);
					GlStateManager.translate(compass.getWidth() / 4,
							(height / 9) + (2 + TextRenderer.getFontRenderer().FONT_HEIGHT), 0);
				} else {
					GlStateManager.translate(-width + (compass.getWidth() / 4), height / 9, 0);
				}
				compass.onDraw(mouseX, mouseY, partialTicks);
			}
		}
		finishDrawingTab(mouseX, mouseY, partialTicks);
		setHoverText(Arrays.asList(new String[] { "Currently Facing", "       " + TextFormatting.GOLD.toString()
				+ StringUtils.capitalize(compass.getCurrentDir().getName()) }));
		drawHoverText(mouseX, mouseY, partialTicks);

	}

	public void setCompass(Compass compass) {
		int size = this.compass.getWidth();
		this.compass = compass;
		this.compass.setWidth(size);
		this.compass.setHeight(size);
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
