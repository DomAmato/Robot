package com.dom.robot.items;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import com.dom.robot.RobotMod;
import com.dom.robot.reference.Reference;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemReferenceManual extends ItemWrittenBook {

	public ItemReferenceManual() {
		super();
		setUnlocalizedName("robot_manual");
		setCreativeTab(RobotMod.roboTab);
		setRegistryName(Reference.MOD_ID, getUnlocalizedName());
	}

	/**
	 * returns a list of items with the same ID, but different meta (eg: dye
	 * returns 16 items)
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		try {
			URL manualUrl = new URL("http://broad-participation.s3.amazonaws.com/manualList.json");

			JsonParser parser = new JsonParser();
			JsonObject manualList = parser.parse(new JsonReader(new InputStreamReader(manualUrl.openStream())))
					.getAsJsonObject();

			JsonArray manualUrls = manualList.get("manuals").getAsJsonArray();
			for (JsonElement mUrl : manualUrls) {
				try {

					URL url = new URL(mUrl.getAsString());

					JsonObject robotDocJson = parser.parse(new JsonReader(new InputStreamReader(url.openStream())))
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
					pagesNbt.appendTag(new NBTTagString(
							"Could not create manual, something went wrong :(\n" + e.getLocalizedMessage()));

					NBTTagCompound tag = new NBTTagCompound();
					tag.setString("author", "DYN Minecraft Team");
					tag.setString("title", "API Manual");
					tag.setTag("pages", pagesNbt);

					ItemStack is = new ItemStack(Items.WRITTEN_BOOK);
					is.setTagCompound(tag);

					items.add(is);
				}
			}
		} catch (Exception e) {
			RobotMod.logger.error("Could not get manual list", e);
			NBTTagList pagesNbt = new NBTTagList();
			pagesNbt.appendTag(new NBTTagString(
					"Could not create manuals, something went wrong :(\n\n" + e.getLocalizedMessage()));

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
