package com.dyn.robot.entity.brain;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.dyn.robot.api.IDYNRobotAccess;
import com.dyn.robot.entity.EntityRobot;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleCommand;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleAnimation;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.api.turtle.TurtleUpgradeType;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.turtle.core.TurtleCommandQueueEntry;
import dan200.computercraft.shared.util.Colour;
import dan200.computercraft.shared.util.DirectionUtil;
import dan200.computercraft.shared.util.Holiday;
import dan200.computercraft.shared.util.HolidayUtil;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public abstract class RobotBrain implements IDYNRobotAccess {
	private static int s_nextInstanceID = 0;
	private static Map<Integer, WeakReference<RobotBrain>> s_allClientBrains = new HashMap();

	public static int assignInstanceID() {
		return s_nextInstanceID++;
	}

	public static void cleanupBrains() {
		if (s_allClientBrains.size() > 0) {
			Iterator<Map.Entry<Integer, WeakReference<RobotBrain>>> it = s_allClientBrains.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Integer, WeakReference<RobotBrain>> entry = it.next();
				WeakReference<RobotBrain> ref = entry.getValue();
				if (ref != null) {
					RobotBrain brain = ref.get();
					if (brain == null) {
						it.remove();
					}
				}
			}
		}
	}

	public static RobotBrain getClientBrain(int instanceID) {
		if (instanceID >= 0) {
			WeakReference<RobotBrain> ref = s_allClientBrains.get(Integer.valueOf(instanceID));
			if (ref != null) {
				RobotBrain brain = ref.get();
				if (brain != null) {
					return brain;
				}
				s_allClientBrains.remove(Integer.valueOf(instanceID));
			}
		}
		return null;
	}

	public static void setClientBrain(int instanceID, RobotBrain brain) {
		if (instanceID >= 0) {
			if (getClientBrain(instanceID) != brain) {
				s_allClientBrains.put(Integer.valueOf(instanceID), new WeakReference(brain));
			}
		}
	}

	private EntityRobot m_owner;
	private LinkedList<TurtleCommandQueueEntry> m_commandQueue;
	private int m_commandsIssued;
	private Map<TurtleSide, ITurtleUpgrade> m_upgrades;
	private Map<TurtleSide, IPeripheral> m_peripherals;
	private Map<TurtleSide, NBTTagCompound> m_upgradeNBTData;
	private int m_selectedSlot;
	private int m_fuelLevel;
	private Colour m_colour;
	private ResourceLocation m_overlay;
	private int m_instanceID;

	private EnumFacing m_direction;

	private TurtleAnimation m_animation;

	private int m_animationProgress;

	private int m_lastAnimationProgress;

	public RobotBrain(EntityRobot robot) {
		m_owner = robot;

		m_commandQueue = new LinkedList();
		m_commandsIssued = 0;

		m_upgrades = new HashMap();
		m_peripherals = new HashMap();
		m_upgradeNBTData = new HashMap();

		m_selectedSlot = 0;
		m_fuelLevel = 0;
		m_colour = null;
		m_overlay = null;

		m_instanceID = -1;
		m_direction = EnumFacing.NORTH;
		m_animation = TurtleAnimation.None;
		m_animationProgress = 0;
		m_lastAnimationProgress = 0;
	}

	@Override
	public void addFuel(int fuel) {
		if (getWorld().isRemote) {
			throw new UnsupportedOperationException();
		}
		int addition = Math.max(fuel, 0);
		setFuelLevel(getFuelLevel() + addition);
	}

	@Override
	public boolean consumeFuel(int fuel) {
		if (getWorld().isRemote) {
			throw new UnsupportedOperationException();
		}
		if (!isFuelNeeded()) {
			return true;
		}
		int consumption = Math.max(fuel, 0);
		if (getFuelLevel() >= consumption) {
			setFuelLevel(getFuelLevel() - consumption);
			return true;
		}
		return false;
	}

	@Override
	public Object[] executeCommand(ILuaContext context, ITurtleCommand command)
			throws LuaException, InterruptedException {
		if (getWorld().isRemote) {
			throw new UnsupportedOperationException();
		}
		int commandID = issueCommand(command);
		for (;;) {
			Object[] response = context.pullEvent("turtle_response");
			if ((response.length >= 3) && ((response[1] instanceof Number)) && ((response[2] instanceof Boolean))) {
				if (((Number) response[1]).intValue() == commandID) {
					Object[] returnValues = new Object[response.length - 2];
					for (int i = 0; i < returnValues.length; i++) {
						returnValues[i] = response[(i + 2)];
					}
					return returnValues;
				}
			}
		}
	}

	private float getAnimationFraction(float f) {
		float next = m_animationProgress / 8.0F;
		float previous = m_lastAnimationProgress / 8.0F;
		return previous + ((next - previous) * f);
	}

	@Override
	public EnumFacing getDirection() {
		return m_direction;
	}

	@Override
	public int getFuelLevel() {
		return Math.min(m_fuelLevel, getFuelLimit());
	}

	@Override
	public int getFuelLimit() {
		return ComputerCraft.turtleFuelLimit;
	}

	public RobotBrain getFutureSelf() {
		if (getOwner().getEntityWorld().isRemote) {
			RobotBrain futureSelf = getClientBrain(m_instanceID);
			if (futureSelf != null) {
				return futureSelf;
			}
		}
		return this;
	}

	public EntityRobot getOwner() {
		return m_owner;
	}

	@Override
	public BlockPos getPosition() {
		return m_owner.getPosition();
	}

	public Vec3 getRenderOffset(float f) {
		switch (m_animation) {
		case MoveForward:
		case MoveBack:
		case MoveUp:
		case MoveDown:
			EnumFacing dir;
			switch (m_animation) {
			case MoveForward:
			default:
				dir = getDirection();
				break;
			case MoveBack:
				dir = getDirection().getOpposite();
				break;
			case MoveUp:
				dir = EnumFacing.UP;
				break;
			case MoveDown:
				dir = EnumFacing.DOWN;
			}
			double distance = -1.0D + getAnimationFraction(f);
			return new Vec3(distance * dir.getFrontOffsetX(), distance * dir.getFrontOffsetY(),
					distance * dir.getFrontOffsetZ());
		}
		return new Vec3(0.0D, 0.0D, 0.0D);
	}

	@Override
	public int getSelectedSlot() {
		return m_selectedSlot;
	}

	public float getToolRenderAngle(TurtleSide side, float f) {
		if (((side == TurtleSide.Left) && (m_animation == TurtleAnimation.SwingLeftTool))
				|| ((side == TurtleSide.Right) && (m_animation == TurtleAnimation.SwingRightTool))) {
			return 45.0F * (float) Math.sin(getAnimationFraction(f) * 3.141592653589793D);
		}
		return 0.0F;
	}

	private String getUpgradeID(ITurtleUpgrade upgrade) {
		if (upgrade != null) {
			return upgrade.getUpgradeID().toString();
		}
		return null;
	}

	@Override
	public Vec3 getVisualPosition(float f) {
		Vec3 offset = getRenderOffset(f);
		BlockPos pos = m_owner.getPosition();
		return new Vec3(pos.getX() + 0.5D + offset.xCoord, pos.getY() + 0.5D + offset.yCoord,
				pos.getZ() + 0.5D + offset.zCoord);
	}

	@Override
	public float getVisualYaw(float f) {
		float forward = DirectionUtil.toYawAngle(getDirection());
		float yaw = forward;
		switch (m_animation) {
		case TurnLeft:
			yaw += 90.0F * (1.0F - getAnimationFraction(f));
			if (yaw >= 360.0F) {
				yaw -= 360.0F;
			}
			break;
		case TurnRight:
			yaw += -90.0F * (1.0F - getAnimationFraction(f));
			if (yaw < 0.0F) {
				yaw += 360.0F;
			}
			break;
		}
		return yaw;
	}

	@Override
	public World getWorld() {
		return m_owner.getEntityWorld();
	}

	@Override
	public boolean isFuelNeeded() {
		return ComputerCraft.turtlesNeedFuel;
	}

	private int issueCommand(ITurtleCommand command) {
		m_commandQueue.offer(new TurtleCommandQueueEntry(++m_commandsIssued, command));
		return m_commandsIssued;
	}

	@Override
	public void playAnimation(TurtleAnimation animation) {
		if (getWorld().isRemote) {
			throw new UnsupportedOperationException();
		}
		m_animation = animation;
		if (m_animation == TurtleAnimation.ShortWait) {
			m_animationProgress = 4;
			m_lastAnimationProgress = 4;
		} else {
			m_animationProgress = 0;
			m_lastAnimationProgress = 0;
		}
	}

	public void readDescription(NBTTagCompound nbttagcompound) {
		if (nbttagcompound.hasKey("leftUpgrade")) {
			setUpgrade(TurtleSide.Left, ComputerCraft.getTurtleUpgrade(nbttagcompound.getString("leftUpgrade")));
		} else {
			setUpgrade(TurtleSide.Left, null);
		}
		if (nbttagcompound.hasKey("rightUpgrade")) {
			setUpgrade(TurtleSide.Right, ComputerCraft.getTurtleUpgrade(nbttagcompound.getString("rightUpgrade")));
		} else {
			setUpgrade(TurtleSide.Right, null);
		}
		m_upgradeNBTData.clear();
		if (nbttagcompound.hasKey("leftUpgradeNBT")) {
			m_upgradeNBTData.put(TurtleSide.Left,
					(NBTTagCompound) nbttagcompound.getCompoundTag("leftUpgradeNBT").copy());
		}
		if (nbttagcompound.hasKey("rightUpgradeNBT")) {
			m_upgradeNBTData.put(TurtleSide.Right,
					(NBTTagCompound) nbttagcompound.getCompoundTag("rightUpgradeNBT").copy());
		}
		if (nbttagcompound.hasKey("colourIndex")) {
			m_colour = Colour.values()[nbttagcompound.getInteger("colourIndex")];
		} else {
			m_colour = null;
		}
		if ((nbttagcompound.hasKey("overlay_mod")) && (nbttagcompound.hasKey("overlay_path"))) {
			String overlay_mod = nbttagcompound.getString("overlay_mod");
			String overlay_path = nbttagcompound.getString("overlay_path");
			m_overlay = new ResourceLocation(overlay_mod, overlay_path);
		} else {
			m_overlay = null;
		}
		m_instanceID = nbttagcompound.getInteger("brainInstanceID");
		setClientBrain(m_instanceID, this);

		TurtleAnimation anim = TurtleAnimation.values()[nbttagcompound.getInteger("animation")];
		if ((anim != m_animation) && (anim != TurtleAnimation.Wait) && (anim != TurtleAnimation.ShortWait)
				&& (anim != TurtleAnimation.None)) {
			m_animation = TurtleAnimation.values()[nbttagcompound.getInteger("animation")];
			m_animationProgress = 0;
			m_lastAnimationProgress = 0;
		}
		m_direction = EnumFacing.getFront(nbttagcompound.getInteger("direction"));
		m_fuelLevel = nbttagcompound.getInteger("fuelLevel");
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		m_direction = EnumFacing.getFront(nbttagcompound.getInteger("dir"));
		m_selectedSlot = nbttagcompound.getInteger("selectedSlot");
		if (nbttagcompound.hasKey("fuelLevel")) {
			m_fuelLevel = nbttagcompound.getInteger("fuelLevel");
		} else {
			m_fuelLevel = 0;
		}
		if (nbttagcompound.hasKey("colourIndex")) {
			m_colour = Colour.values()[nbttagcompound.getInteger("colourIndex")];
		} else {
			m_colour = null;
		}
		if (nbttagcompound.hasKey("overlay_mod")) {
			String overlay_mod = nbttagcompound.getString("overlay_mod");
			if (nbttagcompound.hasKey("overlay_path")) {
				String overlay_path = nbttagcompound.getString("overlay_path");
				m_overlay = new ResourceLocation(overlay_mod, overlay_path);
			} else {
				m_overlay = null;
			}
		} else {
			m_overlay = null;
		}
		ITurtleUpgrade leftUpgrade = null;
		ITurtleUpgrade rightUpgrade = null;
		if (nbttagcompound.hasKey("subType")) {
			int subType = nbttagcompound.getInteger("subType");
			if ((subType & 0x1) > 0) {
				leftUpgrade = ComputerCraft.Upgrades.diamondPickaxe;
			}
			if ((subType & 0x2) > 0) {
				rightUpgrade = ComputerCraft.Upgrades.wirelessModem;
			}
		} else {
			if (nbttagcompound.hasKey("leftUpgrade")) {
				if (nbttagcompound.getTagId("leftUpgrade") == 8) {
					leftUpgrade = ComputerCraft.getTurtleUpgrade(nbttagcompound.getString("leftUpgrade"));
				} else {
					leftUpgrade = ComputerCraft.getTurtleUpgrade(nbttagcompound.getShort("leftUpgrade"));
				}
			}
			if (nbttagcompound.hasKey("rightUpgrade")) {
				if (nbttagcompound.getTagId("rightUpgrade") == 8) {
					rightUpgrade = ComputerCraft.getTurtleUpgrade(nbttagcompound.getString("rightUpgrade"));
				} else {
					rightUpgrade = ComputerCraft.getTurtleUpgrade(nbttagcompound.getShort("rightUpgrade"));
				}
			}
		}
		setUpgrade(TurtleSide.Left, leftUpgrade);
		setUpgrade(TurtleSide.Right, rightUpgrade);

		m_upgradeNBTData.clear();
		if (nbttagcompound.hasKey("leftUpgradeNBT")) {
			m_upgradeNBTData.put(TurtleSide.Left,
					(NBTTagCompound) nbttagcompound.getCompoundTag("leftUpgradeNBT").copy());
		}
		if (nbttagcompound.hasKey("rightUpgradeNBT")) {
			m_upgradeNBTData.put(TurtleSide.Right,
					(NBTTagCompound) nbttagcompound.getCompoundTag("rightUpgradeNBT").copy());
		}
	}

	@Override
	public void setDirection(EnumFacing dir) {
		if (dir.getAxis() == EnumFacing.Axis.Y) {
			dir = EnumFacing.NORTH;
		}
		m_direction = dir;
	}

	@Override
	public void setFuelLevel(int level) {
		m_fuelLevel = Math.min(level, getFuelLimit());
	}

	public void setOwner(EntityRobot owner) {
		m_owner = owner;
	}

	@Override
	public void setSelectedSlot(int slot) {
		if (getWorld().isRemote) {
			throw new UnsupportedOperationException();
		}
		if ((slot >= 0) && (slot < m_owner.getInventory().length)) {
			m_selectedSlot = slot;
		}
	}

	public void setupComputer(ServerComputer computer) {
		updatePeripherals(computer);
	}

	@Override
	public boolean teleportTo(World world, BlockPos pos) {
		// TODO
		// if ((world.isRemote) || (getWorld().isRemote)) {
		// throw new UnsupportedOperationException();
		// }
		// World oldWorld = getWorld();
		// BlockPos oldPos = this.m_owner.getPosition();
		// Block oldBlock = this.m_owner.getBlock();
		// if ((oldWorld == world) && (oldPos.equals(pos))) {
		// return true;
		// }
		// if ((world.isBlockLoaded(pos)) && (world.setBlockState(pos,
		// oldBlock.getDefaultState(), 3)))
		// {
		// Block block = world.getBlockState(pos).getBlock();
		// if (block == oldBlock)
		// {
		// TileEntity newTile = world.getTileEntity(pos);
		// if ((newTile != null) && ((newTile instanceof TileTurtle)))
		// {
		// TileTurtle newTurtle = (TileTurtle)newTile;
		// newTurtle.setWorldObj(world);
		// newTurtle.setPos(pos);
		// newTurtle.transferStateFrom(this.m_owner);
		// newTurtle.createServerComputer().setWorld(world);
		// newTurtle.createServerComputer().setPosition(pos);
		//
		// oldWorld.setBlockToAir(oldPos);
		//
		// newTurtle.updateInput();
		// newTurtle.updateOutput();
		// return true;
		// }
		// }
		// world.setBlockToAir(pos);
		// }
		return false;
	}

	private int toDirection(TurtleSide side) {
		switch (side) {
		case Left:
			return 5;
		}
		return 4;
	}

	public void update() {
		World world = getWorld();
		if (!world.isRemote) {
			updateCommands();
		}
		updateAnimation();
		if (!m_upgrades.isEmpty()) {
			for (Map.Entry<TurtleSide, ITurtleUpgrade> entry : m_upgrades.entrySet()) {
				entry.getValue().update(this, entry.getKey());
			}
		}
	}

	private void updateAnimation() {
		if (m_animation != TurtleAnimation.None) {
			World world = getWorld();
			if (ComputerCraft.turtlesCanPush) {
				if ((m_animation == TurtleAnimation.MoveForward) || (m_animation == TurtleAnimation.MoveBack)
						|| (m_animation == TurtleAnimation.MoveUp) || (m_animation == TurtleAnimation.MoveDown)) {
					BlockPos pos = getPosition();
					EnumFacing moveDir;
					switch (m_animation) {
					case MoveForward:
					default:
						moveDir = m_direction;
						break;
					case MoveBack:
						moveDir = m_direction.getOpposite();
						break;
					case MoveUp:
						moveDir = EnumFacing.UP;
						break;
					case MoveDown:
						moveDir = EnumFacing.DOWN;
					}
					double minX = pos.getX();
					double minY = pos.getY();
					double minZ = pos.getZ();
					double maxX = minX + 1.0D;
					double maxY = minY + 1.0D;
					double maxZ = minZ + 1.0D;

					float pushFrac = 1.0F - ((m_animationProgress + 1) / 8.0F);
					float push = Math.max(pushFrac + 0.0125F, 0.0F);
					if (moveDir.getFrontOffsetX() < 0) {
						minX += moveDir.getFrontOffsetX() * push;
					} else {
						maxX -= moveDir.getFrontOffsetX() * push;
					}
					if (moveDir.getFrontOffsetY() < 0) {
						minY += moveDir.getFrontOffsetY() * push;
					} else {
						maxY -= moveDir.getFrontOffsetY() * push;
					}
					if (moveDir.getFrontOffsetZ() < 0) {
						minZ += moveDir.getFrontOffsetZ() * push;
					} else {
						maxZ -= moveDir.getFrontOffsetZ() * push;
					}
					AxisAlignedBB aabb = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
					List list = world.getEntitiesWithinAABBExcludingEntity((Entity) null, aabb);
					if (!list.isEmpty()) {
						double pushStep = 0.125D;
						double pushStepX = moveDir.getFrontOffsetX() * pushStep;
						double pushStepY = moveDir.getFrontOffsetY() * pushStep;
						double pushStepZ = moveDir.getFrontOffsetZ() * pushStep;
						for (int i = 0; i < list.size(); i++) {
							Entity entity = (Entity) list.get(i);
							entity.moveEntity(pushStepX, pushStepY, pushStepZ);
						}
					}
				}
			}
			if ((world.isRemote) && (m_animation == TurtleAnimation.MoveForward) && (m_animationProgress == 4)) {
				Holiday currentHoliday = HolidayUtil.getCurrentHoliday();
				if (currentHoliday == Holiday.Valentines) {
					Vec3 position = getVisualPosition(1.0F);
					if (position != null) {
						double x = position.xCoord + (world.rand.nextGaussian() * 0.1D);
						double y = position.yCoord + 0.5D + (world.rand.nextGaussian() * 0.1D);
						double z = position.zCoord + (world.rand.nextGaussian() * 0.1D);
						world.spawnParticle(EnumParticleTypes.HEART, x, y, z, world.rand.nextGaussian() * 0.02D,
								world.rand.nextGaussian() * 0.02D, world.rand.nextGaussian() * 0.02D, new int[0]);
					}
				}
			}
			m_lastAnimationProgress = m_animationProgress;
			if (++m_animationProgress >= 8) {
				m_animation = TurtleAnimation.None;
				m_animationProgress = 0;
				m_lastAnimationProgress = 0;
			}
		}
	}

	private void updateCommands() {
		if (m_animation == TurtleAnimation.None) {
			TurtleCommandQueueEntry nextCommand = null;
			if (m_commandQueue.peek() != null) {
				nextCommand = m_commandQueue.remove();
			}
			if (nextCommand != null) {
				TurtleCommandResult result = nextCommand.command.execute(this);

				int callbackID = nextCommand.callbackID;
				if (callbackID >= 0) {
					if ((result != null) && (result.isSuccess())) {
						IComputer computer = m_owner.getComputer();
						if (computer != null) {
							Object[] results = result.getResults();
							if (results != null) {
								Object[] arguments = new Object[results.length + 2];
								arguments[0] = Integer.valueOf(callbackID);
								arguments[1] = Boolean.valueOf(true);
								for (int i = 0; i < results.length; i++) {
									arguments[(2 + i)] = results[i];
								}
								computer.queueEvent("turtle_response", arguments);
							} else {
								computer.queueEvent("turtle_response",
										new Object[] { Integer.valueOf(callbackID), Boolean.valueOf(true) });
							}
						}
					} else {
						IComputer computer = m_owner.getComputer();
						if (computer != null) {
							computer.queueEvent("turtle_response", new Object[] { Integer.valueOf(callbackID),
									Boolean.valueOf(false), result != null ? result.getErrorMessage() : null });
						}
					}
				}
			}
		}
	}

	public void updatePeripherals(ServerComputer serverComputer) {
		if (serverComputer == null) {
			return;
		}
		for (TurtleSide side : TurtleSide.values()) {
			ITurtleUpgrade upgrade = getUpgrade(side);
			IPeripheral peripheral = null;
			if ((upgrade != null) && (upgrade.getType() == TurtleUpgradeType.Peripheral)) {
				peripheral = upgrade.createPeripheral(this, side);
			}
			int dir = toDirection(side);
			if (peripheral != null) {
				if (!m_peripherals.containsKey(side)) {
					serverComputer.setPeripheral(dir, peripheral);
					serverComputer.setRedstoneInput(dir, 0);
					serverComputer.setBundledRedstoneInput(dir, 0);
					m_peripherals.put(side, peripheral);
				} else if (!m_peripherals.get(side).equals(peripheral)) {
					serverComputer.setPeripheral(dir, peripheral);
					serverComputer.setRedstoneInput(dir, 0);
					serverComputer.setBundledRedstoneInput(dir, 0);
					m_peripherals.remove(side);
					m_peripherals.put(side, peripheral);
				}
			} else if (m_peripherals.containsKey(side)) {
				serverComputer.setPeripheral(dir, null);
				m_peripherals.remove(side);
			}
		}
	}

	public void writeDescription(NBTTagCompound nbttagcompound) {
		String leftUpgradeID = getUpgradeID(getUpgrade(TurtleSide.Left));
		if (leftUpgradeID != null) {
			nbttagcompound.setString("leftUpgrade", leftUpgradeID);
		}
		String rightUpgradeID = getUpgradeID(getUpgrade(TurtleSide.Right));
		if (rightUpgradeID != null) {
			nbttagcompound.setString("rightUpgrade", rightUpgradeID);
		}
		if (m_upgradeNBTData.containsKey(TurtleSide.Left)) {
			nbttagcompound.setTag("leftUpgradeNBT", getUpgradeNBTData(TurtleSide.Left).copy());
		}
		if (m_upgradeNBTData.containsKey(TurtleSide.Right)) {
			nbttagcompound.setTag("rightUpgradeNBT", getUpgradeNBTData(TurtleSide.Right).copy());
		}
		if (m_colour != null) {
			nbttagcompound.setInteger("colourIndex", m_colour.ordinal());
		}
		if (m_overlay != null) {
			nbttagcompound.setString("overlay_mod", m_overlay.getResourceDomain());
			nbttagcompound.setString("overlay_path", m_overlay.getResourcePath());
		}
		if (m_instanceID < 0) {
			m_instanceID = assignInstanceID();
		}
		nbttagcompound.setInteger("brainInstanceID", m_instanceID);
		nbttagcompound.setInteger("animation", m_animation.ordinal());
		nbttagcompound.setInteger("direction", m_direction.getIndex());
		nbttagcompound.setInteger("fuelLevel", m_fuelLevel);
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("dir", m_direction.getIndex());
		nbttagcompound.setInteger("selectedSlot", m_selectedSlot);
		nbttagcompound.setInteger("fuelLevel", m_fuelLevel);

		String leftUpgradeID = getUpgradeID(getUpgrade(TurtleSide.Left));
		if (leftUpgradeID != null) {
			nbttagcompound.setString("leftUpgrade", leftUpgradeID);
		}
		String rightUpgradeID = getUpgradeID(getUpgrade(TurtleSide.Right));
		if (rightUpgradeID != null) {
			nbttagcompound.setString("rightUpgrade", rightUpgradeID);
		}
		if (m_colour != null) {
			nbttagcompound.setInteger("colourIndex", m_colour.ordinal());
		}
		if (m_overlay != null) {
			nbttagcompound.setString("overlay_mod", m_overlay.getResourceDomain());
			nbttagcompound.setString("overlay_path", m_overlay.getResourcePath());
		}
		if (m_upgradeNBTData.containsKey(TurtleSide.Left)) {
			nbttagcompound.setTag("leftUpgradeNBT", getUpgradeNBTData(TurtleSide.Left).copy());
		}
		if (m_upgradeNBTData.containsKey(TurtleSide.Right)) {
			nbttagcompound.setTag("rightUpgradeNBT", getUpgradeNBTData(TurtleSide.Right).copy());
		}
	}
}
