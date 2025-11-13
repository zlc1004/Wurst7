/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.AutoSellHack;
import net.wurstclient.hacks.AutoStealHack;

@Mixin(GenericContainerScreen.class)
public abstract class GenericContainerScreenMixin
	extends HandledScreen<GenericContainerScreenHandler>
{
	@Shadow
	@Final
	private int rows;

	@Unique
	private final AutoStealHack autoSteal =
		WurstClient.INSTANCE.getHax().autoStealHack;
	@Unique
	private final AutoSellHack autoSellHack =
		WurstClient.INSTANCE.getHax().autoSellHack;

	public GenericContainerScreenMixin(WurstClient wurst,
		GenericContainerScreenHandler container,
		PlayerInventory playerInventory, Text name)
	{
		super(container, playerInventory, name);
	}

	@Override
	public void init()
	{
		super.init();

		if(!WurstClient.INSTANCE.isEnabled())
			return;

		if(autoSteal.areButtonsVisible())
		{
			addDrawableChild(ButtonWidget
				.builder(Text.literal("Steal"),
					b -> autoSteal.steal(this, rows))
				.dimensions(x + backgroundWidth - 108, y + 4, 50, 12).build());

			addDrawableChild(ButtonWidget
				.builder(Text.literal("Store"),
					b -> autoSteal.store(this, rows))
				.dimensions(x + backgroundWidth - 56, y + 4, 50, 12).build());
		}
		if(autoSellHack.areButtonsVisible())
		{
			addDrawableChild(
				ButtonWidget.builder(Text.literal("AutoSell"), b -> LZFunc())
					.dimensions(x + backgroundWidth - 56, y + 4 - 16, 50, 12)
					.build());
		}

		if(autoSteal.isEnabled())
			autoSteal.steal(this, rows);
		if(autoSellHack.isEnabled())
			LZFunc();
	}

	@Unique
	public void LZFunc()
	{
		runInThread(() -> {
			waitForDelaySell();
			net.minecraft.screen.slot.Slot slot = handler.slots.get(16);
			net.minecraft.screen.slot.Slot slot2 = handler.slots.get(44);
			if(slot.getStack().getItem().toString() == "bucket")
			{
				onMouseClick(slot, slot.id, 0,
					net.minecraft.screen.slot.SlotActionType.QUICK_MOVE);
			}else if(slot2.getStack().getItem()
				.toString() == "red_stained_glass_pane")
			{
				// shiftClickSlots(rows * 9, rows * 9 + 44, 2);
				waitForDelaySell();
				onMouseClick(slot2, slot2.id, 0,
					net.minecraft.screen.slot.SlotActionType.QUICK_MOVE);
			}else
			{
				onMouseClick(slot, slot.id, 0,
					net.minecraft.screen.slot.SlotActionType.QUICK_MOVE);
				onMouseClick(slot2, slot2.id, 0,
					net.minecraft.screen.slot.SlotActionType.QUICK_MOVE);
			}
		});
	}

	@Unique
	private void runInThread(Runnable r)
	{
		new Thread(() -> {
			try
			{
				r.run();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}).start();
	}

	@Unique
	private void waitForDelaySell()
	{
		try
		{
			Thread.sleep(autoSellHack.getDelay());
		}catch(InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Unique
	private void waitForDelaySet(int n)
	{
		try
		{
			Thread.sleep(n);
		}catch(InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}
}
