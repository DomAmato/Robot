package com.dyn.robot.entity.render;

import com.dyn.robot.reference.Reference;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerRobotSuits extends LayerArmorBase<ModelSimpleRobot> {
	private RenderLivingBase<?> renderer;

	public LayerRobotSuits(RenderLivingBase<?> rendererIn) {
		super(rendererIn);
		renderer = rendererIn;
	}

	@Override
	public void doRenderLayer(EntityLivingBase entityLivingBaseIn, float limbSwing, float limbSwingAmount,
			float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		ItemStack armor = entityLivingBaseIn.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

		if (!armor.isEmpty()) {
			ModelSimpleRobot robotModel = (ModelSimpleRobot) renderer.getMainModel();
			renderer.bindTexture(getArmorResource(entityLivingBaseIn, armor, null, null));
			GlStateManager.color(1, 1, 1, 1);
			robotModel.renderSuit(scale);
		}
	}

	@Override
	public ResourceLocation getArmorResource(Entity entity, ItemStack stack, EntityEquipmentSlot slot, String type) {
		if (!stack.isEmpty()) {
			switch (stack.getMetadata()) {
			case 0:
				return new ResourceLocation(Reference.MOD_NAME, "textures/models/armor/armor.png");
			case 1:
				return new ResourceLocation(Reference.MOD_NAME, "textures/models/armor/lava.png");
			case 2:
				return new ResourceLocation(Reference.MOD_NAME, "textures/models/armor/scuba.png");
			}
		}
		return null;
	}

	@Override
	protected void initArmor() {
	}

	@Override
	protected void setModelSlotVisible(ModelSimpleRobot p_188359_1_, EntityEquipmentSlot slotIn) {
		// TODO Auto-generated method stub

	}
}