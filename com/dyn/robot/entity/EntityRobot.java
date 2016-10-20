package com.dyn.robot.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.dyn.robot.entity.ai.EntityAIFollowsOwnerEX;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public abstract class EntityRobot extends EntityCreature {

	public static List getEntityItemsInRadius(World world, double x, double y, double z, int radius) {
		List list = world.getEntitiesWithinAABB(EntityItem.class,
				AxisAlignedBB.fromBounds(x, y, z, x + radius, y + radius, z + radius));
		return list;
	}

	protected boolean m_on;
	protected EntityLivingBase owner;
	public RobotInventory m_inventory;
	public EntityAIFollowsOwnerEX followTask = null;
	private String robotName = "";

	public List<BlockPos> markedChests = new ArrayList();

	public EntityRobot(World worldIn) {
		super(worldIn);
		height = 1;
		width = 0.8f;
		m_on = false;
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

	public EntityLivingBase getOwner() {
		return owner;
	}

	public boolean hasNeededItem() {
		return false;
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

	@Override
	public void onDeath(DamageSource d) {
		super.onDeath(d);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
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

		m_on = nbttagcompound.getBoolean("on");
		robotName = nbttagcompound.getString("robotName");
		String s = "";

        if (nbttagcompound.hasKey("OwnerUUID", 8))
        {
            s = nbttagcompound.getString("OwnerUUID");
        }
        else
        {
            String s1 = nbttagcompound.getString("Owner");
            s = PreYggdrasilConverter.getStringUUIDFromName(s1);
        }

        if (s.length() > 0)
        {
            this.setOwnerId(s);
        }
	}

	public boolean setMoveTo(BlockPos pos, float ms) {
		return setMoveTo(pos.getX(), pos.getY(), pos.getZ(), ms);
	}

	public boolean setMoveTo(double x, double y, double z, float ms) {
		return getNavigator().tryMoveToXYZ((int) x, (int) y, (int) z, ms);
	}

	public boolean setMoveTo(Entity e, float ms) {
		return setMoveTo(e.posX, e.posY, e.posZ, ms);
	}

	public void setOwner(Entity owner) {
		if (owner instanceof EntityPlayer) {
			this.owner = (EntityLiving) owner;
			setOwnerId(owner.getUniqueID().toString());
			tasks.addTask(1, followTask = new EntityAIFollowsOwnerEX(this, (EntityPlayer) owner, 1.5D, 6.0F, 2.0F));
		}
	}

	public boolean shouldStoreItems(int a) {
		return true;
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
		nbttagcompound.setString("robotName", robotName);
		nbttagcompound.setBoolean("on", m_on);
		
		 if (this.getOwnerId() == null)
	        {
			 nbttagcompound.setString("OwnerUUID", "");
	        }
	        else
	        {
	        	nbttagcompound.setString("OwnerUUID", this.getOwnerId());
	        }
	}
	
	public EntityLivingBase getOwnerByID()
    {
        try
        {
            UUID uuid = UUID.fromString(this.getOwnerId());
            return uuid == null ? null : this.worldObj.getPlayerEntityByUUID(uuid);
        }
        catch (IllegalArgumentException var2)
        {
            return null;
        }
    }
	
	public String getOwnerId()
    {
        return this.dataWatcher.getWatchableObjectString(17);
    }
	
	 public void setOwnerId(String ownerUuid)
	    {
	        this.dataWatcher.updateObject(17, ownerUuid);
	        UUID uuid = UUID.fromString(ownerUuid);
	        owner = worldObj.getPlayerEntityByUUID(uuid);
	    }
	 
	 public String getRobotName()
	    {
	        return this.robotName;
	    }
		
		 public void setRobotName(String robotName)
		    {
		        this.robotName = robotName;
		    }
	 public boolean isOwner(EntityLivingBase entityIn)
	    {
	        return entityIn == this.getOwner();
	    }
}
