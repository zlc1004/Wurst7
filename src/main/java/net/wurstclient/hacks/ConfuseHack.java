/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.Random;
import java.util.stream.Stream;

import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.util.FakePlayerEntity;
import net.wurstclient.util.RenderUtils;

@SearchTags({"confuse", "teleport", "movement", "pvp", "disorient"})
public final class ConfuseHack extends Hack
	implements UpdateListener, RenderListener
{
	private enum Mode
	{
		RANDOM_TP("Random TP", "Randomly teleports around target"),
		SWITCH("Switch", "Switches to opposite side of target"),
		CIRCLE("Circle", "Circles around target at high speed");
		
		private final String name;
		private final String description;
		
		private Mode(String name, String description)
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
	
	private final EnumSetting<Mode> mode = new EnumSetting<>("Mode",
		"Movement pattern to use.", Mode.values(), Mode.RANDOM_TP);
	
	private final SliderSetting delay =
		new SliderSetting("Delay", "Delay between teleports in ticks.", 3, 0,
			20, 1, SliderSetting.ValueDisplay.INTEGER);
	
	private final SliderSetting range =
		new SliderSetting("Range", "Range to confuse opponents.", 6, 1, 10, 0.1,
			SliderSetting.ValueDisplay.DECIMAL);
	
	private final SliderSetting circleSpeed = new SliderSetting("Circle speed",
		"Circle mode rotation speed in degrees per tick.", 10, 1, 180, 1,
		SliderSetting.ValueDisplay.INTEGER);
	
	private final CheckboxSetting moveThroughBlocks =
		new CheckboxSetting("Move through blocks",
			"Allows teleporting through solid blocks.", false);
	
	private int delayWaited = 0;
	private double circleProgress = 0;
	private double renderProgress = 0;
	private AbstractClientPlayerEntity target = null;
	private final Random random = new Random();
	
	public ConfuseHack()
	{
		super("Confuse");
		setCategory(Category.MOVEMENT);
		addSetting(mode);
		addSetting(delay);
		addSetting(range);
		addSetting(circleSpeed);
		addSetting(moveThroughBlocks);
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(RenderListener.class, this);
		
		delayWaited = 0;
		circleProgress = 0;
		renderProgress = 0;
		target = null;
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
		// Delay check
		delayWaited++;
		if(delayWaited < delay.getValueI())
			return;
		delayWaited = 0;
		
		// Find target
		target = findTarget();
		if(target == null)
			return;
		
		Vec3d entityPos =
			new Vec3d(target.getX(), target.getY(), target.getZ());
		Vec3d playerPos =
			new Vec3d(MC.player.getX(), MC.player.getY(), MC.player.getZ());
		double rangeValue = range.getValue();
		double halfRange = rangeValue / 2;
		
		switch(mode.getSelected())
		{
			case RANDOM_TP:
			performRandomTeleport(entityPos, playerPos, rangeValue, halfRange);
			break;
			
			case SWITCH:
			performSwitchTeleport(entityPos, playerPos, halfRange);
			break;
			
			case CIRCLE:
			performCircleTeleport(entityPos, playerPos);
			break;
		}
	}
	
	private AbstractClientPlayerEntity findTarget()
	{
		double rangeSq = range.getValueSq();
		
		Stream<AbstractClientPlayerEntity> stream = MC.world.getPlayers()
			.stream().filter(e -> !e.isRemoved()).filter(e -> e != MC.player)
			.filter(e -> !(e instanceof FakePlayerEntity))
			.filter(e -> !WURST.getFriends().isFriend(e))
			.filter(e -> MC.player.squaredDistanceTo(e) <= rangeSq);
		
		return stream
			.min((e1, e2) -> Float.compare(e1.getHealth(), e2.getHealth()))
			.orElse(null);
	}
	
	private void performRandomTeleport(Vec3d entityPos, Vec3d playerPos,
		double rangeValue, double halfRange)
	{
		double x = random.nextDouble() * rangeValue - halfRange;
		double y = 0;
		double z = random.nextDouble() * rangeValue - halfRange;
		Vec3d addend = new Vec3d(x, y, z);
		Vec3d goal = entityPos.add(addend);
		
		// Check if goal position is in air
		if(MC.world.getBlockState(BlockPos.ofFloored(goal.x, goal.y, goal.z))
			.getBlock() != Blocks.AIR)
		{
			goal = new Vec3d(goal.x, playerPos.y, goal.z);
		}
		
		if(MC.world.getBlockState(BlockPos.ofFloored(goal.x, goal.y, goal.z))
			.getBlock() == Blocks.AIR)
		{
			if(canTeleportTo(goal))
				teleportTo(goal);
			else
				delayWaited = delay.getValueI() - 1; // Retry next tick
		}else
		{
			delayWaited = delay.getValueI() - 1; // Retry next tick
		}
	}
	
	private void performSwitchTeleport(Vec3d entityPos, Vec3d playerPos,
		double halfRange)
	{
		Vec3d diff = entityPos.subtract(playerPos);
		Vec3d clampedDiff =
			new Vec3d(MathHelper.clamp(diff.x, -halfRange, halfRange),
				MathHelper.clamp(diff.y, -halfRange, halfRange),
				MathHelper.clamp(diff.z, -halfRange, halfRange));
		Vec3d goal = entityPos.add(clampedDiff);
		
		if(canTeleportTo(goal))
			teleportTo(goal);
		else
			delayWaited = delay.getValueI() - 1; // Retry next tick
	}
	
	private void performCircleTeleport(Vec3d entityPos, Vec3d playerPos)
	{
		circleProgress += circleSpeed.getValueI();
		if(circleProgress > 360)
			circleProgress -= 360;
		
		double rad = Math.toRadians(circleProgress);
		double sin = Math.sin(rad) * 3;
		double cos = Math.cos(rad) * 3;
		Vec3d goal =
			new Vec3d(entityPos.x + sin, playerPos.y, entityPos.z + cos);
		
		if(canTeleportTo(goal))
			teleportTo(goal);
	}
	
	private boolean canTeleportTo(Vec3d goal)
	{
		if(moveThroughBlocks.isChecked())
			return true;
		
		BlockHitResult hit = MC.world.raycast(new RaycastContext(
			new Vec3d(MC.player.getX(), MC.player.getY(), MC.player.getZ()),
			goal, RaycastContext.ShapeType.COLLIDER,
			RaycastContext.FluidHandling.ANY, MC.player));
		
		return !hit.isInsideBlock();
	}
	
	private void teleportTo(Vec3d goal)
	{
		MC.player.updatePosition(goal.x, goal.y, goal.z);
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		if(target == null)
			return;
		
		matrixStack.push();
		RenderUtils.applyRegionalRenderOffset(matrixStack);
		
		// Update animation
		renderProgress += 1.0;
		if(renderProgress > 360)
			renderProgress = 0;
		
		// Draw rainbow circle around target
		Vec3d targetPos =
			new Vec3d(target.getX(), target.getY(), target.getZ());
		drawRainbowCircle(matrixStack, targetPos, renderProgress);
		
		matrixStack.pop();
	}
	
	private void drawRainbowCircle(MatrixStack matrixStack, Vec3d center,
		double progress)
	{
		double radius = 3.0;
		int segments = 36;
		
		for(int i = 0; i < segments; i++)
		{
			double angle1 = Math.toRadians(i * 360.0 / segments);
			double angle2 = Math.toRadians((i + 1) * 360.0 / segments);
			
			double x1 = center.x + Math.sin(angle1) * radius;
			double z1 = center.z + Math.cos(angle1) * radius;
			double y1 = center.y + target.getHeight() / 2;
			
			double x2 = center.x + Math.sin(angle2) * radius;
			double z2 = center.z + Math.cos(angle2) * radius;
			double y2 = center.y + target.getHeight() / 2;
			
			// Calculate rainbow color
			float hue = (float)((i + progress) % 360) / 360.0f;
			int color = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f) | 0xFF000000;
			
			// Draw line segment
			RenderUtils.drawLine(matrixStack, new Vec3d(x1, y1, z1),
				new Vec3d(x2, y2, z2), color, false);
		}
	}
	
	@Override
	public String getRenderName()
	{
		if(target != null)
			return getName() + " [" + target.getName().getString() + "]";
		return getName() + " [No Target]";
	}
}
