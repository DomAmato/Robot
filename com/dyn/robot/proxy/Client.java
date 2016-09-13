package com.dyn.robot.proxy;

import com.dyn.robot.reference.Reference;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;

public class Client implements Proxy {

	@Override
	public void init() {
//		ItemMeshDefinition turtleMeshDefinition = new ItemMeshDefinition() {
//			private ModelResourceLocation turtle_dynamic = new ModelResourceLocation("computercraft:turtle_dynamic",
//					"inventory");
//
//			@Override
//			public ModelResourceLocation getModelLocation(ItemStack stack) {
//				return turtle_dynamic;
//			}
//		};
//		String[] turtleModelNames = { "CC-TurtleJunior", "turtleJunior_black", "turtleJunior_red", "turtleJunior_green",
//				"turtleJunior_brown", "turtleJunior_blue", "turtleJunior_purple", "turtleJunior_cyan",
//				"turtleJunior_lightGrey", "turtleJunior_grey", "turtleJunior_pink", "turtleJunior_lime",
//				"turtleJunior_yellow", "turtleJunior_lightBlue", "turtleJunior_magenta", "turtleJunior_orange",
//				"turtleJunior_white", "turtleJunior_overlay_3dglasses", "turtleJunior_overlay_bandana_blue",
//				"turtleJunior_overlay_bandana_purple", "turtleJunior_overlay_bandana_red",
//				"turtleJunior_overlay_bandana_yellow", "turtleJunior_overlay_creeper", "turtleJunior_overlay_crown",
//				"turtleJunior_overlay_elf", "turtleJunior_overlay_flames", "turtleJunior_overlay_frown",
//				"turtleJunior_overlay_moustache", "turtleJunior_overlay_smile", "turtleJunior_overlay_snorkel",
//				"turtleJunior_overlay_strawhat", "turtleJunior_overlay_sunglasses", "turtleJunior_overlay_teeth",
//				"turtleJunior_overlay_tuxedo" };
//
//		registerItemModel(RobotMod.dynRobot, turtleMeshDefinition, turtleModelNames);
	}

	@Override
	public void openRobotGui() {
		// TODO Auto-generated method stub

	}
	
	public void registerItem(Item item, String name, int meta) {
		ModelResourceLocation location = new ModelResourceLocation(Reference.MOD_ID + ":" + name, "inventory");
		ModelLoader.setCustomModelResourceLocation(item, meta, location);
	}

//	private void registerItemModel(Block block, ItemMeshDefinition definition, String[] names) {
//		registerItemModel(Item.getItemFromBlock(block), definition, names);
//	}
//
//	private void registerItemModel(Item item, ItemMeshDefinition definition, String[] names) {
//		for (int i = 0; i < names.length; i++) {
//			ModelBakery.addVariantName(item, new String[] { "dynrobot:" + names[i] });
//		}
//		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, definition);
//	}
//
//	private void registerItemModel(Item item, String name) {
//		name = Reference.MOD_ID+ ":" + name;
//		final ModelResourceLocation res = new ModelResourceLocation(name, "inventory");
//		ModelBakery.addVariantName(item, new String[] { name });
//		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, stack -> res);
//	}

	/**
	 * @see forge.reference.proxy.Proxy#renderGUI()
	 */
	@Override
	public void renderGUI() {
		// Render GUI when on call from client
	}
}