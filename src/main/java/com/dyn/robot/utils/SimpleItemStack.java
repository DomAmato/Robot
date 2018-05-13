package com.dyn.robot.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SimpleItemStack {
	private Item item;
	public Item getItem() {
		return item;
	}

	public int getMeta() {
		return meta;
	}

	private int meta;
	
	public SimpleItemStack(Item item, int meta) {
		this.item = item;
		this.meta = meta;
	}
	
	public SimpleItemStack(ItemStack stack) {
		this.item = stack.getItem();
		this.meta = stack.getMetadata();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((item == null) ? 0 : item.hashCode());
		result = prime * result + meta;
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if(other instanceof SimpleItemStack) {
			return this.item == ((SimpleItemStack)other).getItem() && this.meta == ((SimpleItemStack)other).getMeta();
		}
		else if(other instanceof ItemStack) {
			return this.item == ((ItemStack)other).getItem() && this.meta == ((ItemStack)other).getMetadata();
		}
		return false;
	}
}
