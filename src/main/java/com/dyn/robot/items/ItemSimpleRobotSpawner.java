package com.dyn.robot.items;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.SimpleRobotEntity;
import com.dyn.robot.reference.Reference;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemSimpleRobotSpawner extends Item {

	public ItemSimpleRobotSpawner() {
		setHasSubtypes(true);
		setUnlocalizedName("robot_spawn");
		setRegistryName(Reference.MOD_ID, "robot_spawn");
		setCreativeTab(RobotMod.roboTab);
	}

	/**
	 * returns a list of items with the same ID, but different meta (eg: dye returns
	 * 16 items)
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (isInCreativeTab(tab)) {
			items.add(new ItemStack(this, 1, 0));
			items.add(new ItemStack(this, 1, 1));
		}
	}

	/**
	 * Checks isDamagable and if it cannot be stacked
	 */
	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
		}
		SimpleRobotEntity robot = (SimpleRobotEntity) EntityList
				.createEntityByIDFromName(new ResourceLocation(Reference.MOD_ID, "robot"), worldIn);
		if (robot == null) {
			return EnumActionResult.FAIL;
		}
		robot.setLocationAndAngles(pos.getX() + 0.5, pos.getY() + 1.3, pos.getZ() + 0.5,
				MathHelper.wrapDegrees(worldIn.rand.nextFloat() * 360.0F), 0.0F);
		robot.rotationYawHead = robot.rotationYaw;
		robot.renderYawOffset = robot.rotationYaw;
		robot.onInitialSpawn(worldIn.getDifficultyForLocation(new BlockPos(robot)), (IEntityLivingData) null);
		worldIn.spawnEntity(robot);
		robot.playLivingSound();
		if (player.getHeldItemMainhand().getMetadata() == 0) {
			robot.setTamable(true);
		} else {
			robot.setTamable(false);
		}
		return EnumActionResult.SUCCESS;

	}
}
