package com.dom.robot.blocks;

import java.util.List;

import com.dom.robot.RobotMod;
import com.dom.robot.entity.EntityRobot;
import com.dom.robot.reference.Reference;
import com.dom.robot.utils.HelperFunctions;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockRobotMagnet extends Block {

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

	public BlockRobotMagnet() {
		super(Material.IRON);
		setDefaultState(blockState.getBaseState().withProperty(BlockRobotMagnet.FACING, EnumFacing.NORTH));
		setUnlocalizedName("robot_magnet");
		setRegistryName(Reference.MOD_ID, "robot_magnet");
		setCreativeTab(RobotMod.roboTab);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { BlockRobotMagnet.FACING });
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	/**
	 * Convert the BlockStateContainer into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(BlockRobotMagnet.FACING).getIndex();
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(BlockRobotMagnet.FACING, placer.getHorizontalFacing());
	}

	/**
	 * Convert the given metadata into a BlockStateContainer for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.getFront(meta);

		if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
			enumfacing = EnumFacing.NORTH;
		}

		return getDefaultState().withProperty(BlockRobotMagnet.FACING, enumfacing);
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
		for (EntityRobot robot : list) {
			if (robot.getOwner() == playerIn) {
				robot.setPosition(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
				robot.rotate(HelperFunctions.getAngleFromFacing(state.getValue(BlockRobotMagnet.FACING)));
			}
		}
		return true;
	}
}
