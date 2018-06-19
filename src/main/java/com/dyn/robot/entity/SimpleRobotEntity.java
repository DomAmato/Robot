package com.dyn.robot.entity;

import com.dyn.robot.RobotMod;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SimpleRobotEntity extends EntityRobot {

	public final int on1 = 50;
	public final int on2 = 75;
	public int counter = 0;

	public SimpleRobotEntity(World worldIn) {
		this(worldIn, null);
	}

	public SimpleRobotEntity(World worldIn, EntityPlayer player) {
		super(worldIn);

		if (player != null) {
			setOwner(player);
		}

		((PathNavigateGround) getNavigator()).setCanSwim(true);
		((PathNavigateGround) getNavigator()).setEnterDoors(true);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(0.1D);
		getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16.0D);
		getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(100.0D);
		getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
		getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
	}

	@Override
	public boolean canBreatheUnderwater() {
		if (hasSuit() && (getSuit().getMetadata() == 2)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean climb(int amount) {
		if (!super.climb(amount)) {
			pauseCodeExecution();
			BlockPos dest = getPosition();
			if (!programPath.isEmpty()) {
				dest = programPath.get(programPath.size() - 1);
			}

			for (int i = 0; i < amount; i++) {
				dest = dest.up().offset(getProgrammedDirection());
				Block block = world.getBlockState(dest).getBlock();
				Block blockdn = world.getBlockState(dest.down()).getBlock();
				if (!blockdn.isPassable(world, dest) && ((block == Blocks.LAVA) || (block == Blocks.FLOWING_LAVA))
						&& hasSuit() && (getSuit().getMetadata() == 1)) {
					addToProgramPath(dest);
				} else {
					resumeExecution();
					return false;
				}
			}
			resumeExecution();
		}
		return true;
	}

	@Override
	public boolean descend(int amount) {
		if (!super.descend(amount)) {
			pauseCodeExecution();
			BlockPos dest = getPosition();
			if (!programPath.isEmpty()) {
				dest = programPath.get(programPath.size() - 1);
			}

			for (int i = 0; i < amount; i++) {
				dest = dest.down().offset(getProgrammedDirection());
				Block block = world.getBlockState(dest).getBlock();
				Block blockdn = world.getBlockState(dest.down()).getBlock();
				if (!blockdn.isPassable(world, dest) && ((block == Blocks.LAVA) || (block == Blocks.FLOWING_LAVA))
						&& hasSuit() && (getSuit().getMetadata() == 1)) {
					addToProgramPath(dest);
				} else {
					resumeExecution();
					return false;
				}
			}
			resumeExecution();
		}
		return true;
	}

	@Override
	protected void dropFewItems(boolean recentlyAttacked, int lootModify) {
		super.dropFewItems(recentlyAttacked, lootModify);
		ItemStack robotStack = new ItemStack(RobotMod.robot_block, 1);
		robotStack.setStackDisplayName(getRobotName());
		robotStack.setTagCompound(getNBTforItemStack());
		dropItem(robotStack);
	}

	public void dropItem(ItemStack is) {
		entityDropItem(is, 0.6F);
	}

	@Override
	public boolean getCanSpawnHere() {
		// dont spawn robots
		return false;
	}

	public ItemStack getSuit() {
		if (robot_inventory.getSuit() != ItemStack.EMPTY) {
			return robot_inventory.getSuit();
		}
		return getItemStackFromSlot(EntityEquipmentSlot.CHEST);
	}

	public boolean hasSuit() {
		return robot_inventory.hasSuit() | !(getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty());
	}

	@Override
	public boolean isBurning() {
		if (hasSuit() && (getSuit().getMetadata() == 1)) {
			return false;
		}
		return super.isBurning();
	}

	@Override
	public boolean isEntityInvulnerable(DamageSource source) {
		if (source.getTrueSource() instanceof EntityPlayer) {
			return true;
		}

		if (hasSuit()) {
			switch (getSuit().getMetadata()) {
			case 0:
				if (source.isExplosion() || source.isMagicDamage() || source.isProjectile()
						|| (source.getTrueSource() != null) || (source == DamageSource.GENERIC)
						|| (source == DamageSource.CACTUS)) {
					return true;
				}
			case 1:
				if (source.isFireDamage()) {
					extinguish();
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean isInLava() {
		//we have to do this otherwise movement is reduced greatly
		if (!hasSuit() || (getSuit().getMetadata() != 1)) {
			return super.isInLava();
		}
		return false;
	}

	@Override
	public boolean isInWater() {
		//we have to do this otherwise movement is reduced greatly
		if (!hasSuit() || (getSuit().getMetadata() != 2)) {
			return super.isInWater();
		}
		return false;
	}

	@Override
	public boolean isPushedByWater() {
		return false;
	}

	@Override
	public void onDeath(DamageSource d) {
		super.onDeath(d);
		if (!world.isRemote) {
			for (int a = 0; a < robot_inventory.getSizeInventory(); a++) {
				if (robot_inventory.getStackInSlot(a) != null) {
					world.spawnEntity(new EntityItem(world, posX, posY + 0.3, posZ, robot_inventory.getStackInSlot(a)));
				}
			}
		} else {
			for (int a = 0; a < (rand.nextInt(10) + 10); a++) {
				world.spawnParticle(EnumParticleTypes.FLAME, posX + (width / 2), posY + (height / 2),
						posZ + (width / 2), (rand.nextDouble() - 0.5) / 8, rand.nextDouble() * 0.2,
						(rand.nextDouble() - 0.5) / 8);
			}
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		Vec3d vec = getLookVec();
		if ((isJumping || (isAirBorne && (motionY >= 0.2))) && ((Math.abs(motionX) + Math.abs(motionZ)) <= 0.2)) {
			motionX = vec.x / 4;
			motionZ = vec.z / 4;
		}

		if (((ticksExisted % 5) == 0)) {
			spawnAntennaParticles(EnumParticleTypes.REDSTONE);
		}
	}

	@Override
	public void readSpawnData(ByteBuf additionalData) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setOnFireFromLava() {
		if (!hasSuit() || (getSuit().getMetadata() != 1)) {
			super.setOnFireFromLava();
		}
	}

	public void slightMoveWhenStill() {
		if ((Math.abs(motionX) + Math.abs(motionZ)) <= 0.4) {
			Vec3d vec = getLookVec();
			motionX = vec.x / 10;
			motionZ = vec.z / 10;
		}
	}

	public void spawnAntennaParticles(EnumParticleTypes particles) {
		double xOffset = rand.nextGaussian() * 0.05D;
		double yOffset = rand.nextGaussian() * 0.05D;
		double zOffset = rand.nextGaussian() * 0.05D;

		world.spawnParticle(particles, posX + xOffset, posY + 1.2 + yOffset, posZ + zOffset, 0, 0, 0);
	}

	public void spawnParticles(EnumParticleTypes particles) {
		for (int var3 = 0; var3 < 30; ++var3) {
			world.spawnParticle(particles, (posX + (rand.nextFloat() * width * 2.0F)) - width,
					posY + 0.5D + (rand.nextFloat() * height), (posZ + (rand.nextFloat() * width * 2.0F)) - width, 0, 0,
					0);
		}
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		// TODO Auto-generated method stub

	}
}
