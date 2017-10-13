package com.dyn.robot.entity;

import com.dyn.robot.RobotMod;
import com.dyn.robot.items.ItemMemoryWipe;
import com.dyn.robot.items.ItemRemote;
import com.dyn.robot.items.ItemWrench;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class SimpleRobotEntity extends EntityRobot {

	public SimpleRobotEntity(World worldIn) {
		this(worldIn, null);
	}

	public SimpleRobotEntity(World worldIn, EntityPlayer player) {
		super(worldIn);

		if (player != null) {
			setOwnerId(player.getUniqueID());
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
	protected void dropFewItems(boolean recentlyAttacked, int lootModify) {
		super.dropFewItems(recentlyAttacked, lootModify);
		dropItem(new ItemStack(RobotMod.robot_block, 1));
	}

	public void dropItem(ItemStack is) {
		entityDropItem(is, 0.6F);
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
	public void onLivingUpdate() {
		super.onLivingUpdate();
		updateArmSwingProgress();
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
}
