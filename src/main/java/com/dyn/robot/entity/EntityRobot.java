package com.dyn.robot.entity;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.dyn.robot.RobotMod;
import com.dyn.robot.api.RobotAPI;
import com.dyn.robot.entity.ai.EntityAIExecuteProgrammedPath;
import com.dyn.robot.entity.ai.EntityAIFollowsOwnerEX;
import com.dyn.robot.entity.ai.EntityAIJumpToward;
import com.dyn.robot.entity.ai.EntityAIRobotAttackTarget;
import com.dyn.robot.entity.inventory.RobotInventory;
import com.dyn.robot.entity.pathing.PathNavigateRobot;
import com.dyn.robot.items.ItemMemoryWipe;
import com.dyn.robot.items.ItemRemote;
import com.dyn.robot.items.ItemWrench;
import com.dyn.robot.python.RunPythonShell;
import com.dyn.robot.utils.FileUtils;
import com.google.common.base.Optional;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
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
import net.minecraft.init.Blocks;
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
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;

public abstract class EntityRobot extends EntityCreature implements IEntityOwnable, IEntityAdditionalSpawnData {
	protected static final DataParameter<Optional<UUID>> ROBOT_OWNER_UNIQUE_ID = EntityDataManager
			.<Optional<UUID>>createKey(EntityRobot.class, DataSerializers.OPTIONAL_UNIQUE_ID);

	protected static final DataParameter<String> ROBOT_NAME = EntityDataManager.<String>createKey(EntityRobot.class,
			DataSerializers.STRING);
	
	protected static final DataParameter<String> LAST_EXECUTED_SCRIPT = EntityDataManager.<String>createKey(EntityRobot.class,
			DataSerializers.STRING);

	public String getLastExecutedScript() {
		return dataManager.get(EntityRobot.LAST_EXECUTED_SCRIPT);
	}
	
	public void setLastExecutedScript(String script) {
		dataManager.set(EntityRobot.LAST_EXECUTED_SCRIPT, script);
	}

	private static final DataParameter<Boolean> ROBOT_FOLLOWING = EntityDataManager
			.<Boolean>createKey(EntityRobot.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> ROBOT_CODE_EXECUTING = EntityDataManager
			.<Boolean>createKey(EntityRobot.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Boolean> TAMABLE = EntityDataManager
			.<Boolean>createKey(EntityRobot.class, DataSerializers.BOOLEAN);
	
	public static List<EntityItem> getEntityItemsInRadius(World world, double x, double y, double z, int radius) {
		List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class,
				new AxisAlignedBB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius));
		return list;
	}

	protected EntityPlayer owner;

	public RobotInventory robot_inventory;
	private List<BlockPos> programPath = new ArrayList();

	private boolean shouldJump;
	public Map<Long, String> messages = new TreeMap<>();
	private boolean pauseCode = false;

	private EntityAIWander wanderTask;

	public int counter = 0;

	private EnumFacing programDir;

	protected boolean shouldSwingArm;

	public EntityRobot(World worldIn) {
		super(worldIn);
		height = .9f;
		width = 0.5f;
		robot_inventory = new RobotInventory("Robot Inventory", 32);
		dataManager.register(EntityRobot.ROBOT_OWNER_UNIQUE_ID, Optional.absent());
		dataManager.register(EntityRobot.ROBOT_NAME, "");
		dataManager.register(EntityRobot.LAST_EXECUTED_SCRIPT, "");
		dataManager.register(EntityRobot.ROBOT_FOLLOWING, Boolean.valueOf(false));
		dataManager.register(EntityRobot.ROBOT_CODE_EXECUTING, Boolean.valueOf(false));
		dataManager.register(EntityRobot.TAMABLE, Boolean.valueOf(true));

		tasks.addTask(1, new EntityAIExecuteProgrammedPath(this, 1.5D));
		tasks.addTask(1, new EntityAIJumpToward(this, 0.4F));
		tasks.addTask(1, new EntityAIRobotAttackTarget(this, EntityLivingBase.class, 1.0D, false));

		tasks.addTask(2, new EntityAIFollowsOwnerEX(this, 1.5D, 6.0F, 1.25F));
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
		navigator.clearPath();
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
				while (world.getBlockState(dest).getBlock().isLadder(world.getBlockState(dest), world, dest, this)) {
					addToProgramPath(dest);
					dest = dest.up();
				}
				dest = dest.offset(getProgrammedDirection());
				Block block = world.getBlockState(dest).getBlock();
				Block blockdn = world.getBlockState(dest.down()).getBlock();
				if (!blockdn.isPassable(world, dest) && block.isPassable(getEntityWorld(), dest)) {
					addToProgramPath(dest);
				}
			} else {
				dest = dest.up().offset(getProgrammedDirection());
				Block block = world.getBlockState(dest).getBlock();
				Block blockdn = world.getBlockState(dest.down()).getBlock();
				if (!blockdn.isPassable(world, dest) && block.isPassable(getEntityWorld(), dest)) {
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
		if ((damageSrc.getTrueSource() instanceof EntityPlayer)
				|| ((damageSrc == DamageSource.FALL) && (damageAmount < 25))) {
			return;
		}
		super.damageEntity(damageSrc, damageAmount);
	}

	public boolean descend(int amount) {
		BlockPos dest = getPosition();
		if (!programPath.isEmpty()) {
			dest = programPath.get(programPath.size() - 1);
		}

		for (int i = 0; i < amount; i++) {
			if (world.getBlockState(dest).getBlock().isLadder(world.getBlockState(dest), world, dest, this)) {
				while (world.getBlockState(dest).getBlock().isLadder(world.getBlockState(dest), world, dest, this)) {
					// this will get the position at the bottom of the ladder
					dest = dest.down();
					addToProgramPath(dest);
				}
			} else {
				dest = dest.down().offset(getProgrammedDirection());
				if (world.getBlockState(dest).getBlock().isLadder(world.getBlockState(dest), world, dest, this)) {
					while (world.getBlockState(dest).getBlock().isLadder(world.getBlockState(dest), world, dest,
							this)) {
						// this will get the position at the bottom of the ladder
						dest = dest.down();
						addToProgramPath(dest);
					}
				} else {
					Block block = world.getBlockState(dest).getBlock();
					Block blockdn = world.getBlockState(dest.down()).getBlock();
					if (!blockdn.isPassable(world, dest) && block.isPassable(getEntityWorld(), dest)) {
						addToProgramPath(dest);
					} else {
						return false;
					}
				}
			}
		}
		return true;
	}

	protected boolean detectRedstoneSignal() {
		int ret = 0;
		IBlockState iblockstate = world.getBlockState(getPosition());
		if (iblockstate.getBlock() == Blocks.REDSTONE_WIRE) {
			return iblockstate.getValue(BlockRedstoneWire.POWER).intValue() > 1;
		}
		for (EnumFacing facing : EnumFacing.values()) {
			BlockPos blockpos = getPosition().offset(facing);
			int i = world.getRedstonePower(blockpos, facing);

			if (i >= 15) {
				return true;
			} else {
				iblockstate = world.getBlockState(blockpos);
				ret = Math.max(ret,
						Math.max(i,
								iblockstate.getBlock() == Blocks.REDSTONE_WIRE
										? iblockstate.getValue(BlockRedstoneWire.POWER).intValue()
										: 0));
			}
		}
		return ret > 0;
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

	public float getDigSpeed(IBlockState state) {
		float f = robot_inventory.getStackInSlot(2).getDestroySpeed(state);

		if (f > 1.0F) {
			int i = EnchantmentHelper.getEfficiencyModifier(this);
			ItemStack itemstack = getHeldItemMainhand();

			if ((i > 0) && !itemstack.isEmpty()) {
				f += (i * i) + 1;
			}
		}

		if (isPotionActive(MobEffects.HASTE)) {
			f *= 1.0F + ((getActivePotionEffect(MobEffects.HASTE).getAmplifier() + 1) * 0.2F);
		}

		if (isPotionActive(MobEffects.MINING_FATIGUE)) {
			float f1;

			switch (getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
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

		if (!onGround) {
			f /= 5.0F;
		}

		return (f < 0 ? 0 : f);
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_BLAZE_HURT;
	}

	public boolean getIsFollowing() {
		return dataManager.get(EntityRobot.ROBOT_FOLLOWING);
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
		return (int) (robot_inventory.getStackInSlot(1) != ItemStack.EMPTY
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
		return (UUID) ((Optional) dataManager.get(EntityRobot.ROBOT_OWNER_UNIQUE_ID)).orNull();
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
	
	public boolean hasOwner() {
		return (getOwner() != null) || getOwnerId() != null;
	}

	public boolean isTamable() {
		return dataManager.get(EntityRobot.TAMABLE);
	}

	public void makeSwingArm(boolean state) {
		shouldSwingArm = state;
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

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (!isSwingInProgress && shouldSwingArm) {
			swingArm(getActiveHand());
		}
		updateArmSwingProgress();
		if ((FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) && (getOwner() != null)) {
			if ((robot_inventory.getStackInSlot(4) != ItemStack.EMPTY) && detectRedstoneSignal() && !shouldExecuteCode()
					&& robot_inventory.hasSDCard()) {
				clearProgramPath();
				startExecutingCode();

				File scriptFile = new File(RobotMod.scriptsLoc, getRobotName() + "/" + LocalDate.now() + "/"
						+ FileUtils.sanitizeFilename(LocalDateTime.now().toLocalTime() + ".py"));
				try {
					FileUtils.writeFile(scriptFile,
							robot_inventory.getStackInSlot(0).getTagCompound().getString("text"));
				} catch (IOException e) {
					RobotMod.logger.error(
							"Failed Logging Script File: " + FileUtils.sanitizeFilename(scriptFile.getName()), e);
				}

				RobotAPI.setRobotId(getEntityId(), getOwner());

				RobotMod.proxy.addScheduledTask(() -> RunPythonShell
						.run(Arrays.asList((robot_inventory.getStackInSlot(0).getTagCompound().getString("text"))
								.split(Pattern.quote("\n"))), getOwner(), getEntityId()));
			}
		}
	}

	public void pauseCodeExecution() {
		pauseCode = true;
	}

	@Override
	protected void playStepSound(BlockPos pos, Block blockIn) {
		playSound(SoundEvents.ENTITY_CHICKEN_STEP, 0.15F, 1.0F);
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
					if (isOwner(player)) {
						RobotMod.proxy.openRobotProgrammingWindow(this);
					} else {
						if (hasOwner()) {
							player.sendMessage(new TextComponentString("Robot belongs to someone else"));
						} else if (isTamable()) {
							RobotMod.proxy.openActivationInterface(this);
						} else {
							player.sendMessage(
									new TextComponentString("Robot is not compatible with remote frequency"));
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

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);

		NBTTagList nbttaglist = nbttagcompound.getTagList("Items", Constants.NBT.TAG_LIST);
		// robot_inventory = new ItemStack[32];
		for (int i = 0; i < nbttaglist.tagCount(); i++) {
			NBTTagCompound itemtag = nbttaglist.getCompoundTagAt(i);
			int slot = itemtag.getByte("Slot") & 0xFF;
			if ((slot >= 0) && (slot < 32)) {
				robot_inventory.setInventorySlotContents(slot, new ItemStack(itemtag));
			}
		}

		dataManager.set(EntityRobot.TAMABLE, nbttagcompound.getBoolean("tame"));

		String robotName = nbttagcompound.getString("robotName");
		dataManager.set(EntityRobot.LAST_EXECUTED_SCRIPT, nbttagcompound.getString("code"));
		if (robotName.length() > 0) {
			setRobotName(robotName);
		}
		if (nbttagcompound.hasUniqueId("OwnerUUID")) {
			UUID ownerID = nbttagcompound.getUniqueId("OwnerUUID");
			setOwnerId(ownerID);
		}

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

	public void setIsFollowing(boolean shouldFollow) {
		dataManager.set(EntityRobot.ROBOT_FOLLOWING, shouldFollow);
	}

	public void setOwner(EntityPlayer player) {
		setOwnerId(player.getUniqueID());
	}

	private void setOwner(UUID playerId) {
		owner = world.getPlayerEntityByUUID(playerId);
	}

	private void setOwnerId(@Nullable UUID ownerUuid) {
		dataManager.set(EntityRobot.ROBOT_OWNER_UNIQUE_ID, Optional.fromNullable(ownerUuid));
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
		dataManager.set(EntityRobot.TAMABLE, isTamable);
	}

	public boolean shouldExecuteCode() {
		return dataManager.get(EntityRobot.ROBOT_CODE_EXECUTING);
	}

	public void startExecutingCode() {
		dataManager.set(EntityRobot.ROBOT_CODE_EXECUTING, true);
		programDir = getHorizontalFacing();

		setPosition(getPosition().getX() + .5, getPosition().getY(), getPosition().getZ() + .5);
		rotate(programDir.getHorizontalAngle());
	}

	public void stopExecutingCode() {
		dataManager.set(EntityRobot.ROBOT_CODE_EXECUTING, false);
	}
	public NBTTagCompound getNBTforItemStack() {
		NBTTagCompound nbttagcompound = new NBTTagCompound();
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
		nbttagcompound.setString("code", dataManager.get(EntityRobot.LAST_EXECUTED_SCRIPT));
		return nbttagcompound;
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
		nbttagcompound.setString("code", dataManager.get(EntityRobot.LAST_EXECUTED_SCRIPT));
		nbttagcompound.setBoolean("tame", dataManager.get(EntityRobot.TAMABLE));

		if (getOwnerId() != null) {
			nbttagcompound.setUniqueId("OwnerUUID", getOwnerId());
		}
	}
}
