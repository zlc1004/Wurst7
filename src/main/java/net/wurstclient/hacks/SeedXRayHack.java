/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;

import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.WurstRenderLayers;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.EasyVertexBuffer;
import net.wurstclient.util.OreData;
import net.wurstclient.util.OreSimulator;
import net.wurstclient.util.RegionPos;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.SeedManager;
import net.wurstclient.WurstClient;

@SearchTags({"SeedXRay", "seed xray", "ore sim", "OreSim", "seed finder"})
public final class SeedXRayHack extends Hack
	implements UpdateListener, RenderListener
{
	public enum AirCheckMode
	{
		OFF("Off", "No air checking - shows all predicted ores"),
		ON_LOAD("On Load", "Checks for air when chunk loads"),
		RECHECK("Recheck", "Continuously rechecks for air exposure");
		
		private final String name;
		private final String description;
		
		private AirCheckMode(String name, String description)
		{
			this.name = name;
			this.description = description;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
	
	private final SliderSetting range = new SliderSetting("Range",
		"How many chunks around the player to simulate ores for.\n"
			+ "Higher values may cause lag.",
		5, 1, 10, 1, ValueDisplay.INTEGER);
	
	private final EnumSetting<AirCheckMode> airCheck =
		new EnumSetting<>("Air check",
			"Whether to check if ores are exposed to air.\n"
				+ "Helps against anti-xray plugins.",
			AirCheckMode.values(), AirCheckMode.RECHECK);
	
	private final CheckboxSetting onlyExposed = new CheckboxSetting(
		"Only exposed", "Only shows ores that are exposed to air.\n"
			+ "Useful for cave mining.",
		false);
	
	private final Map<ChunkPos, Map<OreData, Set<Vec3d>>> chunkCache =
		new ConcurrentHashMap<>();
	private final List<OreData> oreTypes = new ArrayList<>();
	
	private EasyVertexBuffer vertexBuffer;
	private RegionPos bufferRegion;
	
	public SeedXRayHack()
	{
		super("SeedXRay");
		setCategory(Category.RENDER);
		
		addSetting(range);
		addSetting(airCheck);
		addSetting(onlyExposed);
		
		// Add ore type settings
		for(OreData ore : OreData.DEFAULT_ORES)
		{
			oreTypes.add(ore);
			addSetting(ore.enabled);
		}
	}
	
	@Override
	public String getRenderName()
	{
		Long seed = SeedManager.getInstance().getCurrentSeed();
		if(seed == null)
			return getName() + " [No Seed]";
		return getName() + " [" + String.valueOf(seed).substring(0,
			Math.min(8, String.valueOf(seed).length())) + "...]";
	}
	
	@Override
	protected void onEnable()
	{
		Long currentSeed = SeedManager.getInstance().getCurrentSeed();
		if(currentSeed == null)
		{
			ChatUtils.error(
				"No seed available! Use '.seed <seed>' command to set a seed for multiplayer worlds.");
			setEnabled(false);
			return;
		}
		
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(RenderListener.class, this);
		
		// Clear cache when enabling
		chunkCache.clear();
		
		ChatUtils.message("SeedXRay enabled with seed: " + currentSeed);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(RenderListener.class, this);
		
		chunkCache.clear();
		
		if(vertexBuffer != null)
		{
			vertexBuffer.close();
			vertexBuffer = null;
		}
		bufferRegion = null;
	}
	
	@Override
	public void onUpdate()
	{
		if(WurstClient.MC.player == null || WurstClient.MC.world == null)
			return;
		
		Long worldSeed = SeedManager.getInstance().getCurrentSeed();
		if(worldSeed == null)
		{
			ChatUtils.error("Lost seed connection! Disabling SeedXRay.");
			setEnabled(false);
			return;
		}
		
		// Get current chunk position
		ChunkPos playerChunk = WurstClient.MC.player.getChunkPos();
		int rangeValue = range.getValueI();
		
		// Simulate ores for chunks in range
		for(int dx = -rangeValue; dx <= rangeValue; dx++)
		{
			for(int dz = -rangeValue; dz <= rangeValue; dz++)
			{
				ChunkPos chunkPos =
					new ChunkPos(playerChunk.x + dx, playerChunk.z + dz);
				
				// Skip if already cached
				if(chunkCache.containsKey(chunkPos))
					continue;
				
				// Simulate ores for this chunk
				simulateChunkOres(worldSeed, chunkPos);
			}
		}
		
		// Clean up distant chunks
		chunkCache.entrySet().removeIf(entry -> {
			ChunkPos pos = entry.getKey();
			return Math.abs(pos.x - playerChunk.x) > rangeValue + 2
				|| Math.abs(pos.z - playerChunk.z) > rangeValue + 2;
		});
	}
	
	private void simulateChunkOres(long worldSeed, ChunkPos chunkPos)
	{
		// Get enabled ore types
		List<OreData> enabledOres = new ArrayList<>();
		for(OreData ore : oreTypes)
			if(ore.enabled.isChecked())
				enabledOres.add(ore);
			
		if(enabledOres.isEmpty())
			return;
		
		try
		{
			// Simulate each ore type individually to maintain proper tracking
			Map<OreData, Set<Vec3d>> chunkOres = new ConcurrentHashMap<>();
			
			for(OreData ore : enabledOres)
			{
				List<OreData> singleOre = new ArrayList<>();
				singleOre.add(ore);
				
				Set<Vec3d> orePositions =
					OreSimulator.simulateOresInChunk(worldSeed, chunkPos,
						singleOre, airCheck.getSelected() != AirCheckMode.OFF);
				
				if(!orePositions.isEmpty())
					chunkOres.put(ore, orePositions);
			}
			
			chunkCache.put(chunkPos, chunkOres);
		}catch(Exception e)
		{
			// Ignore errors in ore simulation
		}
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		if(WurstClient.MC.player == null)
			return;
		
		// Update vertex buffer if needed
		updateVertexBuffer();
		
		if(vertexBuffer == null || bufferRegion == null)
			return;
		
		matrixStack.push();
		RenderUtils.applyRegionalRenderOffset(matrixStack, bufferRegion);
		
		// Use white color since each vertex already has its own color
		vertexBuffer.draw(matrixStack, WurstRenderLayers.ESP_LINES,
			new float[]{1.0F, 1.0F, 1.0F}, 0.8F);
		
		matrixStack.pop();
	}
	
	private void updateVertexBuffer()
	{
		RegionPos region = RenderUtils.getCameraRegion();
		if(bufferRegion != null && bufferRegion.equals(region))
			return;
		
		if(vertexBuffer != null)
			vertexBuffer.close();
		
		vertexBuffer = EasyVertexBuffer.createAndUpload(DrawMode.DEBUG_LINES,
			VertexFormats.POSITION_COLOR, buffer -> {
				// Collect ore positions with their types for proper coloring
				for(Map.Entry<ChunkPos, Map<OreData, Set<Vec3d>>> chunkEntry : chunkCache
					.entrySet())
				{
					Map<OreData, Set<Vec3d>> chunkOres = chunkEntry.getValue();
					
					for(Map.Entry<OreData, Set<Vec3d>> oreEntry : chunkOres
						.entrySet())
					{
						OreData ore = oreEntry.getKey();
						if(!ore.enabled.isChecked())
							continue;
						
						Set<Vec3d> positions = oreEntry.getValue();
						int color = ore.color | 0xFF000000; // Ensure alpha is
															// set
						
						for(Vec3d pos : positions)
						{
							if(onlyExposed.isChecked()
								&& !isExposed(BlockPos.ofFloored(pos)))
								continue;
							
							float x = (float)(pos.x - region.x());
							float y = (float)pos.y;
							float z = (float)(pos.z - region.z());
							
							// Draw cube outline (12 lines forming a wireframe
							// cube)
							// Bottom face
							buffer.vertex(x, y, z).color(color);
							buffer.vertex(x + 1, y, z).color(color);
							buffer.vertex(x + 1, y, z).color(color);
							buffer.vertex(x + 1, y, z + 1).color(color);
							buffer.vertex(x + 1, y, z + 1).color(color);
							buffer.vertex(x, y, z + 1).color(color);
							buffer.vertex(x, y, z + 1).color(color);
							buffer.vertex(x, y, z).color(color);
							
							// Top face
							buffer.vertex(x, y + 1, z).color(color);
							buffer.vertex(x + 1, y + 1, z).color(color);
							buffer.vertex(x + 1, y + 1, z).color(color);
							buffer.vertex(x + 1, y + 1, z + 1).color(color);
							buffer.vertex(x + 1, y + 1, z + 1).color(color);
							buffer.vertex(x, y + 1, z + 1).color(color);
							buffer.vertex(x, y + 1, z + 1).color(color);
							buffer.vertex(x, y + 1, z).color(color);
							
							// Vertical edges
							buffer.vertex(x, y, z).color(color);
							buffer.vertex(x, y + 1, z).color(color);
							buffer.vertex(x + 1, y, z).color(color);
							buffer.vertex(x + 1, y + 1, z).color(color);
							buffer.vertex(x + 1, y, z + 1).color(color);
							buffer.vertex(x + 1, y + 1, z + 1).color(color);
							buffer.vertex(x, y, z + 1).color(color);
							buffer.vertex(x, y + 1, z + 1).color(color);
						}
					}
				}
			});
		
		bufferRegion = region;
	}
	
	private boolean isExposed(BlockPos pos)
	{
		if(WurstClient.MC.world == null)
			return true;
		
		for(net.minecraft.util.math.Direction direction : net.minecraft.util.math.Direction
			.values())
		{
			BlockPos adjacent = pos.offset(direction);
			if(!WurstClient.MC.world.getBlockState(adjacent).isOpaque())
				return true;
		}
		return false;
	}
}
