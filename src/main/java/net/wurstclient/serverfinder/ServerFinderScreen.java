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

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.ServerInfo.ServerType;
import net.minecraft.client.option.ServerList;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.wurstclient.mixinterface.IMultiplayerScreen;
import net.wurstclient.serverfinder.api.ServersRequest;
import net.wurstclient.serverfinder.api.ServersResponse;

public class ServerFinderScreen extends Screen
{
	private final MultiplayerScreen prevScreen;
	
	private TextFieldWidget motdBox;
	private TextFieldWidget minPlayersBox;
	private TextFieldWidget maxPlayersBox;
	private CheckboxWidget crackedBox;
	private ButtonWidget searchButton;
	
	private ServerFinderState state;
	private int serversFound;
	private String lastError;
	
	public ServerFinderScreen(MultiplayerScreen prevScreen)
	{
		super(Text.literal("ServerSeeker"));
		this.prevScreen = prevScreen;
	}
	
	@Override
	public void init()
	{
		addDrawableChild(searchButton = ButtonWidget
			.builder(Text.literal("Find Servers"), b -> searchOrCancel())
			.dimensions(width / 2 - 100, height / 4 + 120 + 12, 200, 20)
			.build());
		searchButton.active = true;
		
		addDrawableChild(
			ButtonWidget.builder(Text.literal("Back"), b -> close())
				.dimensions(width / 2 - 100, height / 4 + 168 + 12, 200, 20)
				.build());
		
		motdBox = new TextFieldWidget(textRenderer, width / 2 - 100,
			height / 4 + 34, 200, 20, Text.empty());
		motdBox.setMaxLength(200);
		motdBox
			.setPlaceholder(Text.literal("Server description/MOTD (optional)"));
		addSelectableChild(motdBox);
		setFocused(motdBox);
		
		minPlayersBox = new TextFieldWidget(textRenderer, width / 2 - 150,
			height / 4 + 68, 90, 20, Text.empty());
		minPlayersBox.setMaxLength(5);
		minPlayersBox.setPlaceholder(Text.literal("Min"));
		addSelectableChild(minPlayersBox);
		
		maxPlayersBox = new TextFieldWidget(textRenderer, width / 2 + 10,
			height / 4 + 68, 90, 20, Text.empty());
		maxPlayersBox.setMaxLength(5);
		maxPlayersBox.setPlaceholder(Text.literal("Max"));
		addSelectableChild(maxPlayersBox);
		
		crackedBox = CheckboxWidget
			.builder(Text.literal("Cracked servers only"), textRenderer)
			.pos(width / 2 - 100, height / 4 + 96).build();
		addDrawableChild(crackedBox);
		
		state = ServerFinderState.NOT_RUNNING;
		serversFound = 0;
		lastError = null;
	}
	
	private void searchOrCancel()
	{
		if(state.isRunning())
		{
			state = ServerFinderState.CANCELLED;
			enableInputs(true);
			searchButton.setMessage(Text.literal("Find Servers"));
			return;
		}
		
		state = ServerFinderState.SEARCHING;
		enableInputs(false);
		searchButton.setMessage(Text.literal("Cancel"));
		serversFound = 0;
		lastError = null;
		
		// Create request asynchronously
		CompletableFuture.supplyAsync(this::createSearchRequest)
			.thenCompose(this::executeSearch)
			.thenAccept(this::handleSearchResults)
			.exceptionally(this::handleSearchError);
	}
	
	private void enableInputs(boolean enabled)
	{
		motdBox.active = enabled;
		minPlayersBox.active = enabled;
		maxPlayersBox.active = enabled;
		crackedBox.active = enabled;
	}
	
	private ServersRequest createSearchRequest()
	{
		ServersRequest request =
			new ServersRequest(ServerSeekerHttp.getApiKey());
		
		// Set MOTD filter if provided
		String motd = motdBox.getText().trim();
		if(!motd.isEmpty())
			request.setDescription(motd);
		
		// Set player count filters if provided
		String minStr = minPlayersBox.getText().trim();
		String maxStr = maxPlayersBox.getText().trim();
		
		if(!minStr.isEmpty() || !maxStr.isEmpty())
		{
			Integer min = minStr.isEmpty() ? 0 : parseInteger(minStr);
			Integer max = maxStr.isEmpty() ? -1 : parseInteger(maxStr);
			
			if(min != null && max != null)
				request.setOnlinePlayers(min, max);
		}
		
		// Set cracked filter
		if(crackedBox.isChecked())
			request.setCracked(true);
		
		// Set protocol version for current MC version
		request.setProtocolVersion(
			SharedConstants.getGameVersion().protocolVersion());
		
		return request;
	}
	
	private CompletableFuture<ServersResponse> executeSearch(
		ServersRequest request)
	{
		return CompletableFuture.supplyAsync(() -> {
			try
			{
				return ServerSeekerHttp.postJson("/servers", request,
					ServersResponse.class);
			}catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		});
	}
	
	private void handleSearchResults(ServersResponse response)
	{
		client.execute(() -> {
			if(state == ServerFinderState.CANCELLED)
				return;
			
			if(response.isError())
			{
				state = ServerFinderState.ERROR;
				lastError = response.getError();
				enableInputs(true);
				searchButton.setMessage(Text.literal("Find Servers"));
				return;
			}
			
			List<ServersResponse.ServerEntry> servers = response.getData();
			if(servers != null)
			{
				for(ServersResponse.ServerEntry server : servers)
				{
					if(state == ServerFinderState.CANCELLED)
						break;
					
					addServerToList(server);
					serversFound++;
				}
			}
			
			state = ServerFinderState.DONE;
			enableInputs(true);
			searchButton.setMessage(Text.literal("Find Servers"));
		});
	}
	
	private Void handleSearchError(Throwable throwable)
	{
		client.execute(() -> {
			if(state == ServerFinderState.CANCELLED)
				return;
			
			state = ServerFinderState.ERROR;
			lastError = throwable.getMessage();
			if(lastError == null || lastError.isEmpty())
				lastError = "Network error occurred";
			
			enableInputs(true);
			searchButton.setMessage(Text.literal("Find Servers"));
		});
		return null;
	}
	
	private Integer parseInteger(String str)
	{
		try
		{
			return Integer.parseInt(str);
		}catch(NumberFormatException e)
		{
			return null;
		}
	}
	
	// Basically what MultiplayerScreen.addEntry() does,
	// but without changing the current screen.
	private void addServerToList(ServersResponse.ServerEntry server)
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
		// Enable search button if at least one search parameter is provided
		boolean hasSearchCriteria = !motdBox.getText().trim().isEmpty()
			|| !minPlayersBox.getText().trim().isEmpty()
			|| !maxPlayersBox.getText().trim().isEmpty()
			|| crackedBox.isChecked();
		
		searchButton.active = hasSearchCriteria || !state.isRunning();
	}
	
	@Override
	public boolean keyPressed(KeyInput context)
	{
		if(context.key() == GLFW.GLFW_KEY_ENTER)
			searchButton.onPress(context);
		
		return super.keyPressed(context);
	}
	
	@Override
	public boolean mouseClicked(Click context, boolean doubleClick)
	{
		if(context.button() == GLFW.GLFW_MOUSE_BUTTON_4)
		{
			close();
			return true;
		}
		
		return super.mouseClicked(context, doubleClick);
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY,
		float partialTicks)
	{
		context.drawCenteredTextWithShadow(textRenderer, "ServerSeeker",
			width / 2, 20, Colors.WHITE);
		context.drawCenteredTextWithShadow(textRenderer,
			"Search through millions of Minecraft servers", width / 2, 40,
			Colors.LIGHT_GRAY);
		context.drawCenteredTextWithShadow(textRenderer,
			"Found servers will be added to your server list", width / 2, 50,
			Colors.LIGHT_GRAY);
		
		context.drawTextWithShadow(textRenderer, "Server description/MOTD:",
			width / 2 - 100, height / 4 + 24, Colors.LIGHT_GRAY);
		motdBox.render(context, mouseX, mouseY, partialTicks);
		
		context.drawTextWithShadow(textRenderer, "Online players:",
			width / 2 - 150, height / 4 + 58, Colors.LIGHT_GRAY);
		minPlayersBox.render(context, mouseX, mouseY, partialTicks);
		context.drawTextWithShadow(textRenderer, "to", width / 2 - 10,
			height / 4 + 73, Colors.LIGHT_GRAY);
		maxPlayersBox.render(context, mouseX, mouseY, partialTicks);
		
		context.drawCenteredTextWithShadow(textRenderer, state.toString(),
			width / 2, height / 4 + 100, Colors.LIGHT_GRAY);
		
		if(state == ServerFinderState.DONE)
			context.drawCenteredTextWithShadow(textRenderer,
				"Found " + serversFound + " servers", width / 2,
				height / 4 + 110, Colors.LIGHT_GRAY);
		else if(state == ServerFinderState.ERROR && lastError != null)
			context.drawCenteredTextWithShadow(textRenderer,
				"Error: " + lastError, width / 2, height / 4 + 110, Colors.RED);
		
		for(Drawable drawable : drawables)
			drawable.render(context, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void close()
	{
		state = ServerFinderState.CANCELLED;
		client.setScreen(prevScreen);
	}
	
	enum ServerFinderState
	{
		NOT_RUNNING(""),
		SEARCHING("\u00a72Searching..."),
		CANCELLED("\u00a74Cancelled!"),
		DONE("\u00a72Search completed!"),
		ERROR("\u00a74Search failed!");
		
		private final String name;
		
		private ServerFinderState(String name)
		{
			this.name = name;
		}
		
		public boolean isRunning()
		{
			return this == SEARCHING;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
