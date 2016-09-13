package com.dyn.robot.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.dyn.robot.RobotMod;
import com.dyn.robot.api.DynApi;
import com.dyn.robot.api.IDYNRobotAccess;
import com.dyn.robot.entity.brain.DynRobotBrain;
import com.dyn.robot.entity.brain.RobotBrain;
import com.dyn.robot.api.DynRobotAPI;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.turtle.apis.TurtleAPI;
import dan200.computercraft.shared.turtle.core.TurtleBrain;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class DynRobotEntity extends EntityCreature {

	public static List getEntityItemsInRadius(World world, double x, double y, double z, int radius) {
		List list = world.getEntitiesWithinAABB(EntityItem.class,
				AxisAlignedBB.fromBounds(x, y, z, x + radius, y + radius, z + radius));
		return list;
	}

	public boolean tamable, tamed;
	public String tamer;
	public ItemStack[] m_inventory = new ItemStack[36];
	  private RobotBrain m_brain;


	public List<BlockPos> markedChests = new ArrayList();

	public DynRobotEntity(World worldIn) {
		super(worldIn);
		this.height = 1;
		this.width = 0.8f;
		this.m_brain = createBrain();

		((PathNavigateGround) getNavigator()).setAvoidsWater(true);

		this.tasks.addTask(1, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(3, new EntityAILookIdle(this));

		// this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		// this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this,
		// EntityCow.class, 0, true));
	}

	public ItemStack addItemStack(ItemStack is) {
		if ((is == null) || (is.stackSize <= 0)) {
			return null;
		}

		for (int a = 0; a < m_inventory.length; a++) {
			if ((m_inventory[a] == null) || (m_inventory[a].stackSize <= 0)) {
				m_inventory[a] = is;
				return null;
			}
			ItemStack is2 = m_inventory[a];
			if ((is2.getItem() == is.getItem()) && (is2.getItemDamage() == is.getItemDamage())) {
				int amount = Math.min(is.stackSize, is2.getMaxStackSize() - is2.stackSize);
				is.stackSize -= amount;
				is2.stackSize += amount;
				m_inventory[a] = is2;
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

	public boolean decreaseItemStack(ItemStack is) {
		if ((is == null) || (is.stackSize <= 0)) {
			return true;
		}
		for (int a = 0; a < m_inventory.length; a++) {
			ItemStack is2 = m_inventory[a];
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
				m_inventory[a] = is2;
			}
			if (is.stackSize <= 0) {
				return true;
			}
		}
		return is.stackSize > 0;
	}

	public boolean doesInventoryHas(Class<? extends Item> c) {
		for (int a = 0; a < m_inventory.length; a++) {
			if ((m_inventory[a] != null) && c.isInstance(m_inventory[a].getItem())) {
				return true;
			}
		}
		return false;
	}

	public boolean doesInventoryHas(ItemStack is) {
		for (int a = 0; a < m_inventory.length; a++) {
			if (m_inventory[a] != null) {
				ItemStack is2 = m_inventory[a];
				if ((is2.getItem() == is.getItem())
						&& ((is2.getItemDamage() == is.getItemDamage()) || is.isItemStackDamageable())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected void dropFewItems(boolean recentlyAttacked, int lootModify) {
		super.dropFewItems(recentlyAttacked, lootModify);
		dropItem(new ItemStack(RobotMod.dynRobot, 1));
	}

	public void dropItem(ItemStack is) {
		entityDropItem(is, 0.6F);
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

	public boolean hasNeededItem() {
		return false;
	}

	@Override
	public boolean interact(EntityPlayer p) {
		return true;
		// if (p.getHeldItem() != null && p.getHeldItem().getItem() ==
		// CubeBots.itemCubeCall && lifeSpan >= 0) {
		// lifeSpan += 20000;
		// spawnParticles("flame");
		// if (!p.capabilities.isCreativeMode) {
		// p.getHeldItem().stackSize--;
		// if (p.getHeldItem().stackSize <= 0)
		// p.inventory.setInventorySlotContents(p.inventory.currentItem,
		// (ItemStack) null);
		// }
		// return true;
		// }
		// return true;
	}

	@Override
	public boolean isAIDisabled() {
		return false;
	}

	public boolean isInventoryEmpty() {
		for (int a = 0; a < m_inventory.length; a++) {
			if (m_inventory[a] != null) {
				return false;
			}
		}

		return true;
	}

	public boolean isInventoryFull() {
		for (int a = 0; a < m_inventory.length; a++) {
			if ((m_inventory[a] != null) && (m_inventory[a].stackSize == 0)) {
				m_inventory[a] = null;
			}
			if ((m_inventory[a] == null) || (m_inventory[a].stackSize == 0)) {
				return false;
			}
		}

		return true;
	}

	@Override
	protected boolean isMovementBlocked() {
		return super.isMovementBlocked();
	}

	@Override
	protected void jump() {
		super.jump();
		motionY += 0.07;
	}

	@Override
	public void onDeath(DamageSource d) {
		super.onDeath(d);
		if (!worldObj.isRemote) {
			for (int a = 0; a < m_inventory.length; a++) {
				if (m_inventory[a] != null) {
					worldObj.spawnEntityInWorld(new EntityItem(worldObj, posX, posY + 0.3, posZ, m_inventory[a]));
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
		this.m_brain.update();
		if ((isJumping || (isAirBorne && (motionY >= 0.2))) && ((Math.abs(motionX) + Math.abs(motionZ)) <= 0.2)) {
			Vec3 vec = getLookVec();
			motionX = vec.xCoord / 4;
			motionZ = vec.zCoord / 4;
		}
		if (this.ticksExisted % 5 == 0) {
			spawnAntennaParticles(EnumParticleTypes.REDSTONE);
		}

	}

	public void setMoveTo(double x, double y, double z, float ms) {
		getNavigator().tryMoveToXYZ((int) x, (int) y, (int) z, ms);
	}

	public void setMoveTo(Entity e, float ms) {
		setMoveTo(e.posX, e.posY, e.posZ, ms);
	}

	public void setMoveTo(BlockPos pos, float ms) {
		setMoveTo(pos.getX(), pos.getY(), pos.getZ(), ms);
	}

	public boolean shouldStoreItems(int a) {
		return true;
	}

	public void slightMoveWhenStill() {
		if ((Math.abs(motionX) + Math.abs(motionZ)) <= 0.4) {
			Vec3 vec = getLookVec();
			motionX = vec.xCoord / 10;
			motionZ = vec.zCoord / 10;
		}
	}

	public void spawnParticles(EnumParticleTypes particles) {
		for (int var3 = 0; var3 < 7; ++var3) {
			double var4 = rand.nextGaussian() * 0.02D;
			double var6 = rand.nextGaussian() * 0.02D;
			double var8 = rand.nextGaussian() * 0.02D;
			worldObj.spawnParticle(particles, (posX + rand.nextFloat() * width * 2.0F) - width,
					posY + 0.5D + rand.nextFloat() * height, (posZ + rand.nextFloat() * width * 2.0F) - width, var4,
					var6, var8);
		}
	}

	public void spawnAntennaParticles(EnumParticleTypes particles) {
		double xOffset = rand.nextGaussian() * 0.05D;
		double yOffset = rand.nextGaussian() * 0.05D;
		double zOffset = rand.nextGaussian() * 0.05D;
		worldObj.spawnParticle(particles, posX + xOffset, posY + 1.2 + yOffset, posZ + zOffset, 0, 0, 0); // int[0]);
	}

	// part needed for computer craft programming of robot
	protected RobotBrain createBrain() {
		return new DynRobotBrain(this);
	}

	protected final ServerComputer createComputer(int instanceID, int id, int termWidth, int termHeight) {
		ServerComputer computer = new ServerComputer(this.worldObj, id, "robot", instanceID, ComputerFamily.Advanced,
				termWidth, termHeight);

		computer.setPosition(getPosition());
		computer.addAPI(new TurtleAPI(computer.getAPIEnvironment(), getAccess()));
		computer.addAPI(new DynApi(computer.getAPIEnvironment(), (IDYNRobotAccess) getAccess()));
		computer.addAPI(new DynRobotAPI(computer.getAPIEnvironment(), getAccess()));
		this.m_brain.setupComputer(computer);
		return computer;
	}

	protected ServerComputer createComputer(int instanceID, int id) {
		return createComputer(instanceID, id, 39, 13);
	}

	public ITurtleAccess getAccess()
	  {
	    return this.m_brain;
	  }
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	  {
	    super.readFromNBT(nbttagcompound);
	    
	    NBTTagList nbttaglist = nbttagcompound.getTagList("Items", 10);
	    this.m_inventory = new ItemStack[32];
	    for (int i = 0; i < nbttaglist.tagCount(); i++)
	    {
	      NBTTagCompound itemtag = nbttaglist.getCompoundTagAt(i);
	      int slot = itemtag.getByte("Slot") & 0xFF;
	      if ((slot >= 0) && (slot < 32))
	      {
	        this.m_inventory[slot] = ItemStack.loadItemStackFromNBT(itemtag);
	      }
	    }
	    this.m_brain.readFromNBT(nbttagcompound);
	  }
	  
	@Override
	  public void writeToNBT(NBTTagCompound nbttagcompound)
	  {
	    super.writeToNBT(nbttagcompound);
	    
	    NBTTagList nbttaglist = new NBTTagList();
	    for (int i = 0; i < 16; i++) {
	      if (this.m_inventory[i] != null)
	      {
	        NBTTagCompound itemtag = new NBTTagCompound();
	        itemtag.setByte("Slot", (byte)i);
	        this.m_inventory[i].writeToNBT(itemtag);
	        nbttaglist.appendTag(itemtag);
	      }
	    }
	    nbttagcompound.setTag("Items", nbttaglist);
	    
	    this.m_brain.writeToNBT(nbttagcompound);
	  }
	  
	  public EnumFacing getDirection()
	  {
	    return this.m_brain.getDirection();
	  }
	  
	  public void setDirection(EnumFacing dir)
	  {
	    this.m_brain.setDirection(dir);
	  }
	  
	protected double getInteractRange(EntityPlayer player) {
		return 32.0D;
	}

	public boolean onDefaultComputerInteract(EntityPlayer player) {
		if (!worldObj.isRemote) {
			// TODO: open gui here
		}
		return true;
	}

}
