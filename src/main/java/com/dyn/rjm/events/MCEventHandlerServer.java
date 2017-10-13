package com.dyn.rjm.events;

import com.dyn.rjm.api.APIHandler;
import com.dyn.rjm.api.Python2MinecraftApi;

import net.minecraft.world.World;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class MCEventHandlerServer extends MCEventHandler {
	public MCEventHandlerServer() {
		super();
		doRemote = false;
	}

	@Override
	public World[] getWorlds() {
		return FMLCommonHandler.instance().getMinecraftServerInstance().worlds;
	}

	@SubscribeEvent
	public void onChatEvent(ServerChatEvent event) {
		Python2MinecraftApi.ChatDescription cd = new Python2MinecraftApi.ChatDescription(
				event.getPlayer().getEntityId(), event.getMessage());

		for (APIHandler apiHandler : apiHandlers) {
			apiHandler.addChatDescription(cd);
		}
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		runQueue();
	}
}
