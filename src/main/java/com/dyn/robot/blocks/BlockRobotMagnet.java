package com.dyn.robot.blocks;

import java.util.ArrayList;
import java.util.List;

import com.dyn.robot.RobotMod;
import com.dyn.robot.entity.EntityRobot;
import com.dyn.robot.reference.Reference;
import com.dyn.robot.utils.HelperFunctions;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockRobotMagnet extends BlockHorizontal {

	public BlockRobotMagnet() {
		super(Material.IRON);
		setUnlocalizedName("robot_magnet");
		setRegistryName(Reference.MOD_ID, "robot_magnet");
		setDefaultState(blockState.getBaseState().withProperty(BlockHorizontal.FACING, EnumFacing.NORTH));
		setCreativeTab(RobotMod.roboTab);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { BlockHorizontal.FACING });
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	public Item getItemBlock() {
		return new ItemBlock(this).setRegistryName(getRegistryName());
	}

	/**
	 * Convert the BlockStateContainer into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(BlockHorizontal.FACING).getHorizontalIndex();
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(BlockHorizontal.FACING, placer.getHorizontalFacing());
	}

	/**
	 * Convert the given metadata into a BlockStateContainer for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.getHorizontal(meta));
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	// used by the renderer to control lighting and visibility of other blocks.
	// set to true because this block is opaque and occupies the entire 1x1x1
	// space
	// not strictly required because the default (super method) is true
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	// this is called twice, once from
	// net.minecraft.client.multiplayer.PlayerControllerMP.onPlayerRightClick(PlayerControllerMP.java:416)
	// and again from
	// net.minecraft.server.management.ItemInWorldManager.activateBlockOrUseItem(ItemInWorldManager.java:459)
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		List<EntityRobot> list = worldIn.getEntitiesWithinAABB(EntityRobot.class, new AxisAlignedBB(pos.getX() - 10,
				pos.getY() - 10, pos.getZ() - 10, pos.getX() + 10, pos.getY() + 10, pos.getZ() + 10));
		
		List<EntityRobot> ownedRobots = new ArrayList();
		
		for (EntityRobot robot : list) {
			if (robot.isOwner(playerIn)) {
				ownedRobots.add(robot);
			}
		}
		
		if(ownedRobots.size() == 1) {
			ownedRobots.get(0).setPosition(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
			ownedRobots.get(0).rotate(HelperFunctions.getAngleFromFacing(state.getValue(BlockHorizontal.FACING)));
		} else if (worldIn.isRemote && ownedRobots.size() > 1) {
			RobotMod.proxy.openMagnetGui(pos, state, ownedRobots);
		}
		
		return true;
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If
	 * inapplicable, returns the passed blockstate.
	 */
	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(BlockHorizontal.FACING, rot.rotate(state.getValue(BlockHorizontal.FACING)));
	}
}
