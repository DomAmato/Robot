package com.dyn.robot.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelDynRobot extends ModelBase {

	public ModelRenderer bipedHead;
	public ModelRenderer bipedBody;
	public ModelRenderer bipedRightArm;
	public ModelRenderer bipedLeftArm;
	public ModelRenderer bipedRightLeg;
	public ModelRenderer bipedLeftLeg;
	public ModelRenderer antenna;

	public ModelDynRobot() {
		bipedHead = new ModelRenderer(this, 0, 0);
		bipedHead.addBox(-5.5F, -6.0F, -3.5F, 11, 6, 7);
		bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedBody = new ModelRenderer(this, 6, 13);
		bipedBody.addBox(-3.5F, 0.0F, -1.5F, 7, 5, 3);
		bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedRightArm = new ModelRenderer(this, 26, 13);
		bipedRightArm.addBox(-0.5F, 0.0F, -0.5F, 1, 4, 1);
		bipedRightArm.setRotationPoint(-4.0F, 1.0F, 0.0F);
		bipedLeftArm = new ModelRenderer(this, 26, 13);
		bipedLeftArm.mirror = true;
		bipedLeftArm.addBox(-0.5F, 0.0F, -0.5F, 1, 4, 1);
		bipedLeftArm.setRotationPoint(4.0F, 1.0F, 0.0F);
		bipedRightLeg = new ModelRenderer(this, 0, 13);
		bipedRightLeg.addBox(-1.0F, 0.0F, -0.5F, 2, 4, 1);
		bipedRightLeg.setRotationPoint(-1.9F, 4.0F, 0.0F);
		bipedLeftLeg = new ModelRenderer(this, 0, 13);
		bipedLeftLeg.mirror = true;
		bipedLeftLeg.addBox(-1.0F, 0.0F, -0.5F, 2, 4, 1);
		bipedLeftLeg.setRotationPoint(1.9F, 4.0F, 0.0F);
		antenna = new ModelRenderer(this, 0, 21);
		antenna.addBox(-0.5F, -9.0F, -0.5F, 1, 3, 1);
		antenna.setRotationPoint(0.0F, 0.0F, 0.0F);
	}

	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	@Override
	public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_,
			float p_78088_6_, float scale) {
		super.render(entityIn, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale);
		setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);
		GlStateManager.pushMatrix();
		{
			GlStateManager.translate(0.0F, 1.0F, 0.0F);
			antenna.render(scale);
			bipedHead.render(scale);
			bipedBody.render(scale);
			bipedRightArm.render(scale);
			bipedLeftArm.render(scale);
			bipedRightLeg.render(scale);
			bipedLeftLeg.render(scale);

		}
		GlStateManager.popMatrix();
	}

	/**
	 * Sets the model's various rotation angles. For bipeds, par1 and par2 are
	 * used for animating the movement of arms and legs, where par1 represents
	 * the time(so that arms and legs swing back and forth) and par2 represents
	 * how "far" arms and legs can swing at most.
	 */
	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch, float scaleFactor, Entity entityIn) {

		// head rotation seems fine
		bipedHead.rotateAngleY = netHeadYaw / (180F / (float) Math.PI);
		bipedHead.rotateAngleX = headPitch / (180F / (float) Math.PI);

		bipedRightArm.rotateAngleX = MathHelper.cos((limbSwing * 0.6662F) + (float) Math.PI) * 2.0F * limbSwingAmount
				* 0.5F;
		bipedLeftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
		bipedRightArm.rotateAngleZ = 0.0F;
		bipedLeftArm.rotateAngleZ = 0.0F;
		bipedRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		bipedLeftLeg.rotateAngleX = MathHelper.cos((limbSwing * 0.6662F) + (float) Math.PI) * 1.4F * limbSwingAmount;
		bipedRightLeg.rotateAngleY = 0.0F;
		bipedLeftLeg.rotateAngleY = 0.0F;

		bipedRightArm.rotateAngleY = 0.0F;
		bipedLeftArm.rotateAngleY = 0.0F;

		ModelBase.copyModelAngles(bipedHead, antenna);
	}
}
