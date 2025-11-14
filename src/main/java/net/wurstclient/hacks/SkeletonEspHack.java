/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.WurstRenderLayers;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.filterlists.EntityFilterList;
import net.wurstclient.settings.filters.FilterInvisibleSetting;
import net.wurstclient.settings.filters.FilterSleepingSetting;
import net.wurstclient.util.EntityUtils;
import net.wurstclient.util.FakePlayerEntity;
import net.wurstclient.util.RenderUtils;

@SearchTags({"skeleton esp", "player skeleton", "wireframe players"})
public final class SkeletonEspHack extends Hack
	implements UpdateListener, RenderListener
{
	private final ColorSetting color =
		new ColorSetting("Color", "Color of the skeleton lines.", Color.WHITE);
	
	private final CheckboxSetting distanceColors =
		new CheckboxSetting("Distance colors",
			"Changes the color of skeletons depending on distance.", false);
	
	private final EntityFilterList entityFilters = new EntityFilterList(
		new FilterSleepingSetting("Won't show sleeping players.", false),
		new FilterInvisibleSetting("Won't show invisible players.", false));
	
	private final ArrayList<PlayerEntity> players = new ArrayList<>();
	
	public SkeletonEspHack()
	{
		super("SkeletonESP");
		setCategory(Category.RENDER);
		addSetting(color);
		addSetting(distanceColors);
		entityFilters.forEach(this::addSetting);
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(RenderListener.class, this);
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
		players.clear();
		
		Stream<AbstractClientPlayerEntity> stream = MC.world.getPlayers()
			.parallelStream().filter(e -> !e.isRemoved() && e.getHealth() > 0)
			.filter(e -> e != MC.player)
			.filter(e -> !(e instanceof FakePlayerEntity))
			.filter(e -> Math.abs(e.getY() - MC.player.getY()) <= 1e6);
		
		stream = entityFilters.applyTo(stream);
		
		players.addAll(stream.collect(Collectors.toList()));
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		if(players.isEmpty())
			return;
		
		// Skip if first person and not in freecam
		if(MC.options.getPerspective() == Perspective.FIRST_PERSON
			&& !WURST.getHax().freecamHack.isEnabled())
			return;
		
		VertexConsumer buffer =
			RenderUtils.getVCP().getBuffer(WurstRenderLayers.ESP_LINES);
		
		for(PlayerEntity player : players)
		{
			matrixStack.push();
			RenderUtils.applyRenderOffset(matrixStack);
			
			renderSkeleton(matrixStack, buffer, player, partialTicks);
			
			matrixStack.pop();
		}
		
		RenderUtils.getVCP().draw();
	}
	
	private void renderSkeleton(MatrixStack matrixStack, VertexConsumer buffer,
		PlayerEntity player, float partialTicks)
	{
		// Get player position and dimensions
		Vec3d playerPos = EntityUtils.getLerpedPos(player, partialTicks);
		float bodyYaw = player.bodyYaw;
		boolean sneaking = player.isSneaking();
		boolean swimming = player.isInSwimmingPose();
		int skeletonColor = getSkeletonColor(player);
		
		// Calculate skeleton dimensions based on player pose
		float height = sneaking ? 1.5f : 1.8f;
		float headHeight = height - 0.25f;
		float shoulderHeight = height - 0.4f;
		float waistHeight = height * 0.6f;
		float footHeight = 0f;
		
		if(swimming)
		{
			height *= 0.6f;
			headHeight = height - 0.1f;
			shoulderHeight = height - 0.2f;
			waistHeight = height * 0.5f;
		}
		
		matrixStack.push();
		matrixStack.translate(playerPos.x, playerPos.y, playerPos.z);
		
		// Rotate based on body yaw
		matrixStack.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y
			.rotationDegrees(-bodyYaw));
		
		if(swimming)
			matrixStack.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X
				.rotationDegrees(90));
		
		MatrixStack.Entry entry = matrixStack.peek();
		
		// Draw spine
		RenderUtils.drawLine(entry, buffer, 0, footHeight + waistHeight, 0, 0,
			headHeight, 0, skeletonColor);
		
		// Draw head
		RenderUtils.drawLine(entry, buffer, 0, headHeight, 0, 0, height, 0,
			skeletonColor);
		
		// Draw shoulders
		RenderUtils.drawLine(entry, buffer, -0.4f, shoulderHeight, 0, 0.4f,
			shoulderHeight, 0, skeletonColor);
		
		// Draw waist/pelvis
		RenderUtils.drawLine(entry, buffer, -0.15f, waistHeight, 0, 0.15f,
			waistHeight, 0, skeletonColor);
		
		// Draw arms
		RenderUtils.drawLine(entry, buffer, -0.4f, shoulderHeight, 0, -0.4f,
			waistHeight - 0.2f, 0, skeletonColor);
		RenderUtils.drawLine(entry, buffer, 0.4f, shoulderHeight, 0, 0.4f,
			waistHeight - 0.2f, 0, skeletonColor);
		
		// Draw legs
		RenderUtils.drawLine(entry, buffer, -0.15f, waistHeight, 0, -0.15f,
			footHeight, 0, skeletonColor);
		RenderUtils.drawLine(entry, buffer, 0.15f, waistHeight, 0, 0.15f,
			footHeight, 0, skeletonColor);
		
		matrixStack.pop();
	}
	
	private int getSkeletonColor(PlayerEntity player)
	{
		if(distanceColors.isChecked())
			return getColorFromDistance(player);
		
		if(WURST.getFriends().contains(player.getName().getString()))
			return 0x0000FF; // Blue for friends
			
		return color.getColorI();
	}
	
	private int getColorFromDistance(PlayerEntity player)
	{
		double distance = MC.player.distanceTo(player);
		double percent = distance / 60;
		
		if(percent < 0 || percent > 1)
			return 0x00FF00; // Green if out of range
			
		int r, g;
		
		if(percent < 0.5)
		{
			r = 255;
			g = (int)(255 * percent / 0.5);
		}else
		{
			g = 255;
			r = 255 - (int)(255 * (percent - 0.5) / 0.5);
		}
		
		float[] rgb = {r / 255F, g / 255F, 0};
		return RenderUtils.toIntColor(rgb, 1.0F);
	}
}
