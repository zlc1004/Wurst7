/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.WurstClient;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.TextFieldSetting;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.chunk.ChunkUtils;
import net.wurstclient.util.json.JsonUtils;

@SearchTags({"block logger", "block finder", "block tracker", "block esp"})
public final class BlockLoggerHack extends Hack
	implements UpdateListener, RenderListener
{
	private final TextFieldSetting targetBlock = new TextFieldSetting(
		"Block Type",
		"Block type to search for (e.g., 'minecraft:diamond_ore', 'minecraft:chest')",
		"minecraft:diamond_ore");
	
	private final CheckboxSetting enableLogging = new CheckboxSetting(
		"Enable Logging", "Enable saving found blocks to JSON files", true);
	
	// State variables
	private final Set<BlockPos> foundBlocks = new HashSet<>();
	private final Set<BlockPos> printedBlocks = new HashSet<>(); // Track what
																	// we've
																	// already
																	// printed
	private final HashMap<BlockPos, Box> blockBoxes = new HashMap<>();
	private Block targetBlockType;
	private String currentFileName;
	private Path logsFolder;
	
	public BlockLoggerHack()
	{
		super("BlockLogger");
		setCategory(Category.RENDER);
		
		addSetting(targetBlock);
		addSetting(enableLogging);
	}
	
	@Override
	public String getRenderName()
	{
		String blockName = targetBlock.getValue();
		if(blockName.startsWith("minecraft:"))
			blockName = blockName.substring(10);
		return "BlockLogger [" + blockName + ": " + foundBlocks.size() + "]";
	}
	
	@Override
	protected void onEnable()
	{
		// Parse target block
		String blockName = targetBlock.getValue().toLowerCase().trim();
		if(!blockName.contains(":"))
			blockName = "minecraft:" + blockName;
		
		Identifier blockId = Identifier.tryParse(blockName);
		if(blockId == null || !Registries.BLOCK.containsId(blockId))
		{
			ChatUtils.error("Invalid block type: " + targetBlock.getValue());
			setEnabled(false);
			return;
		}
		
		targetBlockType = Registries.BLOCK.get(blockId);
		
		// Setup logging if enabled
		if(enableLogging.isChecked())
		{
			setupLogging();
		}
		
		// Clear previous data
		foundBlocks.clear();
		printedBlocks.clear();
		blockBoxes.clear();
		
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(RenderListener.class, this);
		
		ChatUtils.message("Started logging " + blockName + " blocks");
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(RenderListener.class, this);
		
		if(enableLogging.isChecked() && !foundBlocks.isEmpty())
		{
			ChatUtils.message("Stopped logging. Found " + foundBlocks.size()
				+ " blocks total.");
			if(currentFileName != null)
				ChatUtils.message("Saved to: " + currentFileName);
		}
		
		foundBlocks.clear();
		printedBlocks.clear();
		blockBoxes.clear();
	}
	
	@Override
	public void onUpdate()
	{
		// Search for blocks in loaded chunks
		ChunkUtils.getLoadedChunks().forEach(chunk -> {
			int minX = chunk.getPos().getStartX();
			int minZ = chunk.getPos().getStartZ();
			int maxX = chunk.getPos().getEndX();
			int maxZ = chunk.getPos().getEndZ();
			
			for(int x = minX; x <= maxX; x++)
			{
				for(int z = minZ; z <= maxZ; z++)
				{
					for(int y = MC.world.getBottomY(); y < MC.world
						.getTopY(); y++)
					{
						BlockPos pos = new BlockPos(x, y, z);
						
						// Skip if already found
						if(foundBlocks.contains(pos))
							continue;
						
						// Check if block matches target
						if(MC.world.getBlockState(pos)
							.getBlock() == targetBlockType)
						{
							foundBlocks.add(pos);
							blockBoxes.put(pos, BlockUtils.getBoundingBox(pos));
							
							// Print to chat only once per block
							if(!printedBlocks.contains(pos))
							{
								printedBlocks.add(pos);
								ChatUtils
									.message("Found " + targetBlock.getValue()
										+ " at " + pos.getX() + ", "
										+ pos.getY() + ", " + pos.getZ());
							}
							
							// Save to JSON immediately if logging enabled
							if(enableLogging.isChecked())
							{
								saveBlockToJson(pos);
							}
						}
					}
				}
			}
		});
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		if(blockBoxes.isEmpty())
			return;
		
		// Render block highlights
		List<Box> boxes = new ArrayList<>(blockBoxes.values());
		
		// Draw filled boxes with transparency
		RenderUtils.drawSolidBoxes(matrixStack, boxes, 0x4000FF00, false);
		
		// Draw outlined boxes
		RenderUtils.drawOutlinedBoxes(matrixStack, boxes, 0x8000FF00, false);
		
		// Draw tracers to blocks
		List<Vec3d> ends = boxes.stream().map(Box::getCenter).toList();
		RenderUtils.drawTracers(matrixStack, partialTicks, ends, 0x8000FF00,
			false);
	}
	
	private void setupLogging()
	{
		try
		{
			logsFolder =
				WurstClient.INSTANCE.getWurstFolder().resolve("block_logs");
			Files.createDirectories(logsFolder);
			
			// Create filename with current time
			long currentTime = System.currentTimeMillis();
			String blockName = targetBlock.getValue();
			if(blockName.startsWith("minecraft:"))
				blockName = blockName.substring(10);
			
			currentFileName = currentTime + "_" + blockName + ".json";
			
		}catch(IOException e)
		{
			ChatUtils.error("Failed to setup logging: " + e.getMessage());
			enableLogging.setChecked(false);
		}
	}
	
	private void saveBlockToJson(BlockPos pos)
	{
		if(currentFileName == null || logsFolder == null)
			return;
		
		try
		{
			Path filePath = logsFolder.resolve(currentFileName);
			JsonObject root;
			
			// Load existing data or create new
			if(Files.exists(filePath))
			{
				try
				{
					root = JsonUtils.parseFileToObject(filePath).toJsonObject();
				}catch(Exception e)
				{
					// If file is corrupted, create new
					root = new JsonObject();
				}
			}else
			{
				root = new JsonObject();
				root.addProperty("block_type", targetBlock.getValue());
				root.addProperty("created_time", System.currentTimeMillis());
				root.add("blocks", new JsonArray());
			}
			
			// Add new block
			JsonArray blocks = root.getAsJsonArray("blocks");
			JsonObject blockData = new JsonObject();
			blockData.addProperty("x", pos.getX());
			blockData.addProperty("y", pos.getY());
			blockData.addProperty("z", pos.getZ());
			blockData.addProperty("found_time", System.currentTimeMillis());
			
			blocks.add(blockData);
			
			// Save to file
			JsonUtils.toJson(root, filePath);
			
		}catch(Exception e)
		{
			ChatUtils.error("Failed to save block data: " + e.getMessage());
		}
	}
	
	public Set<BlockPos> getFoundBlocks()
	{
		return new HashSet<>(foundBlocks);
	}
	
	public void clearFoundBlocks()
	{
		foundBlocks.clear();
		printedBlocks.clear();
		blockBoxes.clear();
	}
	
	public void addBlocksFromJson(JsonObject jsonData)
	{
		try
		{
			if(!jsonData.has("blocks"))
				return;
			
			JsonArray blocks = jsonData.getAsJsonArray("blocks");
			for(int i = 0; i < blocks.size(); i++)
			{
				JsonObject blockData = blocks.get(i).getAsJsonObject();
				int x = blockData.get("x").getAsInt();
				int y = blockData.get("y").getAsInt();
				int z = blockData.get("z").getAsInt();
				
				BlockPos pos = new BlockPos(x, y, z);
				foundBlocks.add(pos);
				printedBlocks.add(pos); // Don't print these again
				blockBoxes.put(pos, BlockUtils.getBoundingBox(pos));
			}
			
		}catch(Exception e)
		{
			ChatUtils
				.error("Failed to load blocks from JSON: " + e.getMessage());
		}
	}
}
