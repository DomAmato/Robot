package com.dyn.robot.api;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleCommand;
import dan200.computercraft.api.turtle.TurtleAnimation;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.turtle.core.TurtleBrain;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

public class TurtleSayCommand implements ITurtleCommand {
	private String m_text;

	public TurtleSayCommand(String text) {
		m_text = text;
	}

	@Override
	public TurtleCommandResult execute(ITurtleAccess turtle) {
		TurtleBrain brain = (TurtleBrain) turtle;
		IComputer computer = brain.getOwner().getComputer();
		String label = computer != null ? computer.getLabel() : null;
		IChatComponent nameComponent;
		if (label != null) {
			nameComponent = new ChatComponentText(label);
		} else {
			nameComponent = new ChatComponentTranslation("gui.dynrobot:remote.turtle", new Object[0]);
		}
		MinecraftServer server = MinecraftServer.getServer();
		if ((server != null)) {
			server.getConfigurationManager().sendChatMsg(new ChatComponentTranslation("chat.type.announcement",
					new Object[] { nameComponent, new ChatComponentText(m_text) }));
		}
		turtle.playAnimation(TurtleAnimation.Wait);
		return TurtleCommandResult.success();
	}
}
