package com.dyn.robot.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.network.NetworkManager;
import com.dyn.robot.network.messages.MessageTeleportRobot;
import com.rabbit.gui.component.control.Button;
import com.rabbit.gui.component.display.HorizontalLine;
import com.rabbit.gui.component.display.Panel;
import com.rabbit.gui.component.display.Picture;
import com.rabbit.gui.component.display.Shape;
import com.rabbit.gui.component.display.ShapeType;
import com.rabbit.gui.component.display.TextLabel;
import com.rabbit.gui.component.list.ScrollableDisplayList;
import com.rabbit.gui.component.list.entries.ListEntry;
import com.rabbit.gui.component.list.entries.MultiComponentListEntry;
import com.rabbit.gui.render.TextAlignment;
import com.rabbit.gui.show.Show;
import com.rabbit.gui.utils.DefaultTextures;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

public class MagnetScreen extends Show {

	List<EntityRobot> ownedRobots = new ArrayList();
	BlockPos pos;
	IBlockState state;

	public MagnetScreen(BlockPos pos, IBlockState state, List<EntityRobot> robots) {
		ownedRobots = robots;
		this.pos = pos;
		this.state = state;
	}

	@Override
	public void setup() {
		Panel panel = new Panel((int) (width * .35), (int) (height * .05), (int) (width * .5), (int) (height * .9))
				.setVisible(true).setFocused(true).setCanDrag(true);

		panel.registerComponent(new Picture(0, 0, panel.getWidth(), panel.getHeight(), DefaultTextures.BACKGROUND2));

		List<ListEntry> buttonEntries = new ArrayList();

		buttonEntries.add(new MultiComponentListEntry()
				.registerComponent(new Shape(0, 0, panel.getWidth() - 10, 30, ShapeType.RECT, Color.lightGray), 0, 0)
				.registerComponent(new TextLabel(0, 0, panel.getWidth() - 10, 10, Color.black,
						"Activated Robots Nearby", TextAlignment.CENTER), 5, 10)
				.registerComponent(new HorizontalLine(0, 0, panel.getWidth() - 4, 2, Color.white), 0, 30));

		for (EntityRobot robot : ownedRobots) {
			buttonEntries.add(new MultiComponentListEntry()
					.registerComponent(new TextLabel(0, 0, (int) (panel.getWidth() * .8), 10, robot.getRobotName()), 5,
							7)
					.registerComponent(new TextLabel(0, 0, (int) (panel.getWidth() * .8), 10,
							"Pos - X:" + (int) (robot.posX) + " Y:" + (int) (robot.posY) + " Z:" + (int) (robot.posZ)),
							5, 17)
					.registerComponent(
							new Button(0, 0, (int) (panel.getWidth() * .3), 28, "Warp").setClickListener(btn -> {
								NetworkManager.sendToServer(new MessageTeleportRobot(robot.getEntityId(), pos.up(),
										state.getValue(BlockHorizontal.FACING)));
								Minecraft.getMinecraft().setIngameFocus();
							}), (int) (panel.getWidth() * .7) - 10, 1)
					.registerComponent(new HorizontalLine(0, 0, panel.getWidth() - 4, 2, Color.white), 0, 30));
		}

		if (buttonEntries.size() == 1) {
			buttonEntries.add(new MultiComponentListEntry()
					.registerComponent(new TextLabel(0, 0, panel.getWidth() - 10, 10, "-- No Robots Found -- ")
							.setTextAlignment(TextAlignment.CENTER), 5, 10));
		}

		panel.registerComponent(
				new ScrollableDisplayList(5, 5, panel.getWidth() - 10, panel.getHeight() - 10, 30, buttonEntries));

		// this is my lazy way of not having to rearrange things
		// panel.reverseComponents();

		registerComponent(panel);
	}
}
