/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.Comparator;
import java.util.Random;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.ChatUtils;

@SearchTags({"ItemTp", "item tp", "item teleport", "AutoPickup", "auto pickup"})
public final class ItemTpHack extends Hack implements UpdateListener
{
	private final Random random = new Random();
	
	private final SliderSetting range =
		new SliderSetting("Range", 4.25, 1, 6, 0.05, ValueDisplay.DECIMAL);
	
	private final SliderSetting cooldown = new SliderSetting("Cooldown",
		"Cooldown between teleports in ticks (20 ticks = 1 second).", 20, 1,
		100, 1, ValueDisplay.INTEGER);
	
	private final CheckboxSetting directTeleport =
		new CheckboxSetting("Direct teleport",
			"Teleports directly to item instead of near it.", true);
	
	private final CheckboxSetting autoDisable =
		new CheckboxSetting("Auto disable",
			"Automatically disables when no items are in range.", false);
	
	private long lastTeleportTime = 0;
	
	public ItemTpHack()
	{
		super("ItemTP");
		setCategory(Category.MOVEMENT);
		
		addSetting(range);
		addSetting(cooldown);
		addSetting(directTeleport);
		addSetting(autoDisable);
	}
	
	@Override
	protected void onEnable()
	{
		lastTeleportTime = 0;
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		// Check cooldown
		long currentTime = System.currentTimeMillis();
		long cooldownMs = (long)(cooldown.getValueI() * 50); // Convert ticks to
																// ms
		if(currentTime - lastTeleportTime < cooldownMs)
			return;
		
		ClientPlayerEntity player = MC.player;
		if(player == null || MC.world == null)
			return;
		
		// Find closest item entity
		double rangeSq = Math.pow(range.getValue(), 2);
		Stream<Entity> stream = StreamSupport
			.stream(MC.world.getEntities().spliterator(), false)
			.filter(e -> e instanceof ItemEntity).filter(e -> !e.isRemoved())
			.filter(e -> player.squaredDistanceTo(e) <= rangeSq);
		
		Entity closestItem =
			stream.min(Comparator.comparingDouble(player::squaredDistanceTo))
				.orElse(null);
		
		if(closestItem == null)
		{
			if(autoDisable.isChecked())
			{
				ChatUtils.message("No items in range. Disabling ItemTP.");
				setEnabled(false);
			}
			return;
		}
		
		// Teleport to item
		if(directTeleport.isChecked())
		{
			// Teleport directly to item position
			player.setPosition(closestItem.getX(), closestItem.getY(),
				closestItem.getZ());
		}else
		{
			// Use TpAura-style random teleport near item
			player.setPosition(closestItem.getX() + random.nextInt(3) * 2 - 2,
				closestItem.getY(),
				closestItem.getZ() + random.nextInt(3) * 2 - 2);
		}
		
		lastTeleportTime = currentTime;
		
		ItemEntity itemEntity = (ItemEntity)closestItem;
		ChatUtils.message(
			"Teleported to " + itemEntity.getStack().getName().getString()
				+ " (" + itemEntity.getStack().getCount() + ")");
	}
	
	@Override
	public String getRenderName()
	{
		long currentTime = System.currentTimeMillis();
		long cooldownMs = (long)(cooldown.getValueI() * 50);
		long timeLeft = cooldownMs - (currentTime - lastTeleportTime);
		
		if(timeLeft > 0)
		{
			return getName() + " [" + (timeLeft / 1000.0) + "s]";
		}
		
		return getName();
	}
}
