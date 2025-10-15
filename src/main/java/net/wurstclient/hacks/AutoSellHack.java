/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

public final class AutoSellHack extends Hack
{
	private final SliderSetting delay = new SliderSetting("Delay",
		"Delay between moving stacks of items.\n"
			+ "Should be at least 70ms for NoCheat+ servers.",
		100, 0, 2500, 10, ValueDisplay.INTEGER.withSuffix("ms"));
	
	private final CheckboxSetting buttons =
		new CheckboxSetting("AutoSell button", false);
	
	public AutoSellHack()
	{
		super("AutoSell");
		setCategory(Category.ITEMS);
		addSetting(buttons);
		addSetting(delay);
	}
	
	public boolean areButtonsVisible()
	{
		return buttons.isChecked();
	}
	
	public long getDelay()
	{
		return delay.getValueI();
	}
	
	// See ContainerScreenMixin and ShulkerBoxScreenMixin
}
