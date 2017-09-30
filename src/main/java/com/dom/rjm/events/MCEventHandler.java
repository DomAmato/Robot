package com.dom.rjm.events;

import java.util.ArrayList;
import java.util.List;

import com.dom.rjm.RaspberryJamMod;
import com.dom.rjm.actions.ServerAction;
import com.dom.rjm.actions.SetBlockNBT;
import com.dom.rjm.api.APIHandler;
import com.dom.rjm.network.CodeEvent;
import com.dom.rjm.util.Location;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

abstract public class MCEventHandler {
	protected List<ServerAction> serverActionQueue = new ArrayList<>();
	public volatile boolean stopChanges = false;
	protected volatile boolean pause = false;
	protected boolean doRemote;
	protected List<APIHandler> apiHandlers = new ArrayList<>();

	public MCEventHandler() {
	}

	public String describeBlockState(Location pos) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		synchronized (serverActionQueue) {
			for (int i = serverActionQueue.size() - 1; i >= 0; i--) {
				ServerAction entry = serverActionQueue.get(i);
				if (entry.contains(pos.getWorld(), x, y, z)) {
					return entry.describe();
				}
			}
		}

		IBlockState state = pos.getWorld().getBlockState(pos);
		Block block = state.getBlock();
		int meta = block.getMetaFromState(state);
		String describe = "" + Block.getIdFromBlock(block) + "," + meta + ",";

		TileEntity tileEntity = pos.getWorld().getTileEntity(pos);
		if (tileEntity == null) {
			return describe;
		}
		NBTTagCompound tag = new NBTTagCompound();
		tileEntity.writeToNBT(tag);
		SetBlockNBT.scrubNBT(tag);
		return describe + tag.toString();
	}

	// @SubscribeEvent
	// public void onInitMapGenEvent(InitMapGenEvent event) {
	// System.out.println("Init map gen");
	// FMLCommonHandler.instance().getMinecraftServerInstance().setDifficultyForAllWorlds(EnumDifficulty.PEACEFUL);
	// }

	// @SubscribeEvent
	// public void onKeyInput(InputEvent.KeyInputEvent event) {
	// if(KeyBindings.superchat.isPressed()) {
	// System.out.println("superchat");
	// Minecraft.getMinecraft().displayGuiScreen(new MyChat());
	// }
	// }

	public int getBlockId(Location pos) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		synchronized (serverActionQueue) {
			for (int i = serverActionQueue.size() - 1; i >= 0; i--) {
				ServerAction entry = serverActionQueue.get(i);
				if (entry.contains(pos.getWorld(), x, y, z)) {
					return entry.getBlockId();
				}
			}
		}

		return Block.getIdFromBlock(pos.getWorld().getBlockState(pos).getBlock());
	}

	public int getBlockMeta(Location pos) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		synchronized (serverActionQueue) {
			for (int i = serverActionQueue.size() - 1; i >= 0; i--) {
				ServerAction entry = serverActionQueue.get(i);
				if (entry.contains(pos.getWorld(), x, y, z)) {
					return entry.getBlockId();
				}
			}
		}
		IBlockState state = pos.getWorld().getBlockState(pos);
		return state.getBlock().getMetaFromState(state);
	}

	public IBlockState getBlockState(Location pos) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		synchronized (serverActionQueue) {
			for (int i = serverActionQueue.size() - 1; i >= 0; i--) {
				ServerAction entry = serverActionQueue.get(i);
				if (entry.contains(pos.getWorld(), x, y, z)) {
					return entry.getBlockState();
				}
			}
		}

		return pos.getWorld().getBlockState(pos);
	}

	public abstract World[] getWorlds();

	@SubscribeEvent
	public void onFailEvent(CodeEvent.FailEvent event) {
		for (APIHandler apiHandler : apiHandlers) {
			apiHandler.onFail(event);
		}
	}

	@SubscribeEvent
	public void onPlayerInteractEvent(LeftClickBlock event) {
		if ((event.getEntityPlayer() == null)
				|| (event.getEntityPlayer().getEntityWorld().isRemote != RaspberryJamMod.clientOnlyAPI)) {
			return;
		}

		for (APIHandler apiHandler : apiHandlers) {
			apiHandler.onClick(event);
		}
	}

	@SubscribeEvent
	public void onPlayerInteractEvent(RightClickBlock event) {
		if ((event.getEntityPlayer() == null)
				|| (event.getEntityPlayer().getEntityWorld().isRemote != RaspberryJamMod.clientOnlyAPI)) {
			return;
		}

		for (APIHandler apiHandler : apiHandlers) {
			apiHandler.onClick(event);
		}
	}

	@SubscribeEvent
	public void onSuccessEvent(CodeEvent.RobotSuccessEvent event) {
		for (APIHandler apiHandler : apiHandlers) {
			apiHandler.onSuccess(event);
		}
	}

	public void queueServerAction(ServerAction s) {
		synchronized (serverActionQueue) {
			serverActionQueue.add(s);
		}
	}

	public void registerAPIHandler(APIHandler h) {
		apiHandlers.add(h);
	}

	public void runQueue() {
		if (!pause) {
			synchronized (serverActionQueue) {
				for (ServerAction entry : serverActionQueue) {
					if (!RaspberryJamMod.apiActive) {
						break;
					}
					entry.execute();
				}
				serverActionQueue.clear();
			}
		} else if (!RaspberryJamMod.apiActive) {
			synchronized (serverActionQueue) {
				serverActionQueue.clear();
			}
		}
	}

	public void setPause(boolean b) {
		pause = b;
	}

	public void setStopChanges(boolean stopChanges) {
		this.stopChanges = stopChanges;
	}

	public void unregisterAPIHandler(APIHandler h) {
		apiHandlers.remove(h);
	}
}
