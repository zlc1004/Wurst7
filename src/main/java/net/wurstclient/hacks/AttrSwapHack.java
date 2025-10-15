/*
 * Copyright (c) 2025 Kobosh.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.RightClickListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"attr swap", "attribute swap", "enchant swap", "enchantment swap",
	"durability swap", "nbt swap"})
public final class AttrSwapHack extends Hack
	implements RightClickListener, UpdateListener
{
	private final SliderSetting targetSlot = new SliderSetting("Target Slot",
		"Hotbar slot to switch to after using an item.\n"
			+ "This creates the timing window needed for attribute swapping.",
		1, 1, 9, 1, ValueDisplay.INTEGER);

	private boolean shouldSwap = false;
	private int ticksToWait = 0;

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
	}

	@Override
	protected void onDisable()
	{
		EVENTS.remove(RightClickListener.class, this);
		EVENTS.remove(UpdateListener.class, this);
		shouldSwap = false;
		ticksToWait = 0;
	}

	@Override
	public void onRightClick(RightClickEvent event)
	{
		if(MC.player == null)
			return;

		// Schedule slot swap for next tick
		shouldSwap = true;
		ticksToWait = 1;
	}

	@Override
	public void onUpdate()
	{
		if(MC.player == null)
			return;

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
}
