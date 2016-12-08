package com.dyn.robot.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.dyn.DYNServerMod;
import com.dyn.robot.entity.ai.EntityAIExecuteProgrammedPath;
import com.dyn.robot.entity.ai.EntityAIFollowsOwnerEX;
import com.dyn.robot.entity.ai.EntityAIJumpToward;
import com.dyn.robot.entity.pathing.PathNavigateRobot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public abstract class EntityRobot extends EntityCreature implements IEntityOwnable {
	public static List getEntityItemsInRadius(World world, double x, double y, double z, int radius) {
		List list = world.getEntitiesWithinAABB(EntityItem.class,
				AxisAlignedBB.fromBounds(x, y, z, x + radius, y + radius, z + radius));
		return list;
	}

	protected boolean shouldFollow;
	protected EntityPlayer owner;
	public RobotInventory m_inventory;
	private List<BlockPos> programPath = new ArrayList();

	private boolean executeCode = false;
	public List<BlockPos> markedChests = new ArrayList();
	private boolean shouldJump;
	public Map<Long, String> messages = new TreeMap<Long, String>();
	private boolean pauseCode = false;

	private EntityAIWander wanderTask;

	public final int on1 = 50;
	public final int on2 = 75;
	public int counter = 0;

	public EntityRobot(World worldIn) {
		super(worldIn);
		height = .9f;
		width = 0.5f;
		shouldFollow = false;
		executeCode = false;
		m_inventory = new RobotInventory(this);
		dataWatcher.addObject(17, "");// owner uuid
		dataWatcher.addObject(18, "");// robot name

		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			tasks.addTask(1, new EntityAIExecuteProgrammedPath(this, 1.5D));
		}
		tasks.addTask(1, new EntityAIJumpToward(this, 0.4F));
		tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		tasks.addTask(3, wanderTask = new EntityAIWander(this, 1.0D));
		tasks.addTask(4, new EntityAILookIdle(this));
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

	public ItemStack addItemStackToInventory(ItemStack is) {
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

	public void addMessage(String message) {
		long time = System.currentTimeMillis();

		messages.put(time, message);
		if (messages.size() > 3) {
			messages.remove(messages.keySet().iterator().next());
		}
	}

	public void addToProgramPath(BlockPos pos) {
		// block pos is integer based but we want to move to the center of the
		// block
		programPath.add(pos);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(0.1D);
		getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(32.0D);
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

	public void clearProgramPath() {
		programPath.clear();
	}

	public void climb(int amount) {
		// TODO: make this better
		BlockPos dest = getPosition();
		if (!programPath.isEmpty()) {
			dest = programPath.get(programPath.size() - 1);
		}
		if (isOnLadder() || worldObj.getBlockState(getPosition()).getBlock().isLadder(worldObj, getPosition(), this)) {
			for (int i = 0; i < amount; i++) {
				dest = dest.up();
				addToProgramPath(dest);
			}
		} else {
			for (int i = 0; i < amount; i++) {
				dest = dest.up().offset(getHorizontalFacing());
				addToProgramPath(dest);
			}
		}
	}

	/**
	 * Deals damage to the entity. If its a EntityPlayer then will take damage
	 * from the armor first and then health second with the reduced value. Args:
	 * damageAmount
	 */
	@Override
	protected void damageEntity(DamageSource damageSrc, float damageAmount) {
		if (!(damageSrc.getEntity() instanceof EntityPlayer)
				&& ((damageSrc == DamageSource.fall) && (damageAmount > 10))) {
			super.damageEntity(damageSrc, damageAmount);
		}
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

	@Override
	public boolean getCanSpawnHere() {
		// dont spawn robots
		return false;
	}

	public boolean getIsFollowing() {
		return shouldFollow;
	}

	@Override
	public int getMaxFallHeight() {
		return 10;
	}

	public Map<Long, String> getMessages() {
		Map<Long, String> messages = new TreeMap<Long, String>();
		long time = System.currentTimeMillis();
		for (Map.Entry<Long, String> entry : this.messages.entrySet()) {
			if (time > (entry.getKey() + 10000L)) {
				continue;
			}
			messages.put(entry.getKey(), entry.getValue());
		}
		return this.messages = messages;
	}

	@Override
	protected PathNavigate getNewNavigator(World worldIn) {
		return new PathNavigateRobot(this, worldIn);
	}

	@Override
	public EntityPlayer getOwner() {
		return owner;
	}

	public EntityLivingBase getOwnerByID() {
		try {
			UUID uuid = UUID.fromString(getOwnerId());
			return uuid == null ? null : worldObj.getPlayerEntityByUUID(uuid);
		} catch (IllegalArgumentException var2) {
			return null;
		}
	}

	@Override
	public String getOwnerId() {
		return dataWatcher.getWatchableObjectString(17);
	}

	public List<BlockPos> getProgramPath() {
		return programPath;
	}

	public String getRobotName() {
		return dataWatcher.getWatchableObjectString(18);
	}

	public boolean getShouldJump() {
		return shouldJump;
	}

	public boolean hasNeededItem() {
		return false;
	}

	@Override
	public boolean isAIDisabled() {
		return false;
	}

	public boolean isCodePaused() {
		return pauseCode;
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

	public boolean isOwner(EntityPlayer entityIn) {
		try {
			if (getOwner() == null) {
				if (getOwnerId().equals(entityIn.getUniqueID().toString())) {
					owner = entityIn;
				}
			}
		} catch (Exception e) {
			DYNServerMod.logger.info("No Owner Information Present");
		}
		return (entityIn == getOwner()) || getOwnerId().equals(entityIn.getUniqueID().toString());
	}

	public void moveBackward(int num) {
		if (getIsFollowing()) {
			setIsFollowing(false);
		}
		pauseCodeExecution();
		BlockPos dest = getPosition();
		if (!programPath.isEmpty()) {
			dest = programPath.get(programPath.size() - 1);
		}
		switch (getHorizontalFacing()) {
		case NORTH:
			for (int i = 0; i < num; i++) {
				dest = dest.south();
				addToProgramPath(dest);
			}
			break;
		case SOUTH:
			for (int i = 0; i < num; i++) {
				dest = dest.north();
				addToProgramPath(dest);
			}
			break;
		case EAST:
			for (int i = 0; i < num; i++) {
				dest = dest.west();
				addToProgramPath(dest);
			}
			break;
		case WEST:
			for (int i = 0; i < num; i++) {
				dest = dest.east();
				addToProgramPath(dest);
			}
			break;
		default:
			dest = getPosition();
			break;
		}
		resumeExecution();
	}

	public void moveForward(int num) {
		if (getIsFollowing()) {
			setIsFollowing(false);
		}
		pauseCodeExecution();
		BlockPos dest = getPosition();
		if (!programPath.isEmpty()) {
			dest = programPath.get(programPath.size() - 1);
		}
		switch (getHorizontalFacing()) {
		case NORTH:
			for (int i = 0; i < num; i++) {
				dest = dest.north();
				addToProgramPath(dest);
			}
			break;
		case SOUTH:
			for (int i = 0; i < num; i++) {
				dest = dest.south();
				addToProgramPath(dest);
			}
			break;
		case EAST:
			for (int i = 0; i < num; i++) {
				dest = dest.east();
				addToProgramPath(dest);
			}
			break;
		case WEST:
			for (int i = 0; i < num; i++) {
				dest = dest.west();
				addToProgramPath(dest);
			}
			break;
		default:
			dest = getPosition();
			break;
		}
		resumeExecution();
	}

	private void pauseCodeExecution() {
		pauseCode = true;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);

		NBTTagList nbttaglist = nbttagcompound.getTagList("Items", 10);
		// m_inventory = new ItemStack[32];
		for (int i = 0; i < nbttaglist.tagCount(); i++) {
			NBTTagCompound itemtag = nbttaglist.getCompoundTagAt(i);
			int slot = itemtag.getByte("Slot") & 0xFF;
			if ((slot >= 0) && (slot < 32)) {
				m_inventory.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(itemtag));
			}
		}

		shouldFollow = nbttagcompound.getBoolean("follow");
		String robotName = nbttagcompound.getString("robotName");
		if (robotName.length() > 0) {
			setRobotName(robotName);
		}
		String ownerID = nbttagcompound.getString("OwnerUUID");
		if (ownerID.length() > 0) {
			setOwnerId(ownerID);
		}
	}

	public void reinitNonEssentialAI() {
		tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		tasks.addTask(4, new EntityAILookIdle(this));
	}

	public void removeNonEssentialAI() {
		List<EntityAIBase> nonEssentialAIs = new ArrayList();
		for (EntityAITaskEntry task : tasks.taskEntries) {
			if ((task.action instanceof EntityAIWatchClosest) || (task.action instanceof EntityAILookIdle)) {
				nonEssentialAIs.add(task.action);
			}
		}
		for (EntityAIBase ai : nonEssentialAIs) {
			tasks.removeTask(ai);
		}
	}

	private void resumeExecution() {
		pauseCode = false;
	}

	public void setIsFollowing(boolean shouldFollow) {
		this.shouldFollow = shouldFollow;
	}

	public void setOwner(EntityPlayer player) {
		owner = player;
		tasks.addTask(2, new EntityAIFollowsOwnerEX(this, 1.5D, 6.0F, 1.25F));
		setOwnerId(player.getUniqueID().toString());
	}

	public void setOwner(UUID playerId) {
		owner = worldObj.getPlayerEntityByUUID(playerId);
		tasks.addTask(2, new EntityAIFollowsOwnerEX(this, 1.5D, 6.0F, 1.25F));
	}

	public void setOwnerId(String ownerUuid) {
		dataWatcher.updateObject(17, ownerUuid);
		tasks.removeTask(wanderTask);
		setOwner(UUID.fromString(ownerUuid));
	}

	public void setRobotName(String robotName) {
		dataWatcher.updateObject(18, robotName);
		setCustomNameTag(robotName);
		setAlwaysRenderNameTag(true);
	}

	public void setShouldJump(boolean state) {
		shouldJump = state;
	}

	public boolean shouldExecuteCode() {
		return executeCode;
	}

	public boolean shouldStoreItems(int a) {
		return true;
	}

	public void startExecutingCode() {
		executeCode = true;
	}

	public void stopExecutingCode() {
		executeCode = false;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);

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
		nbttagcompound.setString("robotName", dataWatcher.getWatchableObjectString(18));
		nbttagcompound.setBoolean("follow", shouldFollow);

		if (getOwnerId() == null) {
			nbttagcompound.setString("OwnerUUID", "");
		} else {
			nbttagcompound.setString("OwnerUUID", getOwnerId());
		}
	}
}
