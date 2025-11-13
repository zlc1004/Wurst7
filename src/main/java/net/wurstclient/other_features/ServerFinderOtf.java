/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.other_features;

import net.wurstclient.DontBlock;
import net.wurstclient.SearchTags;
import net.wurstclient.other_feature.OtherFeature;

@SearchTags({"Server Seeker", "Server Finder", "Find Servers", "Find Players"})
@DontBlock
public final class ServerFinderOtf extends OtherFeature
{
	public ServerFinderOtf()
	{
		super("ServerSeeker",
			"Advanced server discovery using the ServerSeeker API. Search through millions of servers by MOTD, player count, or find specific players. Use the 'ServerSeeker' and 'Find Players' buttons on the server selection screen.");
	}
}
