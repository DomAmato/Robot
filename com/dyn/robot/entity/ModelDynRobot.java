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
		this.bipedHead = new ModelRenderer(this, 0, 0);
		this.bipedHead.addBox(-5.5F, -6.0F, -3.5F, 11, 6, 7);
		this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.bipedBody = new ModelRenderer(this, 6, 13);
		this.bipedBody.addBox(-3.5F, 0.0F, -1.5F, 7, 5, 3);
		this.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.bipedRightArm = new ModelRenderer(this, 26, 13);
		this.bipedRightArm.addBox(-0.5F, 0.0F, -0.5F, 1, 4, 1);
		this.bipedRightArm.setRotationPoint(-4.0F, 1.0F, 0.0F);
		this.bipedLeftArm = new ModelRenderer(this, 26, 13);
		this.bipedLeftArm.mirror = true;
		this.bipedLeftArm.addBox(-0.5F, 0.0F, -0.5F, 1, 4, 1);
		this.bipedLeftArm.setRotationPoint(4.0F, 1.0F, 0.0F);
		this.bipedRightLeg = new ModelRenderer(this, 0, 13);
		this.bipedRightLeg.addBox(-1.0F, 0.0F, -0.5F, 2, 4, 1);
		this.bipedRightLeg.setRotationPoint(-1.9F, 4.0F, 0.0F);
		this.bipedLeftLeg = new ModelRenderer(this, 0, 13);
		this.bipedLeftLeg.mirror = true;
		this.bipedLeftLeg.addBox(-1.0F, 0.0F, -0.5F, 2, 4, 1);
		this.bipedLeftLeg.setRotationPoint(1.9F, 4.0F, 0.0F);
		this.antenna = new ModelRenderer(this, 0, 21);
		this.antenna.addBox(-0.5F, -9.0F, -0.5F, 1, 3, 1);
		this.antenna.setRotationPoint(0.0F, 0.0F, 0.0F);
	}

	/**
	 * Sets the models various rotation angles then renders the model.
	 */
	@Override
	public void render(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_,
			float p_78088_6_, float scale) {
		this.setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, entityIn);
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 1.0F, 0.0F);
		this.antenna.render(scale);
		this.bipedHead.render(scale);
		this.bipedBody.render(scale);
		this.bipedRightArm.render(scale);
		this.bipedLeftArm.render(scale);
		//with the full biped rotation stuff we have to do this for some reason...
		//GlStateManager.translate(0.0F, -0.5F, 0.0F);
		this.bipedRightLeg.render(scale);
		this.bipedLeftLeg.render(scale);

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

		//head rotation seems fine
		this.bipedHead.rotateAngleY = netHeadYaw / (180F / (float) Math.PI);
		this.bipedHead.rotateAngleX = headPitch / (180F / (float) Math.PI);
		
		this.bipedRightArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount
				* 0.5F;
		this.bipedLeftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
		this.bipedRightArm.rotateAngleZ = 0.0F;
		this.bipedLeftArm.rotateAngleZ = 0.0F;
		this.bipedRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.bipedLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.bipedRightLeg.rotateAngleY = 0.0F;
		this.bipedLeftLeg.rotateAngleY = 0.0F;

		this.bipedRightArm.rotateAngleY = 0.0F;
		this.bipedLeftArm.rotateAngleY = 0.0F;
		
		ModelDynRobot.copyModelAngles(bipedHead, antenna);
	}
}
