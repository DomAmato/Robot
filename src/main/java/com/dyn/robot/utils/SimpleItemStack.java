package com.dyn.robot.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SimpleItemStack {
	private Item item;
	private int meta;

	public SimpleItemStack(Item item, int meta) {
		this.item = item;
		this.meta = meta;
	}

	public SimpleItemStack(ItemStack stack) {
		item = stack.getItem();
		meta = stack.getMetadata();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof SimpleItemStack) {
			return (item == ((SimpleItemStack) other).getItem()) && (meta == ((SimpleItemStack) other).getMeta());
		} else if (other instanceof ItemStack) {
			return (item == ((ItemStack) other).getItem()) && (meta == ((ItemStack) other).getMetadata());
		}
		return false;
	}

	public Item getItem() {
		return item;
	}

	public int getMeta() {
		return meta;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((item == null) ? 0 : item.hashCode());
		result = (prime * result) + meta;
		return result;
	}
}
