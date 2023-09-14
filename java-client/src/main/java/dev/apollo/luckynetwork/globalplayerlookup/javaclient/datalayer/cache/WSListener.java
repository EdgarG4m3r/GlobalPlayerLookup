package dev.apollo.luckynetwork.globalplayerlookup.javaclient.datalayer.cache;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import java.util.UUID;

public class WSListener extends WebSocketListener {

    private CacheInvalidator cacheInvalidator;

    public WSListener(CacheInvalidator cacheInvalidator)
    {
        this.cacheInvalidator = cacheInvalidator;
    }


    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        cacheInvalidator.onOpen(webSocket, response);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        if (text.startsWith("invalidate:"))
        {
            //invalidate:" + uuid.toString() + ":" + name
            String[] split = text.split(":");
            if (split.length == 3)
            {
                try
                {
                    UUID uuid = UUID.fromString(split[1]);
                    this.cacheInvalidator.getCacheDriver().getPlayerCache().invalidate(uuid);
                }
                catch (Exception e)
                {
                    //ignore
                }
                String name = split[2];
                this.cacheInvalidator.getCacheDriver().getNameCache().invalidate(name);
            }
        }
        if (text.startsWith("invalidateall"))
        {
            this.cacheInvalidator.getCacheDriver().getPlayerCache().invalidateAll();
            this.cacheInvalidator.getCacheDriver().getNameCache().invalidateAll();
        }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        cacheInvalidator.onClosing(webSocket, code, reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        cacheInvalidator.onFailure(webSocket, t, response);
    }

}
