package com.dyn.robot.api;

import java.util.Map;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleCommand;
import dan200.computercraft.core.apis.IAPIEnvironment;
import dan200.computercraft.core.apis.ILuaAPI;
import dan200.computercraft.shared.turtle.core.InteractDirection;
import dan200.computercraft.shared.turtle.core.TurtleCheckRedstoneCommand;
import dan200.computercraft.shared.turtle.core.TurtleInspectCommand;
import dan200.computercraft.shared.turtle.core.TurtleSetRedstoneCommand;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class DynRobotAPI implements ILuaAPI {
	private IAPIEnvironment m_environment;
	private ITurtleAccess m_turtle;

	public DynRobotAPI(IAPIEnvironment environment, ITurtleAccess turtle) {
		m_environment = environment;
		m_turtle = turtle;
	}

	@Override
	public void advance(double _dt) {
	}

	@Override
	public Object[] callMethod(ILuaContext context, int method, Object[] args)
			throws LuaException, InterruptedException {
		switch (method) {
		case 0:
			return tryInspectCommand(context, new TurtleInspectCommand(InteractDirection.Forward));
		case 1:
			return tryInspectCommand(context, new TurtleInspectCommand(InteractDirection.Up));
		case 2:
			return tryInspectCommand(context, new TurtleInspectCommand(InteractDirection.Down));
		case 3:
			int slot = parseOptionalSlotNumber(args, 0, m_turtle.getSelectedSlot());
			ItemStack stack = m_turtle.getInventory().getStackInSlot(slot);
			if ((stack != null) && (stack.stackSize > 0)) {
				Item item = stack.getItem();
				String name = Item.itemRegistry.getNameForObject(item).toString();
				return new Object[] { name };
			}
			return new Object[] { "minecraft:air" };
		case 4:
			return tryCommand(context, new TurtleCheckRedstoneCommand(m_environment, InteractDirection.Forward));
		case 5:
			return tryCommand(context, new TurtleCheckRedstoneCommand(m_environment, InteractDirection.Up));
		case 6:
			return tryCommand(context, new TurtleCheckRedstoneCommand(m_environment, InteractDirection.Down));
		case 7: {
			int value = parseRedstone(args, 0);
			return tryCommand(context, new TurtleSetRedstoneCommand(m_environment, InteractDirection.Forward, value));
		}
		case 8: {
			int value = parseRedstone(args, 0);
			return tryCommand(context, new TurtleSetRedstoneCommand(m_environment, InteractDirection.Up, value));
		}
		case 9: {
			int value = parseRedstone(args, 0);
			return tryCommand(context, new TurtleSetRedstoneCommand(m_environment, InteractDirection.Down, value));
		}
		case 10:
			if (args.length < 1) {
				throw new LuaException("Expected string");
			}
			String text;
			if (args[0] != null) {
				text = args[0].toString();
				if (text.endsWith(".0")) {
					text = text.substring(0, text.length() - 2);
				}
			} else {
				text = "nil";
			}
			return tryCommand(context, new TurtleSayCommand(text));
		}
		return null;
	}

	@Override
	public String[] getMethodNames() {
		return new String[] { "inspect", "inspectUp", "inspectDown", "getItemName", "checkRedstone", "checkRedstoneUp",
				"checkRedstoneDown", "setRedstone", "setRedstoneUp", "setRedstoneDown", "say" };
	}

	@Override
	public String[] getNames() {
		return new String[] { "dynrobot" };
	}

	private int parseOptionalSlotNumber(Object[] arguments, int index, int fallback) throws LuaException {
		if ((arguments.length <= index) || (!(arguments[index] instanceof Number))) {
			return fallback;
		}
		int slot = ((Number) arguments[index]).intValue();
		if ((slot >= 1) && (slot <= 16)) {
			return slot - 1;
		}
		throw new LuaException("Slot number " + slot + " out of range");
	}

	private int parseRedstone(Object[] arguments, int index) throws LuaException {
		if ((arguments.length <= index) || (!(arguments[index] instanceof Boolean))) {
			throw new LuaException("Expected boolean");
		}
		boolean bool = ((Boolean) arguments[index]).booleanValue();
		return bool ? 15 : 0;
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void startup() {
	}

	private Object[] tryCommand(ILuaContext context, ITurtleCommand command) throws LuaException, InterruptedException {
		return m_turtle.executeCommand(context, command);
	}

	private Object[] tryInspectCommand(ILuaContext context, TurtleInspectCommand command)
			throws LuaException, InterruptedException {
		Object[] results = m_turtle.executeCommand(context, command);
		if ((results.length >= 2) && ((results[1] instanceof Map))) {
			Map<Object, Object> table = (Map) results[1];
			return new Object[] { table.get("name") };
		}
		return new Object[] { "minecraft:air" };
	}
}
