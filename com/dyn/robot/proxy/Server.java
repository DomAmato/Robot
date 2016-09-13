package com.dyn.robot.proxy;

import net.minecraft.item.Item;

public class Server implements Proxy {

	@Override
	public void init() {
		// MinecraftForge.EVENT_BUS.register(this);

	}

	@Override
	public void openRobotGui() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see forge.reference.proxy.Proxy#renderGUI()
	 */
	@Override
	public void renderGUI() {
		// Actions on render GUI for the server (logging)

	}

	@Override
	public void registerItem(Item item, String name, int meta) {
		// TODO Auto-generated method stub
		
	}

}