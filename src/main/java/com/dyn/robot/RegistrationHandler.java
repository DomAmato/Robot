package com.dyn.robot;

import com.dyn.robot.blocks.RobotBlockTileEntity;
import com.dyn.robot.blocks.RobotJammerTileEntity;
import com.dyn.robot.entity.SimpleRobotEntity;
import com.dyn.robot.reference.Reference;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class RegistrationHandler {
	private static int entityID = 0;

	private static <E extends Entity> EntityEntryBuilder<E> createBuilder(final String name) {
		final EntityEntryBuilder<E> builder = EntityEntryBuilder.create();
		final ResourceLocation registryName = new ResourceLocation(Reference.MOD_ID, name);
		return builder.id(registryName, RegistrationHandler.entityID++).name(name);

	}

	@SubscribeEvent()
	public static void registerBlocks(final RegistryEvent.Register<Block> event) {
		event.getRegistry().register(RobotMod.robot_block);
		event.getRegistry().register(RobotMod.robot_jammer);
		event.getRegistry().register(RobotMod.robot_magent);
	}

	@SubscribeEvent
	public static void registerEntities(final RegistryEvent.Register<EntityEntry> event) {
		EntityEntry result = RegistrationHandler.createBuilder("robot").entity(SimpleRobotEntity.class)
				.tracker(64, 3, false).spawn(EnumCreatureType.CREATURE, 1, 1, 3, Biomes.MESA).build();
		event.getRegistry().register(result);
	}

	@SubscribeEvent
	public static void registerItemBlocks(final RegistryEvent.Register<Item> event) {
		// instead of using the static instance create a new block instance instead?
		event.getRegistry().register(RobotMod.robot_block.getItemBlock());
		event.getRegistry().register(RobotMod.robot_jammer.getItemBlock());
		event.getRegistry().register(RobotMod.robot_magent.getItemBlock());

		RegistrationHandler.registerTileEntities();
	}

	@SubscribeEvent()
	public static void registerItems(final RegistryEvent.Register<Item> event) {
		event.getRegistry().register(RobotMod.robot_remote);
		event.getRegistry().register(RobotMod.robot_wrench);
		event.getRegistry().register(RobotMod.robot_spawner);
		event.getRegistry().register(RobotMod.expChip);
		event.getRegistry().register(RobotMod.meter);
		event.getRegistry().register(RobotMod.card);
		event.getRegistry().register(RobotMod.sim_card);
		event.getRegistry().register(RobotMod.ram);
		event.getRegistry().register(RobotMod.whistle);
		event.getRegistry().register(RobotMod.neuralyzer);
		event.getRegistry().register(RobotMod.manual);
		event.getRegistry().register(RobotMod.equipment);
	}

	/**
	 * Register this mod's {@link SoundEvent}s.
	 *
	 * @param event
	 *            The event
	 */
	@SubscribeEvent
	public static void registerSoundEvents(final RegistryEvent.Register<SoundEvent> event) {
		event.getRegistry().register(RobotMod.ROBOT_ON);
		event.getRegistry().register(RobotMod.ROBOT_REMOTE);
		event.getRegistry().register(RobotMod.ROBOT_ERROR);
		event.getRegistry().register(RobotMod.ROBOT_HARSH);
		event.getRegistry().register(RobotMod.ROBOT_BEEP);
		event.getRegistry().register(RobotMod.ROBOT_WHISTLE);
	}

	private static void registerTileEntities() {
		GameRegistry.registerTileEntity(RobotBlockTileEntity.class, "robot_block_te");
		GameRegistry.registerTileEntity(RobotJammerTileEntity.class, "robot_jammer_te");
	}

}