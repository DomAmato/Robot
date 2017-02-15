package com.dyn.robot.blocks;

import java.util.Random;

import com.dyn.DYNServerMod;
import com.dyn.robot.RobotMod;
import com.dyn.robot.items.ItemRemote;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDynRobot extends BlockFalling {

	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	private String robotName;

	public BlockDynRobot() {
		super(Material.iron);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addDestroyEffects(World world, BlockPos pos, EffectRenderer effectRenderer) {
		return false;
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, new IProperty[] { FACING });
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.CUTOUT;
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex();
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.getFront(meta);

		if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
			enumfacing = EnumFacing.NORTH;
		}

		return getDefaultState().withProperty(FACING, enumfacing);
	}

	@Override
	public boolean isFullCube() {
		return false;
	}

	// used by the renderer to control lighting and visibility of other blocks.
	// set to true because this block is opaque and occupies the entire 1x1x1
	// space
	// not strictly required because the default (super method) is true
	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        super.updateTick(worldIn, pos, state, rand);
        
    }
	
	// this is called twice, once from
	// net.minecraft.client.multiplayer.PlayerControllerMP.onPlayerRightClick(PlayerControllerMP.java:416)
	// and again from
	// net.minecraft.server.management.ItemInWorldManager.activateBlockOrUseItem(ItemInWorldManager.java:459)
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumFacing side, float hitX, float hitY, float hitZ) {
		// without this it fires twice
		if (worldIn.isRemote) {
			if ((playerIn.getCurrentEquippedItem() != null)
					&& (playerIn.getCurrentEquippedItem().getItem() instanceof ItemRemote)) {
				RobotMod.proxy.openRemoteInterface(worldIn, this, pos);
			}
			return true;
		}
		return true;
	}

	/**
     * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
     * IBlockstate
     */
	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
			int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
	}
	
	 /**
     * Called by ItemBlocks after a block is set in the world, to allow post-place logic
     */
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
    	this.robotName = stack.getDisplayName().trim();
    }

	public String getRobotName() {
		return robotName;
	}

	public void setRobotName(String robotName) {
		this.robotName = robotName;
	}

	public boolean hasName() {
		return robotName != null && !robotName.equals("Robot");
	}
}
