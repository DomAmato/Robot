package com.dyn.robot;

import java.util.Map.Entry;

import com.dyn.robot.entity.SimpleRobotEntity;
import com.dyn.robot.reference.Reference;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTippedArrow;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
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
		StringBuilder sb = new StringBuilder();
		sb.append("from .block import Block\n\n");
		for (Entry<ResourceLocation, Block> entry : event.getRegistry().getEntries()) {
			Item bItem = Item.getItemFromBlock(entry.getValue());
			if (!(bItem instanceof ItemPotion) && !(bItem instanceof ItemEnchantedBook)
					&& !(bItem instanceof ItemMonsterPlacer) && !(bItem instanceof ItemTippedArrow)) {
				NonNullList<ItemStack> items = NonNullList.create();
				bItem.getSubItems(CreativeTabs.SEARCH, items);
				for (ItemStack item : items) {
					if (Block.getBlockFromItem(item.getItem()) != Blocks.AIR) {
						Block block = Block.getBlockFromItem(item.getItem());
						sb.append(String.format("%1$-33s",
								item.getDisplayName().toUpperCase().replaceAll(" ", "_").replaceAll("([#'\\)\\(])+",
										""))
								+ " = Block(" + Block.getIdFromBlock(block) + ", " + item.getMetadata() + ", \""
								+ item.getDisplayName() + "\")\n");
					}
				}
			}

		}
		sb.toString();
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

		event.getRegistry().register(RobotMod.robot_magent.getItemBlock());

		RegistrationHandler.registerTileEntities();
	}

	@SubscribeEvent
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

		StringBuilder sb = new StringBuilder();
		sb.append("from .item import Item\n\n");
		for (Entry<ResourceLocation, Item> entry : event.getRegistry().getEntries()) {
			if (!(entry.getValue() instanceof ItemPotion) && !(entry.getValue() instanceof ItemEnchantedBook)
					&& !(entry.getValue() instanceof ItemMonsterPlacer)
					&& !(entry.getValue() instanceof ItemTippedArrow)) {
				NonNullList<ItemStack> items = NonNullList.create();
				entry.getValue().getSubItems(CreativeTabs.SEARCH, items);
				for (ItemStack item : items) {
					if (Block.getBlockFromItem(item.getItem()) != Blocks.AIR) {
						Block block = Block.getBlockFromItem(item.getItem());
						sb.append(String.format("%1$-33s",
								item.getDisplayName().toUpperCase().replaceAll(" ", "_").replaceAll("([#'\\)\\(])+",
										""))
								+ " = Item(" + Block.getIdFromBlock(block) + ", " + item.getMetadata() + ", \""
								+ item.getDisplayName() + "\")\n");
					}
				}
			}

		}
		sb.toString();
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

	}

}