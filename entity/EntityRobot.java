package com.dyn.robot.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.dyn.DYNServerMod;
import com.dyn.render.hud.path.EntityPathRenderer;
import com.dyn.robot.entity.ai.EntityAIExecuteProgrammedPath;
import com.dyn.robot.entity.ai.EntityAIFollowsOwnerEX;
import com.dyn.robot.entity.ai.EntityAIJumpToward;
import com.dyn.robot.entity.ai.EntityAIRobotAttackTarget;
import com.dyn.robot.entity.ai.EntiyAIBuildSchematic;
import com.dyn.robot.entity.inventory.RobotInventory;
import com.dyn.robot.entity.pathing.PathNavigateRobot;
import com.dyn.utils.HelperFunctions;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public abstract class EntityRobot extends EntityCreature implements IEntityOwnable, IEntityAdditionalSpawnData {
	public static List getEntityItemsInRadius(World world, double x, double y, double z, int radius) {
		List list = world.getEntitiesWithinAABB(EntityItem.class,
				AxisAlignedBB.fromBounds(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius));
		return list;
	}

	protected boolean shouldFollow;
	protected boolean isTamable;
	protected EntityPlayer owner;
	public RobotInventory robot_inventory;
	private List<BlockPos> programPath = new ArrayList();

	private boolean executeCode = false;
	private boolean shouldJump;
	public Map<Long, String> messages = new TreeMap<>();
	private boolean pauseCode = false;

	private EntityAIWander wanderTask;

	public final int on1 = 50;
	public final int on2 = 75;
	public int counter = 0;

	private EnumFacing programDir;
	private boolean buildSchematic;

	public EntityRobot(World worldIn) {
		super(worldIn);
		height = .9f;
		width = 0.5f;
		shouldFollow = false;
		executeCode = false;
		isTamable = false;
		robot_inventory = new RobotInventory("Robot Inventory", 30, this);
		dataWatcher.addObject(17, "");// owner uuid
		dataWatcher.addObject(18, "");// robot name

		try {
			Class.forName("mobi.omegacentauri.raspberryjammod.RaspberryJamMod");
			tasks.addTask(1, new EntityAIExecuteProgrammedPath(this, 1.5D));
			tasks.addTask(1, new EntityAIJumpToward(this, 0.4F));
			tasks.addTask(1, new EntityAIRobotAttackTarget(this, EntityLivingBase.class, 1.0D, false));
			tasks.addTask(1, new EntiyAIBuildSchematic(this));
		} catch (ClassNotFoundException er) {
			// this is just to make sure rjm exists
		}

		tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		tasks.addTask(3, wanderTask = new EntityAIWander(this, 1.0D));
		tasks.addTask(4, new EntityAILookIdle(this));
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
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.18D);
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

	public boolean climb(int amount) {
		BlockPos dest = getPosition();
		if (!programPath.isEmpty()) {
			dest = programPath.get(programPath.size() - 1);
		}

		for (int i = 0; i < amount; i++) {
			if (worldObj.getBlockState(dest).getBlock().isLadder(worldObj, dest, this)) {
				dest = dest.up();

				if (!worldObj.getBlockState(dest).getBlock().isLadder(worldObj, dest, this)) {
					dest = dest.offset(getProgrammedDirection());
				}
				addToProgramPath(dest);
			} else {
				dest = dest.up().offset(getProgrammedDirection());
				Block block = worldObj.getBlockState(dest).getBlock();
				Block blockdn = worldObj.getBlockState(dest.down()).getBlock();
				if (blockdn.getMaterial().blocksMovement() && block.isPassable(getEntityWorld(), dest)) {
					addToProgramPath(dest);
				} else {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Deals damage to the entity. If its a EntityPlayer then will take damage
	 * from the armor first and then health second with the reduced value. Args:
	 * damageAmount
	 */
	@Override
	protected void damageEntity(DamageSource damageSrc, float damageAmount) {
		if (!(damageSrc.getEntity() instanceof EntityPlayer)
				&& !((damageSrc == DamageSource.fall) && (damageAmount > 25))) {
			super.damageEntity(damageSrc, damageAmount);
		}
	}

	public boolean descend(int amount) {
		BlockPos dest = getPosition();
		if (!programPath.isEmpty()) {
			dest = programPath.get(programPath.size() - 1);
		}

		for (int i = 0; i < amount; i++) {
			if (worldObj.getBlockState(dest).getBlock().isLadder(worldObj, dest, this)) {
				dest = dest.down();
				addToProgramPath(dest);
			} else {
				dest = dest.down().offset(getProgrammedDirection());
				if (worldObj.getBlockState(dest).getBlock().isLadder(worldObj, dest, this)) {
					addToProgramPath(dest);
				} else {
					Block block = worldObj.getBlockState(dest).getBlock();
					Block blockdn = worldObj.getBlockState(dest.down()).getBlock();
					if (blockdn.getMaterial().blocksMovement() && block.isPassable(getEntityWorld(), dest)) {
						addToProgramPath(dest);
					} else {
						return false;
					}
				}
			}
		}
		return true;
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

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected String getDeathSound() {
		return "mob.irongolem.death";
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected String getHurtSound() {
		return "mob.blaze.hit";
	}

	public boolean getIsFollowing() {
		return shouldFollow;
	}

	@Override
	protected float getJumpUpwardsMotion() {
		return 0.41F;
	}

	/**
	 * Returns the sound this mob makes while it's alive.
	 */
	@Override
	protected String getLivingSound() {
		return "dynrobot:robot.beep";
	}

	@Override
	public int getMaxFallHeight() {
		return 20;
	}

	public int getMemorySize() {
		return (int) (robot_inventory.getStackInSlot(1) != null
				? Math.pow(2, (4 + robot_inventory.getStackInSlot(1).getItemDamage())) : 8);
	}

	public Map<Long, String> getMessages() {
		Map<Long, String> messages = new TreeMap<>();
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

	public EnumFacing getProgrammedDirection() {
		return programDir;
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

	/**
	 * Get number of ticks, at least during which the living entity will be
	 * silent.
	 */
	@Override
	public int getTalkInterval() {
		return 240;
	}

	public boolean hasNeededItem() {
		return false;
	}

	public void InsertToProgramPath(int loc, BlockPos pos) {
		// block pos is integer based but we want to move to the center of the
		// block
		programPath.add(loc, pos);
	}

	@Override
	public boolean isAIDisabled() {
		return false;
	}

	public boolean isCodePaused() {
		return pauseCode;
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
			return false;
		}
		return (entityIn == getOwner()) || getOwnerId().equals(entityIn.getUniqueID().toString());
	}

	public boolean isTamable() {
		return isTamable;
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
		switch (getProgrammedDirection()) {
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
		switch (getProgrammedDirection()) {
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

	public void pauseCodeExecution() {
		pauseCode = true;
	}

	@Override
	protected void playStepSound(BlockPos pos, Block blockIn) {
		playSound("mob.chicken.step", 0.15F, 1.0F);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);

		NBTTagList nbttaglist = nbttagcompound.getTagList("Items", 10);
		// robot_inventory = new ItemStack[32];
		for (int i = 0; i < nbttaglist.tagCount(); i++) {
			NBTTagCompound itemtag = nbttaglist.getCompoundTagAt(i);
			int slot = itemtag.getByte("Slot") & 0xFF;
			if ((slot >= 0) && (slot < 32)) {
				robot_inventory.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(itemtag));
			}
		}

		shouldFollow = nbttagcompound.getBoolean("follow");
		isTamable = nbttagcompound.getBoolean("tame");

		String robotName = nbttagcompound.getString("robotName");
		if (robotName.length() > 0) {
			setRobotName(robotName);
		}
		String ownerID = nbttagcompound.getString("OwnerUUID");
		if (ownerID.length() > 0) {
			setOwnerId(ownerID);
		}
	}

	/**
	 * Called by the client when it receives a Entity spawn packet. Data should
	 * be read out of the stream in the same way as it was written.
	 *
	 * @param data
	 *            The packet data stream
	 */
	@Override
	public void readSpawnData(ByteBuf additionalData) {
		isTamable = additionalData.readBoolean();
		shouldFollow = additionalData.readBoolean();
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

	public void resumeExecution() {
		pauseCode = false;
	}

	public void rotate(float yaw) {
		rotationYaw = yaw;
		setRotationYawHead(yaw);
		setRenderYawOffset(yaw);
		programDir = EnumFacing.fromAngle(yaw);
	}

	public void setBuildSchematic(boolean buildSchematic) {
		this.buildSchematic = buildSchematic;
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
		EntityPathRenderer.removeEntityForPathRendering(this);
	}

	public void setRobotName(String robotName) {
		dataWatcher.updateObject(18, robotName);
		setCustomNameTag(robotName);
		setAlwaysRenderNameTag(true);
	}

	public void setShouldJump(boolean state) {
		shouldJump = state;
	}

	public void setTamable(boolean isTamable) {
		this.isTamable = isTamable;
	}

	// I think its ok if we don't sync this variable, it should only happen in
	// code and doesnt need persistence
	public boolean shouldBuildSchematic() {
		return buildSchematic;
	}

	public boolean shouldExecuteCode() {
		return executeCode;
	}

	public void startExecutingCode() {
		executeCode = true;
		programDir = getHorizontalFacing();

		setPosition(getPosition().getX() + .5, getPosition().getY(), getPosition().getZ() + .5);
		rotate(HelperFunctions.getAngleFromFacing(programDir));
	}

	public void stopExecutingCode() {
		executeCode = false;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);

		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0; i < robot_inventory.getSizeInventory(); i++) {
			if (robot_inventory.getStackInSlot(i) != null) {
				NBTTagCompound itemtag = new NBTTagCompound();
				itemtag.setByte("Slot", (byte) i);
				robot_inventory.getStackInSlot(i).writeToNBT(itemtag);
				nbttaglist.appendTag(itemtag);
			}
		}
		nbttagcompound.setTag("Items", nbttaglist);

		nbttagcompound.setString("robotName", dataWatcher.getWatchableObjectString(18));
		nbttagcompound.setBoolean("follow", shouldFollow);
		nbttagcompound.setBoolean("tame", isTamable);

		if (getOwnerId() == null) {
			nbttagcompound.setString("OwnerUUID", "");
		} else {
			nbttagcompound.setString("OwnerUUID", getOwnerId());
		}
	}

	/**
	 * Called by the server when constructing the spawn packet. Data should be
	 * added to the provided stream.
	 *
	 * @param buffer
	 *            The packet data stream
	 */
	@Override
	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeBoolean(isTamable);
		buffer.writeBoolean(shouldFollow);
	}
}
