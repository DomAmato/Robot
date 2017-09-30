package com.dom.robot.items;

import com.dom.robot.RobotMod;
import com.dom.robot.entity.SimpleRobotEntity;
import com.dom.robot.reference.Reference;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemSimpleRobotSpawner extends Item {

	public ItemSimpleRobotSpawner() {
		setHasSubtypes(true);
		setUnlocalizedName("robot_spawn");
		setRegistryName(Reference.MOD_ID, getUnlocalizedName());
		setCreativeTab(RobotMod.roboTab);
	}

	/**
	 * returns a list of items with the same ID, but different meta (eg: dye returns
	 * 16 items)
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		items.add(new ItemStack(this, 1, 0));
		items.add(new ItemStack(this, 1, 1));
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
		SimpleRobotEntity robot = (SimpleRobotEntity) ItemMonsterPlacer.spawnCreature(worldIn,
				new ResourceLocation(Reference.MOD_ID, "RobotEntity"), pos.getX() + 0.5, pos.getY() + 1.3,
				pos.getZ() + 0.5);
		if (player.getHeldItemMainhand().getMetadata() == 0) {
			robot.setTamable(true);
		}
		if (robot != null) {
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.FAIL;
	}
}
