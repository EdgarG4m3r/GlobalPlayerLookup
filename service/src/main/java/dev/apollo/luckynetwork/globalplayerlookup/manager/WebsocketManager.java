package dev.apollo.luckynetwork.globalplayerlookup.manager;

import io.javalin.websocket.WsContext;

import java.util.concurrent.ConcurrentHashMap;

public class WebsocketManager {

    private ConcurrentHashMap<String, WsContext> connections = new ConcurrentHashMap<>();

    public void addConnection(WsContext ctx) {
        connections.put(ctx.getSessionId(), ctx);
    }

    public void removeConnection(WsContext ctx) {
        WsContext context = connections.get(ctx.getSessionId());
        if (context != null) {
            context.closeSession();
            connections.remove(context.getSessionId());
        }
    }

    public void removeConnection(String sessionId) {
        WsContext ctx = connections.get(sessionId);
        if (ctx != null) {
            ctx.closeSession();
            connections.remove(ctx.getSessionId());
        }
    }

    public void removeConnection() {
        connections.forEach((sessionId, ctx) -> {
            ctx.closeSession();
            connections.remove(sessionId);
        });
    }

    public void sendMessage(String sessionId, String message) {
        WsContext ctx = connections.get(sessionId);
        if (ctx != null) {
            ctx.send(message);
        }
    }

    public void sendMessageToAll(String message) {
        connections.forEach((sessionId, ctx) -> {
            ctx.send(message);
        });
    }

}
