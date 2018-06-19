package com.dyn.robot.blocks;

import java.util.Random;

import com.dyn.robot.RobotMod;
import com.dyn.robot.reference.Reference;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockRobotJammer extends Block implements ITileEntityProvider {

	public static final PropertyBool POWERED = PropertyBool.create("powered");

	public BlockRobotJammer() {
		super(Material.IRON);
		setRegistryName(Reference.MOD_ID, "robot_jammer");
		setUnlocalizedName("robot_jammer");
		setDefaultState(blockState.getBaseState().withProperty(BlockRobotJammer.POWERED, Boolean.valueOf(false)));
		setCreativeTab(RobotMod.roboTab);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { BlockRobotJammer.POWERED });
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new RobotJammerTileEntity();
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
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state) {
		int i = 0;
		if (state.getValue(BlockRobotJammer.POWERED).booleanValue()) {
			i |= 8;
		}

		return i;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(BlockRobotJammer.POWERED, Boolean.valueOf((meta & 8) > 0));
	}

	@Override
	public boolean isFullCube(IBlockState iBlockState) {
		return false;
	}

	// used by the renderer to control lighting and visibility of other blocks.
	// set to true because this block is opaque and occupies the entire 1x1x1
	// space
	// not strictly required because the default (super method) is true
	@Override
	public boolean isOpaqueCube(IBlockState iBlockState) {
		return false;
	}

	/**
	 * Called when a neighboring block was changed and marks that this state should
	 * perform any checks during a neighbor change. Cases may include when redstone
	 * power is updated, cactus blocks popping off due to a neighboring solid block,
	 * etc.
	 */
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (!worldIn.isRemote) {
			if (state.getValue(BlockRobotJammer.POWERED).booleanValue() && !worldIn.isBlockPowered(pos)) {
				worldIn.setBlockState(pos, state.withProperty(BlockRobotJammer.POWERED, Boolean.valueOf(false)));
			} else if (!state.getValue(BlockRobotJammer.POWERED).booleanValue() && worldIn.isBlockPowered(pos)) {
				worldIn.setBlockState(pos, state.withProperty(BlockRobotJammer.POWERED, Boolean.valueOf(true)));
			}
		}
	}

	// this is called twice, once from
	// net.minecraft.client.multiplayer.PlayerControllerMP.onPlayerRightClick(PlayerControllerMP.java:416)
	// and again from
	// net.minecraft.server.management.ItemInWorldManager.activateBlockOrUseItem(ItemInWorldManager.java:459)
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		// without this it fires twice
		if (worldIn.isRemote) {
			// open the gui here
		}
		return true;
	}

	/**
	 * Called by ItemBlocks after a block is set in the world, to allow post-place
	 * logic
	 */
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer,
			ItemStack stack) {
		if (!worldIn.isRemote) {
			if (state.getValue(BlockRobotJammer.POWERED).booleanValue() && !worldIn.isBlockPowered(pos)) {
				worldIn.setBlockState(pos, state.withProperty(BlockRobotJammer.POWERED, Boolean.valueOf(false)));
			} else if (!state.getValue(BlockRobotJammer.POWERED).booleanValue() && worldIn.isBlockPowered(pos)) {
				worldIn.setBlockState(pos, state.withProperty(BlockRobotJammer.POWERED, Boolean.valueOf(true)));
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World worldIn, BlockPos pos, Random rand) {
		if (state.getValue(BlockRobotJammer.POWERED)) {
			double d0 = pos.getX() + (rand.nextFloat());
			double d1 = pos.getY() + 0.6F + (rand.nextFloat() * 0.3D);
			double d2 = pos.getZ() + (rand.nextFloat());
			worldIn.spawnParticle(EnumParticleTypes.CRIT_MAGIC, d0, d1, d2, 0.0D, 0.0D, 0.0D, new int[0]);
		}
	}
}
