package com.dyn.robot.gui;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.network.NetworkManager;
import com.dyn.robot.network.messages.MessageOpenRobotInventory;
import com.dyn.robot.network.messages.MessageReplaceSDCardItemStack;
import com.dyn.robot.network.messages.MessageRunRobotScript;
import com.dyn.robot.network.messages.MessageToggleRobotFollow;
import com.dyn.robot.reference.Reference;
import com.dyn.robot.utils.FileUtils;
import com.google.common.collect.Lists;
import com.rabbit.gui.component.code.CodeInterface;
import com.rabbit.gui.component.control.Button;
import com.rabbit.gui.component.control.DraggableCamera;
import com.rabbit.gui.component.control.PictureButton;
import com.rabbit.gui.component.control.TextBox;
import com.rabbit.gui.component.display.Compass;
import com.rabbit.gui.component.display.Panel;
import com.rabbit.gui.component.display.Picture;
import com.rabbit.gui.component.display.TextLabel;
import com.rabbit.gui.component.display.tabs.CompassTab;
import com.rabbit.gui.component.display.tabs.ItemTab;
import com.rabbit.gui.component.display.tabs.PictureTab;
import com.rabbit.gui.component.display.tabs.Tab;
import com.rabbit.gui.show.Show;
import com.rabbit.gui.utils.DefaultTextures;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

public class RobotProgrammingInterface extends Show {

	protected final EntityRobot robot;
	private String termText;
	private String fileName;

	private String errorText;
	private boolean showError;

	private CodeInterface codeWindow;

	private Panel errorPanel;
	private TextLabel errorLabel;
	private TextBox fileNameEntry;

	private ItemStack sdCard;

	private Tab followTab;
	private Tab invTab;
	private Button runButton;
	private boolean btnStatus;

	public RobotProgrammingInterface() {
		title = "Robot Programmer";
		termText = "#Welcome to the progamming interface!\nfrom api.robot import *\nrobot = Robot()\n\nrobot.say(\"hello world\")\n";
		errorText = "";
		showError = false;
		robot = null;
		btnStatus = true;
	}

	public RobotProgrammingInterface(EntityRobot robot) {
		title = "Robot Remote Interface";
		termText = "#Welcome to the progamming interface!\nfrom api.robot import *\nrobot = Robot()\n\nrobot.say(\"hello world\")\n";
		errorText = "";
		showError = false;
		btnStatus = true;
		this.robot = robot;
	}

	public String getConsoleText() {
		return codeWindow.getText();
	}

	public EntityRobot getRobot() {
		return robot;
	}

	public void handleCompletion() {
		runButton.setIsEnabled(true);
		btnStatus = true;
	}

	public void handleErrorMessage(String error, String code, int line) {
		// we have to subtract 2 since lines start at 0 and the error produces
		// an extra space
		RobotMod.logger.info("Handling Error in Interface");
		runButton.setIsEnabled(true);
		btnStatus = true;
		errorPanel.setVisible(true);
		errorPanel.setFocused(true);
		errorLabel.setText(error);
		errorText = error;
		showError = true;
		if (error.contains("NameError") || error.contains("RequestError") || error.contains("TypeError")
				|| error.contains("AttributeError")) {
			// some errors dont seem to have the same offset as other errors
			codeWindow.notifyError(line - 1, code, error);
		} else {
			codeWindow.notifyError(line - 2, code, error);
		}

		Minecraft.getMinecraft().player.world.playSound(Minecraft.getMinecraft().player,
				Minecraft.getMinecraft().player.getPosition(), RobotMod.ROBOT_ERROR, SoundCategory.PLAYERS, 1, 1);

	}

	@Override
	public void onClose() {
		super.onClose();
		RobotMod.proxy.toggleRenderRobotProgramInterface(true);
	}

	@Override
	public void setup() {
		super.setup();

		sdCard = robot.robot_inventory.getSDCard();

		Panel mainPanel = new Panel((int) (width * .55), 0, (int) (width * .45), height).setFocused(true)
				.setCanDrag(true)/* .setCanResize(true) */;

		Panel savePanel = new Panel((int) (width * .33), (int) (height * .33), (int) (width * .45),
				(int) (height * .33)).setVisible(false).setFocused(false).setCanDrag(true).setZ(1000);

		registerComponent(savePanel);

		savePanel.registerComponent(
				new Picture(0, 0, (savePanel.getWidth()), (savePanel.getHeight()), DefaultTextures.BACKGROUND1));

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
							if (sdCard.hasTagCompound()) {
								// TODO: Warn the user they are overwriting
								if ((fileName != null) && !fileName.isEmpty()) {
									RobotMod.logger.info("Overwriting SD Card");
									try {
										FileUtils.writeFile(new File(RobotMod.scriptsLoc, fileName + ".py"),
												Arrays.asList(termText.split(Pattern.quote("\n"))));
									} catch (Exception e) {
										handleErrorMessage("Could not create script file: " + e.getLocalizedMessage(),
												"", 0);
										RobotMod.logger.error("Could not create script file", e);
									}

									NBTTagCompound tag = new NBTTagCompound();

									String author = Minecraft.getMinecraft().player.getName();
									if ((author == null) || author.isEmpty()) {
										author = Minecraft.getMinecraft().player.getName();
									}

									tag.setString("author", author);
									tag.setString("title", fileName);
									tag.setString("text", termText);

									ItemStack is = new ItemStack(RobotMod.card);
									is.setTagCompound(tag);

									sdCard = is;

									NetworkManager
											.sendToServer(new MessageReplaceSDCardItemStack(robot.getEntityId(), is));

									fileNameEntry.setText("");
									fileName = "";
									savePanel.setVisible(false);
									mainPanel.setFocused(true);
								}
							} else {
								if ((fileName != null) && !fileName.isEmpty()) {
									RobotMod.logger.info("Saving to SD Card");
									try {
										FileUtils.writeFile(new File(RobotMod.scriptsLoc, fileName + ".py"),
												Arrays.asList(termText.split(Pattern.quote("\n"))));
									} catch (Exception e) {
										RobotMod.logger.error("Could not create script file", e);
									}

									NBTTagCompound tag = new NBTTagCompound();

									tag.setString("author", Minecraft.getMinecraft().player.getName());
									tag.setString("title", fileName);
									tag.setString("text", termText);

									sdCard.setTagCompound(tag);

									NetworkManager.sendToServer(
											new MessageReplaceSDCardItemStack(robot.getEntityId(), sdCard));

									fileNameEntry.setText("");
									fileName = "";
									savePanel.setVisible(false);
									mainPanel.setFocused(true);
									mainPanel.setDimming(true);
								}
							}
						}));

		savePanel.registerComponent(
				new Button(((savePanel.getWidth() / 3) * 2) - 23, savePanel.getHeight() - 35, 45, 15, "Cancel")
						.setClickListener(btn -> {
							fileNameEntry.setText("");
							fileName = "";
							savePanel.setVisible(false);
							mainPanel.setFocused(true);
						}));

		registerComponent(mainPanel);

		mainPanel.setDimming(true);

		Compass robotComp = new Compass(0, 0, 0, 0, robot);
		Compass playerComp = new Compass(0, 0, 0, 0, Minecraft.getMinecraft().player);

		mainPanel.registerComponent(new CompassTab(0, 0, 50, 40, "Robot", 90, robot).setClickListener(tab -> {
			if (((CompassTab) tab).getCompass().getTrackingEntity() instanceof EntityPlayer) {
				((CompassTab) tab).setCompass(robotComp);
				((CompassTab) tab).setTitle("Robot");
			} else {
				((CompassTab) tab).setCompass(playerComp);
				((CompassTab) tab).setTitle("Player");
			}
		}));

		mainPanel.registerComponent(invTab = new ItemTab(0, 50, 40, 40, "", 90, Blocks.CHEST)
				.setHoverText(Lists.newArrayList("Open Robot", "Inventory")).setDrawHoverText(true)
				.setClickListener(tab -> {
					NetworkManager.sendToServer(new MessageOpenRobotInventory(robot.getEntityId()));
					RobotMod.proxy.toggleRenderRobotProgramInterface(true);
				}));

		mainPanel.registerComponent(followTab = new PictureTab(0, mainPanel.getHeight() - 40, 40, 40, "", 90,
				robot.getIsFollowing() ? new ResourceLocation(Reference.MOD_NAME, "textures/gui/robot_stand.png")
						: new ResourceLocation(Reference.MOD_NAME, "textures/gui/robot_follow.png"))
								.setHoverText(robot.getIsFollowing() ? Lists.newArrayList("Make Robot", "Stand still")
										: Lists.newArrayList("Make Robot", "Follow Me"))
								.setDrawHoverText(true).setClickListener(tab -> {
									if (!robot.getIsFollowing()) {
										NetworkManager
												.sendToServer(new MessageToggleRobotFollow(robot.getEntityId(), true));
										((PictureTab) tab).setPicture(
												new ResourceLocation(Reference.MOD_NAME, "textures/gui/robot_stand.png"));
										tab.setHoverText(Lists.newArrayList("Make Robot", "Stand still"));
										robot.setIsFollowing(true);
									} else {
										NetworkManager
												.sendToServer(new MessageToggleRobotFollow(robot.getEntityId(), false));
										((PictureTab) tab).setPicture(
												new ResourceLocation(Reference.MOD_NAME, "textures/gui/robot_follow.png"));
										tab.setHoverText(Lists.newArrayList("Make Robot", "Follow Me"));
										robot.setIsFollowing(false);
									}
								}));

		// The Panel background
		mainPanel.registerComponent(
				new Picture(0, 0, (mainPanel.getWidth()), (mainPanel.getHeight()), DefaultTextures.BACKGROUND2));

		mainPanel.registerComponent(
				(codeWindow = new CodeInterface(10, 15, mainPanel.getWidth() - 20, mainPanel.getHeight() - 35))
						.setText(termText).setBackgroundVisibility(false).setDrawUnicode(true)
						.setTextChangedListener((TextBox textbox, String previousText) -> {
							List<String> codeLines = Lists.newArrayList();
							for (String line : previousText.split(Pattern.quote("\n"))) {
								line.trim();
								if (line.isEmpty() || (line.charAt(0) == '#') || line.contains("import")) {
									continue;
								}
								codeLines.add(line);
							}
							if (codeLines.size() > robot.getMemorySize()) {
								handleErrorMessage(
										"Robot out of memory, can only process " + robot.getMemorySize()
												+ " lines but program contains " + codeLines.size(),
										previousText.split(
												Pattern.quote("\n"))[previousText.split(Pattern.quote("\n")).length
														- 1],
										previousText.split(Pattern.quote("\n")).length);
								textbox.setText(termText);
							} else {
								termText = previousText;
							}
						}));

		// menu panel
		mainPanel.registerComponent(
				new Button(0, 0, (mainPanel.getWidth() - 15) / 4, 15, "<<Game").setClickListener(btn -> {
					// followTab.setHidden(true);
					// robotDirTab.setHidden(true);
					// playerDirTab.setHidden(true);
					// invTab.setHidden(true);
					RobotMod.proxy.toggleRenderRobotProgramInterface(true);
					Minecraft.getMinecraft().setIngameFocus();
				}));

		mainPanel.registerComponent(
				new Button((mainPanel.getWidth() - 15) / 4, 0, (mainPanel.getWidth() - 15) / 4, 15, "Load")
						.setClickListener(btn -> {
							if (sdCard.hasTagCompound()) {
								codeWindow.setTextWithEvent(sdCard.getTagCompound().getString("text"));
							}
						}).setIsEnabled(!sdCard.isEmpty() && sdCard.hasTagCompound()));

		mainPanel.registerComponent(
				new Button((mainPanel.getWidth() - 15) / 2, 0, (mainPanel.getWidth() - 15) / 4, 15, "Save")
						.setClickListener(btn -> {
							savePanel.setVisible(true);
							savePanel.setFocused(true);
							mainPanel.setDimming(false);
							mainPanel.setFocused(false);
						}).setIsEnabled(!sdCard.isEmpty()));

		mainPanel.registerComponent(runButton = new Button(((mainPanel.getWidth() - 15) / 4) * 3, 0,
				(mainPanel.getWidth() - 15) / 4, 15, "Run").setIsEnabled(btnStatus).setClickListener(btn -> {
					List<String> codeLines = Lists.newArrayList();
					for (String line : termText.split(Pattern.quote("\n"))) {
						line.trim();
						if (line.isEmpty() || (line.charAt(0) == '#') || line.contains("import")) {
							continue;
						}
						codeLines.add(line);
					}
					if (codeLines.size() > robot.getMemorySize()) {
						handleErrorMessage(
								"Robot out of memory, can only process " + robot.getMemorySize()
										+ " lines but program contains " + codeLines.size(),
								termText.split(Pattern.quote("\n"))[termText.split(Pattern.quote("\n")).length - 1],
								termText.split(Pattern.quote("\n")).length);
					} else {
						btn.setIsEnabled(false);
						btnStatus = false;
						codeWindow.clearError();
						errorPanel.setVisible(false);

						NetworkManager.sendToServer(new MessageRunRobotScript(termText, robot.getEntityId(), true));
						((PictureTab) followTab)
								.setPicture(new ResourceLocation(Reference.MOD_NAME, "textures/gui/robot_follow.png"));
						followTab.setHoverText(Lists.newArrayList("Make Robot", "Follow Me"));
						robot.setIsFollowing(false);
					}
				}));

		mainPanel.registerComponent(new PictureButton(mainPanel.getWidth() - 15, 0, 15, 15, DefaultTextures.EXIT)
				.setDrawsButton(false).setClickListener(btn -> {
					RobotMod.proxy.toggleRenderRobotProgramInterface(false);
					Minecraft.getMinecraft().setIngameFocus();
				}));

		mainPanel.registerComponent(errorPanel = new Panel(0, (int) (mainPanel.getHeight() * .8), mainPanel.getWidth(),
				(int) (mainPanel.getHeight() * .2)).setVisible(showError).setCanDrag(true));

		errorPanel.registerComponent(
				new Picture(0, 0, (errorPanel.getWidth()), (errorPanel.getHeight()), DefaultTextures.BACKGROUND2));

		errorPanel.registerComponent(
				errorLabel = new TextLabel(10, 20, errorPanel.getWidth() - 20, errorPanel.getHeight() - 20, errorText)
						.setMultilined(true));

		errorPanel.registerComponent(new Picture(10, 5, (errorPanel.getWidth() / 10), (errorPanel.getHeight() / 4),
				new ResourceLocation(Reference.MOD_NAME, "textures/gui/error2.png")));

		errorPanel.registerComponent(new PictureButton(mainPanel.getWidth() - 10, 0, 10, 10, DefaultTextures.EXIT)
				.setDrawsButton(false).setClickListener(btn -> {
					errorPanel.setVisible(false);
					showError = false;
				}));

		registerComponent(new DraggableCamera(0, 0, width, height).setEnabled(true));

		// TODO: make this grab from the api or something
		List<String> robotMembers = Lists.newArrayList();
		robotMembers.add("forward()");
		robotMembers.add("backward()");
		if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 4))) {
			robotMembers.add("inspect()");
		}
		robotMembers.add("left()");
		robotMembers.add("right()");
		if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 2))) {
			robotMembers.add("mine()");
		}
		if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 3))) {
			robotMembers.add("place()");
		}
		if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 1))) {
			robotMembers.add("jump()");
		}
		if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 15))) {
			robotMembers.add("say()");
		}
		if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 5))) {
			robotMembers.add("interact()");
		}
		if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 6))) {
			robotMembers.add("detect()");
		}
		if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 7))) {
			robotMembers.add("attack()");
		}
		if (robot.robot_inventory.containsItem(new ItemStack(RobotMod.expChip, 1, 0))) {
			robotMembers.add("climb()");
			robotMembers.add("descend()");
		}

		Collections.sort(robotMembers);

		codeWindow.addClassMembers("Robot", robotMembers);
	}

}
