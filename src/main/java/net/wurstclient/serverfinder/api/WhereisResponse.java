/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.serverfinder.api;

import java.util.List;

public class WhereisResponse
{
	private String error;
	private List<PlayerServerEntry> data;
	
	public boolean isError()
	{
		return error != null;
	}
	
	public String getError()
	{
		return error;
	}
	
	public List<PlayerServerEntry> getData()
	{
		return data;
	}
	
	public static class PlayerServerEntry
	{
		private String ip;
		private int port;
		private String description;
		private boolean cracked;
		private String version;
		private int protocol;
		private int onlinePlayers;
		private int maxPlayers;
		private long lastSeen;
		private String country;
		private String software;
		
		public String getIp()
		{
			return ip;
		}
		
		public int getPort()
		{
			return port;
		}
		
		public String getDescription()
		{
			return description;
		}
		
		public boolean isCracked()
		{
			return cracked;
		}
		
		public String getVersion()
		{
			return version;
		}
		
		public int getProtocol()
		{
			return protocol;
		}
		
		public int getOnlinePlayers()
		{
			return onlinePlayers;
		}
		
		public int getMaxPlayers()
		{
			return maxPlayers;
		}
		
		public long getLastSeen()
		{
			return lastSeen;
		}
		
		public String getCountry()
		{
			return country;
		}
		
		public String getSoftware()
		{
			return software;
		}
		
		public String getAddress()
		{
			return port == 25565 ? ip : ip + ":" + port;
		}
	}
}