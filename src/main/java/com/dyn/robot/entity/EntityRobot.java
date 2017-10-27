package com.dyn.robot.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.ai.EntityAIExecuteProgrammedPath;
import com.dyn.robot.entity.ai.EntityAIFollowsOwnerEX;
import com.dyn.robot.entity.ai.EntityAIJumpToward;
import com.dyn.robot.entity.ai.EntityAIRobotAttackTarget;
import com.dyn.robot.entity.inventory.RobotInventory;
import com.dyn.robot.entity.pathing.PathNavigateRobot;
import com.dyn.robot.items.ItemMemoryWipe;
import com.dyn.robot.items.ItemRemote;
import com.dyn.robot.items.ItemWrench;
import com.dyn.robot.utils.HelperFunctions;
import com.google.common.base.Optional;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
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
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public abstract class EntityRobot extends EntityCreature implements IEntityOwnable, IEntityAdditionalSpawnData {
	protected static final DataParameter<Optional<UUID>> OWNER_UNIQUE_ID = EntityDataManager
			.<Optional<UUID>>createKey(EntityRobot.class, DataSerializers.OPTIONAL_UNIQUE_ID);

	protected static final DataParameter<String> ROBOT_NAME = EntityDataManager.<String>createKey(EntityRobot.class,
			DataSerializers.STRING);

	public static List<EntityItem> getEntityItemsInRadius(World world, double x, double y, double z, int radius) {
		List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class,
				new AxisAlignedBB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius));
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

	public int counter = 0;

	private EnumFacing programDir;
	private boolean buildSchematic;

	protected boolean shouldSwingArm;

	public EntityRobot(World worldIn) {
		super(worldIn);
		height = .9f;
		width = 0.5f;
		shouldFollow = false;
		executeCode = false;
		isTamable = false;
		robot_inventory = new RobotInventory("Robot Inventory", 30, this);
		dataManager.register(EntityRobot.OWNER_UNIQUE_ID, Optional.absent());
		dataManager.register(EntityRobot.ROBOT_NAME, "");// robot name

		tasks.addTask(1, new EntityAIExecuteProgrammedPath(this, 1.5D));
		tasks.addTask(1, new EntityAIJumpToward(this, 0.4F));
		tasks.addTask(1, new EntityAIRobotAttackTarget(this, EntityLivingBase.class, 1.0D, false));

		tasks.addTask(2, new EntityAIFollowsOwnerEX(this, 1.5D, 6.0F, 1.25F));
		tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		tasks.addTask(3, wanderTask = new EntityAIWander(this, 1.0D));
		tasks.addTask(4, new EntityAILookIdle(this));
	}

	/**
	 * Called when a player interacts with a mob. e.g. gets milk from a cow, gets
	 * into the saddle on a pig.
	 */
	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		ItemStack itemstack = player.inventory.getCurrentItem();
		if (world.isRemote) {
			if (itemstack != null) {
				if ((itemstack.getItem() instanceof ItemRemote) && isEntityAlive()) {
					if (isOwner(player) || ((owner == null) && isTamable)) {
						RobotMod.proxy.openRobotProgrammingWindow(this);
					} else {
						if (owner != null) {
							player.sendMessage(new TextComponentString("Robot belongs to someone else"));
						} else {
							player.sendMessage(new TextComponentString("Robot is not compatible with remote"));
						}
					}
					return true;
				} else if ((itemstack.getItem() instanceof ItemWrench) && isEntityAlive()) {
					((ItemWrench) player.inventory.getCurrentItem().getItem()).setEntity(this);
				} else if ((itemstack.getItem() instanceof ItemMemoryWipe) && isEntityAlive()) {
					((ItemMemoryWipe) player.inventory.getCurrentItem().getItem()).setEntity(this);
				}
			}
		} else {
			if (itemstack != null) {
				if ((itemstack.getItem() instanceof ItemWrench) && isEntityAlive()) {
					((ItemWrench) player.inventory.getCurrentItem().getItem()).setEntity(this);
				} else if ((itemstack.getItem() instanceof ItemMemoryWipe) && isEntityAlive()) {
					((ItemMemoryWipe) player.inventory.getCurrentItem().getItem()).setEntity(this);
				}
			}
		}
		return super.processInteract(player, hand);
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
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(0.1D);
		getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
		getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(100.0D);
		getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.18D);
		getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
		getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
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
		// clear the robots current path too
		navigator.clearPathEntity();
		programPath.clear();
	}

	public boolean climb(int amount) {
		BlockPos dest = getPosition();
		if (!programPath.isEmpty()) {
			dest = programPath.get(programPath.size() - 1);
		}

		for (int i = 0; i < amount; i++) {
			if (world.getBlockState(dest).getBlock().isLadder(world.getBlockState(dest), world, dest, this)) {
				dest = dest.up();

				if (!world.getBlockState(dest).getBlock().isLadder(world.getBlockState(dest), world, dest, this)) {
					dest = dest.offset(getProgrammedDirection());
				}
				addToProgramPath(dest);
			} else {
				dest = dest.up().offset(getProgrammedDirection());
				Block block = world.getBlockState(dest).getBlock();
				Block blockdn = world.getBlockState(dest.down()).getBlock();
				if (blockdn.getMaterial(world.getBlockState(dest)).blocksMovement()
						&& block.isPassable(getEntityWorld(), dest)) {
					addToProgramPath(dest);
				} else {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	protected PathNavigate createNavigator(World worldIn) {
		return new PathNavigateRobot(this, worldIn);
	}

	/**
	 * Deals damage to the entity. If its a EntityPlayer then will take damage from
	 * the armor first and then health second with the reduced value. Args:
	 * damageAmount
	 */
	@Override
	protected void damageEntity(DamageSource damageSrc, float damageAmount) {
		if (!(damageSrc.getTrueSource() instanceof EntityPlayer)
				&& !((damageSrc == DamageSource.FALL) && (damageAmount > 25))) {
			super.damageEntity(damageSrc, damageAmount);
		}
	}

	public boolean descend(int amount) {
		BlockPos dest = getPosition();
		if (!programPath.isEmpty()) {
			dest = programPath.get(programPath.size() - 1);
		}

		for (int i = 0; i < amount; i++) {
			if (world.getBlockState(dest).getBlock().isLadder(world.getBlockState(dest), world, dest, this)) {
				dest = dest.down();
				addToProgramPath(dest);
			} else {
				dest = dest.down().offset(getProgrammedDirection());
				if (world.getBlockState(dest).getBlock().isLadder(world.getBlockState(dest), world, dest, this)) {
					addToProgramPath(dest);
				} else {
					Block block = world.getBlockState(dest).getBlock();
					Block blockdn = world.getBlockState(dest.down()).getBlock();
					if (blockdn.getMaterial(world.getBlockState(dest)).blocksMovement()
							&& block.isPassable(getEntityWorld(), dest)) {
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

	/**
	 * Returns the sound this mob makes while it's alive.
	 */
	@Override
	protected SoundEvent getAmbientSound() {
		return RobotMod.ROBOT_BEEP;
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
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_IRONGOLEM_DEATH;
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_BLAZE_HURT;
	}

	public boolean getIsFollowing() {
		return shouldFollow;
	}

	@Override
	protected float getJumpUpwardsMotion() {
		return 0.41F;
	}

	@Override
	public int getMaxFallHeight() {
		return 20;
	}

	public int getMemorySize() {
		return (int) (robot_inventory.getStackInSlot(1) != null
				? Math.pow(2, (4 + robot_inventory.getStackInSlot(1).getItemDamage()))
				: 8);
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
	public EntityPlayer getOwner() {
		return owner;
	}

	public EntityLivingBase getOwnerByID() {
		try {
			UUID uuid = getOwnerId();
			return uuid == null ? null : world.getPlayerEntityByUUID(uuid);
		} catch (IllegalArgumentException var2) {
			return null;
		}
	}

	@Override
	public UUID getOwnerId() {
		return (UUID) ((Optional) dataManager.get(EntityRobot.OWNER_UNIQUE_ID)).orNull();
	}

	public EnumFacing getProgrammedDirection() {
		return programDir;
	}

	public List<BlockPos> getProgramPath() {
		return programPath;
	}

	public String getRobotName() {
		return dataManager.get(EntityRobot.ROBOT_NAME);
	}

	public boolean getShouldJump() {
		return shouldJump;
	}

	/**
	 * Get number of ticks, at least during which the living entity will be silent.
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
				if (getOwnerId().equals(entityIn.getUniqueID())) {
					owner = entityIn;
				}
			}
		} catch (Exception e) {
			RobotMod.logger.info("No Owner Information Present");
			return false;
		}
		return (entityIn == getOwner()) || getOwnerId().equals(entityIn.getUniqueID());
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
		playSound(SoundEvents.ENTITY_CHICKEN_STEP, 0.15F, 1.0F);
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
				robot_inventory.setInventorySlotContents(slot, new ItemStack(itemtag));
			}
		}

		shouldFollow = nbttagcompound.getBoolean("follow");
		isTamable = nbttagcompound.getBoolean("tame");

		String robotName = nbttagcompound.getString("robotName");
		if (robotName.length() > 0) {
			setRobotName(robotName);
		}
		UUID ownerID = nbttagcompound.getUniqueId("OwnerUUID");
		if (ownerID != null) {
			setOwnerId(ownerID);
		}
	}

	/**
	 * Called by the client when it receives a Entity spawn packet. Data should be
	 * read out of the stream in the same way as it was written.
	 *
	 * @param data
	 *            The packet data stream
	 */
	@Override
	public void readSpawnData(ByteBuf additionalData) {
		isTamable = additionalData.readBoolean();
		shouldFollow = additionalData.readBoolean();
	}

	public void reinitIdleAI() {
		tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		tasks.addTask(4, new EntityAILookIdle(this));
	}

	public void removeIdleAI() {
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
		setOwnerId(player.getUniqueID());
	}

	public void setOwner(UUID playerId) {
		owner = world.getPlayerEntityByUUID(playerId);
		if(world.isRemote) {
			
		}
	}

	public void setOwnerId(UUID ownerUuid) {
		dataManager.set(EntityRobot.OWNER_UNIQUE_ID, Optional.fromNullable(ownerUuid));
		tasks.removeTask(wanderTask);
		setOwner(ownerUuid);
	}

	public void setRobotName(String robotName) {
		dataManager.set(EntityRobot.ROBOT_NAME, robotName);
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

		nbttagcompound.setString("robotName", dataManager.get(EntityRobot.ROBOT_NAME));
		nbttagcompound.setBoolean("follow", shouldFollow);
		nbttagcompound.setBoolean("tame", isTamable);

		if (getOwnerId() == null) {
			nbttagcompound.setString("OwnerUUID", "");
		} else {
			nbttagcompound.setUniqueId("OwnerUUID", getOwnerId());
		}
	}

	public float getDigSpeed(IBlockState state) {
		float f = this.robot_inventory.getStackInSlot(2).getStrVsBlock(state);

		if (f > 1.0F) {
			int i = EnchantmentHelper.getEfficiencyModifier(this);
			ItemStack itemstack = this.getHeldItemMainhand();

			if (i > 0 && !itemstack.isEmpty()) {
				f += (float) (i * i + 1);
			}
		}

		if (this.isPotionActive(MobEffects.HASTE)) {
			f *= 1.0F + (float) (this.getActivePotionEffect(MobEffects.HASTE).getAmplifier() + 1) * 0.2F;
		}

		if (this.isPotionActive(MobEffects.MINING_FATIGUE)) {
			float f1;

			switch (this.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
			case 0:
				f1 = 0.3F;
				break;
			case 1:
				f1 = 0.09F;
				break;
			case 2:
				f1 = 0.0027F;
				break;
			case 3:
			default:
				f1 = 8.1E-4F;
			}

			f *= f1;
		}

		if (!this.onGround) {
			f /= 5.0F;
		}

		return (f < 0 ? 0 : f);
	}

	/**
	 * Called by the server when constructing the spawn packet. Data should be added
	 * to the provided stream.
	 *
	 * @param buffer
	 *            The packet data stream
	 */
	@Override
	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeBoolean(isTamable);
		buffer.writeBoolean(shouldFollow);
	}

	public void makeSwingArm(boolean state) {
		shouldSwingArm = state;
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (!isSwingInProgress && shouldSwingArm) {
			swingArm(getActiveHand());
		}
		updateArmSwingProgress();
	}
}
