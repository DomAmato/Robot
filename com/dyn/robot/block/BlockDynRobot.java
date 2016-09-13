package com.dyn.robot.block;

import dan200.computercraft.shared.computer.blocks.TileComputerBase;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.turtle.blocks.BlockTurtle;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDynRobot extends BlockTurtle {
	

	public static class Properties
	  {
	    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	  }
	
	public BlockDynRobot() {
	    setDefaultState(this.blockState.getBaseState().withProperty(Properties.FACING, EnumFacing.NORTH));
	}

	@Override
	protected TileComputerBase createTile(ComputerFamily family) {
		return new TileDynRobot();
	}

	@Override
	public ComputerFamily getFamily(IBlockState state) {
		return ComputerFamily.Normal;
	}

	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.SOLID;
	}

	@Override
	public int getRenderType() {
		return 3;
	}
	
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	  {
	    state = state.withProperty(Properties.FACING, getDirection(world, pos));
	    return state;
	  }
}
