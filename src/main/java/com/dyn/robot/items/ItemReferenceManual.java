package com.dyn.robot.items;

import java.io.InputStreamReader;

import com.dyn.robot.RobotMod;
import com.dyn.robot.reference.Reference;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemReferenceManual extends ItemWrittenBook {

	public ItemReferenceManual() {
		super();
		setUnlocalizedName("robot_manual");
		setCreativeTab(RobotMod.roboTab);
		setRegistryName(Reference.MOD_ID, "robot_manual");
	}

	/**
	 * returns a list of items with the same ID, but different meta (eg: dye returns
	 * 16 items)
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (isInCreativeTab(tab)) {
			try {

				JsonParser parser = new JsonParser();

				JsonObject robotDocJson = parser
						.parse(new JsonReader(new InputStreamReader(
								ItemReferenceManual.class.getResourceAsStream("/assets/roboticraft/manual.json"))))
						.getAsJsonObject();

				JsonArray funcArray = robotDocJson.get("functions").getAsJsonArray();

				NBTTagList pagesNbt = new NBTTagList();

				for (JsonElement func : funcArray) {
					JsonObject jsonObj = func.getAsJsonObject();
					String text = TextFormatting.GOLD + jsonObj.get("package").getAsString() + "\n"
							+ TextFormatting.DARK_GREEN + jsonObj.get("short").getAsString() + "\n"
							+ TextFormatting.BLUE + jsonObj.get("argument_list").getAsString() + "\n\n"
							+ TextFormatting.BLACK + jsonObj.get("description").getAsString();
					ITextComponent itextcomponent = new TextComponentString(text);
					text = ITextComponent.Serializer.componentToJson(itextcomponent);
					pagesNbt.appendTag(new NBTTagString(text));
				}

				ItemStack is = new ItemStack(Items.WRITTEN_BOOK);
				is.setTagInfo("author", new NBTTagString("DYN Minecraft Team"));
				is.setTagInfo("title", new NBTTagString("Python API Manual"));
				is.setTagInfo("pages", pagesNbt);

				items.add(is);
			} catch (Exception e) {
				RobotMod.logger.error("Could not create Manual", e);
				NBTTagList pagesNbt = new NBTTagList();
				ITextComponent itextcomponent = new TextComponentString(
						"Could not create manual, something went wrong :(\n" + e.getLocalizedMessage());
				String text = ITextComponent.Serializer.componentToJson(itextcomponent);
				pagesNbt.appendTag(new NBTTagString(text));

				NBTTagCompound tag = new NBTTagCompound();
				tag.setString("author", "DYN Minecraft Team");
				tag.setString("title", "API Manual");
				tag.setTag("pages", pagesNbt);

				ItemStack is = new ItemStack(Items.WRITTEN_BOOK);
				is.setTagCompound(tag);

				items.add(is);
			}
		}
	}

	/**
	 * Called when item is crafted/smelted. Used only by maps so far.
	 */
	@Override
	public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
		try {

			JsonParser parser = new JsonParser();

			JsonObject robotDocJson = parser
					.parse(new JsonReader(new InputStreamReader(
							ItemReferenceManual.class.getResourceAsStream("/assets/roboticraft/manual.json"))))
					.getAsJsonObject();

			JsonArray funcArray = robotDocJson.get("functions").getAsJsonArray();

			NBTTagList pagesNbt = new NBTTagList();

			for (JsonElement func : funcArray) {
				JsonObject jsonObj = func.getAsJsonObject();
				String text = TextFormatting.GOLD + jsonObj.get("package").getAsString() + "\n"
						+ TextFormatting.DARK_GREEN + jsonObj.get("short").getAsString() + "\n" + TextFormatting.BLUE
						+ jsonObj.get("argument_list").getAsString() + "\n\n" + TextFormatting.BLACK
						+ jsonObj.get("description").getAsString();
				ITextComponent itextcomponent = new TextComponentString(text);
				text = ITextComponent.Serializer.componentToJson(itextcomponent);
				pagesNbt.appendTag(new NBTTagString(text));
			}

			stack.setTagInfo("author", new NBTTagString("DYN Minecraft Team"));
			stack.setTagInfo("title", new NBTTagString("Python API Manual"));
			stack.setTagInfo("pages", pagesNbt);

		} catch (Exception e) {
			RobotMod.logger.error("Could not create Manual", e);
			NBTTagList pagesNbt = new NBTTagList();
			ITextComponent itextcomponent = new TextComponentString(
					"Could not create manual, something went wrong :(\n" + e.getLocalizedMessage());
			String text = ITextComponent.Serializer.componentToJson(itextcomponent);
			pagesNbt.appendTag(new NBTTagString(text));

			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("author", "DYN Minecraft Team");
			tag.setString("title", "API Manual");
			tag.setTag("pages", pagesNbt);

			stack.setTagCompound(tag);
		}
	}

	/**
	 * Called when the equipped item is right clicked.
	 */
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		ItemStack itemstack = new ItemStack(Items.WRITTEN_BOOK);

		itemstack.setTagCompound(playerIn.getHeldItem(handIn).getTagCompound());

		if (!worldIn.isRemote) {
			resolveContents(itemstack, playerIn);
		}

		playerIn.openBook(itemstack, handIn);
		playerIn.addStat(StatList.getObjectUseStats(this));
		return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
	}

	private void resolveContents(ItemStack stack, EntityPlayer player) {
		if (stack.getTagCompound() != null) {
			NBTTagCompound nbttagcompound = stack.getTagCompound();

			if (!nbttagcompound.getBoolean("resolved")) {
				nbttagcompound.setBoolean("resolved", true);

				if (ItemWrittenBook.validBookTagContents(nbttagcompound)) {
					NBTTagList nbttaglist = nbttagcompound.getTagList("pages", 8);

					for (int i = 0; i < nbttaglist.tagCount(); ++i) {
						String s = nbttaglist.getStringTagAt(i);
						ITextComponent itextcomponent;

						try {
							itextcomponent = ITextComponent.Serializer.fromJsonLenient(s);
							itextcomponent = TextComponentUtils.processComponent(player, itextcomponent, player);
						} catch (Exception var9) {
							itextcomponent = new TextComponentString(s);
						}

						nbttaglist.set(i, new NBTTagString(ITextComponent.Serializer.componentToJson(itextcomponent)));
					}

					nbttagcompound.setTag("pages", nbttaglist);

					if ((player instanceof EntityPlayerMP) && (player.getHeldItemMainhand() == stack)) {
						Slot slot = player.openContainer.getSlotFromInventory(player.inventory,
								player.inventory.currentItem);
						((EntityPlayerMP) player).connection.sendPacket(new SPacketSetSlot(0, slot.slotNumber, stack));
					}
				}
			}
		}
	}

}
