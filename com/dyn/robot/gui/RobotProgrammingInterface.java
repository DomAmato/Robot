package com.dyn.robot.gui;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.rabbit.gui.component.control.Button;
import com.rabbit.gui.component.control.MultiTextbox;
import com.rabbit.gui.component.control.PictureButton;
import com.rabbit.gui.component.control.TextBox;
import com.rabbit.gui.component.display.Panel;
import com.rabbit.gui.component.display.Picture;
import com.rabbit.gui.show.Show;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class RobotProgrammingInterface extends Show {

	protected final EntityRobot robot;
	private String termText;

	public RobotProgrammingInterface() {
		title = "Turtle Programmer";
		termText = "Welcome to the progamming interface!";
		robot = null;
	}

	public RobotProgrammingInterface(EntityRobot robot) {
		title = "Robot Remote Interface";
		this.robot = robot;
	}

	public EntityRobot getRobot() {
		return robot;
	}

	@Override
	public void setup() {
		super.setup();

		Panel panel = new Panel((int) (width * .55), 0, (int) (width * .45), height);

		registerComponent(panel);
		// The Panel background
		panel.registerComponent(new Picture(0, 0, (panel.getWidth()), (panel.getHeight()),
				new ResourceLocation("dyn", "textures/gui/background2.png")));

		panel.registerComponent(new MultiTextbox(10, 15, panel.getWidth() - 20, panel.getHeight() - 25)
				.setText(termText).setBackgroundVisibility(false).setDrawUnicode(true)
				.setTextChangedListener((TextBox textbox, String previousText) -> {
					termText = previousText;
				}));

		panel.registerComponent(new PictureButton(panel.getWidth() - 15, 0, 15, 15,
				new ResourceLocation("dyn", "textures/gui/exit.png")).setClickListener(btn -> {
					RobotMod.proxy.toggleRenderRobotProgramInterface(false);
					Minecraft.getMinecraft().setIngameFocus();
				}));

		panel.registerComponent(new Button(0, 0, 45, 15, "<<Game").setClickListener(btn -> {
			RobotMod.proxy.toggleRenderRobotProgramInterface(true);
			Minecraft.getMinecraft().setIngameFocus();
		}));
	}

}
