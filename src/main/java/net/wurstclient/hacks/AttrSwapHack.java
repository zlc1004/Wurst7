/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.client.gui.DrawContext;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.GUIRenderListener;
import net.wurstclient.events.RightClickListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"attr swap", "attribute swap", "enchant swap", "enchantment swap",
	"durability swap", "nbt swap"})
public final class AttrSwapHack extends Hack
	implements RightClickListener, UpdateListener, GUIRenderListener
{
	private final SliderSetting targetSlot = new SliderSetting("Target Slot",
		"Hotbar slot to switch to after using an item.\n"
			+ "This creates the timing window needed for attribute swapping.",
		1, 1, 9, 1, ValueDisplay.INTEGER);
	
	private boolean shouldSwap = false;
	private int ticksToWait = 0;
	private int highlightTicks = 0;
	
	public AttrSwapHack()
	{
		super("AttrSwap");
		setCategory(Category.ITEMS);
		addSetting(targetSlot);
	}
	
	@Override
	public String getRenderName()
	{
		return getName() + " [" + targetSlot.getValueString() + "]";
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(RightClickListener.class, this);
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(GUIRenderListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(RightClickListener.class, this);
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(GUIRenderListener.class, this);
		shouldSwap = false;
		ticksToWait = 0;
		highlightTicks = 0;
	}
	
	@Override
	public void onRightClick(RightClickEvent event)
	{
		if(MC.player == null)
			return;
		
		// Schedule slot swap for next tick
		shouldSwap = true;
		ticksToWait = 1;
		// Start highlighting immediately to show which slot will be targeted
		highlightTicks = 30; // 1.5 seconds of highlighting
	}
	
	@Override
	public void onUpdate()
	{
		if(MC.player == null)
			return;
		
		// Decrease highlight timer
		if(highlightTicks > 0)
			highlightTicks--;
		
		if(!shouldSwap)
			return;
		
		// Wait for the specified number of ticks
		if(ticksToWait > 0)
		{
			ticksToWait--;
			return;
		}
		
		// Perform the slot swap
		int currentSlot = MC.player.getInventory().getSelectedSlot();
		int targetSlotIndex = targetSlot.getValueI() - 1; // Convert 1-9 to 0-8
		
		// Don't swap if already on target slot
		if(currentSlot != targetSlotIndex)
		{
			MC.player.getInventory().setSelectedSlot(targetSlotIndex);
			// Start highlight animation for 20 ticks (1 second)
			highlightTicks = 20;
		}
		
		shouldSwap = false;
	}
	
	@Override
	public void onRenderGUI(DrawContext context, float partialTicks)
	{
		if(MC.player == null || !isEnabled())
			return;
			
		// Only render highlight when we have active highlighting or are about
		// to swap
		if(highlightTicks <= 0 && !shouldSwap)
			return;
		
		// Calculate hotbar position (similar to taco hack)
		int screenWidth = context.getScaledWindowWidth();
		int screenHeight = context.getScaledWindowHeight();
		
		// Hotbar center position
		int hotbarCenterX = screenWidth / 2;
		int hotbarY = screenHeight - 22; // Hotbar is typically 22 pixels from
											// bottom
		
		// Each slot is 20 pixels wide
		int slotSize = 20;
		int targetSlotIndex = targetSlot.getValueI() - 1; // Convert 1-9 to 0-8
		
		// Calculate target slot position
		int slotX =
			hotbarCenterX - (9 * slotSize / 2) + (targetSlotIndex * slotSize);
		int slotY = hotbarY;
		
		// Orange color with some transparency
		int orangeColor = 0x80FF8C00; // Semi-transparent dark orange
		
		// Make it more visible when actively swapping
		if(shouldSwap && ticksToWait <= 0)
			orangeColor = 0xFFFF4500; // Bright orange-red when swapping
			
		// Draw orange highlight around the slot
		int padding = 2;
		context.fill(slotX - padding, slotY - padding,
			slotX + slotSize + padding, slotY + slotSize + padding,
			orangeColor);
	}
}
