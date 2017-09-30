package com.dom.rjm.events;

import com.dom.rjm.api.APIHandler;
import com.dom.rjm.api.APIRegistry;
import com.dom.rjm.api.Python2MinecraftApi;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MCEventHandlerClientOnly extends MCEventHandler {
	protected World[] worlds = { null };

	public MCEventHandlerClientOnly() {
		super();
		doRemote = true;
	}

	@Override
	public World[] getWorlds() {
		worlds[0] = Minecraft.getMinecraft().world;
		return worlds;
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onChatEvent(ClientChatReceivedEvent event) {
		Python2MinecraftApi.ChatDescription cd = new Python2MinecraftApi.ChatDescription(
				Minecraft.getMinecraft().player.getEntityId(), event.getMessage().toString());
		for (APIHandler apiHandler : apiHandlers) {
			apiHandler.addChatDescription(cd);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onClientTick(TickEvent.ClientTickEvent event) {
		runQueue();
	}
}
