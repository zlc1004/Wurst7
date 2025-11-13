/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.serverfinder;

import java.util.List;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.wurstclient.serverfinder.api.WhereisResponse;

public class GetPlayersScreen extends Screen
{
	private final MultiplayerScreen prevScreen;
	private List<WhereisResponse.PlayerServerEntry> servers;
	private int scroll = 0;
	private static final int SERVERS_PER_PAGE = 12;
	
	public GetPlayersScreen(MultiplayerScreen prevScreen, List<WhereisResponse.PlayerServerEntry> servers)
	{
		super(Text.literal("Get Players"));
		this.prevScreen = prevScreen;
		this.servers = servers;
	}
	
	@Override
	protected void init()
	{
		addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> close())
			.dimensions(width / 2 - 100, height - 30, 200, 20).build());
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		super.render(context, mouseX, mouseY, delta);
		
		// Draw title
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, Colors.WHITE);
		
		// Draw servers list
		if(servers != null && !servers.isEmpty())
		{
			int y = 50;
			int startIndex = scroll;
			int endIndex = Math.min(startIndex + SERVERS_PER_PAGE, servers.size());
			
			for(int i = startIndex; i < endIndex; i++)
			{
				WhereisResponse.PlayerServerEntry server = servers.get(i);
				String text = server.getAddress() + " - " + server.getOnlinePlayers() + "/" + server.getMaxPlayers();
				context.drawTextWithShadow(textRenderer, text, 20, y, Colors.WHITE);
				y += 20;
			}
			
			// Draw pagination info
			int totalPages = (servers.size() + SERVERS_PER_PAGE - 1) / SERVERS_PER_PAGE;
			int currentPage = scroll / SERVERS_PER_PAGE + 1;
			String pageInfo = "Page " + currentPage + " / " + totalPages;
			context.drawCenteredTextWithShadow(textRenderer, pageInfo, width / 2, height - 60, Colors.GRAY);
		}
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount)
	{
		if(servers != null && servers.size() > SERVERS_PER_PAGE)
		{
			scroll += (verticalAmount > 0 ? -1 : 1) * 3;
			int maxScroll = Math.max(0, servers.size() - SERVERS_PER_PAGE);
			scroll = Math.max(0, Math.min(scroll, maxScroll));
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}
	
	@Override
	public void close()
	{
		client.setScreen(prevScreen);
	}
}
