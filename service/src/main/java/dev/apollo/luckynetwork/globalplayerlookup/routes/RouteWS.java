package dev.apollo.luckynetwork.globalplayerlookup.routes;

import dev.apollo.luckynetwork.globalplayerlookup.authentication.Role;
import dev.apollo.luckynetwork.globalplayerlookup.handlers.ws.WebSocket;
import io.javalin.Javalin;

public class RouteWS {

    public static void registerRoute(Javalin web) {
        web.ws("/ws", ws -> {
            new WebSocket().handle(ws);
        }, Role.WEBSOCKET);
    }

}
