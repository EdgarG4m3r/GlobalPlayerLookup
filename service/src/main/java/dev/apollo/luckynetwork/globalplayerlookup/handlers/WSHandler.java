package dev.apollo.luckynetwork.globalplayerlookup.handlers;

import io.javalin.websocket.WsConfig;

public interface WSHandler {
    void handle(WsConfig ws);
}
