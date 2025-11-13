/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.block.Blocks;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.InventoryUtils;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags({"lava cast", "griefing", "lava", "obsidian"})
public final class LavacastHack extends Hack
	implements UpdateListener, RenderListener
{
	private enum Stage
	{
		NONE,
		LAVA_DOWN,
		LAVA_UP,
		WATER_DOWN,
		WATER_UP
	}
	
	private final SliderSetting tickInterval =
		new SliderSetting("Tick interval", "Interval between actions.", 2, 0,
			20, 1, SliderSetting.ValueDisplay.INTEGER);
	
	private final SliderSetting distMin = new SliderSetting("Min distance",
		"Top plane cutoff.", 5, 0, 10, 1, SliderSetting.ValueDisplay.INTEGER);
	
	private final SliderSetting lavaDownMult = new SliderSetting(
		"Lava down multiplier", "Controls the shape of the cast.", 40, 1, 100,
		1, SliderSetting.ValueDisplay.INTEGER);
	
	private final SliderSetting lavaUpMult = new SliderSetting(
		"Lava up multiplier", "Controls the shape of the cast.", 8, 1, 100, 1,
		SliderSetting.ValueDisplay.INTEGER);
	
	private final SliderSetting waterDownMult = new SliderSetting(
		"Water down multiplier", "Controls the shape of the cast.", 4, 1, 100,
		1, SliderSetting.ValueDisplay.INTEGER);
	
	private final SliderSetting waterUpMult = new SliderSetting(
		"Water up multiplier", "Controls the shape of the cast.", 1, 1, 100, 1,
		SliderSetting.ValueDisplay.INTEGER);
	
	private int dist;
	private BlockPos placeFluidPos;
	private int tick;
	private Stage stage = Stage.NONE;
	
	public LavacastHack()
	{
		super("Lavacast");
		setCategory(Category.BLOCKS);
		addSetting(tickInterval);
		addSetting(distMin);
		addSetting(lavaDownMult);
		addSetting(lavaUpMult);
		addSetting(waterDownMult);
		addSetting(waterUpMult);
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(RenderListener.class, this);
		
		if(MC.player == null || MC.world == null)
		{
			setEnabled(false);
			return;
		}
		
		tick = 0;
		stage = Stage.NONE;
		placeFluidPos = getTargetBlockPos();
		if(placeFluidPos == null)
		{
			placeFluidPos = MC.player.getBlockPos().down(2);
		}else
		{
			placeFluidPos = placeFluidPos.up();
		}
		
		dist = -1;
		getDistance(new Vec3i(1, 0, 0));
		getDistance(new Vec3i(-1, 0, 0));
		getDistance(new Vec3i(0, 0, 1));
		getDistance(new Vec3i(0, 0, -1));
		
		if(dist < 1)
		{
			ChatUtils.error("Couldn't locate bottom.");
			setEnabled(false);
			return;
		}
		
		ChatUtils.message("Distance: " + dist);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(RenderListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(MC.player == null || MC.world == null)
			return;
		
		tick++;
		if(shouldBreakOnTick())
			return;
		
		if(dist < distMin.getValueI())
		{
			setEnabled(false);
			return;
		}
		
		tick = 0;
		if(checkMineBlock())
			return;
		
		switch(stage)
		{
			case NONE:
			RotationUtils.getNeededRotations(Vec3d.ofCenter(placeFluidPos))
				.sendPlayerLookPacket();
			placeLava();
			stage = Stage.LAVA_DOWN;
			break;
			
			case LAVA_DOWN:
			RotationUtils.getNeededRotations(Vec3d.ofCenter(placeFluidPos))
				.sendPlayerLookPacket();
			pickupLiquid();
			stage = Stage.LAVA_UP;
			break;
			
			case LAVA_UP:
			RotationUtils.getNeededRotations(Vec3d.ofCenter(placeFluidPos))
				.sendPlayerLookPacket();
			placeWater();
			stage = Stage.WATER_DOWN;
			break;
			
			case WATER_DOWN:
			RotationUtils.getNeededRotations(Vec3d.ofCenter(placeFluidPos))
				.sendPlayerLookPacket();
			pickupLiquid();
			stage = Stage.WATER_UP;
			break;
			
			case WATER_UP:
			dist--;
			RotationUtils.getNeededRotations(Vec3d.ofCenter(placeFluidPos))
				.sendPlayerLookPacket();
			placeLava();
			stage = Stage.LAVA_DOWN;
			break;
		}
	}
	
	private boolean shouldBreakOnTick()
	{
		if(stage == Stage.LAVA_DOWN && tick < dist * lavaDownMult.getValueI())
			return true;
		if(stage == Stage.LAVA_UP && tick < dist * lavaUpMult.getValueI())
			return true;
		if(stage == Stage.WATER_DOWN && tick < dist * waterDownMult.getValueI())
			return true;
		if(stage == Stage.WATER_UP && tick < dist * waterUpMult.getValueI())
			return true;
		if(tick < tickInterval.getValueI())
			return true;
		return false;
	}
	
	private boolean checkMineBlock()
	{
		if(stage == Stage.NONE
			&& MC.world.getBlockState(placeFluidPos).getBlock() != Blocks.AIR)
		{
			RotationUtils.getNeededRotations(Vec3d.ofCenter(placeFluidPos))
				.sendPlayerLookPacket();
			updateBlockBreakingProgress();
			return true;
		}
		return false;
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		if(placeFluidPos == null)
			return;
		
		matrixStack.push();
		RenderUtils.applyRegionalRenderOffset(matrixStack);
		
		Box box = new Box(placeFluidPos);
		float[] color = getStageColor();
		int quadColor =
			((int)(0.3F * 255) << 24) | ((int)(color[0] * 255) << 16)
				| ((int)(color[1] * 255) << 8) | (int)(color[2] * 255);
		int lineColor = ((int)(color[0] * 255) << 16)
			| ((int)(color[1] * 255) << 8) | (int)(color[2] * 255) | 0xFF000000;
		
		RenderUtils.drawSolidBox(matrixStack, box, quadColor, false);
		RenderUtils.drawOutlinedBox(matrixStack, box, lineColor, false);
		
		matrixStack.pop();
	}
	
	private float[] getStageColor()
	{
		switch(stage)
		{
			case LAVA_DOWN:
			return new float[]{1F, 0.7F, 0.04F}; // Orange
			
			case LAVA_UP:
			return new float[]{1F, 0.7F, 0.5F}; // Light orange
			
			case WATER_DOWN:
			return new float[]{0.04F, 0.04F, 1F}; // Blue
			
			case WATER_UP:
			return new float[]{0.5F, 0.5F, 1F}; // Light blue
			
			default:
			return new float[]{0.5F, 0.5F, 0.5F}; // Gray
		}
	}
	
	private void placeLava()
	{
		int slot = InventoryUtils.indexOf(Items.LAVA_BUCKET);
		if(slot == -1)
		{
			ChatUtils.error("No lava bucket found.");
			setEnabled(false);
			return;
		}
		
		int prevSlot = MC.player.getInventory().getSelectedSlot();
		MC.player.getInventory().setSelectedSlot(slot);
		MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
		MC.player.getInventory().setSelectedSlot(prevSlot);
	}
	
	private void placeWater()
	{
		int slot = InventoryUtils.indexOf(Items.WATER_BUCKET);
		if(slot == -1)
		{
			ChatUtils.error("No water bucket found.");
			setEnabled(false);
			return;
		}
		
		int prevSlot = MC.player.getInventory().getSelectedSlot();
		MC.player.getInventory().setSelectedSlot(slot);
		MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
		MC.player.getInventory().setSelectedSlot(prevSlot);
	}
	
	private void pickupLiquid()
	{
		int slot = InventoryUtils.indexOf(Items.BUCKET);
		if(slot == -1)
		{
			ChatUtils.error("No bucket found.");
			setEnabled(false);
			return;
		}
		
		int prevSlot = MC.player.getInventory().getSelectedSlot();
		MC.player.getInventory().setSelectedSlot(slot);
		MC.interactionManager.interactItem(MC.player, Hand.MAIN_HAND);
		MC.player.getInventory().setSelectedSlot(prevSlot);
	}
	
	private void updateBlockBreakingProgress()
	{
		MC.interactionManager.updateBlockBreakingProgress(placeFluidPos,
			Direction.UP);
	}
	
	private BlockPos getTargetBlockPos()
	{
		HitResult blockHit = MC.crosshairTarget;
		if(blockHit.getType() != HitResult.Type.BLOCK)
			return null;
		
		return ((BlockHitResult)blockHit).getBlockPos();
	}
	
	private void getDistance(Vec3i offset)
	{
		BlockPos pos = placeFluidPos.down().add(offset);
		final BlockHitResult result =
			MC.world.raycast(new RaycastContext(Vec3d.ofCenter(pos),
				Vec3d.ofCenter(pos.down(250)),
				RaycastContext.ShapeType.COLLIDER,
				RaycastContext.FluidHandling.ANY, MC.player));
		
		if(result == null || result.getType() != HitResult.Type.BLOCK)
			return;
		
		int newDist = placeFluidPos.getY() - result.getBlockPos().getY();
		if(newDist > dist)
			dist = newDist;
	}
	
	@Override
	public String getRenderName()
	{
		return getName() + " [" + stage.toString() + "]";
	}
}