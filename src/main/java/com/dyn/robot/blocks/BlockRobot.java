package com.dyn.robot.blocks;

import com.dyn.robot.RobotMod;
import com.dyn.robot.items.ItemRemote;
import com.dyn.robot.reference.Reference;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockRobot extends BlockFalling {

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	private String robotName;

	public BlockRobot() {
		super(Material.IRON);
		setDefaultState(blockState.getBaseState().withProperty(BlockRobot.FACING, EnumFacing.SOUTH));
		setRegistryName(Reference.MOD_ID, "robot_block");
		setUnlocalizedName("robot_block");
		setCreativeTab(RobotMod.roboTab);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { BlockRobot.FACING });
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
		return state.getValue(BlockRobot.FACING).getHorizontalIndex();
	}

	public String getRobotName() {
		return robotName;
	}

	/**
	 * Called by ItemBlocks just before a block is actually set in the world, to
	 * allow for adjustments to the IBlockstate
	 */
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(BlockRobot.FACING, placer.getHorizontalFacing().getOpposite());
	}

	/**
	 * Convert the given metadata into a BlockStateContainer for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(BlockRobot.FACING, EnumFacing.getHorizontal(meta));
	}

	public boolean hasName() {
		return (robotName != null) && !robotName.equals("Robot");
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

	// this is called twice, once from
	// net.minecraft.client.multiplayer.PlayerControllerMP.onPlayerRightClick(PlayerControllerMP.java:416)
	// and again from
	// net.minecraft.server.management.ItemInWorldManager.activateBlockOrUseItem(ItemInWorldManager.java:459)
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		// without this it fires twice
		if (worldIn.isRemote) {
			if ((playerIn.inventory.getCurrentItem() != null)
					&& (playerIn.inventory.getCurrentItem().getItem() instanceof ItemRemote)) {
				RobotMod.proxy.openActivationInterface(worldIn, this, pos);
			}
			return true;
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
		robotName = stack.getDisplayName().trim();
	}

	public void setRobotName(String robotName) {
		this.robotName = robotName;
	}
}
