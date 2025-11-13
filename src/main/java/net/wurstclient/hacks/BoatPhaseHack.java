/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;

@SearchTags({"boat phase", "boat noclip", "boat clip"})
public final class BoatPhaseHack extends Hack implements UpdateListener
{
	private final CheckboxSetting lockYaw = new CheckboxSetting("Lock boat yaw",
		"Locks boat yaw to the direction you're facing.", true);
	
	private final CheckboxSetting verticalControl =
		new CheckboxSetting("Vertical control",
			"Whether or not space/ctrl can be used to move vertically.", true);
	
	private final CheckboxSetting adjustHorizontalSpeed =
		new CheckboxSetting("Adjust horizontal speed",
			"Whether or not horizontal speed is modified.", false);
	
	private final CheckboxSetting fall =
		new CheckboxSetting("Fall", "Toggles vertical glide.", false);
	
	private final SliderSetting horizontalSpeed = new SliderSetting(
		"Horizontal speed", "Horizontal speed in blocks per second.", 10, 0, 50,
		0.5, SliderSetting.ValueDisplay.DECIMAL);
	
	private final SliderSetting verticalSpeed = new SliderSetting(
		"Vertical speed", "Vertical speed in blocks per second.", 5, 0, 20, 0.1,
		SliderSetting.ValueDisplay.DECIMAL);
	
	private final SliderSetting fallSpeed = new SliderSetting("Fall speed",
		"How fast you fall in blocks per second.", 0.625, 0, 10, 0.025,
		SliderSetting.ValueDisplay.DECIMAL);
	
	private BoatEntity boat = null;
	
	public BoatPhaseHack()
	{
		super("BoatPhase");
		setCategory(Category.MOVEMENT);
		addSetting(lockYaw);
		addSetting(verticalControl);
		addSetting(adjustHorizontalSpeed);
		addSetting(fall);
		addSetting(horizontalSpeed);
		addSetting(verticalSpeed);
		addSetting(fallSpeed);
	}
	
	@Override
	protected void onEnable()
	{
		boat = null;
		
		// Disable BoatGlitch if it's enabled
		if(WURST.getHax().boatGlitchHack.isEnabled())
			WURST.getHax().boatGlitchHack.setEnabled(false);
		
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		if(boat != null)
		{
			boat.noClip = false;
			boat = null;
		}
		
		EVENTS.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		// Check if player is in a boat
		if(MC.player.hasVehicle()
			&& MC.player.getVehicle() instanceof BoatEntity)
		{
			if(boat != MC.player.getVehicle())
			{
				if(boat != null)
					boat.noClip = false;
				
				boat = (BoatEntity)MC.player.getVehicle();
			}
		}else
		{
			if(boat != null)
			{
				boat.noClip = false;
				boat = null;
			}
			return;
		}
		
		if(boat == null)
			return;
		
		// Enable noclip
		boat.noClip = true;
		
		// Lock yaw to player's facing direction
		if(lockYaw.isChecked())
			boat.setYaw(MC.player.getYaw());
		
		// Calculate velocity
		Vec3d velocity;
		
		if(adjustHorizontalSpeed.isChecked())
		{
			// Calculate horizontal velocity based on player's facing direction
			float yawRad = MC.player.getYaw() * MathHelper.RADIANS_PER_DEGREE;
			double speed = horizontalSpeed.getValue() / 20.0; // Convert to
																// per-tick
			
			double velX = 0;
			double velZ = 0;
			
			if(MC.options.forwardKey.isPressed())
			{
				velX -= MathHelper.sin(yawRad) * speed;
				velZ += MathHelper.cos(yawRad) * speed;
			}
			if(MC.options.backKey.isPressed())
			{
				velX += MathHelper.sin(yawRad) * speed;
				velZ -= MathHelper.cos(yawRad) * speed;
			}
			if(MC.options.leftKey.isPressed())
			{
				velX -= MathHelper.cos(yawRad) * speed;
				velZ -= MathHelper.sin(yawRad) * speed;
			}
			if(MC.options.rightKey.isPressed())
			{
				velX += MathHelper.cos(yawRad) * speed;
				velZ += MathHelper.sin(yawRad) * speed;
			}
			
			velocity = new Vec3d(velX, boat.getVelocity().y, velZ);
		}else
		{
			velocity = boat.getVelocity();
		}
		
		double velX = velocity.x;
		double velY = velocity.y;
		double velZ = velocity.z;
		
		// Handle vertical movement
		if(verticalControl.isChecked())
		{
			if(MC.options.jumpKey.isPressed())
				velY = verticalSpeed.getValue() / 20.0;
			else if(MC.options.sprintKey.isPressed())
				velY = -verticalSpeed.getValue() / 20.0;
			else if(fall.isChecked())
				velY = -fallSpeed.getValue() / 20.0;
			else
				velY = 0;
		}else if(fall.isChecked())
		{
			velY = -fallSpeed.getValue() / 20.0;
		}
		
		// Apply the new velocity
		boat.setVelocity(velX, velY, velZ);
	}
}