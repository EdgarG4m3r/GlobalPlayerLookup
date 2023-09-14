package dev.apollo.luckynetwork.globalplayerlookup.routes;

import dev.apollo.luckynetwork.globalplayerlookup.authentication.Role;
import dev.apollo.luckynetwork.globalplayerlookup.handlers.get.v1.Players;
import io.javalin.Javalin;

public class RouteGET {

    public static void registerRoute(Javalin web) {
        web.get("/players/{query}", ctx -> {
            new Players().handle(ctx);
        }, Role.AUTHENTICATED);
    }
}
