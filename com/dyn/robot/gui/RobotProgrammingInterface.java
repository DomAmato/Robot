package com.dyn.robot.gui;

import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.dyn.DYNServerMod;
import com.dyn.robot.RobotMod;
import com.dyn.robot.api.RobotAPI;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.server.network.NetworkDispatcher;
import com.dyn.server.network.messages.MessageRunPythonScript;
import com.dyn.utils.FileUtils;
import com.rabbit.gui.component.control.Button;
import com.rabbit.gui.component.control.CodeInterface;
import com.rabbit.gui.component.control.PictureButton;
import com.rabbit.gui.component.control.TextBox;
import com.rabbit.gui.component.display.Panel;
import com.rabbit.gui.component.display.Picture;
import com.rabbit.gui.component.display.TextLabel;
import com.rabbit.gui.component.list.DisplayList;
import com.rabbit.gui.component.list.ScrollableDisplayList;
import com.rabbit.gui.component.list.entries.ListEntry;
import com.rabbit.gui.component.list.entries.SelectElementEntry;
import com.rabbit.gui.component.list.entries.SelectListEntry;
import com.rabbit.gui.component.list.entries.SelectStringEntry;
import com.rabbit.gui.show.Show;

import mobi.omegacentauri.raspberryjammod.network.CodeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import scala.actors.threadpool.Arrays;

public class RobotProgrammingInterface extends Show {

	protected final EntityRobot robot;
	private String termText;
	private String fileName;

	private File[] files;
	private File selectedFile;

	private File currentDir;

	private CodeInterface codeWindow;
	private TextLabel openPath;

	private Panel errorPanel;
	private TextLabel errorLabel;
	private ScrollableDisplayList fileList;
	private TextBox fileNameEntry;

	private Path pathBase = Paths.get(Minecraft.getMinecraft().mcDataDir.getAbsolutePath());

	public RobotProgrammingInterface() {
		title = "Robot Programmer";
		termText = "#Welcome to the progamming interface!";

		currentDir = DYNServerMod.scriptsLoc;
		files = DYNServerMod.scriptsLoc.listFiles((FilenameFilter) (file, name) -> name.toLowerCase().endsWith(".py"));
		robot = null;
	}

	public RobotProgrammingInterface(EntityRobot robot) {
		title = "Robot Remote Interface";
		termText = "#Welcome to the progamming interface!";

		currentDir = DYNServerMod.scriptsLoc;
		files = DYNServerMod.scriptsLoc.listFiles((FilenameFilter) (file, name) -> name.toLowerCase().endsWith(".py"));
		this.robot = robot;
		RobotAPI.robotId = robot.getEntityId();
	}

	public void entrySelected(SelectListEntry entry, DisplayList dlist, int mouseX, int mouseY) {
		if (entry.getTitle().equals("..")) {
			dlist.clear();
			currentDir = currentDir.getParentFile();
			openPath.setText("Open File: " + pathBase.relativize(Paths.get(currentDir.getAbsolutePath())));
			files = currentDir.listFiles((FilenameFilter) (file, name) -> name.toLowerCase().endsWith(".py")
					|| new File(file, name).isDirectory());
			if (!pathBase.relativize(Paths.get(currentDir.getAbsolutePath())).equals(".")) {
				// dont leave the minecraft directory
				dlist.add(new SelectStringEntry("..", (SelectStringEntry sentry, DisplayList sdlist, int smouseX,
						int smouseY) -> entrySelected(sentry, sdlist, smouseX, smouseY)));
			}
			for (File subfile : files) {
				dlist.add(new SelectElementEntry(subfile, subfile.getName(),
						(SelectElementEntry sentry, DisplayList sdlist, int smouseX, int smouseY) -> {
							entrySelected(sentry, sdlist, smouseX, smouseY);
						}));
			}

		} else {
			if (((File) entry.getValue()).isDirectory()) {
				currentDir = (File) entry.getValue();
				openPath.setText("Open File: " + pathBase.relativize(Paths.get(currentDir.getAbsolutePath())));
				dlist.clear();
				files = currentDir.listFiles((FilenameFilter) (file, name) -> name.toLowerCase().endsWith(".py")
						|| new File(file, name).isDirectory());
				dlist.add(new SelectStringEntry("..", (SelectStringEntry sentry, DisplayList sdlist, int smouseX,
						int smouseY) -> entrySelected(sentry, sdlist, smouseX, smouseY)));
				for (File subfile : files) {
					dlist.add(new SelectElementEntry(subfile, subfile.getName(),
							(SelectElementEntry sentry, DisplayList sdlist, int smouseX, int smouseY) -> {
								entrySelected(sentry, sdlist, smouseX, smouseY);
							}));
				}
			} else {
				selectedFile = (File) entry.getValue();
			}
		}
	}

	public EntityRobot getRobot() {
		return robot;
	}

	public void handleErrorMessage(CodeEvent.ErrorEvent e) {
		// we have to subtract 2 since lines start at 0 and the error produces
		// an extra space
		errorPanel.setVisible(true);
		errorLabel.setText(e.getError());
		if (e.getError().contains("NameError")) {
			// name errors dont seem to have the same offset as other errors
			codeWindow.notifyError(e.getLine() - 1, e.getCode(), e.getError());
		} else {
			codeWindow.notifyError(e.getLine() - 2, e.getCode(), e.getError());
		}

	}

	@Override
	public void setup() {
		super.setup();

		Panel openPanel = new Panel((int) (width * .25), (int) (height * .23), (int) (width * .5), (int) (height * .55))
				.setVisible(false);

		registerComponent(openPanel);

		openPanel.registerComponent(new Picture(0, 0, (openPanel.getWidth()), (openPanel.getHeight()),
				new ResourceLocation("dyn", "textures/gui/background.png")));

		openPanel.registerComponent(openPath = new TextLabel(10, 10, 100, 15, Color.black,
				"Open File: " + pathBase.relativize(Paths.get(currentDir.getAbsolutePath()))));

		// components
		ArrayList<ListEntry> scriptFiles = new ArrayList<ListEntry>();

		scriptFiles.add(new SelectStringEntry("..", (SelectStringEntry entry, DisplayList dlist, int mouseX,
				int mouseY) -> entrySelected(entry, dlist, mouseX, mouseY)));
		for (File file : files) {
			scriptFiles.add(new SelectElementEntry(file, file.getName(), (SelectElementEntry entry, DisplayList dlist,
					int mouseX, int mouseY) -> entrySelected(entry, dlist, mouseX, mouseY)));
		}

		openPanel.registerComponent(fileList = new ScrollableDisplayList((int) (openPanel.getWidth() * .1),
				(int) (openPanel.getHeight() * .2), (int) (openPanel.getWidth() * .8),
				(int) (openPanel.getHeight() * .6), 15, scriptFiles));

		openPanel.registerComponent(
				new Button((int) (openPanel.getWidth() * .2), (int) (openPanel.getHeight() * .85), 45, 15, "Open")
						.setClickListener(btn -> {
							termText = FileUtils.readFile(selectedFile);
							codeWindow.setText(termText);
							openPanel.setVisible(false);
						}));

		openPanel.registerComponent(
				new Button((int) (openPanel.getWidth() * .7), (int) (openPanel.getHeight() * .85), 45, 15, "Cancel")
						.setClickListener(btn -> {
							openPanel.setVisible(false);
						}));

		Panel savePanel = new Panel((int) (width * .33), (int) (height * .33), (int) (width * .45),
				(int) (height * .33)).setVisible(false);

		registerComponent(savePanel);

		savePanel.registerComponent(new Picture(0, 0, (savePanel.getWidth()), (savePanel.getHeight()),
				new ResourceLocation("dyn", "textures/gui/background.png")));

		savePanel.registerComponent(new TextLabel((int) (savePanel.getWidth() * .1), (int) (savePanel.getHeight() * .1),
				(int) (savePanel.getWidth() * .8), 15, "Save File As:"));

		savePanel.registerComponent(fileNameEntry = new TextBox((int) (savePanel.getWidth() * .1),
				(int) (savePanel.getHeight() * .25), (int) (savePanel.getWidth() * .8), 15, "File Name")
						.setTextChangedListener((TextBox textbox, String previousText) -> {
							fileName = previousText;
						}));

		savePanel.registerComponent(
				new Button((savePanel.getWidth() / 3) - 23, savePanel.getHeight() - 35, 45, 15, "Save")
						.setClickListener(btn -> {
							if ((fileName != null) && !fileName.isEmpty()) {
								try {
									FileUtils.writeFile(new File(DYNServerMod.scriptsLoc, fileName + ".py"),
											Arrays.asList(termText.split(Pattern.quote("\n"))));
								} catch (Exception e) {
									DYNServerMod.logger.error("Could not create script file", e);
								}
							}
							fileNameEntry.setText("");
							fileName = "";
							savePanel.setVisible(false);
						}));

		savePanel.registerComponent(
				new Button(((savePanel.getWidth() / 3) * 2) - 23, savePanel.getHeight() - 35, 45, 15, "Cancel")
						.setClickListener(btn -> {
							fileNameEntry.setText("");
							fileName = "";
							savePanel.setVisible(false);
						}));

		Panel panel = new Panel((int) (width * .55), 0, (int) (width * .45), height);

		registerComponent(panel);
		// The Panel background
		panel.registerComponent(new Picture(0, 0, (panel.getWidth()), (panel.getHeight()),
				new ResourceLocation("dyn", "textures/gui/background2.png")));

		panel.registerComponent((codeWindow = new CodeInterface(10, 15, panel.getWidth() - 20, panel.getHeight() - 35))
				.setText(termText).setBackgroundVisibility(false).setDrawUnicode(true)
				.setTextChangedListener((TextBox textbox, String previousText) -> {
					termText = previousText;
				}));

		// menu panel
		panel.registerComponent(new Button(0, 0, (panel.getWidth() - 15) / 4, 15, "<<Game").setClickListener(btn -> {
			RobotMod.proxy.toggleRenderRobotProgramInterface(true);
			Minecraft.getMinecraft().setIngameFocus();
		}));

		panel.registerComponent(new Button((panel.getWidth() - 15) / 4, 0, (panel.getWidth() - 15) / 4, 15, "Open")
				.setClickListener(btn -> {
					files = DYNServerMod.scriptsLoc
							.listFiles((FilenameFilter) (file, name) -> name.toLowerCase().endsWith(".py"));

					fileList.clear();
					fileList.add(new SelectStringEntry("..", (SelectStringEntry entry, DisplayList dlist, int mouseX,
							int mouseY) -> entrySelected(entry, dlist, mouseX, mouseY)));
					for (File file : files) {
						fileList.add(new SelectElementEntry(file, file.getName(),
								(SelectElementEntry entry, DisplayList dlist, int mouseX,
										int mouseY) -> entrySelected(entry, dlist, mouseX, mouseY)));
					}
					openPanel.setVisible(true);
				}));

		panel.registerComponent(new Button((panel.getWidth() - 15) / 2, 0, (panel.getWidth() - 15) / 4, 15, "Save")
				.setClickListener(btn -> {
					savePanel.setVisible(true);
				}));

		panel.registerComponent(new Button(((panel.getWidth() - 15) / 4) * 3, 0, (panel.getWidth() - 15) / 4, 15, "Run")
				.setClickListener(btn -> {
					codeWindow.clearError();
					errorPanel.setVisible(false);
					NetworkDispatcher.sendToServer(new MessageRunPythonScript(termText));
				}));

		panel.registerComponent(new PictureButton(panel.getWidth() - 15, 0, 15, 15,
				new ResourceLocation("dyn", "textures/gui/exit.png")).setDrawsButton(false).setClickListener(btn -> {
					RobotMod.proxy.toggleRenderRobotProgramInterface(false);
					Minecraft.getMinecraft().setIngameFocus();
				}));

		panel.registerComponent(errorPanel = new Panel(0, (int) (panel.getHeight() * .8), panel.getWidth(),
				(int) (panel.getHeight() * .2)).setVisible(false));

		errorPanel.registerComponent(new Picture(0, 0, (errorPanel.getWidth()), (errorPanel.getHeight()),
				new ResourceLocation("dyn", "textures/gui/background2.png")));

		errorPanel.registerComponent(
				errorLabel = new TextLabel(10, 20, errorPanel.getWidth() - 20, errorPanel.getHeight() - 20, "")
						.setMultilined(true));

		errorPanel.registerComponent(new TextLabel(10, 5, errorPanel.getWidth() - 20, 10, "Error Description:"));

		errorPanel.registerComponent(new PictureButton(panel.getWidth() - 10, 0, 10, 10,
				new ResourceLocation("dyn", "textures/gui/exit.png")).setDrawsButton(false).setClickListener(btn -> {
					errorPanel.setVisible(false);
				}));

	}

}
