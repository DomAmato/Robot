package com.dyn.robot.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.network.NetworkManager;
import com.dyn.robot.network.messages.MessageOpenRobotInventory;
import com.rabbit.gui.component.control.Button;
import com.rabbit.gui.component.display.HorizontalLine;
import com.rabbit.gui.component.display.Line;
import com.rabbit.gui.component.display.Panel;
import com.rabbit.gui.component.display.Picture;
import com.rabbit.gui.component.display.Shape;
import com.rabbit.gui.component.display.ShapeType;
import com.rabbit.gui.component.display.TextLabel;
import com.rabbit.gui.component.list.DisplayList;
import com.rabbit.gui.component.list.ScrollableDisplayList;
import com.rabbit.gui.component.list.entries.ButtonEntry;
import com.rabbit.gui.component.list.entries.ListEntry;
import com.rabbit.gui.component.list.entries.MultiComponentListEntry;
import com.rabbit.gui.component.list.entries.SelectElementEntry;
import com.rabbit.gui.render.TextAlignment;
import com.rabbit.gui.show.Show;
import com.rabbit.gui.utils.DefaultTextures;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class RemoteScreen extends Show {

	public RemoteScreen() {
	}
	
	@Override
	public void setup() {
		Panel panel = new Panel((int) (width * .35), (int) (height * .05), (int) (width * .5), (int) (height * .9))
				.setVisible(true).setFocused(true).setCanDrag(true);

		panel.registerComponent(new Picture(0, 0, panel.getWidth(), panel.getHeight(), DefaultTextures.BACKGROUND2));

		List<ListEntry> buttonEntries = new ArrayList();

		EntityPlayer player = Minecraft.getMinecraft().player;

		buttonEntries.add(new MultiComponentListEntry()
				.registerComponent(new Shape(0,0,panel.getWidth()-10, 30, ShapeType.RECT, Color.lightGray), 0, 0)
				.registerComponent(new TextLabel(0, 0, (int) (panel.getWidth()-10), 10, Color.black, "Activated Robots Ready to Connect", TextAlignment.CENTER), 5, 10)
				.registerComponent(new HorizontalLine(0, 0, panel.getWidth() - 4, 2, Color.white), 0, 30));

		for(EntityRobot robot : RobotMod.currentRobots) {
			if(!robot.robot_inventory.getStackInSlot(3).isEmpty() || robot.getPosition().distanceSq(player.getPosition()) < (64 * 64)) {
				buttonEntries.add(new MultiComponentListEntry()
						.registerComponent(new TextLabel(0, 0, (int) (panel.getWidth() * .8), 10, robot.getRobotName()),
								5, 7)
						.registerComponent(new TextLabel(0, 0, (int) (panel.getWidth() * .8), 10,
								"Pos - X:" + (int) (robot.posX) + " Y:" + (int) (robot.posY) + " Z:"
										+ (int) (robot.posZ)),
								5, 17)
						.registerComponent(new Button(0, 0, (int) (panel.getWidth() * .3), 28, "Connect")
								.setClickListener(btn -> {
									NetworkManager.sendToServer(new MessageOpenRobotInventory(robot.getEntityId()));
								}), (int) (panel.getWidth() * .7)-10, 1)
						.registerComponent(new HorizontalLine(0, 0, panel.getWidth() - 4, 2, Color.white), 0, 30));
			}
		}

		if (buttonEntries.size() == 1) {
			buttonEntries.add(new MultiComponentListEntry()
					.registerComponent(new TextLabel(0, 0, (int) (panel.getWidth()-10), 10, "-- No Robots Found -- ")
							.setTextAlignment(TextAlignment.CENTER), 5, 10));
		}

		panel.registerComponent(
				new ScrollableDisplayList(5, 5, panel.getWidth() - 10, panel.getHeight() - 10, 30, buttonEntries));

		// this is my lazy way of not having to rearrange things
		// panel.reverseComponents();

		registerComponent(panel);
	}
}
