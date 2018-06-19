package com.dyn.robot.entity.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelSimpleRobot extends ModelBase {

	public ModelRenderer robotHead;
	public ModelRenderer robotBody;
	public ModelRenderer robotRightArm;
	public ModelRenderer robotLeftArm;
	public ModelRenderer robotRightLeg;
	public ModelRenderer robotLeftLeg;
	public ModelRenderer antenna;

	// suit rendering
	public ModelRenderer robotHeadwear;
	public ModelRenderer robotLeftArmwear;
	public ModelRenderer robotRightArmwear;
	public ModelRenderer robotLeftLegwear;
	public ModelRenderer robotRightLegwear;
	public ModelRenderer robotBodyWear;

	/**
	 * Records whether the model should be rendered holding an item in the right
	 * hand, and if that item is a block.
	 */
	public int heldItemRight;

	public ModelSimpleRobot() {
		// Robot Renderers
		robotHead = new ModelRenderer(this, 0, 0);
		robotHead.addBox(-5.5F, -6.0F, -3.5F, 11, 6, 7, 0);
		robotHead.setRotationPoint(0.0F, 16.0F, 0.0F);
		robotBody = new ModelRenderer(this, 6, 13);
		robotBody.addBox(-3.5F, 0.0F, -1.5F, 7, 5, 3, 0);
		robotBody.setRotationPoint(0.0F, 16.0F, 0.0F);
		robotRightArm = new ModelRenderer(this, 26, 13);
		robotRightArm.addBox(-0.5F, 0.0F, -0.5F, 1, 4, 1, 0);
		robotRightArm.setRotationPoint(-4.0F, 17.0F, 0.0F);
		robotLeftArm = new ModelRenderer(this, 26, 13);
		robotLeftArm.mirror = true;
		robotLeftArm.addBox(-0.5F, 0.0F, -0.5F, 1, 4, 1, 0);
		robotLeftArm.setRotationPoint(4.0F, 17.0F, 0.0F);
		robotRightLeg = new ModelRenderer(this, 0, 13);
		robotRightLeg.addBox(-1.0F, 0.0F, -0.5F, 2, 4, 1, 0);
		robotRightLeg.setRotationPoint(-1.9F, 20.0F, 0.0F);
		robotLeftLeg = new ModelRenderer(this, 0, 13);
		robotLeftLeg.mirror = true;
		robotLeftLeg.addBox(-1.0F, 0.0F, -0.5F, 2, 4, 1, 0);
		robotLeftLeg.setRotationPoint(1.9F, 20.0F, 0.0F);
		antenna = new ModelRenderer(this, 0, 21);
		antenna.addBox(-0.5F, -9.0F, -0.5F, 1, 3, 1, 0);
		antenna.setRotationPoint(0.0F, 16.0F, 0.0F);

		// Suit Renderers
		robotHeadwear = new ModelRenderer(this, 0, 0);
		robotHeadwear.addBox(-5.5F, -6.0F, -3.5F, 11, 6, 7, 0.5f);
		robotHeadwear.setRotationPoint(0.0F, 16.0F, 0.0F);
		robotBodyWear = new ModelRenderer(this, 6, 13);
		robotBodyWear.addBox(-3.5F, 0.0F, -1.5F, 7, 5, 3, 0.25f);
		robotBodyWear.setRotationPoint(0.0F, 16.0F, 0.0F);
		robotRightArmwear = new ModelRenderer(this, 26, 13);
		robotRightArmwear.addBox(-0.5F, 0.0F, -0.5F, 1, 4, 1, 0.25f);
		robotRightArmwear.setRotationPoint(-4.0F, 17.0F, 0.0F);
		robotLeftArmwear = new ModelRenderer(this, 26, 13);
		robotLeftArmwear.mirror = true;
		robotLeftArmwear.addBox(-0.5F, 0.0F, -0.5F, 1, 4, 1, 0.25f);
		robotLeftArmwear.setRotationPoint(4.0F, 17.0F, 0.0F);
		robotRightLegwear = new ModelRenderer(this, 0, 13);
		robotRightLegwear.addBox(-1.0F, 0.0F, -0.5F, 2, 4, 1, 0.25f);
		robotRightLegwear.setRotationPoint(-1.9F, 20.0F, 0.0F);
		robotLeftLegwear = new ModelRenderer(this, 0, 13);
		robotLeftLegwear.mirror = true;
		robotLeftLegwear.addBox(-1.0F, 0.0F, -0.5F, 2, 4, 1, 0.25f);
		robotLeftLegwear.setRotationPoint(1.9F, 20.0F, 0.0F);
	}

	public void postRenderArm(float scale) {
		robotRightArm.postRender(scale);
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
			antenna.render(scale);
			robotHead.render(scale);
			robotBody.render(scale);
			robotRightArm.render(scale);
			robotLeftArm.render(scale);
			robotRightLeg.render(scale);
			robotLeftLeg.render(scale);
		}
		GlStateManager.popMatrix();
	}

	public void renderSuit(float scale) {
		GlStateManager.pushMatrix();
		{
			robotHeadwear.render(scale);
			robotBodyWear.render(scale);
			robotRightArmwear.render(scale);
			robotLeftArmwear.render(scale);
			robotRightLegwear.render(scale);
			robotLeftLegwear.render(scale);
		}
		GlStateManager.popMatrix();
	}

	/**
	 * Sets the model's various rotation angles. For robots, par1 and par2 are used
	 * for animating the movement of arms and legs, where par1 represents the
	 * time(so that arms and legs swing back and forth) and par2 represents how
	 * "far" arms and legs can swing at most.
	 */
	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch, float scaleFactor, Entity entityIn) {

		robotHead.rotateAngleY = netHeadYaw / (180F / (float) Math.PI);
		robotHead.rotateAngleX = headPitch / (180F / (float) Math.PI);

		robotRightArm.rotateAngleX = MathHelper.cos((limbSwing * 0.6662F) + (float) Math.PI) * 2.0F * limbSwingAmount
				* 0.5F;
		robotLeftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
		robotRightArm.rotateAngleZ = 0.0F;
		robotLeftArm.rotateAngleZ = 0.0F;
		robotRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		robotLeftLeg.rotateAngleX = MathHelper.cos((limbSwing * 0.6662F) + (float) Math.PI) * 1.4F * limbSwingAmount;
		robotRightLeg.rotateAngleY = 0.0F;
		robotLeftLeg.rotateAngleY = 0.0F;

		robotRightArm.rotateAngleY = 0.0F;
		robotLeftArm.rotateAngleY = 0.0F;

		switch (heldItemRight) {
		case 0:
		case 2:
		default:
			break;
		case 1:
			robotRightArm.rotateAngleX = (robotRightArm.rotateAngleX * 0.5F)
					- (((float) Math.PI / 10F) * heldItemRight);
			break;
		case 3:
			robotRightArm.rotateAngleX = (robotRightArm.rotateAngleX * 0.5F)
					- (((float) Math.PI / 10F) * heldItemRight);
			robotRightArm.rotateAngleY = -0.5235988F;
		}

		if (swingProgress > -9990.0F) {
			float f = swingProgress;
			robotBody.rotateAngleY = MathHelper.sin(MathHelper.sqrt(f) * (float) Math.PI * 2.0F) * 0.2F;
			robotRightArm.rotateAngleY += robotBody.rotateAngleY;
			robotLeftArm.rotateAngleY += robotBody.rotateAngleY;
			robotLeftArm.rotateAngleX += robotBody.rotateAngleY;
			f = 1.0F - swingProgress;
			f = f * f;
			f = f * f;
			f = 1.0F - f;
			float f1 = MathHelper.sin(f * (float) Math.PI);
			float f2 = MathHelper.sin(swingProgress * (float) Math.PI) * -(robotHead.rotateAngleX - 0.7F) * 0.75F;
			robotRightArm.rotateAngleX = (float) (robotRightArm.rotateAngleX - ((f1 * 1.2D) + f2));
			robotRightArm.rotateAngleY += robotBody.rotateAngleY * 2.0F;
		}

		robotRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
		robotLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;

		ModelBase.copyModelAngles(robotHead, antenna);
		ModelBase.copyModelAngles(robotHead, robotHeadwear);
		ModelBase.copyModelAngles(robotLeftLeg, robotLeftLegwear);
		ModelBase.copyModelAngles(robotRightLeg, robotRightLegwear);
		ModelBase.copyModelAngles(robotLeftArm, robotLeftArmwear);
		ModelBase.copyModelAngles(robotRightArm, robotRightArmwear);
		ModelBase.copyModelAngles(robotBody, robotBodyWear);
	}
}
