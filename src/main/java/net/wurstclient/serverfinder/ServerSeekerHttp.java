/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.serverfinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

public class ServerSeekerHttp
{
	private static final String API_BASE_URL = "https://api.serverseeker.net";
	private static final String API_KEY = "ZzOluD4Uj0TPrRPZuE94UtBuIVjYxNMt";
	private static final Gson GSON = new Gson();
	
	public static <T> T postJson(String endpoint, Object request,
		Class<T> responseClass) throws IOException
	{
		String url = API_BASE_URL + endpoint;
		HttpURLConnection connection =
			(HttpURLConnection)URI.create(url).toURL().openConnection();
		
		try
		{
			// Setup request
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("User-Agent",
				"WurstClient-ServerSeeker");
			connection.setDoOutput(true);
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(10000);
			
			// Send request body
			String json = GSON.toJson(request);
			try(OutputStream os = connection.getOutputStream())
			{
				byte[] input = json.getBytes("utf-8");
				os.write(input, 0, input.length);
			}
			
			// Read response
			int responseCode = connection.getResponseCode();
			if(responseCode != 200)
				throw new IOException("HTTP " + responseCode + " response");
			
			try(BufferedReader br = new BufferedReader(
				new InputStreamReader(connection.getInputStream(), "utf-8")))
			{
				StringBuilder response = new StringBuilder();
				String responseLine;
				while((responseLine = br.readLine()) != null)
					response.append(responseLine.trim());
				
				return GSON.fromJson(response.toString(), responseClass);
			}
		}catch(JsonParseException e)
		{
			throw new IOException("Failed to parse JSON response", e);
		}finally
		{
			connection.disconnect();
		}
	}
	
	public static String getApiKey()
	{
		return API_KEY;
	}
}