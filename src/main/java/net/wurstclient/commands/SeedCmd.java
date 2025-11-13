/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import net.wurstclient.WurstClient;

import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.SeedManager;

public final class SeedCmd extends Command
{
	public SeedCmd()
	{
		super("seed", "Manages world seeds for SeedXRay.", ".seed <seed>",
			".seed get", ".seed remove");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length == 0)
			throw new CmdSyntaxError();
		
		SeedManager seedManager = SeedManager.getInstance();
		
		switch(args[0].toLowerCase())
		{
			case "get":
			handleGetSeed(seedManager);
			break;
			
			case "remove":
			case "delete":
			case "clear":
			handleRemoveSeed(seedManager);
			break;
			
			default:
			handleSetSeed(seedManager, args[0]);
			break;
		}
	}
	
	private void handleGetSeed(SeedManager seedManager)
	{
		Long currentSeed = seedManager.getCurrentSeed();
		
		if(currentSeed == null)
		{
			ChatUtils.message("No seed available for current world.");
			if(!MC.isIntegratedServerRunning())
				ChatUtils.message(
					"Use '.seed <seed>' to set a seed for multiplayer worlds.");
		}else
		{
			String source = WurstClient.MC.isIntegratedServerRunning()
				? "(from integrated server)" : "(manually set)";
			ChatUtils.message("Current seed: " + currentSeed + " " + source);
		}
	}
	
	private void handleRemoveSeed(SeedManager seedManager) throws CmdException
	{
		if(WurstClient.MC.isIntegratedServerRunning())
			throw new CmdError("Cannot remove seed in singleplayer.");
		
		seedManager.removeSeed();
	}
	
	private void handleSetSeed(SeedManager seedManager, String seedInput)
		throws CmdException
	{
		if(WurstClient.MC.isIntegratedServerRunning())
			throw new CmdError(
				"Cannot set seed in singleplayer - seed is automatically detected.");
		
		seedManager.setSeed(seedInput);
	}
	
	@Override
	public String getPrimaryAction()
	{
		return "Get Seed";
	}
	
	@Override
	public void doPrimaryAction()
	{
		WURST.getCmdProcessor().process("seed get");
	}
}