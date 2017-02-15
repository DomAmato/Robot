package com.dyn.robot.entity;

import com.dyn.robot.RobotMod;
import com.dyn.robot.items.ItemRemote;
import com.dyn.robot.items.ItemWrench;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class DynRobotEntity extends EntityRobot {

	public DynRobotEntity(World worldIn) {
		this(worldIn, null);
	}

	public DynRobotEntity(World worldIn, EntityPlayer player) {
		super(worldIn);

		if (player != null) {
			setOwnerId(player.getUniqueID().toString());
		}

		((PathNavigateGround) getNavigator()).setAvoidsWater(true);
		((PathNavigateGround) getNavigator()).setEnterDoors(true);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(0.1D);
		getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(16.0D);
		getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(100.0D);
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.2D);
		getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(1.0D);
	}

	@Override
	protected void dropFewItems(boolean recentlyAttacked, int lootModify) {
		super.dropFewItems(recentlyAttacked, lootModify);
		dropItem(new ItemStack(RobotMod.dynRobot, 1));
	}

	public void dropItem(ItemStack is) {
		entityDropItem(is, 0.6F);
	}

	/**
	 * Called when a player interacts with a mob. e.g. gets milk from a cow,
	 * gets into the saddle on a pig.
	 */
	@Override
	public boolean interact(EntityPlayer player) {
		ItemStack itemstack = player.inventory.getCurrentItem();
		if (worldObj.isRemote) {
			if (itemstack != null) {
				if ((itemstack.getItem() instanceof ItemRemote) && isEntityAlive()) {
					if (isOwner(player) || ((owner == null) && isTamable)) {
						RobotMod.proxy.openRobotProgrammingWindow(this);//.openRemoteInterface(this);
					} else {
						if (owner != null) {
							player.addChatComponentMessage(new ChatComponentText("Robot belongs to someone else"));
						} else {
							player.addChatComponentMessage(
									new ChatComponentText("Robot is not compatible with remote"));
						}
					}
					return true;
				} else if ((itemstack.getItem() instanceof ItemWrench) && isEntityAlive()) {
					((ItemWrench) player.inventory.getCurrentItem().getItem()).setEntity(this);
				}
			}
		} else {
			if ((itemstack.getItem() instanceof ItemWrench) && isEntityAlive()) {
				((ItemWrench) player.inventory.getCurrentItem().getItem()).setEntity(this);
			}
		}
		return super.interact(player);
	}

	@Override
	public void onDeath(DamageSource d) {
		super.onDeath(d);
		if (!worldObj.isRemote) {
			for (int a = 0; a < m_inventory.getSizeInventory(); a++) {
				if (m_inventory.getStackInSlot(a) != null) {
					worldObj.spawnEntityInWorld(
							new EntityItem(worldObj, posX, posY + 0.3, posZ, m_inventory.getStackInSlot(a)));
				}
			}
		} else {
			for (int a = 0; a < (rand.nextInt(10) + 10); a++) {
				worldObj.spawnParticle(EnumParticleTypes.FLAME, posX + (width / 2), posY + (height / 2),
						posZ + (width / 2), (rand.nextDouble() - 0.5) / 8, rand.nextDouble() * 0.2,
						(rand.nextDouble() - 0.5) / 8);
			}
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		Vec3 vec = getLookVec();
		if ((isJumping || (isAirBorne && (motionY >= 0.2))) && ((Math.abs(motionX) + Math.abs(motionZ)) <= 0.2)) {
			motionX = vec.xCoord / 4;
			motionZ = vec.zCoord / 4;
		}

		if (((ticksExisted % 5) == 0)) {
			spawnAntennaParticles(EnumParticleTypes.REDSTONE);
		}
	}

	public void slightMoveWhenStill() {
		if ((Math.abs(motionX) + Math.abs(motionZ)) <= 0.4) {
			Vec3 vec = getLookVec();
			motionX = vec.xCoord / 10;
			motionZ = vec.zCoord / 10;
		}
	}

	public void spawnAntennaParticles(EnumParticleTypes particles) {
		double xOffset = rand.nextGaussian() * 0.05D;
		double yOffset = rand.nextGaussian() * 0.05D;
		double zOffset = rand.nextGaussian() * 0.05D;

		worldObj.spawnParticle(particles, posX + xOffset, posY + 1.2 + yOffset, posZ + zOffset, 0, 0, 0);
	}

	public void spawnParticles(EnumParticleTypes particles) {
		for (int var3 = 0; var3 < 30; ++var3) {
			worldObj.spawnParticle(particles, (posX + (rand.nextFloat() * width * 2.0F)) - width,
					posY + 0.5D + (rand.nextFloat() * height), (posZ + (rand.nextFloat() * width * 2.0F)) - width, 0, 0,
					0);
		}
	}
}
