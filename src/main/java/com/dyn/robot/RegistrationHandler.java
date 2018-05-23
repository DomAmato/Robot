package com.dyn.robot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;

import com.dyn.robot.blocks.RobotBlockTileEntity;
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
import net.minecraftforge.fml.common.eventhandler.EventPriority;
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

	// we want to register last so that we can create a file
	// of all the items in the registry
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void registerBlocks(final RegistryEvent.Register<Block> event) {
		event.getRegistry().register(RobotMod.robot_block);
		event.getRegistry().register(RobotMod.robot_magent);
		File file = new File(RobotMod.apiFileLocation, "/core/blocks.py");
		Set<String> names = new HashSet();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write("from .block import Block\n\n");
			for (Entry<ResourceLocation, Block> entry : event.getRegistry().getEntries()) {
				Item bItem = Item.getItemFromBlock(entry.getValue());
				if (!(bItem instanceof ItemPotion) && !(bItem instanceof ItemEnchantedBook)
						&& !(bItem instanceof ItemMonsterPlacer) && !(bItem instanceof ItemTippedArrow)) {
					NonNullList<ItemStack> items = NonNullList.create();
					bItem.getSubItems(CreativeTabs.SEARCH, items);
					if (!items.isEmpty()) {
						for (ItemStack item : items) {
							if (Block.getBlockFromItem(item.getItem()) != Blocks.AIR) {
								Block block = Block.getBlockFromItem(item.getItem());
								if (names.add(item.getDisplayName().toUpperCase().replaceAll(" ", "_")
										.replaceAll("([^A-Z0-9a-z\\s_])+", ""))) {
									writer.write(String.format("%1$-33s",
											item.getDisplayName().toUpperCase().replaceAll(" ", "_")
													.replaceAll("([^A-Z0-9a-z\\s_])+", ""))
											+ " = Block(" + Block.getIdFromBlock(block) + ", " + item.getMetadata()
											+ ", \"" + item.getDisplayName() + "\")\n");
								} else {
									if (names.add(entry.getValue().getRegistryName().getResourcePath().toUpperCase()
											.replaceAll(" ", "_").replaceAll("([^A-Z0-9a-z\\s_])+", ""))) {
										writer.write(String.format("%1$-33s",
												entry.getValue().getRegistryName().getResourcePath().toUpperCase()
														.replaceAll(" ", "_").replaceAll("([^A-Z0-9a-z\\s_])+", ""))
												+ " = Block(" + Block.getIdFromBlock(entry.getValue()) + ", " + 0
												+ ", \"" + WordUtils.capitalizeFully(entry.getValue().getRegistryName()
														.getResourcePath().replace("_", " "))
												+ "\")\n");
									} else {
										if (Block.getIdFromBlock(block) == 80) {
											// Snow blocks
											writer.write(String.format("%1$-33s", "SNOW_BLOCK") + " = Block("
													+ Block.getIdFromBlock(entry.getValue()) + ", " + 0
													+ ", \"Snow Block\")\n");
										} else {
											RobotMod.logger.info("Found Duplicate name for Block: "
													+ entry.getValue().getRegistryName().getResourcePath() + " - "
													+ Block.getIdFromBlock(block));

										}
									}
								}
							}
						}
					} else {
						if (names.add(entry.getValue().getRegistryName().getResourcePath().toUpperCase()
								.replaceAll(" ", "_").replaceAll("([^A-Z0-9a-z\\s_])+", ""))) {
							writer.write(String.format("%1$-33s",
									entry.getValue().getRegistryName().getResourcePath().toUpperCase()
											.replaceAll(" ", "_").replaceAll("([^A-Z0-9a-z\\s_])+", ""))
									+ " = Block(" + Block.getIdFromBlock(entry.getValue()) + ", " + 0 + ", \""
									+ WordUtils.capitalizeFully(
											entry.getValue().getRegistryName().getResourcePath().replace("_", " "))
									+ "\")\n");
						} else {
							RobotMod.logger.info("Found Duplicate name for Block: "
									+ entry.getValue().getRegistryName().getResourcePath() + " - "
									+ Block.getIdFromBlock(entry.getValue()));
						}
					}
				}
			}
			writer.write("\n# This looks weird but it makes indexing blocks way easier\nBLOCKS = {\n");
			for (String name : names) {
				if (name != "AIR") {
					writer.write(name.toUpperCase() + ":" + name.toUpperCase() + ",\n");
				}
			}
			writer.write("AIR : AIR\n}");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	// we want to register last so that we can create a file
	// of all the items in the registry
	@SubscribeEvent(priority = EventPriority.LOWEST)
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

		File file = new File(RobotMod.apiFileLocation, "/core/items.py");
		Set<String> names = new HashSet();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write("from .item import Item\n\n");
			writer.write(String.format("%1$-33s", "EMPTY") + " = Item(0, 0, \"Empty\")\n");
			for (Entry<ResourceLocation, Item> entry : event.getRegistry().getEntries()) {
				if (!(entry.getValue() instanceof ItemPotion) && !(entry.getValue() instanceof ItemEnchantedBook)
						&& !(entry.getValue() instanceof ItemMonsterPlacer)
						&& !(entry.getValue() instanceof ItemTippedArrow)) {
					NonNullList<ItemStack> items = NonNullList.create();
					entry.getValue().getSubItems(CreativeTabs.SEARCH, items);
					for (ItemStack item : items) {
						if (names.add(item.getDisplayName().toUpperCase().replaceAll(" ", "_")
								.replaceAll("([^A-Z0-9a-z\\s_])+", ""))) {
							writer.write(String.format("%1$-33s",
									item.getDisplayName().toUpperCase().replaceAll(" ", "_")
											.replaceAll("([^A-Z0-9a-z\\s_])+", ""))
									+ " = Item(" + Item.getIdFromItem(item.getItem()) + ", " + item.getMetadata()
									+ ", \"" + item.getDisplayName().replaceAll("([^A-Z0-9a-z\\s_])+", "") + "\")\n");
						} else {
							if (names.add(item.getItem().getRegistryName().getResourcePath().toUpperCase())) {
								writer.write(String.format("%1$-33s",
										item.getItem().getRegistryName().getResourcePath().toUpperCase()) + " = Item("
										+ Item.getIdFromItem(item.getItem()) + ", " + item.getMetadata() + ", \""
										+ WordUtils.capitalizeFully(
												item.getItem().getRegistryName().getResourcePath().replaceAll("_", " "))
										+ "\")\n");
							} else {
								switch (Item.getIdFromItem(item.getItem())) {
								case 80:
									names.add("SNOW_BLOCK");
									writer.write(String.format("%1$-33s", "SNOW_BLOCK") + " = Item("
											+ Item.getIdFromItem(item.getItem()) + ", " + 0 + ", \"Snow Block\")\n");
									break;
								case 322:
									names.add("ENCHANTED_GOLDEN_APPLE");
									writer.write(String.format("%1$-33s", "ENCHANTED_GOLDEN_APPLE") + " = Item("
											+ Item.getIdFromItem(item.getItem()) + ", " + item.getMetadata()
											+ ", \"Enchanted Golden Apple\")\n");
									break;
								case 360:
									names.add("MELON_SLICE");
									writer.write(String.format("%1$-33s", "MELON_SLICE") + " = Item("
											+ Item.getIdFromItem(item.getItem()) + ", " + 0 + ", \"Melon Slice\")\n");
									break;
								default:
									RobotMod.logger.info("Found Duplicate name for Item: "
											+ item.getItem().getRegistryName().getResourcePath() + " - "
											+ Item.getIdFromItem(item.getItem()));
								}
							}
						}
					}
				}

			}
			writer.write("\n# This looks weird but it makes indexing items way easier\nITEMS = {\n");
			for (String name : names) {
				writer.write(name.toUpperCase() + ":" + name.toUpperCase() + ",\n");
			}
			writer.write("EMPTY : EMPTY\n}");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	}

}