package com.dyn.robot.entity.render;

import com.dyn.robot.entity.ModelDynRobot;
import com.dyn.robot.reference.Reference;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderDynRobot extends RenderLiving {

	public RenderDynRobot(RenderManager rendermanagerIn, ModelDynRobot modelDynRobot, float shadowSize) {
		super(rendermanagerIn, modelDynRobot, shadowSize);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity par1Entity) {
		return new ResourceLocation(Reference.MOD_ID + ":" + "textures/models/robot.png");
	}

}