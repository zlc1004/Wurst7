/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.wurstclient.WurstClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages world seeds for SeedXRay functionality.
 * Handles seed storage, retrieval, and conversion.
 */
public class SeedManager
{
	private static final SeedManager INSTANCE = new SeedManager();
	private final Map<String, Long> seeds = new HashMap<>();
	
	private SeedManager()
	{}
	
	public static SeedManager getInstance()
	{
		return INSTANCE;
	}
	
	/**
	 * Gets the seed for the current world.
	 * Returns null if no seed is available.
	 */
	public Long getCurrentSeed()
	{
		MinecraftClient mc = WurstClient.MC;
		
		// Try to get seed from integrated server first
		if(mc.isIntegratedServerRunning() && mc.getServer() != null)
			return mc.getServer().getOverworld().getSeed();
		
		// Try to get manually set seed for multiplayer
		String worldName = getWorldName();
		if(worldName != null)
			return seeds.get(worldName);
		
		return null;
	}
	
	/**
	 * Sets the seed for the current world (multiplayer use).
	 */
	public void setSeed(String seedInput)
	{
		MinecraftClient mc = WurstClient.MC;
		if(mc.isIntegratedServerRunning())
		{
			ChatUtils.warning(
				"Cannot set seed in singleplayer - seed is automatically detected.");
			return;
		}
		
		String worldName = getWorldName();
		if(worldName == null)
		{
			ChatUtils.error("Cannot determine world name.");
			return;
		}
		
		long numericSeed = convertToSeed(seedInput);
		seeds.put(worldName, numericSeed);
		ChatUtils.message("Seed set to: " + numericSeed);
	}
	
	/**
	 * Removes the stored seed for the current world.
	 */
	public void removeSeed()
	{
		String worldName = getWorldName();
		if(worldName != null && seeds.remove(worldName) != null)
			ChatUtils.message("Seed removed for current world.");
		else
			ChatUtils.error("No seed found for current world.");
	}
	
	/**
	 * Gets a readable world name for the current server/world.
	 */
	private String getWorldName()
	{
		MinecraftClient mc = WurstClient.MC;
		if(mc.getCurrentServerEntry() != null)
			return mc.getCurrentServerEntry().address;
		
		if(mc.world != null)
			return "singleplayer_"
				+ mc.world.getRegistryKey().getValue().toString();
		
		return null;
	}
	
	/**
	 * Converts string input to a numeric seed.
	 * Follows Minecraft's seed conversion rules.
	 */
	private long convertToSeed(String input)
	{
		try
		{
			return Long.parseLong(input.trim());
		}catch(NumberFormatException e)
		{
			return input.trim().hashCode();
		}
	}
	
	/**
	 * Saves seeds to NBT data.
	 */
	public NbtCompound toNbt()
	{
		NbtCompound nbt = new NbtCompound();
		for(Map.Entry<String, Long> entry : seeds.entrySet())
			nbt.putLong(entry.getKey(), entry.getValue());
		return nbt;
	}
	
	/**
	 * Loads seeds from NBT data.
	 */
	public void fromNbt(NbtCompound nbt)
	{
		seeds.clear();
		for(String key : nbt.getKeys())
		{
			if(nbt.contains(key))
			{
				try
				{
					// Handle Optional<Long> return type
					java.util.Optional<Long> optionalSeed = nbt.getLong(key);
					if(optionalSeed.isPresent())
						seeds.put(key, optionalSeed.get());
				}catch(Exception e)
				{
					// Skip invalid entries
				}
			}
		}
	}
}