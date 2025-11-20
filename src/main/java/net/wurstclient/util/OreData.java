/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.util;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.wurstclient.settings.CheckboxSetting;

/**
 * Contains ore generation data for SeedXRay.
 * Stores the properties needed to simulate ore generation.
 */
public class OreData
{
	public final String name;
	public final Block block;
	public final CheckboxSetting enabled;
	public final int color;
	public final IntProvider count;
	public final IntProvider heightRange;
	public final int size;
	public final float rarity;
	public final float discardOnAir;
	public final boolean scattered;
	public final int generationStep;
	public final int index;
	
	private OreData(String name, Block block, int color, IntProvider count,
		IntProvider heightRange, int size, float rarity, float discardOnAir,
		boolean scattered, int generationStep, int index)
	{
		this.name = name;
		this.block = block;
		this.enabled =
			new CheckboxSetting(name, "Show " + name + " ores.", true);
		this.color = color;
		this.count = count;
		this.heightRange = heightRange;
		this.size = size;
		this.rarity = rarity;
		this.discardOnAir = discardOnAir;
		this.scattered = scattered;
		this.generationStep = generationStep;
		this.index = index;
	}
	
	/**
	 * Pre-configured ore data for common ores.
	 */
	public static final List<OreData> DEFAULT_ORES = Arrays.asList(
		// Coal
		new OreData("Coal", Blocks.COAL_ORE, 0x2F2C36,
			UniformIntProvider.create(1, 2), UniformIntProvider.create(5, 90),
			17, 1.0f, 0.0f, false, 6, 0),
		new OreData("Deepslate Coal", Blocks.DEEPSLATE_COAL_ORE, 0x2F2C36,
			UniformIntProvider.create(1, 2), UniformIntProvider.create(-64, 0),
			17, 1.0f, 0.0f, false, 6, 1),
		
		// Iron
		new OreData("Iron", Blocks.IRON_ORE, 0xECAD77,
			ConstantIntProvider.create(1), UniformIntProvider.create(15, 112),
			9, 1.0f, 0.0f, false, 6, 2),
		new OreData("Deepslate Iron", Blocks.DEEPSLATE_IRON_ORE, 0xECAD77,
			ConstantIntProvider.create(1), UniformIntProvider.create(-64, 72),
			9, 1.0f, 0.0f, false, 6, 3),
		
		// Gold
		new OreData("Gold", Blocks.GOLD_ORE, 0xF7E51E,
			ConstantIntProvider.create(1), UniformIntProvider.create(-64, 32),
			9, 1.0f, 0.0f, false, 6, 4),
		new OreData("Deepslate Gold", Blocks.DEEPSLATE_GOLD_ORE, 0xF7E51E,
			ConstantIntProvider.create(1), UniformIntProvider.create(-64, -48),
			9, 1.0f, 0.0f, false, 6, 5),
		
		// Diamond
		new OreData("Diamond", Blocks.DIAMOND_ORE, 0x21F4FF,
			ConstantIntProvider.create(1), UniformIntProvider.create(-64, 16),
			8, 1.0f, 0.0f, false, 6, 6),
		new OreData("Deepslate Diamond", Blocks.DEEPSLATE_DIAMOND_ORE, 0x21F4FF,
			ConstantIntProvider.create(1), UniformIntProvider.create(-64, 16),
			8, 1.0f, 0.0f, false, 6, 7),
		
		// Redstone
		new OreData("Redstone", Blocks.REDSTONE_ORE, 0xF50717,
			ConstantIntProvider.create(1), UniformIntProvider.create(-64, 16),
			8, 1.0f, 0.0f, false, 6, 8),
		new OreData("Deepslate Redstone", Blocks.DEEPSLATE_REDSTONE_ORE,
			0xF50717, ConstantIntProvider.create(1),
			UniformIntProvider.create(-64, 16), 8, 1.0f, 0.0f, false, 6, 9),
		
		// Lapis
		new OreData("Lapis", Blocks.LAPIS_ORE, 0x081ABD,
			ConstantIntProvider.create(1), UniformIntProvider.create(-64, 64),
			7, 1.0f, 0.0f, false, 6, 10),
		new OreData("Deepslate Lapis", Blocks.DEEPSLATE_LAPIS_ORE, 0x081ABD,
			ConstantIntProvider.create(1), UniformIntProvider.create(-64, 64),
			7, 1.0f, 0.0f, false, 6, 11),
		
		// Copper
		new OreData("Copper", Blocks.COPPER_ORE, 0xEF9700,
			ConstantIntProvider.create(1), UniformIntProvider.create(-16, 112),
			10, 1.0f, 0.0f, false, 6, 12),
		new OreData("Deepslate Copper", Blocks.DEEPSLATE_COPPER_ORE, 0xEF9700,
			ConstantIntProvider.create(1), UniformIntProvider.create(-64, 0),
			10, 1.0f, 0.0f, false, 6, 13),
		
		// Emerald
		new OreData("Emerald", Blocks.EMERALD_ORE, 0x1BD12D,
			ConstantIntProvider.create(1), UniformIntProvider.create(-16, 320),
			3, 0.5f, 0.0f, true, 6, 14),
		new OreData("Deepslate Emerald", Blocks.DEEPSLATE_EMERALD_ORE, 0x1BD12D,
			ConstantIntProvider.create(1), UniformIntProvider.create(-64, 0), 3,
			0.5f, 0.0f, true, 6, 15),
		
		// Nether ores
		new OreData("Nether Gold", Blocks.NETHER_GOLD_ORE, 0xF7E51E,
			ConstantIntProvider.create(1), UniformIntProvider.create(10, 117),
			10, 1.0f, 0.0f, false, 7, 16),
		new OreData("Nether Quartz", Blocks.NETHER_QUARTZ_ORE, 0xCDCDCD,
			ConstantIntProvider.create(1), UniformIntProvider.create(10, 117),
			14, 1.0f, 0.0f, false, 7, 17),
		new OreData("Ancient Debris", Blocks.ANCIENT_DEBRIS, 0xD11BF5,
			ConstantIntProvider.create(1), UniformIntProvider.create(8, 22), 3,
			1.0f, 0.0f, true, 7, 18));
}
