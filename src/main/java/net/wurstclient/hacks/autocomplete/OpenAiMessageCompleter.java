/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks.autocomplete;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.json.WsonObject;

public final class OpenAiMessageCompleter extends MessageCompleter
{
	public OpenAiMessageCompleter(ModelSettings modelSettings)
	{
		super(modelSettings);
	}

	@Override
	protected JsonObject buildParams(String prompt, int maxSuggestions)
	{
		// build the request parameters
		JsonObject params = new JsonObject();

		// determine model name and type
		boolean customModel = !modelSettings.customModel.getValue().isBlank();
		String modelName =
			customModel ? modelSettings.customModel.getValue() : "gpt-4o-mini"; // default
																				// model
																				// when
																				// no
																				// custom
																				// model
																				// is
																				// set
		boolean chatModel = customModel
			? modelSettings.customModelType.getSelected().isChat() : true; // default
																			// to
																			// chat
																			// model

		// add the model name
		params.addProperty("model", modelName);

		// add the prompt, depending on model type
		if(chatModel)
		{
			JsonArray messages = new JsonArray();
			JsonObject systemMessage = new JsonObject();
			systemMessage.addProperty("role", "system");
			systemMessage.addProperty("content",
				"Complete the following text. Reply only with the completion."
					+ " You are not an assistant.");
			messages.add(systemMessage);
			JsonObject promptMessage = new JsonObject();
			promptMessage.addProperty("role", "user");
			promptMessage.addProperty("content", prompt);
			messages.add(promptMessage);
			params.add("messages", messages);

		}else
		{
			params.addProperty("prompt", prompt);
		}

		// add parameters (some APIs might not support all of these)
		params.addProperty("max_tokens", modelSettings.maxTokens.getValueI());
		params.addProperty("temperature", 0.7); // default temperature
		params.addProperty("top_p", modelSettings.topP.getValue());
		params.addProperty("n", maxSuggestions);

		// these parameters might not be supported by all APIs
		if(modelSettings.presencePenalty.getValue() != 0)
			params.addProperty("presence_penalty",
				modelSettings.presencePenalty.getValue());
		if(modelSettings.frequencyPenalty.getValue() != 0)
			params.addProperty("frequency_penalty",
				modelSettings.frequencyPenalty.getValue());

		// stop sequence (some APIs use "stop", others might use
		// "stop_sequences")
		String stopSeq = modelSettings.stopSequence.getSelected().getSequence();
		params.addProperty("stop", stopSeq);

		return params;
	}

	@Override
	protected WsonObject requestCompletions(JsonObject parameters)
		throws IOException, JsonException
	{
		// determine if using custom model and endpoint
		boolean customModel = !modelSettings.customModel.getValue().isBlank();
		boolean chatModel = customModel
			? modelSettings.customModelType.getSelected().isChat() : true; // default
																			// to
																			// chat
																			// model

		// get the API URL
		URL url =
			URI.create(chatModel ? modelSettings.openaiChatEndpoint.getValue()
				: modelSettings.openaiLegacyEndpoint.getValue()).toURL();

		// set up the API request
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");

		// determine auth token to use
		String authToken = getAuthToken();
		if(authToken != null && !authToken.isBlank())
			conn.setRequestProperty("Authorization", authToken);

		// debug logging for custom endpoints
		if(customModel)
		{
			System.out.println("[AutoComplete] Custom endpoint request:");
			System.out.println("URL: " + url);
			System.out.println(
				"Auth: " + (authToken != null ? "***SET***" : "***NOT_SET***"));
			System.out
				.println("Request body: " + JsonUtils.GSON.toJson(parameters));
		}

		// set the request body
		conn.setDoOutput(true);
		try(OutputStream os = conn.getOutputStream())
		{
			os.write(JsonUtils.GSON.toJson(parameters).getBytes());
			os.flush();
		}

		// check response code and provide better error handling
		int responseCode = conn.getResponseCode();
		if(responseCode != 200)
		{
			// read error response
			String errorResponse = "";
			try
			{
				if(conn.getErrorStream() != null)
				{
					errorResponse =
						new String(conn.getErrorStream().readAllBytes());
				}
			}catch(Exception e)
			{
				// ignore if we can't read error stream
			}

			String errorMsg = String.format(
				"API request failed with HTTP %d for URL: %s\nError response: %s",
				responseCode, url, errorResponse);

			if(customModel)
			{
				System.err.println("[AutoComplete] " + errorMsg);
				System.err.println(
					"[AutoComplete] This custom endpoint might not be OpenAI-compatible.");
				System.err.println(
					"[AutoComplete] Check the API documentation for required parameters.");
			}

			throw new IOException(errorMsg);
		}

		// parse the response
		return JsonUtils.parseConnectionToObject(conn);
	}

	@Override
	protected String[] extractCompletions(WsonObject response)
		throws JsonException
	{
		ArrayList<String> completions = new ArrayList<>();

		// extract choices from response
		ArrayList<WsonObject> choices =
			response.getArray("choices").getAllObjects();

		// extract completions from choices
		// determine if using custom model
		boolean customModel = !modelSettings.customModel.getValue().isBlank();
		boolean chatModel = customModel
			? modelSettings.customModelType.getSelected().isChat() : true; // default
																			// to
																			// chat
																			// model

		if(chatModel)
			for(WsonObject choice : choices)
			{
				WsonObject message = choice.getObject("message");
				String content = message.getString("content");
				completions.add(content);
			}
		else
			for(WsonObject choice : choices)
				completions.add(choice.getString("text"));

		// remove newlines
		for(String completion : completions)
			completion = completion.replace("\n", " ");

		return completions.toArray(new String[completions.size()]);
	}

	private String getAuthToken()
	{
		// try custom auth token first
		String customToken = modelSettings.customAuthToken.getValue().trim();
		if(!customToken.isBlank())
		{
			if(modelSettings.useBearerToken.isChecked())
			{
				// add Bearer prefix if not already present
				if(!customToken.toLowerCase().startsWith("bearer "))
					return "Bearer " + customToken;
				return customToken;
			}else
			{
				// use plain token, remove Bearer prefix if present
				if(customToken.toLowerCase().startsWith("bearer "))
					return customToken.substring(7);
				return customToken;
			}
		}

		// fallback to environment variable (always uses Bearer for
		// compatibility)
		String envToken = System.getenv("WURST_OPENAI_KEY");
		if(envToken != null && !envToken.isBlank())
			return "Bearer " + envToken;

		return null;
	}
}
