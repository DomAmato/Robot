package com.dyn.robot;

import java.util.HashSet;
import java.util.Set;

import com.dyn.robot.blocks.BlockRobotJammer;
import com.dyn.robot.reference.Reference;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Reference.MOD_ID)
public class ModelManager {
	public static final ModelManager INSTANCE = new ModelManager();

	/**
	 * Register this mod's {@link Fluid}, {@link Block} and {@link Item} models.
	 *
	 * @param event
	 *            The event
	 */
	@SubscribeEvent
	public static void registerAllModels(final ModelRegistryEvent event) {
		ModelManager.INSTANCE.registerBlockModels();
		ModelManager.INSTANCE.registerItemModels();
	}

	/**
	 * A {@link StateMapperBase} used to create property strings.
	 */
	private final StateMapperBase propertyStringMapper = new StateMapperBase() {
		@Override
		protected ModelResourceLocation getModelResourceLocation(final IBlockState state) {
			return new ModelResourceLocation("minecraft:air");
		}
	};

	/**
	 * The {@link Item}s that have had models registered so far.
	 */
	private final Set<Item> itemsRegistered = new HashSet<>();

	private ModelManager() {
	}

	/**
	 * Register a model for a metadata value of the {@link Block}'s {@link Item}.
	 * <p>
	 * Uses the registry name as the domain/path and the {@link IBlockState} as the
	 * variant.
	 *
	 * @param state
	 *            The state to use as the variant
	 * @param metadata
	 *            The item metadata to register the model for
	 */
	private void registerBlockItemModel(final IBlockState state) {
		final Block block = state.getBlock();
		final Item item = Item.getItemFromBlock(block);

		if (item != Items.AIR) {
			registerItemModel(item, new ModelResourceLocation(block.getRegistryName(),
					propertyStringMapper.getPropertyString(state.getProperties())));
		}

	}

	/**
	 * Register this mod's {@link Block} models.
	 */
	private void registerBlockModels() {
		registerBlockItemModel(RobotMod.robot_block.getDefaultState());
		registerBlockItemModel(
				RobotMod.robot_jammer.getDefaultState().withProperty(BlockRobotJammer.POWERED, Boolean.valueOf(true)));
		registerBlockItemModel(RobotMod.robot_magent.getDefaultState());
	}

	/**
	 * Register a single model for an {@link Item}.
	 * <p>
	 * Uses the registry name as the domain/path and {@code "inventory"} as the
	 * variant.
	 *
	 * @param item
	 *            The Item
	 */
	private void registerItemModel(final Item item) {
		registerItemModel(item, item.getRegistryName().toString());
	}

	/**
	 * Register an {@link ItemMeshDefinition} for an {@link Item}.
	 *
	 * @param item
	 *            The Item
	 * @param meshDefinition
	 *            The ItemMeshDefinition
	 */
	private void registerItemModel(final Item item, final ItemMeshDefinition meshDefinition) {
		itemsRegistered.add(item);
		ModelLoader.setCustomMeshDefinition(item, meshDefinition);
	}

	/**
	 * Register a single model for an {@link Item}.
	 * <p>
	 * Uses {@code fullModelLocation} as the domain, path and variant.
	 *
	 * @param item
	 *            The Item
	 * @param fullModelLocation
	 *            The full model location
	 */
	private void registerItemModel(final Item item, final ModelResourceLocation fullModelLocation) {
		ModelBakery.registerItemVariants(item, fullModelLocation);
		// Ensure the custom model is loaded and prevent the
		// default model from being loaded
		registerItemModel(item, stack -> fullModelLocation);
	}

	/**
	 * Register a single model for an {@link Item}.
	 * <p>
	 * Uses {@code modelLocation} as the domain/path and {@link "inventory"} as the
	 * variant.
	 *
	 * @param item
	 *            The Item
	 * @param modelLocation
	 *            The model location
	 */
	private void registerItemModel(final Item item, final String modelLocation) {
		final ModelResourceLocation fullModelLocation = new ModelResourceLocation(modelLocation, "inventory");
		registerItemModel(item, fullModelLocation);
	}

	/**
	 * Register a model for a metadata value of an {@link Item}.
	 * <p>
	 * Uses {@code modelResourceLocation} as the domain, path and variant.
	 *
	 * @param item
	 *            The Item
	 * @param metadata
	 *            The metadata
	 * @param modelResourceLocation
	 *            The full model location
	 */
	private void registerItemModelForMeta(final Item item, final int metadata,
			final ModelResourceLocation modelResourceLocation) {
		itemsRegistered.add(item);
		ModelLoader.setCustomModelResourceLocation(item, metadata, modelResourceLocation);
	}

	/**
	 * Register this mod's {@link Item} models.
	 */
	private void registerItemModels() {

		registerItemModelForMeta(RobotMod.robot_spawner, 0,
				new ModelResourceLocation(Reference.MOD_ID + ":robot_spawn_plus", "inventory"));
		registerItemModelForMeta(RobotMod.robot_spawner, 1,
				new ModelResourceLocation(Reference.MOD_ID + ":robot_spawn", "inventory"));
		// registerItemModel(RobotMod.expChip,
		// Reference.MOD_ID+":expansion_chip");
		for (int i = 0; i < 16; i++) {
			registerItemModelForMeta(RobotMod.expChip, i,
					new ModelResourceLocation(Reference.MOD_ID + ":expansion_chip_" + i, "inventory"));
		}
		// registerItemModel(RobotMod.ram, Reference.MOD_ID+":robot_memory");
		for (int i = 0; i < 8; i++) {
			registerItemModelForMeta(RobotMod.ram, i,
					new ModelResourceLocation(Reference.MOD_ID + ":robot_memory_" + i, "inventory"));
		}

		for (int i = 0; i < 3; i++) {
			registerItemModelForMeta(RobotMod.equipment, i,
					new ModelResourceLocation(Reference.MOD_ID + ":robot_suit_" + i, "inventory"));
		}

		registerItemModel(RobotMod.card);
		registerItemModel(RobotMod.sim_card);
		registerItemModel(RobotMod.meter);
		registerItemModel(RobotMod.robot_remote);
		registerItemModel(RobotMod.robot_wrench);
		registerItemModel(RobotMod.whistle);
		registerItemModel(RobotMod.neuralyzer);
		registerItemModel(RobotMod.manual);
	}
}