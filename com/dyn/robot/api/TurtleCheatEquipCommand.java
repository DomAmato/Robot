package com.dyn.robot.api;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleCommand;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleAnimation;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.api.turtle.TurtleSide;

public class TurtleCheatEquipCommand implements ITurtleCommand {
	private final TurtleSide m_side;
	private final ITurtleUpgrade m_upgrade;

	public TurtleCheatEquipCommand(TurtleSide side, ITurtleUpgrade upgrade) {
		m_side = side;
		m_upgrade = upgrade;
	}

	@Override
	public TurtleCommandResult execute(ITurtleAccess turtle) {
		ITurtleUpgrade newUpgrade = m_upgrade;
		ITurtleUpgrade oldUpgrade = turtle.getUpgrade(m_side);
		turtle.setUpgrade(m_side, newUpgrade);
		if (newUpgrade != oldUpgrade) {
			turtle.playAnimation(TurtleAnimation.Wait);
		}
		return TurtleCommandResult.success();
	}
}
