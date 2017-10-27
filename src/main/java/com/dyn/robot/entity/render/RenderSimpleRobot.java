package com.dyn.robot.entity.render;

import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.entity.SimpleRobotEntity;
import com.dyn.robot.reference.Reference;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;

public class RenderSimpleRobot extends RenderLiving<SimpleRobotEntity> {

	RenderChatBubble chatBubble = new RenderChatBubble();

	public RenderSimpleRobot(RenderManager rendermanagerIn, ModelSimpleRobot modelDynRobot, float shadowSize) {
		super(rendermanagerIn, modelDynRobot, shadowSize);
		this.addLayer(new LayerHeldItem(this) {
			@Override
			public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float p_177141_2_, float p_177141_3_,
					float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
				ItemStack itemstack = entitylivingbaseIn.getHeldItemMainhand();

				if (itemstack != null) {
					GlStateManager.pushMatrix();

					((ModelSimpleRobot) getMainModel()).postRenderArm(0.0625F);
					GlStateManager.translate(0F, 0.275F, -0.0625F);

					Item item = itemstack.getItem();

					if ((item instanceof ItemBlock) && (Block.getBlockFromItem(item).getRenderType(
							Block.getBlockFromItem(item).getDefaultState()) == EnumBlockRenderType.MODEL)) {
						GlStateManager.translate(0.0F, 0.1875F, -0.3125F);
						GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
						GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
						GlStateManager.scale(-0.375F, -0.375F, 0.375F);
					} else {
						GlStateManager.scale(-.5, -.5, .5);
					}

					GlStateManager.pushMatrix();
					GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
					Minecraft.getMinecraft().getItemRenderer().renderItemSide(entitylivingbaseIn, itemstack,
							ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, false);
					GlStateManager.popMatrix();

					GlStateManager.popMatrix();
				}
			}
		});
	}

	@Override
	protected ResourceLocation getEntityTexture(SimpleRobotEntity entity) {
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
	public void renderName(SimpleRobotEntity entity, double x, double y, double z) {
		super.renderName(entity, x, y, z);
		if ((entity.getMessages() != null) && (entity.getMessages().size() > 0)) {
			float height = 1.6F;
			float offset = entity.height
					* (1.2f + (entity.getAlwaysRenderNameTag() ? (entity.hasCustomName() ? 0.15f : 0.25f) : 0.0f));
			chatBubble.renderMessages(entity, x, y + offset, z, 0.666667f * height,
					isInRange(entity, renderManager.renderViewEntity, 4.0));
		}
	}

}