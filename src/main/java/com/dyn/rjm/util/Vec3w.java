package com.dyn.rjm.util;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Vec3w extends Vec3d {
	private World world;

	public Vec3w(World w, double x, double y, double z) {
		super(x, y, z);
		setWorld(w);
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}
}
