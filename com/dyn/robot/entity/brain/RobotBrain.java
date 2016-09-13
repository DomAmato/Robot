package com.dyn.robot.entity.brain;

import com.dyn.robot.api.IDYNRobotAccess;
import com.google.common.base.Objects;
import dan200.computercraft.ComputerCraft;
import dan200.computercraft.ComputerCraft.Upgrades;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.ITurtleCommand;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.turtle.TurtleAnimation;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.api.turtle.TurtleUpgradeType;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.turtle.core.TurtleCommandQueueEntry;
import dan200.computercraft.shared.util.Colour;
import dan200.computercraft.shared.util.DirectionUtil;
import dan200.computercraft.shared.util.Holiday;
import dan200.computercraft.shared.util.HolidayUtil;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class RobotBrain
  implements IDYNRobotAccess
{
  private static int s_nextInstanceID = 0;
  private static Map<Integer, WeakReference<RobotBrain>> s_allClientBrains = new HashMap();
  private static final int ANIM_DURATION = 8;
  private EntityLiving m_owner;
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
  
  public static int assignInstanceID()
  {
    return s_nextInstanceID++;
  }
  
  public static RobotBrain getClientBrain(int instanceID)
  {
    if (instanceID >= 0)
    {
      WeakReference<RobotBrain> ref = (WeakReference)s_allClientBrains.get(Integer.valueOf(instanceID));
      if (ref != null)
      {
        RobotBrain brain = (RobotBrain)ref.get();
        if (brain != null) {
          return brain;
        }
        s_allClientBrains.remove(Integer.valueOf(instanceID));
      }
    }
    return null;
  }
  
  public static void setClientBrain(int instanceID, RobotBrain brain)
  {
    if (instanceID >= 0) {
      if (getClientBrain(instanceID) != brain) {
        s_allClientBrains.put(Integer.valueOf(instanceID), new WeakReference(brain));
      }
    }
  }
  
  public static void cleanupBrains()
  {
    if (s_allClientBrains.size() > 0)
    {
      Iterator<Map.Entry<Integer, WeakReference<RobotBrain>>> it = s_allClientBrains.entrySet().iterator();
      while (it.hasNext())
      {
        Map.Entry<Integer, WeakReference<RobotBrain>> entry = (Map.Entry)it.next();
        WeakReference<RobotBrain> ref = (WeakReference)entry.getValue();
        if (ref != null)
        {
          RobotBrain brain = (RobotBrain)ref.get();
          if (brain == null) {
            it.remove();
          }
        }
      }
    }
  }
  
  public RobotBrain(EntityLiving robot)
  {
    this.m_owner = robot;
    
    this.m_commandQueue = new LinkedList();
    this.m_commandsIssued = 0;
    
    this.m_upgrades = new HashMap();
    this.m_peripherals = new HashMap();
    this.m_upgradeNBTData = new HashMap();
    
    this.m_selectedSlot = 0;
    this.m_fuelLevel = 0;
    this.m_colour = null;
    this.m_overlay = null;
    
    this.m_instanceID = -1;
    this.m_direction = EnumFacing.NORTH;
    this.m_animation = TurtleAnimation.None;
    this.m_animationProgress = 0;
    this.m_lastAnimationProgress = 0;
  }
  
  public RobotBrain getFutureSelf()
  {
    if (getOwner().getEntityWorld().isRemote)
    {
      RobotBrain futureSelf = getClientBrain(this.m_instanceID);
      if (futureSelf != null) {
        return futureSelf;
      }
    }
    return this;
  }
  
  public void setOwner(EntityLiving owner)
  {
    this.m_owner = owner;
  }
  
  public EntityLiving getOwner()
  {
    return this.m_owner;
  }
  
  public void setupComputer(ServerComputer computer)
  {
    updatePeripherals(computer);
  }
  
  public void update()
  {
    World world = getWorld();
    if (!world.isRemote) {
      updateCommands();
    }
    updateAnimation();
    if (!this.m_upgrades.isEmpty()) {
      for (Map.Entry<TurtleSide, ITurtleUpgrade> entry : this.m_upgrades.entrySet()) {
        ((ITurtleUpgrade)entry.getValue()).update(this, (TurtleSide)entry.getKey());
      }
    }
  }
  
  public void readFromNBT(NBTTagCompound nbttagcompound)
  {
    this.m_direction = EnumFacing.getFront(nbttagcompound.getInteger("dir"));
    this.m_selectedSlot = nbttagcompound.getInteger("selectedSlot");
    if (nbttagcompound.hasKey("fuelLevel")) {
      this.m_fuelLevel = nbttagcompound.getInteger("fuelLevel");
    } else {
      this.m_fuelLevel = 0;
    }
    if (nbttagcompound.hasKey("colourIndex")) {
      this.m_colour = Colour.values()[nbttagcompound.getInteger("colourIndex")];
    } else {
      this.m_colour = null;
    }
    if (nbttagcompound.hasKey("overlay_mod"))
    {
      String overlay_mod = nbttagcompound.getString("overlay_mod");
      if (nbttagcompound.hasKey("overlay_path"))
      {
        String overlay_path = nbttagcompound.getString("overlay_path");
        this.m_overlay = new ResourceLocation(overlay_mod, overlay_path);
      }
      else
      {
        this.m_overlay = null;
      }
    }
    else
    {
      this.m_overlay = null;
    }
    ITurtleUpgrade leftUpgrade = null;
    ITurtleUpgrade rightUpgrade = null;
    if (nbttagcompound.hasKey("subType"))
    {
      int subType = nbttagcompound.getInteger("subType");
      if ((subType & 0x1) > 0) {
        leftUpgrade = ComputerCraft.Upgrades.diamondPickaxe;
      }
      if ((subType & 0x2) > 0) {
        rightUpgrade = ComputerCraft.Upgrades.wirelessModem;
      }
    }
    else
    {
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
    
    this.m_upgradeNBTData.clear();
    if (nbttagcompound.hasKey("leftUpgradeNBT")) {
      this.m_upgradeNBTData.put(TurtleSide.Left, (NBTTagCompound)nbttagcompound.getCompoundTag("leftUpgradeNBT").copy());
    }
    if (nbttagcompound.hasKey("rightUpgradeNBT")) {
      this.m_upgradeNBTData.put(TurtleSide.Right, (NBTTagCompound)nbttagcompound.getCompoundTag("rightUpgradeNBT").copy());
    }
  }
  
  public void writeToNBT(NBTTagCompound nbttagcompound)
  {
    nbttagcompound.setInteger("dir", this.m_direction.getIndex());
    nbttagcompound.setInteger("selectedSlot", this.m_selectedSlot);
    nbttagcompound.setInteger("fuelLevel", this.m_fuelLevel);
    
    String leftUpgradeID = getUpgradeID(getUpgrade(TurtleSide.Left));
    if (leftUpgradeID != null) {
      nbttagcompound.setString("leftUpgrade", leftUpgradeID);
    }
    String rightUpgradeID = getUpgradeID(getUpgrade(TurtleSide.Right));
    if (rightUpgradeID != null) {
      nbttagcompound.setString("rightUpgrade", rightUpgradeID);
    }
    if (this.m_colour != null) {
      nbttagcompound.setInteger("colourIndex", this.m_colour.ordinal());
    }
    if (this.m_overlay != null)
    {
      nbttagcompound.setString("overlay_mod", this.m_overlay.getResourceDomain());
      nbttagcompound.setString("overlay_path", this.m_overlay.getResourcePath());
    }
    if (this.m_upgradeNBTData.containsKey(TurtleSide.Left)) {
      nbttagcompound.setTag("leftUpgradeNBT", (NBTTagCompound)getUpgradeNBTData(TurtleSide.Left).copy());
    }
    if (this.m_upgradeNBTData.containsKey(TurtleSide.Right)) {
      nbttagcompound.setTag("rightUpgradeNBT", (NBTTagCompound)getUpgradeNBTData(TurtleSide.Right).copy());
    }
  }
  
  private String getUpgradeID(ITurtleUpgrade upgrade)
  {
    if (upgrade != null) {
      return upgrade.getUpgradeID().toString();
    }
    return null;
  }
  
  public void writeDescription(NBTTagCompound nbttagcompound)
  {
    String leftUpgradeID = getUpgradeID(getUpgrade(TurtleSide.Left));
    if (leftUpgradeID != null) {
      nbttagcompound.setString("leftUpgrade", leftUpgradeID);
    }
    String rightUpgradeID = getUpgradeID(getUpgrade(TurtleSide.Right));
    if (rightUpgradeID != null) {
      nbttagcompound.setString("rightUpgrade", rightUpgradeID);
    }
    if (this.m_upgradeNBTData.containsKey(TurtleSide.Left)) {
      nbttagcompound.setTag("leftUpgradeNBT", (NBTTagCompound)getUpgradeNBTData(TurtleSide.Left).copy());
    }
    if (this.m_upgradeNBTData.containsKey(TurtleSide.Right)) {
      nbttagcompound.setTag("rightUpgradeNBT", (NBTTagCompound)getUpgradeNBTData(TurtleSide.Right).copy());
    }
    if (this.m_colour != null) {
      nbttagcompound.setInteger("colourIndex", this.m_colour.ordinal());
    }
    if (this.m_overlay != null)
    {
      nbttagcompound.setString("overlay_mod", this.m_overlay.getResourceDomain());
      nbttagcompound.setString("overlay_path", this.m_overlay.getResourcePath());
    }
    if (this.m_instanceID < 0) {
      this.m_instanceID = assignInstanceID();
    }
    nbttagcompound.setInteger("brainInstanceID", this.m_instanceID);
    nbttagcompound.setInteger("animation", this.m_animation.ordinal());
    nbttagcompound.setInteger("direction", this.m_direction.getIndex());
    nbttagcompound.setInteger("fuelLevel", this.m_fuelLevel);
  }
  
  public void readDescription(NBTTagCompound nbttagcompound)
  {
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
    this.m_upgradeNBTData.clear();
    if (nbttagcompound.hasKey("leftUpgradeNBT")) {
      this.m_upgradeNBTData.put(TurtleSide.Left, (NBTTagCompound)nbttagcompound.getCompoundTag("leftUpgradeNBT").copy());
    }
    if (nbttagcompound.hasKey("rightUpgradeNBT")) {
      this.m_upgradeNBTData.put(TurtleSide.Right, (NBTTagCompound)nbttagcompound.getCompoundTag("rightUpgradeNBT").copy());
    }
    if (nbttagcompound.hasKey("colourIndex")) {
      this.m_colour = Colour.values()[nbttagcompound.getInteger("colourIndex")];
    } else {
      this.m_colour = null;
    }
    if ((nbttagcompound.hasKey("overlay_mod")) && (nbttagcompound.hasKey("overlay_path")))
    {
      String overlay_mod = nbttagcompound.getString("overlay_mod");
      String overlay_path = nbttagcompound.getString("overlay_path");
      this.m_overlay = new ResourceLocation(overlay_mod, overlay_path);
    }
    else
    {
      this.m_overlay = null;
    }
    this.m_instanceID = nbttagcompound.getInteger("brainInstanceID");
    setClientBrain(this.m_instanceID, this);
    
    TurtleAnimation anim = TurtleAnimation.values()[nbttagcompound.getInteger("animation")];
    if ((anim != this.m_animation) && (anim != TurtleAnimation.Wait) && (anim != TurtleAnimation.ShortWait) && (anim != TurtleAnimation.None))
    {
      this.m_animation = TurtleAnimation.values()[nbttagcompound.getInteger("animation")];
      this.m_animationProgress = 0;
      this.m_lastAnimationProgress = 0;
    }
    this.m_direction = EnumFacing.getFront(nbttagcompound.getInteger("direction"));
    this.m_fuelLevel = nbttagcompound.getInteger("fuelLevel");
  }
  
  public World getWorld()
  {
    return this.m_owner.getEntityWorld();
  }
  
  public BlockPos getPosition()
  {
    return this.m_owner.getPosition();
  }
  
  public boolean teleportTo(World world, BlockPos pos)
  {
    if ((world.isRemote) || (getWorld().isRemote)) {
      throw new UnsupportedOperationException();
    }
    World oldWorld = getWorld();
    BlockPos oldPos = this.m_owner.getPosition();
    Block oldBlock = this.m_owner.getBlock();
    if ((oldWorld == world) && (oldPos.equals(pos))) {
      return true;
    }
    if ((world.isBlockLoaded(pos)) && (world.setBlockState(pos, oldBlock.getDefaultState(), 3)))
    {
      Block block = world.getBlockState(pos).getBlock();
      if (block == oldBlock)
      {
        TileEntity newTile = world.getTileEntity(pos);
        if ((newTile != null) && ((newTile instanceof TileTurtle)))
        {
          TileTurtle newTurtle = (TileTurtle)newTile;
          newTurtle.setWorldObj(world);
          newTurtle.setPos(pos);
          newTurtle.transferStateFrom(this.m_owner);
          newTurtle.createServerComputer().setWorld(world);
          newTurtle.createServerComputer().setPosition(pos);
          
          oldWorld.setBlockToAir(oldPos);
          
          newTurtle.updateInput();
          newTurtle.updateOutput();
          return true;
        }
      }
      world.setBlockToAir(pos);
    }
    return false;
  }
  
  public Vec3 getVisualPosition(float f)
  {
    Vec3 offset = getRenderOffset(f);
    BlockPos pos = this.m_owner.getPosition();
    return new Vec3(pos.getX() + 0.5D + offset.xCoord, pos.getY() + 0.5D + offset.yCoord, pos.getZ() + 0.5D + offset.zCoord);
  }
  
  public float getVisualYaw(float f)
  {
    float forward = DirectionUtil.toYawAngle(getDirection());
    float yaw = forward;
    switch (this.m_animation)
    {
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
  
  public EnumFacing getDirection()
  {
    return this.m_direction;
  }
  
  public void setDirection(EnumFacing dir)
  {
    if (dir.getAxis() == EnumFacing.Axis.Y) {
      dir = EnumFacing.NORTH;
    }
    this.m_direction = dir;
//    this.m_owner.updateOutput();
//    this.m_owner.updateInput();
//    this.m_owner.onTileEntityChange();
  }
  
  public int getSelectedSlot()
  {
    return this.m_selectedSlot;
  }
  
  public void setSelectedSlot(int slot)
  {
    if (getWorld().isRemote) {
      throw new UnsupportedOperationException();
    }
    if ((slot >= 0) && (slot < this.m_owner.getInventory().length))
    {
      this.m_selectedSlot = slot;
    }
  }
  
  public IInventory getInventory()
  {
    return this.m_owner;
  }
  
  public boolean isFuelNeeded()
  {
    return ComputerCraft.turtlesNeedFuel;
  }
  
  public int getFuelLevel()
  {
    return Math.min(this.m_fuelLevel, getFuelLimit());
  }
  
  public void setFuelLevel(int level)
  {
    this.m_fuelLevel = Math.min(level, getFuelLimit());
  }
  
  public int getFuelLimit()
  {
    return ComputerCraft.turtleFuelLimit;
  }
  
  public boolean consumeFuel(int fuel)
  {
    if (getWorld().isRemote) {
      throw new UnsupportedOperationException();
    }
    if (!isFuelNeeded()) {
      return true;
    }
    int consumption = Math.max(fuel, 0);
    if (getFuelLevel() >= consumption)
    {
      setFuelLevel(getFuelLevel() - consumption);
      return true;
    }
    return false;
  }
  
  public void addFuel(int fuel)
  {
    if (getWorld().isRemote) {
      throw new UnsupportedOperationException();
    }
    int addition = Math.max(fuel, 0);
    setFuelLevel(getFuelLevel() + addition);
  }
  
  private int issueCommand(ITurtleCommand command)
  {
    this.m_commandQueue.offer(new TurtleCommandQueueEntry(++this.m_commandsIssued, command));
    return this.m_commandsIssued;
  }
  
  public Object[] executeCommand(ILuaContext context, ITurtleCommand command)
    throws LuaException, InterruptedException
  {
    if (getWorld().isRemote) {
      throw new UnsupportedOperationException();
    }
    int commandID = issueCommand(command);
    for (;;)
    {
      Object[] response = context.pullEvent("turtle_response");
      if ((response.length >= 3) && ((response[1] instanceof Number)) && ((response[2] instanceof Boolean))) {
        if (((Number)response[1]).intValue() == commandID)
        {
          Object[] returnValues = new Object[response.length - 2];
          for (int i = 0; i < returnValues.length; i++) {
            returnValues[i] = response[(i + 2)];
          }
          return returnValues;
        }
      }
    }
  }
  
  public void playAnimation(TurtleAnimation animation)
  {
    if (getWorld().isRemote) {
      throw new UnsupportedOperationException();
    }
    this.m_animation = animation;
    if (this.m_animation == TurtleAnimation.ShortWait)
    {
      this.m_animationProgress = 4;
      this.m_lastAnimationProgress = 4;
    }
    else
    {
      this.m_animationProgress = 0;
      this.m_lastAnimationProgress = 0;
    }
  }
  
  public Vec3 getRenderOffset(float f)
  {
    switch (this.m_animation)
    {
    case MoveForward: 
    case MoveBack: 
    case MoveUp: 
    case MoveDown: 
      EnumFacing dir;
      switch (this.m_animation)
      {
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
      return new Vec3(distance * dir.getFrontOffsetX(), distance * dir.getFrontOffsetY(), distance * dir.getFrontOffsetZ());
    }
    return new Vec3(0.0D, 0.0D, 0.0D);
  }
  
  public float getToolRenderAngle(TurtleSide side, float f)
  {
    if (((side == TurtleSide.Left) && (this.m_animation == TurtleAnimation.SwingLeftTool)) || ((side == TurtleSide.Right) && (this.m_animation == TurtleAnimation.SwingRightTool))) {
      return 45.0F * (float)Math.sin(getAnimationFraction(f) * 3.141592653589793D);
    }
    return 0.0F;
  }
  
  private int toDirection(TurtleSide side)
  {
    switch (side)
    {
    case Left: 
      return 5;
    }
    return 4;
  }
  
  public void updatePeripherals(ServerComputer serverComputer)
  {
    if (serverComputer == null) {
      return;
    }
    for (TurtleSide side : TurtleSide.values())
    {
      ITurtleUpgrade upgrade = getUpgrade(side);
      IPeripheral peripheral = null;
      if ((upgrade != null) && (upgrade.getType() == TurtleUpgradeType.Peripheral)) {
        peripheral = upgrade.createPeripheral(this, side);
      }
      int dir = toDirection(side);
      if (peripheral != null)
      {
        if (!this.m_peripherals.containsKey(side))
        {
          serverComputer.setPeripheral(dir, peripheral);
          serverComputer.setRedstoneInput(dir, 0);
          serverComputer.setBundledRedstoneInput(dir, 0);
          this.m_peripherals.put(side, peripheral);
        }
        else if (!((IPeripheral)this.m_peripherals.get(side)).equals(peripheral))
        {
          serverComputer.setPeripheral(dir, peripheral);
          serverComputer.setRedstoneInput(dir, 0);
          serverComputer.setBundledRedstoneInput(dir, 0);
          this.m_peripherals.remove(side);
          this.m_peripherals.put(side, peripheral);
        }
      }
      else if (this.m_peripherals.containsKey(side))
      {
        serverComputer.setPeripheral(dir, null);
        this.m_peripherals.remove(side);
      }
    }
  }
  
  private void updateCommands()
  {
    if (this.m_animation == TurtleAnimation.None)
    {
      TurtleCommandQueueEntry nextCommand = null;
      if (this.m_commandQueue.peek() != null) {
        nextCommand = (TurtleCommandQueueEntry)this.m_commandQueue.remove();
      }
      if (nextCommand != null)
      {
        TurtleCommandResult result = nextCommand.command.execute(this);
        
        int callbackID = nextCommand.callbackID;
        if (callbackID >= 0) {
          if ((result != null) && (result.isSuccess()))
          {
            IComputer computer = this.m_owner.getComputer();
            if (computer != null)
            {
              Object[] results = result.getResults();
              if (results != null)
              {
                Object[] arguments = new Object[results.length + 2];
                arguments[0] = Integer.valueOf(callbackID);
                arguments[1] = Boolean.valueOf(true);
                for (int i = 0; i < results.length; i++) {
                  arguments[(2 + i)] = results[i];
                }
                computer.queueEvent("turtle_response", arguments);
              }
              else
              {
                computer.queueEvent("turtle_response", new Object[] { Integer.valueOf(callbackID), Boolean.valueOf(true) });
              }
            }
          }
          else
          {
            IComputer computer = this.m_owner.getComputer();
            if (computer != null) {
              computer.queueEvent("turtle_response", new Object[] { Integer.valueOf(callbackID), Boolean.valueOf(false), result != null ? result.getErrorMessage() : null });
            }
          }
        }
      }
    }
  }
  
  private void updateAnimation()
  {
    if (this.m_animation != TurtleAnimation.None)
    {
      World world = getWorld();
      if (ComputerCraft.turtlesCanPush) {
        if ((this.m_animation == TurtleAnimation.MoveForward) || (this.m_animation == TurtleAnimation.MoveBack) || (this.m_animation == TurtleAnimation.MoveUp) || (this.m_animation == TurtleAnimation.MoveDown))
        {
          BlockPos pos = getPosition();
          EnumFacing moveDir;
          switch (this.m_animation)
          {
          case MoveForward: 
          default: 
            moveDir = this.m_direction;
            break;
          case MoveBack: 
            moveDir = this.m_direction.getOpposite();
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
          
          float pushFrac = 1.0F - (this.m_animationProgress + 1) / 8.0F;
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
          List list = world.getEntitiesWithinAABBExcludingEntity((Entity)null, aabb);
          if (!list.isEmpty())
          {
            double pushStep = 0.125D;
            double pushStepX = moveDir.getFrontOffsetX() * pushStep;
            double pushStepY = moveDir.getFrontOffsetY() * pushStep;
            double pushStepZ = moveDir.getFrontOffsetZ() * pushStep;
            for (int i = 0; i < list.size(); i++)
            {
              Entity entity = (Entity)list.get(i);
              entity.moveEntity(pushStepX, pushStepY, pushStepZ);
            }
          }
        }
      }
      if ((world.isRemote) && (this.m_animation == TurtleAnimation.MoveForward) && (this.m_animationProgress == 4))
      {
        Holiday currentHoliday = HolidayUtil.getCurrentHoliday();
        if (currentHoliday == Holiday.Valentines)
        {
          Vec3 position = getVisualPosition(1.0F);
          if (position != null)
          {
            double x = position.xCoord + world.rand.nextGaussian() * 0.1D;
            double y = position.yCoord + 0.5D + world.rand.nextGaussian() * 0.1D;
            double z = position.zCoord + world.rand.nextGaussian() * 0.1D;
            world.spawnParticle(EnumParticleTypes.HEART, x, y, z, world.rand.nextGaussian() * 0.02D, world.rand.nextGaussian() * 0.02D, world.rand.nextGaussian() * 0.02D, new int[0]);
          }
        }
      }
      this.m_lastAnimationProgress = this.m_animationProgress;
      if (++this.m_animationProgress >= 8)
      {
        this.m_animation = TurtleAnimation.None;
        this.m_animationProgress = 0;
        this.m_lastAnimationProgress = 0;
      }
    }
  }
  
  private float getAnimationFraction(float f)
  {
    float next = this.m_animationProgress / 8.0F;
    float previous = this.m_lastAnimationProgress / 8.0F;
    return previous + (next - previous) * f;
  }
}
