package dev.apollo.luckynetwork.globalplayerlookup.javaclient.datalayer;

import dev.apollo.luckynetwork.globalplayerlookup.commons.exceptions.APIErrorException;
import dev.apollo.luckynetwork.globalplayerlookup.commons.exceptions.ClientErrorException;
import dev.apollo.luckynetwork.globalplayerlookup.commons.exceptions.ProxyErrorException;
import dev.apollo.luckynetwork.globalplayerlookup.commons.objects.LookedPlayer;
import dev.apollo.luckynetwork.globalplayerlookup.javaclient.GlobalPlayerLookupClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class GlobalLookupDriver {

    private GlobalPlayerLookupClient instance;
    private OkHttpClient client;

    public GlobalLookupDriver(GlobalPlayerLookupClient instance) {
        this.instance = instance;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public GlobalLookupDriver(GlobalPlayerLookupClient instance, OkHttpClient client) {
        this.instance = instance;
        this.client = client;
    }

    public Optional<LookedPlayer> getPlayer(String query) throws ClientErrorException, ProxyErrorException, APIErrorException
    {
        String fullURL = instance.getConfig().getUrl() + "/player/" + query;

        //okhttp
        Request request = new Request.Builder()
                .url(fullURL)
                .header("Authorization", instance.getConfig().getApiKey())
                .header("Username", instance.getConfig().getUsername())
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .build();

        try (Response response = client.newCall(request).execute()) {
            JSONParser parser = new JSONParser();
            JSONObject responseJSON;
            switch (response.code())
            {
                case 200:
                    try
                    {
                        responseJSON = (JSONObject) parser.parse(response.body().string());
                        JSONObject playerJSON = (JSONObject) responseJSON.get("player");
                        LookedPlayer player = LookedPlayer.fromJSON(playerJSON);
                        return Optional.of(player);
                    }
                    catch (ParseException e)
                    {
                        throw new APIErrorException("The API sent an invalid response.");
                    }
                    catch (IllegalArgumentException e)
                    {
                        throw new APIErrorException("The API sent an invalid response because we can't parse the player data.");
                    }
                case 404:
                    return Optional.empty();
                case 500:
                    try
                    {
                        responseJSON = (JSONObject) parser.parse(response.body().string());
                        String exception = (String) responseJSON.get("message");
                        String information = (String) responseJSON.get("friendlyMessage");
                        if (exception.startsWith("PROXY_"))
                        {
                            throw new ProxyErrorException(exception + " : " + information);
                        }
                        else
                        {
                            throw new APIErrorException("The API encountered an internal error : " + information);
                        }
                    }
                    catch (ParseException e)
                    {
                        throw new APIErrorException("The API encountered an internal error and client was unable to parse the response.");
                    }
                case 401:
                case 403:
                    throw new ClientErrorException("The API key is invalid.");
                case 429:
                    throw new ClientErrorException("The client has made too many requests.");
                default:
                    throw new ClientErrorException("The client encountered unexpected code");
            }
        } catch (IOException e) {
            throw new ClientErrorException("The client encountered an error while making the request : " + e.getMessage());
        }
    }

    public void shutdown()
    {
        this.client.dispatcher().executorService().shutdown();
        this.client.connectionPool().evictAll();
    }
}
