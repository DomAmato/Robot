package com.dyn.robot.entity.render;

import com.dyn.robot.entity.ModelDynRobot;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class DynRobotRenderer implements IRenderFactory {

	@Override
	public Render createRenderFor(RenderManager manager) {
		return new RenderDynRobot(manager, new ModelDynRobot(), 0.3F);
	}

}