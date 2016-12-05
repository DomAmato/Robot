package com.dyn.robot.entity.render;

import com.dyn.render.util.RenderChatBubble;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.entity.ModelDynRobot;
import com.dyn.robot.reference.Reference;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderDynRobot extends RenderLiving<EntityRobot> {

	RenderChatBubble chatBubble = new RenderChatBubble();

	public RenderDynRobot(RenderManager rendermanagerIn, ModelDynRobot modelDynRobot, float shadowSize) {
		super(rendermanagerIn, modelDynRobot, shadowSize);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityRobot entity) {
		entity.counter++;
		entity.counter %= 101;
		if ((entity.counter > entity.on1) && (entity.counter < entity.on2)) {
			return new ResourceLocation(Reference.MOD_ID + ":" + "textures/models/robot2.png");
		} else if (entity.counter >= entity.on2) {
			return new ResourceLocation(Reference.MOD_ID + ":" + "textures/models/robot3.png");
		}
		return new ResourceLocation(Reference.MOD_ID + ":" + "textures/models/robot.png");
	}

	public boolean isInRange(EntityRobot entity, Entity player, double range) {
		double x = entity.posX - player.posX;
		double y = entity.posY - player.posY;
		double z = entity.posZ - player.posZ;
		if (x < 0.0) {
			x = -x;
		}
		if (z < 0.0) {
			z = -z;
		}
		if (y < 0.0) {
			y = -y;
		}
		return ((player.posY < 0.0) || (y <= range)) && (x <= range) && (z <= range);
	}

	@Override
	public void renderName(EntityRobot entity, double x, double y, double z) {
		super.renderName(entity, x, y, z);
		if ((entity.getMessages() != null) && (entity.getMessages().size() > 0)) {
			float height = 1.6F;
			float offset = entity.height
					* (1.2f + (entity.getAlwaysRenderNameTag() ? (entity.hasCustomName() ? 0.15f : 0.25f) : 0.0f));
			chatBubble.renderMessages(entity, x, y + offset, z, 0.666667f * height,
					isInRange(entity, renderManager.livingPlayer, 4.0));
		}
	}

}