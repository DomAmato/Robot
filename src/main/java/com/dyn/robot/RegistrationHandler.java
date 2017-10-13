package com.dyn.robot;

import com.dyn.robot.entity.SimpleRobotEntity;
import com.dyn.robot.items.ItemSimpleRobotBlock;
import com.dyn.robot.reference.Reference;
import com.google.common.base.Preconditions;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class RegistrationHandler {
	private static int entityID = 0;

	private static <E extends Entity> EntityEntryBuilder<E> createBuilder(final String name) {
		final EntityEntryBuilder<E> builder = EntityEntryBuilder.create();
		final ResourceLocation registryName = new ResourceLocation(Reference.MOD_ID, name);
		return builder.id(registryName, RegistrationHandler.entityID++).name(name);

	}

	@SubscribeEvent
	public static void registerBlocks(final RegistryEvent.Register<Block> event) {
		event.getRegistry().register(RobotMod.robot_block);
		event.getRegistry().register(RobotMod.robot_magent);
	}

	@SubscribeEvent
	public static void registerEntities(final RegistryEvent.Register<EntityEntry> event) {
		EntityEntry result = RegistrationHandler.createBuilder("robot").entity(SimpleRobotEntity.class)
				.tracker(64, 3, false).build();
		event.getRegistry().register(result);
	}

	@SubscribeEvent
	public static void registerItemBlocks(final RegistryEvent.Register<Item> event) {
		ItemSimpleRobotBlock roblock = new ItemSimpleRobotBlock(RobotMod.robot_block);
		final Block block = roblock.getBlock();
		final ResourceLocation registryName = Preconditions.checkNotNull(block.getRegistryName(),
				"Block %s has null registry name", block);
		event.getRegistry().register(roblock.setRegistryName(registryName));

		ItemBlock robmag = new ItemBlock(RobotMod.robot_magent);
		final Block robmagblock = robmag.getBlock();
		final ResourceLocation robmagregistryName = Preconditions.checkNotNull(robmagblock.getRegistryName(),
				"Block %s has null registry name", robmagblock);
		event.getRegistry().register(robmag.setRegistryName(robmagregistryName));

		RegistrationHandler.registerTileEntities();
	}

	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> event) {
		event.getRegistry().register(RobotMod.robot_remote);
		event.getRegistry().register(RobotMod.robot_wrench);
		event.getRegistry().register(RobotMod.robot_spawner);
		event.getRegistry().register(RobotMod.expChip);
		event.getRegistry().register(RobotMod.card);
		event.getRegistry().register(RobotMod.ram);
		event.getRegistry().register(RobotMod.whistle);
		event.getRegistry().register(RobotMod.neuralyzer);
		event.getRegistry().register(RobotMod.printer);
		event.getRegistry().register(RobotMod.manual);
	}

	private static void registerTileEntities() {

	}
}