package com.dyn.robot.entity;

import java.util.ArrayList;
import java.util.List;

import com.dyn.robot.entity.ai.EntityAIFollowsOwnerEX;
import com.dyn.robot.entity.brain.DynRobotBrain;
import com.dyn.robot.entity.brain.RobotBrain;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.shared.common.IDirectionalTile;
import dan200.computercraft.shared.common.ITerminal;
import dan200.computercraft.shared.computer.core.ClientComputer;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.util.DirectionUtil;
import dan200.computercraft.shared.util.PeripheralUtil;
import dan200.computercraft.shared.util.RedstoneUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public abstract class EntityRobot extends EntityCreature implements IDirectionalTile {

	public static List getEntityItemsInRadius(World world, double x, double y, double z, int radius) {
		List list = world.getEntitiesWithinAABB(EntityItem.class,
				AxisAlignedBB.fromBounds(x, y, z, x + radius, y + radius, z + radius));
		return list;
	}

	protected int m_instanceID;
	protected int m_computerID;
	protected String m_label;
	protected boolean m_on;
	protected boolean m_startOn;
	protected String owner;
	public RobotInventory m_inventory;

	protected RobotBrain m_brain;

	public List<BlockPos> markedChests = new ArrayList();

	public EntityRobot(World worldIn) {
		super(worldIn);
		height = 1;
		width = 0.8f;
		m_instanceID = -1;
		m_computerID = -1;
		m_label = null;
		m_on = false;
		m_startOn = false;
		m_brain = createBrain();
		m_inventory = new RobotInventory(this);
	}

	public ItemStack addItemStack(ItemStack is) {
		if ((is == null) || (is.stackSize <= 0)) {
			return null;
		}

		for (int a = 0; a < m_inventory.getSizeInventory(); a++) {
			if ((m_inventory.getStackInSlot(a) == null) || (m_inventory.getStackInSlot(a).stackSize <= 0)) {
				m_inventory.setInventorySlotContents(a, is);
				return null;
			}
			ItemStack is2 = m_inventory.getStackInSlot(a);
			if ((is2.getItem() == is.getItem()) && (is2.getItemDamage() == is.getItemDamage())) {
				int amount = Math.min(is.stackSize, is2.getMaxStackSize() - is2.stackSize);
				is.stackSize -= amount;
				is2.stackSize += amount;
				m_inventory.setInventorySlotContents(a, is2);
			}
			if (is.stackSize <= 0) {
				return null;
			}
		}
		return is;
	}

	public void addItemToChest(ItemStack is, TileEntityChest chest) {
		if ((is == null) || (is.stackSize <= 0) || (chest == null)) {
			return;
		}

		for (int a = 0; a < chest.getSizeInventory(); a++) {
			ItemStack is2 = chest.getStackInSlot(a);
			if ((is2 == null) || (is2.stackSize <= 0)) {
				chest.setInventorySlotContents(a, is.copy());
				decreaseItemStack(is);
				return;
			}
			if ((is2.getItem() == is.getItem()) && (is2.getItemDamage() == is.getItemDamage())) {
				int amount = Math.min(is.stackSize, is2.getMaxStackSize() - is2.stackSize);
				ItemStack is3 = is.copy();
				is3.stackSize = amount;
				decreaseItemStack(is3);
				is.stackSize -= amount;
				is2.stackSize += amount;
				chest.setInventorySlotContents(a, is2);
			}
			if (is.stackSize <= 0) {
				return;
			}
		}
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(0.1D);
		getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(16.0D);
		getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(100.0D);
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.2D);
		getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage);
		getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(1.0D);
	}

	@Override
	public boolean attackEntityAsMob(Entity par1Entity) {
		return par1Entity.attackEntityFrom(DamageSource.causeMobDamage(this), 1.0f);
	}

	@Override
	protected boolean canDespawn() {
		return false;
	}

	public boolean canExecuteAI() {
		return true;
	}

	protected boolean canNameWithTag(EntityPlayer player) {
		return false;
	}

	// part needed for computer craft programming of robot
	protected RobotBrain createBrain() {
		return new DynRobotBrain(this);
	}

	public ClientComputer createClientComputer() {
		if (worldObj.isRemote) {
			if (m_instanceID < 0) {
				m_instanceID = ComputerCraft.clientComputerRegistry.getUnusedInstanceID();
			}
			if (m_instanceID >= 0) {
				if (!ComputerCraft.clientComputerRegistry.contains(m_instanceID)) {
					ComputerCraft.clientComputerRegistry.add(m_instanceID, new ClientComputer(m_instanceID));
				}
				return ComputerCraft.clientComputerRegistry.get(m_instanceID);
			}
		}
		return null;
	}

	public IComputer createComputer() {
		if (worldObj.isRemote) {
			return createClientComputer();
		}
		return createServerComputer();
	}

	protected abstract ServerComputer createComputer(int paramInt1, int paramInt2);

	public ServerComputer createServerComputer() {
		if (!worldObj.isRemote) {
			if (m_instanceID < 0) {
				m_instanceID = ComputerCraft.serverComputerRegistry.getUnusedInstanceID();
			}
			if (!ComputerCraft.serverComputerRegistry.contains(m_instanceID)) {
				ServerComputer computer = createComputer(m_instanceID, m_computerID);
				ComputerCraft.serverComputerRegistry.add(m_instanceID, computer);
			}
			return ComputerCraft.serverComputerRegistry.get(m_instanceID);
		}
		return null;
	}

	public boolean decreaseItemStack(ItemStack is) {
		if ((is == null) || (is.stackSize <= 0)) {
			return true;
		}
		for (int a = 0; a < m_inventory.getSizeInventory(); a++) {
			ItemStack is2 = m_inventory.getStackInSlot(a);
			if (is2 == null) {
				continue;
			}
			if ((is2.getItem() == is.getItem())
					&& ((is2.getItemDamage() == is.getItemDamage()) || !is.getItem().getHasSubtypes())) {
				int amount = Math.min(is.stackSize, is2.stackSize);
				is.stackSize -= amount;
				is2.stackSize -= amount;
				if (is2.stackSize <= 0) {
					is2 = null;
				}
				m_inventory.setInventorySlotContents(a, is2);
			}
			if (is.stackSize <= 0) {
				return true;
			}
		}
		return is.stackSize > 0;
	}

	public void destroy() {
		unload();
		for (EnumFacing dir : EnumFacing.VALUES) {
			RedstoneUtil.propogateRedstoneOutput(worldObj, getPosition(), dir);
		}
	}

	public boolean doesInventoryHas(Class<? extends Item> c) {
		for (int a = 0; a < m_inventory.getSizeInventory(); a++) {
			if ((m_inventory.getStackInSlot(a) != null) && c.isInstance(m_inventory.getStackInSlot(a).getItem())) {
				return true;
			}
		}
		return false;
	}

	public boolean doesInventoryHas(ItemStack is) {
		for (int a = 0; a < m_inventory.getSizeInventory(); a++) {
			if (m_inventory.getStackInSlot(a) != null) {
				ItemStack is2 = m_inventory.getStackInSlot(a);
				if ((is2.getItem() == is.getItem())
						&& ((is2.getItemDamage() == is.getItemDamage()) || is.isItemStackDamageable())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean filterItemToGet(ItemStack is) {
		return true;
	}

	public boolean filterItemToStore(ItemStack is) {
		return true;
	}

	public ITurtleAccess getAccess() {
		return m_brain;
	}

	public boolean getBundledRedstoneConnectivity(EnumFacing side) {
		int localDir = remapLocalSide(DirectionUtil.toLocal(this, side));
		return !isRedstoneBlockedOnSide(localDir);
	}

	public int getBundledRedstoneOutput(EnumFacing side) {
		int localDir = remapLocalSide(DirectionUtil.toLocal(this, side));
		if (!isRedstoneBlockedOnSide(localDir)) {
			if (!worldObj.isRemote) {
				ServerComputer computer = getServerComputer();
				if (computer != null) {
					return computer.getBundledRedstoneOutput(localDir);
				}
			}
		}
		return 0;
	}

	@Override
	public boolean getCanSpawnHere() {
		// dont spawn robots
		return false;
	}

	public ClientComputer getClientComputer() {
		if (worldObj.isRemote) {
			return ComputerCraft.clientComputerRegistry.get(m_instanceID);
		}
		return null;
	}

	public IComputer getComputer() {
		if (worldObj.isRemote) {
			return getClientComputer();
		}
		return getServerComputer();
	}

	@Override
	public EnumFacing getDirection() {
		// alternatively we can do this but if the robot turns its head it will
		// give a different direction than we probably want
		// this.getHorizontalFacing();
		return m_brain.getDirection();
	}

	public String getOwner() {
		return owner;
	}

	public boolean getRedstoneConnectivity(EnumFacing side) {
		int localDir = remapLocalSide(DirectionUtil.toLocal(this, side));
		return !isRedstoneBlockedOnSide(localDir);
	}

	public int getRedstoneOutput(EnumFacing side) {
		int localDir = remapLocalSide(DirectionUtil.toLocal(this, side));
		if (!isRedstoneBlockedOnSide(localDir)) {
			if ((worldObj != null) && (!worldObj.isRemote)) {
				ServerComputer computer = getServerComputer();
				if (computer != null) {
					return computer.getRedstoneOutput(localDir);
				}
			}
		}
		return 0;
	}

	public ServerComputer getServerComputer() {
		if (!worldObj.isRemote) {
			return ComputerCraft.serverComputerRegistry.get(m_instanceID);
		}
		return null;
	}

	public ITerminal getTerminal() {
		return getComputer();
	}

	public boolean hasNeededItem() {
		return false;
	}

	public void invalidate() {
		unload();
	}

	@Override
	public boolean isAIDisabled() {
		return false;
	}

	public boolean isInventoryEmpty() {
		for (int a = 0; a < m_inventory.getSizeInventory(); a++) {
			if (m_inventory.getStackInSlot(a) != null) {
				return false;
			}
		}
		return true;
	}

	public boolean isInventoryFull() {
		for (int a = 0; a < m_inventory.getSizeInventory(); a++) {
			if ((m_inventory.getStackInSlot(a) != null) && (m_inventory.getStackInSlot(a).stackSize == 0)) {
				m_inventory.removeStackFromSlot(a);
			}
			if ((m_inventory.getStackInSlot(a) == null) || (m_inventory.getStackInSlot(a).stackSize == 0)) {
				return false;
			}
		}

		return true;
	}

	protected boolean isPeripheralBlockedOnSide(int localSide) {
		return false;
	}

	protected boolean isRedstoneBlockedOnSide(int localSide) {
		return false;
	}

	@Override
	public void onDeath(DamageSource d) {
		super.onDeath(d);
		unload();
	}

	public void onNeighbourChange() {
		updateInput();
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		m_brain.update();
		if (!worldObj.isRemote) {
			ServerComputer computer = createServerComputer();
			if (computer != null) {
				if (m_startOn) {
					computer.turnOn();
					m_startOn = false;
				}
				computer.keepAlive();
				if (computer.hasOutputChanged()) {
					for (EnumFacing dir : EnumFacing.VALUES) {
						RedstoneUtil.propogateRedstoneOutput(worldObj, getPosition(), dir);
					}
				}
				m_computerID = computer.getID();
				m_label = computer.getLabel();
				m_on = computer.isOn();
			}
		} else {
			ClientComputer computer = createClientComputer();
			if (computer != null) {
				if (computer.hasOutputChanged()) {

				}
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		NBTTagList nbttaglist = nbttagcompound.getTagList("Items", 10);
		// m_inventory = new ItemStack[32];
		for (int i = 0; i < nbttaglist.tagCount(); i++) {
			NBTTagCompound itemtag = nbttaglist.getCompoundTagAt(i);
			int slot = itemtag.getByte("Slot") & 0xFF;
			if ((slot >= 0) && (slot < 32)) {
				m_inventory.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(itemtag));
			}
		}
		int id = -1;
		if (nbttagcompound.hasKey("computerID")) {
			id = nbttagcompound.getInteger("computerID");
		} else if (nbttagcompound.hasKey("userDir")) {
			String userDir = nbttagcompound.getString("userDir");
			try {
				id = Integer.parseInt(userDir);
			} catch (NumberFormatException e) {
			}
		}
		m_computerID = id;
		if (nbttagcompound.hasKey("label")) {
			m_label = nbttagcompound.getString("label");
		} else {
			m_label = null;
		}
		m_startOn = nbttagcompound.getBoolean("on");
		m_on = m_startOn;
		m_instanceID = nbttagcompound.getInteger("instanceID");

		if (nbttagcompound.hasKey("owner")) {
			owner = nbttagcompound.getString("owner");
		} else {
			owner = null;
		}
		m_brain.readFromNBT(nbttagcompound);
	}

	protected int remapLocalSide(int localSide) {
		return localSide;
	}

	public void setComputerID(int id) {
		if ((!worldObj.isRemote) && (m_computerID != id)) {
			m_computerID = id;
			ServerComputer computer = getServerComputer();
			if (computer != null) {
				computer.setID(m_computerID);
			}
		}
	}

	@Override
	public void setDirection(EnumFacing dir) {
		m_brain.setDirection(dir);
	}

	public void setLabel(String label) {
		if (!worldObj.isRemote) {
			createServerComputer().setLabel(label);
		}
	}

	public void setMoveTo(BlockPos pos, float ms) {
		setMoveTo(pos.getX(), pos.getY(), pos.getZ(), ms);
	}

	public void setMoveTo(double x, double y, double z, float ms) {
		getNavigator().tryMoveToXYZ((int) x, (int) y, (int) z, ms);
	}

	public void setMoveTo(Entity e, float ms) {
		setMoveTo(e.posX, e.posY, e.posZ, ms);
	}

	public void setOwner(Entity owner) {
		if (owner instanceof EntityPlayer) {
			this.owner = owner.getName();
			tasks.addTask(1, new EntityAIFollowsOwnerEX(this, (EntityPlayer) owner, 1.0F, 1.5F, 10.0F));
			m_brain.setOwnerName(owner.getName());
		}
	}

	public boolean shouldStoreItems(int a) {
		return true;
	}

	protected void transferStateFrom(EntityRobot copy) {
		if ((copy.m_computerID != m_computerID) || (copy.m_instanceID != m_instanceID)) {
			unload();
			m_instanceID = copy.m_instanceID;
			m_computerID = copy.m_computerID;
			m_label = copy.m_label;
			m_on = copy.m_on;
			m_startOn = copy.m_startOn;
		}
		copy.m_instanceID = -1;
	}

	protected void unload() {
		if (m_instanceID >= 0) {
			if (!worldObj.isRemote) {
				ComputerCraft.serverComputerRegistry.remove(m_instanceID);
			}
			m_instanceID = -1;
		}
	}

	public void updateInput() {
		if ((worldObj == null) || (worldObj.isRemote)) {
			return;
		}
		ServerComputer computer = getServerComputer();
		if (computer != null) {
			BlockPos pos = computer.getPosition();
			for (EnumFacing dir : EnumFacing.VALUES) {
				BlockPos offset = pos.offset(dir);
				EnumFacing offsetSide = dir.getOpposite();
				int localDir = remapLocalSide(DirectionUtil.toLocal(this, dir));
				if (!isRedstoneBlockedOnSide(localDir)) {
					computer.setRedstoneInput(localDir, RedstoneUtil.getRedstoneOutput(worldObj, offset, offsetSide));
					computer.setBundledRedstoneInput(localDir,
							RedstoneUtil.getBundledRedstoneOutput(worldObj, offset, offsetSide));
				}
				if (!isPeripheralBlockedOnSide(localDir)) {
					computer.setPeripheral(localDir, PeripheralUtil.getPeripheral(worldObj, offset, offsetSide));
				}
			}
		}
	}

	public void updateOutput() {
		for (EnumFacing dir : EnumFacing.VALUES) {
			RedstoneUtil.propogateRedstoneOutput(worldObj, getPosition(), dir);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0; i < 16; i++) {
			if (m_inventory.getStackInSlot(i) != null) {
				NBTTagCompound itemtag = new NBTTagCompound();
				itemtag.setByte("Slot", (byte) i);
				m_inventory.getStackInSlot(i).writeToNBT(itemtag);
				nbttaglist.appendTag(itemtag);
			}
		}
		nbttagcompound.setTag("Items", nbttaglist);

		if (m_computerID >= 0) {
			nbttagcompound.setInteger("computerID", m_computerID);
		}
		if (m_label != null) {
			nbttagcompound.setString("label", m_label);
		}
		nbttagcompound.setBoolean("on", m_on);
		nbttagcompound.setInteger("instanceID", createServerComputer().getInstanceID());
		nbttagcompound.setString("owner", owner);
		m_brain.writeToNBT(nbttagcompound);
	}
}
