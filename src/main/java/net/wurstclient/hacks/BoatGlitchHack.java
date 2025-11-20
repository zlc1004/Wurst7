/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import org.lwjgl.glfw.GLFW;

import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.KeyPressListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;

@SearchTags({"boat glitch", "boat clip", "boat noclip"})
public final class BoatGlitchHack extends Hack
	implements UpdateListener, KeyPressListener
{
	private final CheckboxSetting toggleAfter = new CheckboxSetting(
		"Toggle after", "Disables the module when finished.", true);
	
	private final CheckboxSetting remount = new CheckboxSetting("Remount",
		"Remounts the boat when finished.", true);
	
	private Entity boat = null;
	private int dismountTicks = 0;
	private int remountTicks = 0;
	private boolean dontPhase = true;
	private boolean boatPhaseEnabled = false;
	
	public BoatGlitchHack()
	{
		super("BoatGlitch");
		setCategory(Category.MOVEMENT);
		addSetting(toggleAfter);
		addSetting(remount);
	}
	
	@Override
	protected void onEnable()
	{
		dontPhase = true;
		dismountTicks = 0;
		remountTicks = 0;
		boat = null;
		
		// Check if BoatPhase is enabled and disable it temporarily
		if(WURST.getHax().boatPhaseHack.isEnabled())
		{
			boatPhaseEnabled = true;
			WURST.getHax().boatPhaseHack.setEnabled(false);
		}else
		{
			boatPhaseEnabled = false;
		}
		
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(KeyPressListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		if(boat != null)
		{
			boat.noClip = false;
			boat = null;
		}
		
		// Re-enable BoatPhase if it was enabled before
		if(boatPhaseEnabled && !WURST.getHax().boatPhaseHack.isEnabled())
			WURST.getHax().boatPhaseHack.setEnabled(true);
		
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(KeyPressListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		// Handle boat movement and noclip
		if(dismountTicks == 0 && !dontPhase && MC.player.hasVehicle())
		{
			Entity vehicle = MC.player.getVehicle();
			if(vehicle instanceof BoatEntity)
			{
				if(boat != vehicle)
				{
					if(boat != null)
						boat.noClip = false;
					
					boat = vehicle;
				}
				
				if(boat != null)
				{
					boat.noClip = true;
					dismountTicks = 5;
				}
			}
		}
		
		// Handle dismount ticks
		if(dismountTicks > 0)
		{
			dismountTicks--;
			if(dismountTicks == 0)
			{
				if(boat != null)
				{
					boat.noClip = false;
					if(toggleAfter.isChecked() && !remount.isChecked())
					{
						setEnabled(false);
					}else if(remount.isChecked())
					{
						remountTicks = 5;
					}
				}
				dontPhase = true;
			}
		}
		
		// Handle remount ticks
		if(remountTicks > 0)
		{
			remountTicks--;
			if(remountTicks == 0 && boat != null)
			{
				MC.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket
					.interact(boat, false, Hand.MAIN_HAND));
				if(toggleAfter.isChecked())
					setEnabled(false);
			}
		}
	}
	
	@Override
	public void onKeyPress(KeyPressEvent event)
	{
		// Check if sneak key is pressed
		if(event.getAction() == GLFW.GLFW_PRESS && event
			.getKeyCode() == MC.options.sneakKey.getDefaultKey().getCode())
		{
			if(MC.player.hasVehicle()
				&& MC.player.getVehicle() instanceof BoatEntity)
			{
				dontPhase = false;
				boat = null;
			}
		}
	}
}
