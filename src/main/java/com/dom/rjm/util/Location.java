package com.dom.rjm.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class Location extends BlockPos {
	static final int WORLD_SPACING = 2000;
	static final int WORLD_SPACING_HALF = Location.WORLD_SPACING / 2;

	static public double decodeAltitude(double y) {
		if (y > -Location.WORLD_SPACING_HALF) {
			return y;
		}
		double i = Math.floor((Location.WORLD_SPACING_HALF - y) / Location.WORLD_SPACING);
		return y + (Location.WORLD_SPACING * i);
	}

	// Altitudes for world number i are >-WORLD_SPACING_HALF-WORLD_SPACING*i and
	// <= WORLD_SPACING_HALF-WORLD_SPACING*i, with altitude 0 being at
	// -WORLD_SPACING*i.
	// For instance, world 0 (the overworld in the stock setup) is from
	// -WORLD_SPACING_HALF (not
	// inclusive) to WORLD_SPACING_HALF (inclusive), with world 1 right below
	// that, and so on.
	// This allows most old scripts to work fine in multiworld settings.

	public static Location decodeLocation(World[] serverWorlds, int x, int y, int z) {
		World w = Location.getWorldByEncodedAltitude(serverWorlds, y);
		BlockPos spawnPos = w.getSpawnPoint();
		return new Location(w, x + spawnPos.getX(), (int) Location.decodeAltitude(y) + spawnPos.getY(),
				z + spawnPos.getZ());
	}

	public static Vec3w decodeVec3w(World[] serverWorlds, double x, double y, double z) {
		World w = Location.getWorldByEncodedAltitude(serverWorlds, y);
		BlockPos spawnPos = w.getSpawnPoint();
		return new Vec3w(w, x + spawnPos.getX(), (int) Location.decodeAltitude(y) + spawnPos.getY(),
				z + spawnPos.getZ());
	}

	static public double encodeAltitude(int worldIndex, double y) {
		if (worldIndex == 0) {
			if (y > -Location.WORLD_SPACING_HALF) {
				return y;
			} else {
				return 1 - Location.WORLD_SPACING_HALF;
			}
		}
		if (y >= Location.WORLD_SPACING_HALF) {
			y = Location.WORLD_SPACING_HALF - 1;
		}
		return y - (Location.WORLD_SPACING * worldIndex);
	}

	public static Vec3d encodeVec3(World[] serverWorlds, World w, Vec3d pos) {
		for (int i = 0; i < serverWorlds.length; i++) {
			if (serverWorlds[i] == w) {
				BlockPos spawnPos = w.getSpawnPoint();
				return new Vec3d(pos.x - spawnPos.getX(), Location.encodeAltitude(i, pos.y - spawnPos.getY()),
						pos.z - spawnPos.getZ());
			}
		}
		return pos;
	}

	public static Vec3i encodeVec3i(World[] serverWorlds, World w, int x, int y, int z) {
		for (int i = 0; i < serverWorlds.length; i++) {
			if (serverWorlds[i] == w) {
				BlockPos spawnPos = w.getSpawnPoint();
				return new Vec3i(x - spawnPos.getX(), Location.encodeAltitude(i, y - spawnPos.getY()),
						z - spawnPos.getZ());
			}
		}
		return new Vec3i(x, y, z);
	}

	static public World getWorldByEncodedAltitude(World[] serverWorlds, double y) {
		int i = (int) Math.floor((Location.WORLD_SPACING_HALF - y) / Location.WORLD_SPACING);
		if (i < 0) {
			i = 0;
		}
		if (i >= serverWorlds.length) {
			i = 0;
		}
		return serverWorlds[i];
	}

	private World world;

	public Location(World world, BlockPos pos) {
		super(pos);
		setWorld(world);
	}

	public Location(World world, double x, double y, double z) {
		super(x, y, z);
		setWorld(world);
	}

	Location(World world, int x, int y, int z) {
		super(x, y, z);
		setWorld(world);
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}
}
