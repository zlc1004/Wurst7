/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.util;

import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.wurstclient.WurstClient;

/**
 * Simulates ore generation using Minecraft's world generation algorithms.
 * Based on the OreSim implementation from meteor-rejects.
 */
public class OreSimulator
{
	/**
	 * Simulates ore generation for a chunk using the world seed.
	 */
	public static Set<Vec3d> simulateOresInChunk(long worldSeed,
		ChunkPos chunkPos, List<OreData> enabledOres, boolean airCheck)
	{
		Set<Vec3d> orePositions = new HashSet<>();
		
		int chunkX = chunkPos.x << 4;
		int chunkZ = chunkPos.z << 4;
		
		ChunkRandom random = new ChunkRandom(Random.create());
		long populationSeed =
			random.setPopulationSeed(worldSeed, chunkX, chunkZ);
		
		for(OreData ore : enabledOres)
		{
			if(!ore.enabled.isChecked())
				continue;
			
			random.setDecoratorSeed(populationSeed, ore.index,
				ore.generationStep);
			
			int repeat = ore.count.get(random);
			
			for(int i = 0; i < repeat; i++)
			{
				if(ore.rarity != 1.0f
					&& random.nextFloat() >= 1.0f / ore.rarity)
					continue;
				
				int x = random.nextInt(16) + chunkX;
				int z = random.nextInt(16) + chunkZ;
				int y = ore.heightRange.get(random);
				
				BlockPos origin = new BlockPos(x, y, z);
				
				Set<Vec3d> veinOres;
				if(ore.scattered)
					veinOres = generateScatteredOres(random, origin, ore.size,
						airCheck);
				else
					veinOres = generateNormalVein(random, origin, ore.size,
						ore.discardOnAir, airCheck);
				
				orePositions.addAll(veinOres);
			}
		}
		
		return orePositions;
	}
	
	/**
	 * Generates a normal ore vein using Minecraft's algorithm.
	 */
	private static Set<Vec3d> generateNormalVein(ChunkRandom random,
		BlockPos origin, int veinSize, float discardOnAir, boolean airCheck)
	{
		Set<Vec3d> ores = new HashSet<>();
		
		float angle = random.nextFloat() * (float)Math.PI;
		float g = veinSize / 8.0f;
		int i = MathHelper.ceil((veinSize / 16.0f * 2.0f + 1.0f) / 2.0f);
		
		double startX = origin.getX() + Math.sin(angle) * g;
		double endX = origin.getX() - Math.sin(angle) * g;
		double startZ = origin.getZ() + Math.cos(angle) * g;
		double endZ = origin.getZ() - Math.cos(angle) * g;
		double startY = origin.getY() + random.nextInt(3) - 2;
		double endY = origin.getY() + random.nextInt(3) - 2;
		
		int minX = origin.getX() - MathHelper.ceil(g) - i;
		int minY = origin.getY() - 2 - i;
		int minZ = origin.getZ() - MathHelper.ceil(g) - i;
		int sizeX = 2 * (MathHelper.ceil(g) + i);
		int sizeY = 2 * (2 + i);
		int sizeZ = 2 * (MathHelper.ceil(g) + i);
		
		// Check if vein would be above ground
		ClientWorld world = WurstClient.MC.world;
		if(world != null)
		{
			for(int x = minX; x <= minX + sizeX; x++)
			{
				for(int z = minZ; z <= minZ + sizeZ; z++)
				{
					if(minY <= world.getTopY(Heightmap.Type.MOTION_BLOCKING, x,
						z))
					{
						return generateVeinPart(random, veinSize, startX, endX,
							startZ, endZ, startY, endY, minX, minY, minZ, sizeX,
							sizeY, sizeZ, discardOnAir, airCheck);
					}
				}
			}
		}
		
		return ores;
	}
	
	/**
	 * Generates the individual blocks of an ore vein.
	 */
	private static Set<Vec3d> generateVeinPart(ChunkRandom random, int veinSize,
		double startX, double endX, double startZ, double endZ, double startY,
		double endY, int minX, int minY, int minZ, int sizeX, int sizeY,
		int sizeZ, float discardOnAir, boolean airCheck)
	{
		Set<Vec3d> ores = new HashSet<>();
		BitSet bitSet = new BitSet(sizeX * sizeY * sizeZ);
		double[] nodeData = new double[veinSize * 4];
		
		// Generate vein nodes
		for(int node = 0; node < veinSize; node++)
		{
			float progress = (float)node / (float)veinSize;
			double x = MathHelper.lerp(progress, startX, endX);
			double y = MathHelper.lerp(progress, startY, endY);
			double z = MathHelper.lerp(progress, startZ, endZ);
			double radius = random.nextDouble() * veinSize / 16.0;
			double ellipsoidRadius =
				(Math.sin(Math.PI * progress) + 1.0) * radius + 1.0;
			ellipsoidRadius /= 2.0;
			
			nodeData[node * 4] = x;
			nodeData[node * 4 + 1] = y;
			nodeData[node * 4 + 2] = z;
			nodeData[node * 4 + 3] = ellipsoidRadius;
		}
		
		// Eliminate overlapping nodes
		for(int i = 0; i < veinSize - 1; i++)
		{
			if(nodeData[i * 4 + 3] <= 0.0)
				continue;
			
			for(int j = i + 1; j < veinSize; j++)
			{
				if(nodeData[j * 4 + 3] <= 0.0)
					continue;
				
				double dx = nodeData[i * 4] - nodeData[j * 4];
				double dy = nodeData[i * 4 + 1] - nodeData[j * 4 + 1];
				double dz = nodeData[i * 4 + 2] - nodeData[j * 4 + 2];
				double dr = nodeData[i * 4 + 3] - nodeData[j * 4 + 3];
				
				if(dr * dr > dx * dx + dy * dy + dz * dz)
				{
					if(dr > 0.0)
						nodeData[j * 4 + 3] = -1.0;
					else
						nodeData[i * 4 + 3] = -1.0;
				}
			}
		}
		
		// Generate blocks for each node
		for(int node = 0; node < veinSize; node++)
		{
			double radius = nodeData[node * 4 + 3];
			if(radius < 0.0)
				continue;
			
			double centerX = nodeData[node * 4];
			double centerY = nodeData[node * 4 + 1];
			double centerZ = nodeData[node * 4 + 2];
			
			int nodeMinX = Math.max(MathHelper.floor(centerX - radius), minX);
			int nodeMinY = Math.max(MathHelper.floor(centerY - radius), minY);
			int nodeMinZ = Math.max(MathHelper.floor(centerZ - radius), minZ);
			int nodeMaxX =
				Math.max(MathHelper.floor(centerX + radius), nodeMinX);
			int nodeMaxY =
				Math.max(MathHelper.floor(centerY + radius), nodeMinY);
			int nodeMaxZ =
				Math.max(MathHelper.floor(centerZ + radius), nodeMinZ);
			
			for(int x = nodeMinX; x <= nodeMaxX; x++)
			{
				double dx = (x + 0.5 - centerX) / radius;
				if(dx * dx >= 1.0)
					continue;
				
				for(int y = nodeMinY; y <= nodeMaxY; y++)
				{
					double dy = (y + 0.5 - centerY) / radius;
					if(dx * dx + dy * dy >= 1.0)
						continue;
					
					for(int z = nodeMinZ; z <= nodeMaxZ; z++)
					{
						double dz = (z + 0.5 - centerZ) / radius;
						if(dx * dx + dy * dy + dz * dz >= 1.0)
							continue;
						
						int index = (x - minX) + (y - minY) * sizeX
							+ (z - minZ) * sizeX * sizeY;
						if(bitSet.get(index))
							continue;
						
						bitSet.set(index);
						
						if(y >= -64 && y < 320)
						{
							if(shouldPlaceOre(new BlockPos(x, y, z),
								discardOnAir, random, airCheck))
								ores.add(new Vec3d(x, y, z));
						}
					}
				}
			}
		}
		
		return ores;
	}
	
	/**
	 * Generates scattered ores (like emerald).
	 */
	private static Set<Vec3d> generateScatteredOres(ChunkRandom random,
		BlockPos origin, int maxSize, boolean airCheck)
	{
		Set<Vec3d> ores = new HashSet<>();
		int count = random.nextInt(maxSize + 1);
		
		for(int i = 0; i < count; i++)
		{
			int size = Math.min(i, 7);
			int x = randomCoord(random, size) + origin.getX();
			int y = randomCoord(random, size) + origin.getY();
			int z = randomCoord(random, size) + origin.getZ();
			
			if(shouldPlaceOre(new BlockPos(x, y, z), 1.0f, random, airCheck))
				ores.add(new Vec3d(x, y, z));
		}
		
		return ores;
	}
	
	/**
	 * Determines if an ore should be placed at the given position.
	 */
	private static boolean shouldPlaceOre(BlockPos pos, float discardOnAir,
		ChunkRandom random, boolean airCheck)
	{
		if(!airCheck)
			return true;
		
		ClientWorld world = WurstClient.MC.world;
		if(world == null)
			return true;
		
		// Check if block is solid (for air check)
		BlockState state = world.getBlockState(pos);
		if(!state.isOpaque())
			return false;
		
		// Apply discard on air chance
		if(discardOnAir == 0.0f
			|| (discardOnAir != 1.0f && random.nextFloat() >= discardOnAir))
			return true;
		
		// Check if adjacent to air
		for(Direction direction : Direction.values())
		{
			BlockPos adjacent = pos.offset(direction);
			if(!world.getBlockState(adjacent).isOpaque()
				&& discardOnAir != 1.0f)
				return false;
		}
		
		return true;
	}
	
	/**
	 * Generates a random coordinate offset for scattered ores.
	 */
	private static int randomCoord(ChunkRandom random, int size)
	{
		return Math.round((random.nextFloat() - random.nextFloat()) * size);
	}
}