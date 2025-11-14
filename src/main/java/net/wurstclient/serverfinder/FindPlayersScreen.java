/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.serverfinder;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.ServerInfo.ServerType;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.wurstclient.mixinterface.IMultiplayerScreen;
import net.wurstclient.serverfinder.api.WhereisRequest;
import net.wurstclient.serverfinder.api.WhereisResponse;

public class FindPlayersScreen extends Screen
{
	private final MultiplayerScreen prevScreen;
	
	private TextFieldWidget playerNameBox;
	private ButtonWidget searchButton;
	private ButtonWidget getPlayersButton;
	
	private FindPlayerState state;
	private int serversFound;
	private String lastError;
	private List<WhereisResponse.PlayerServerEntry> lastResults;
	
	public FindPlayersScreen(MultiplayerScreen prevScreen)
	{
		super(Text.literal("Find Players"));
		this.prevScreen = prevScreen;
	}
	
	@Override
	protected void init()
	{
		addDrawableChild(searchButton = ButtonWidget
			.builder(Text.literal("Find Player"), b -> searchOrCancel())
			.dimensions(width / 2 - 100, height / 4 + 96 + 12, 200, 20)
			.build());
		searchButton.active = false;
		
		addDrawableChild(getPlayersButton = ButtonWidget
			.builder(Text.literal("Get Players"), b -> openGetPlayersScreen())
			.dimensions(width / 2 - 100, height / 4 + 120 + 12, 200, 20)
			.build());
		getPlayersButton.active = false;
		
		addDrawableChild(
			ButtonWidget.builder(Text.literal("Back"), b -> close())
				.dimensions(width / 2 - 100, height / 4 + 144 + 12, 200, 20)
				.build());
		
		playerNameBox = new TextFieldWidget(textRenderer, width / 2 - 100,
			height / 4 + 34, 200, 20, Text.empty());
		playerNameBox.setMaxLength(16);
		playerNameBox.setPlaceholder(Text.literal("Enter player name"));
		addSelectableChild(playerNameBox);
		setInitialFocus(playerNameBox);
		
		state = FindPlayerState.NOT_RUNNING;
		serversFound = 0;
		lastError = null;
		lastResults = null;
	}
	
	private void searchOrCancel()
	{
		if(state.isRunning())
		{
			state = FindPlayerState.CANCELLED;
			playerNameBox.active = true;
			searchButton.setMessage(Text.literal("Find Player"));
			return;
		}
		
		String playerName = playerNameBox.getText().trim();
		if(playerName.isEmpty())
			return;
		
		state = FindPlayerState.SEARCHING;
		playerNameBox.active = false;
		getPlayersButton.active = false;
		searchButton.setMessage(Text.literal("Cancel"));
		serversFound = 0;
		lastError = null;
		lastResults = null;
		
		// Create request asynchronously
		CompletableFuture.supplyAsync(() -> createPlayerRequest(playerName))
			.thenCompose(this::executePlayerSearch)
			.thenAccept(this::handlePlayerResults)
			.exceptionally(this::handlePlayerError);
	}
	
	private void openGetPlayersScreen()
	{
		if(client == null || lastResults == null || lastResults.isEmpty())
			return;
		
		client.setScreen(new GetPlayersScreen(prevScreen, lastResults));
	}
	
	private WhereisRequest createPlayerRequest(String playerName)
	{
		WhereisRequest request =
			new WhereisRequest(ServerSeekerHttp.getApiKey());
		request.setPlayerName(playerName);
		return request;
	}
	
	private CompletableFuture<WhereisResponse> executePlayerSearch(
		WhereisRequest request)
	{
		return CompletableFuture.supplyAsync(() -> {
			try
			{
				return ServerSeekerHttp.postJson("/whereis", request,
					WhereisResponse.class);
			}catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		});
	}
	
	private void handlePlayerResults(WhereisResponse response)
	{
		client.execute(() -> {
			if(state == FindPlayerState.CANCELLED)
				return;
			
			if(response.isError())
			{
				state = FindPlayerState.ERROR;
				lastError = response.getError();
				playerNameBox.active = true;
				searchButton.setMessage(Text.literal("Find Player"));
				return;
			}
			
			lastResults = response.getData();
			if(lastResults != null && !lastResults.isEmpty())
			{
				for(WhereisResponse.PlayerServerEntry server : lastResults)
				{
					if(state == FindPlayerState.CANCELLED)
						break;
					
					addServerToList(server);
					serversFound++;
				}
				getPlayersButton.active = true;
			}
			
			state = FindPlayerState.DONE;
			playerNameBox.active = true;
			searchButton.setMessage(Text.literal("Find Player"));
		});
	}
	
	private Void handlePlayerError(Throwable throwable)
	{
		client.execute(() -> {
			if(state == FindPlayerState.CANCELLED)
				return;
			
			state = FindPlayerState.ERROR;
			lastError = throwable.getMessage();
			if(lastError == null || lastError.isEmpty())
				lastError = "Network error occurred";
			
			playerNameBox.active = true;
			searchButton.setMessage(Text.literal("Find Player"));
		});
		return null;
	}
	
	private void addServerToList(WhereisResponse.PlayerServerEntry server)
	{
		ServerList serverList = prevScreen.getServerList();
		String address = server.getAddress();
		
		if(serverList.get(address) != null)
			return;
		
		// Create a descriptive name for the server
		String name = "ServerSeeker #" + (serversFound + 1);
		if(server.getDescription() != null
			&& !server.getDescription().trim().isEmpty())
		{
			String desc = server.getDescription().replaceAll("§.", "").trim();
			if(desc.length() > 30)
				desc = desc.substring(0, 30) + "...";
			name += " - " + desc;
		}
		
		serverList.add(new ServerInfo(name, address, ServerType.OTHER), false);
		serverList.saveFile();
		
		MultiplayerServerListWidget selector =
			((IMultiplayerScreen)prevScreen).getServerListSelector();
		selector.setSelected(null);
		selector.setServers(serverList);
	}
	
	@Override
	public void tick()
	{
		searchButton.active = !playerNameBox.getText().trim().isEmpty();
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if(keyCode == GLFW.GLFW_KEY_ENTER && searchButton.active)
		{
			searchButton.onPress();
			return true;
		}
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		super.render(context, mouseX, mouseY, delta);
		
		// Draw title
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2,
			height / 4 - 40, Colors.WHITE);
		
		// Draw input label
		context.drawTextWithShadow(textRenderer, "Player Name:",
			width / 2 - 100, height / 4 + 24, Colors.WHITE);
		
		// Draw text field
		playerNameBox.render(context, mouseX, mouseY, delta);
		
		// Draw status
		String status = getStatusText();
		if(status != null)
		{
			int color =
				state == FindPlayerState.ERROR ? Colors.RED : Colors.WHITE;
			context.drawCenteredTextWithShadow(textRenderer, status, width / 2,
				height / 4 + 64, color);
		}
	}
	
	private String getStatusText()
	{
		switch(state)
		{
			case SEARCHING:
			return "Searching...";
			
			case DONE:
			return serversFound > 0 ? "Found " + serversFound + " servers"
				: "No servers found";
			
			case ERROR:
			return lastError != null ? "Error: " + lastError
				: "An error occurred";
			
			default:
			return null;
		}
	}
	
	@Override
	public void close()
	{
		client.setScreen(prevScreen);
	}
	
	private enum FindPlayerState
	{
		NOT_RUNNING,
		SEARCHING,
		DONE,
		CANCELLED,
		ERROR;
		
		public boolean isRunning()
		{
			return this == SEARCHING;
		}
	}
}
