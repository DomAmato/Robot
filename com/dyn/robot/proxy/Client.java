package com.dyn.robot.proxy;

import java.util.List;

import com.dyn.render.RenderMod;
import com.dyn.robot.entity.DynRobotEntity;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.entity.render.DynRobotRenderer;
import com.dyn.robot.reference.Reference;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Client implements Proxy {

	@Override
	public void init() {

	}

	@Override
	public void openRobotGui() {
		// TODO Auto-generated method stub

	}

	@Override
	public void openRobotProgrammingWindow(World world, BlockPos pos, Entity entity) {
		int radius = 5;
		// hopefully this works... its possible robots will overlap each
		// other

		List<EntityRobot> robots = world.getEntitiesWithinAABB(DynRobotEntity.class, AxisAlignedBB.fromBounds(
				pos.getX(), pos.getY(), pos.getZ(), pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius));
		for (EntityRobot robot : robots) {
			System.out.println("dyn robot owners: " + robot.getOwner());
		}
		if (robots.size() > 0) {
			if (robots.get(0).getClientComputer() == null) {
				robots.get(0).createClientComputer().turnOn();
			} else if (!robots.get(0).getClientComputer().isOn()) {
				robots.get(0).getClientComputer().turnOn();
			}
			System.out.println("Found " + robots.size() + " Robots");
			// display the gui here
			// eventually would be nice to have tabbed panels
			if (RenderMod.proxy.getProgrammingInterface().getRobot() != robots.get(0)) {
				RenderMod.proxy.createNewProgrammingInterface(robots.get(0));
			}

			RenderMod.proxy.openRobotInterface();
		} else {
			System.out.println("No robots owned by player found");
		}
	}

	@Override
	public void preInit() {
		RenderingRegistry.registerEntityRenderingHandler(DynRobotEntity.class, new DynRobotRenderer());
	}

	@Override
	public void registerBlockItem(Block block) {
		String blockName = block.getUnlocalizedName().replace("tile.", "");
		Item blockItem = GameRegistry.findItem(Reference.MOD_ID, blockName);
		ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation(Reference.MOD_ID + ":" + blockName,
				"inventory");
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(blockItem, 0, itemModelResourceLocation);
	}

	@Override
	public void registerItem(Item item, String name, int meta) {
		if (name.contains("item.")) {
			name = name.replace("item.", "");
		}
		ModelResourceLocation location = new ModelResourceLocation(Reference.MOD_ID + ":" + name, "inventory");
		ModelLoader.setCustomModelResourceLocation(item, meta, location);
	}

	/**
	 * @see forge.reference.proxy.Proxy#renderGUI()
	 */
	@Override
	public void renderGUI() {
		// Render GUI when on call from client
	}
}