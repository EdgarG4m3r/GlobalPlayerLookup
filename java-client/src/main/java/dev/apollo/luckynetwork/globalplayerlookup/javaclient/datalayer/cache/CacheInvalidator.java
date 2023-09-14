package dev.apollo.luckynetwork.globalplayerlookup.javaclient.datalayer.cache;

import dev.apollo.luckynetwork.globalplayerlookup.javaclient.datalayer.CacheDriver;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CacheInvalidator {

    private OkHttpClient httpClient;
    private CacheDriver cacheDriver;
    private boolean connected = false;
    private boolean shouldReconnect = true;
    private WebSocket webSocket;
    private long delayBeforeReconnect = 1L; // initial delay in seconds
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public CacheInvalidator(CacheDriver cacheDriver)
    {
        this.cacheDriver = cacheDriver;
        this.httpClient = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .pingInterval(10, TimeUnit.SECONDS)
                .callTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        startWebSocket();
    }

    public CacheInvalidator(CacheDriver cacheDriver, OkHttpClient httpClient)
    {
        this.cacheDriver = cacheDriver;
        this.httpClient = httpClient;
        startWebSocket();
    }

    public void startWebSocket()
    {
        if (webSocket != null)
        {
            shouldReconnect = false;
            connected = false;
            webSocket.close(1000, "Reconnecting");
        }

        // delay before executing startWebSocket task
        scheduler.schedule(() -> {
            Request request = new Request.Builder()
                    .url("wss:// " + cacheDriver.getInstance().getConfig().getUrl() + "/ws" + "?token=" + cacheDriver.getInstance().getConfig().getApiKey())
                    .build();

            WSListener wsListener = new WSListener(this);
            webSocket = httpClient.newWebSocket(request, wsListener);

        }, delayBeforeReconnect, TimeUnit.SECONDS);
    }

    public void shutdown()
    {
        this.shouldReconnect = false;
        this.connected = false;
        this.webSocket.close(1000, "Shutting down");
        this.httpClient.dispatcher().executorService().shutdown();
        this.httpClient.connectionPool().evictAll();
        this.scheduler.shutdown();
    }

    public CacheDriver getCacheDriver() {
        return cacheDriver;
    }

    public void onOpen(WebSocket webSocket, Response response)
    {
        this.connected = true;
        this.delayBeforeReconnect = 1L; // Reset the delay, as we successfully connected
    }

    public void onClosing(WebSocket webSocket, int code, String reason)
    {
        this.connected = false;
        if (shouldReconnect)
        {
            // In case of normal closure, no need for backoff, make an immediate reconnection attempt
            startWebSocket();
        }
    }

    public void onFailure(WebSocket webSocket, Throwable t, Response response)
    {
        this.connected = false;
        if (shouldReconnect)
        {
            delayBeforeReconnect = Math.min(delayBeforeReconnect * 2, 30);
            startWebSocket();
        }
    }

    public boolean isConnected()
    {
        return connected;
    }
}
