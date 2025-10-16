/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.DrawContext;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.GUIRenderListener;
import net.wurstclient.events.LeftClickListener;
import net.wurstclient.events.RightClickListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"attr swap", "attribute swap", "enchant swap", "enchantment swap",
	"durability swap", "nbt swap"})
public final class AttrSwapHack extends Hack implements LeftClickListener,
	RightClickListener, UpdateListener, GUIRenderListener
{
	private final SliderSetting targetSlot = new SliderSetting("Target Slot",
		"Hotbar slot to switch to after using an item.\n"
			+ "This creates the timing window needed for attribute swapping.",
		1, 1, 9, 1, ValueDisplay.INTEGER);
	
	private final SliderSetting waitTime = new SliderSetting("Wait time",
		"Number of ticks to wait before swapping slots.", 0, 0, 1, 1,
		ValueDisplay.INTEGER);
	
	private final CheckboxSetting autoSwitchBack = new CheckboxSetting(
		"Auto switch back",
		"Automatically switch back to original slot when button is released.",
		false);
	
	private final SliderSetting switchBackDelay =
		new SliderSetting("Switch back delay",
			"Number of ticks to wait before switching back to original slot.",
			5, 0, 20, 1, ValueDisplay.INTEGER);
	
	private final CheckboxSetting enableLeftClick = new CheckboxSetting(
		"Left Click", "Enable swapping on left click (attack).", true);
	
	private final CheckboxSetting enableRightClick = new CheckboxSetting(
		"Right Click", "Enable swapping on right click (use item).", true);
	
	private boolean shouldSwap = false;
	private int ticksToWait = 0;
	private int originalSlot = -1;
	private boolean shouldSwitchBack = false;
	private int switchBackTicks = 0;
	private boolean leftButtonWasPressed = false;
	private boolean rightButtonWasPressed = false;
	
	public AttrSwapHack()
	{
		super("AttrSwap");
		setCategory(Category.ITEMS);
		addSetting(targetSlot);
		addSetting(waitTime);
		addSetting(autoSwitchBack);
		addSetting(switchBackDelay);
		addSetting(enableLeftClick);
		addSetting(enableRightClick);
	}
	
	@Override
	public String getRenderName()
	{
		return getName() + " [" + targetSlot.getValueString() + "]";
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(LeftClickListener.class, this);
		EVENTS.add(RightClickListener.class, this);
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(GUIRenderListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(LeftClickListener.class, this);
		EVENTS.remove(RightClickListener.class, this);
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(GUIRenderListener.class, this);
		shouldSwap = false;
		ticksToWait = 0;
		originalSlot = -1;
		shouldSwitchBack = false;
		switchBackTicks = 0;
		leftButtonWasPressed = false;
		rightButtonWasPressed = false;
	}
	
	@Override
	public void onLeftClick(LeftClickEvent event)
	{
		if(MC.player == null || !enableLeftClick.isChecked())
			return;
		
		// Store original slot before swapping
		if(originalSlot == -1)
			originalSlot = MC.player.getInventory().getSelectedSlot();
		
		// Schedule slot swap with configured wait time
		shouldSwap = true;
		ticksToWait = waitTime.getValueI();
		leftButtonWasPressed = true;
	}
	
	@Override
	public void onRightClick(RightClickEvent event)
	{
		if(MC.player == null || !enableRightClick.isChecked())
			return;
		
		// Store original slot before swapping
		if(originalSlot == -1)
			originalSlot = MC.player.getInventory().getSelectedSlot();
		
		// Schedule slot swap with configured wait time
		shouldSwap = true;
		ticksToWait = waitTime.getValueI();
		rightButtonWasPressed = true;
	}
	
	@Override
	public void onUpdate()
	{
		if(MC.player == null)
			return;
		
		// Check mouse button states for auto switch back
		if(autoSwitchBack.isChecked())
		{
			long windowHandle = MC.getWindow().getHandle();
			boolean leftPressed = GLFW.glfwGetMouseButton(windowHandle,
				GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
			boolean rightPressed = GLFW.glfwGetMouseButton(windowHandle,
				GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
			
			// Check if buttons were released
			if((leftButtonWasPressed && !leftPressed)
				|| (rightButtonWasPressed && !rightPressed))
			{
				if(originalSlot != -1 && !shouldSwitchBack)
				{
					shouldSwitchBack = true;
					switchBackTicks = switchBackDelay.getValueI();
				}
			}
			
			// Update button states
			if(!leftPressed)
				leftButtonWasPressed = false;
			if(!rightPressed)
				rightButtonWasPressed = false;
		}
		
		// Handle switch back logic
		if(shouldSwitchBack)
		{
			if(switchBackTicks > 0)
			{
				switchBackTicks--;
				return;
			}
			
			// Switch back to original slot
			if(originalSlot != -1)
			{
				MC.player.getInventory().setSelectedSlot(originalSlot);
				originalSlot = -1;
				shouldSwitchBack = false;
			}
		}
		
		// Handle initial swap logic
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
		}
		
		shouldSwap = false;
	}
	
	@Override
	public void onRenderGUI(DrawContext context, float partialTicks)
	{
		if(MC.player == null || !isEnabled())
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
		
		// Orange color - always visible when hack is enabled
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
