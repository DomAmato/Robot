package com.dyn.robot.block;

import com.dyn.robot.api.DynApi;
import com.dyn.robot.api.IDYNRobotAccess;
import com.dyn.robot.entity.brain.DynRobotBrain;
import com.dyn.robot.api.DynRobotAPI;

import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.turtle.core.TurtleBrain;
import net.minecraft.entity.player.EntityPlayer;

public class TileDynRobot extends TileTurtle {
	@Override
	protected TurtleBrain createBrain() {
		return new DynRobotBrain(this);
	}

	@Override
	protected ServerComputer createComputer(int instanceID, int id) {
		ServerComputer computer = super.createComputer(instanceID, id, 35, 21);
		computer.addAPI(new DynApi(computer.getAPIEnvironment(), getAccess()));
		computer.addAPI(new DynRobotAPI(computer.getAPIEnvironment(), getAccess()));
		return computer;
	}

	@Override
	public IDYNRobotAccess getAccess() {
		return (IDYNRobotAccess) super.getAccess();
	}

	@Override
	protected double getInteractRange(EntityPlayer player) {
		return 32.0D;
	}

	@Override
	public boolean onDefaultComputerInteract(EntityPlayer player) {
		if (!worldObj.isRemote) {
			// TODO: open gui here
		}
		return true;
	}

	@Override
	public void openGUI(EntityPlayer player) {
	}
}
