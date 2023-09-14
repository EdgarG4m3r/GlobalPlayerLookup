package dev.apollo.luckynetwork.globalplayerlookup.routes;

import dev.apollo.luckynetwork.globalplayerlookup.response.StandarizedResponses;
import io.javalin.Javalin;

public class RouteDEFAULT {

    public static void registerRoute(Javalin web) {
        web.get("*", ctx -> {
            StandarizedResponses.methodNotAllowed(ctx, "Method not exist!");
        });
        web.post("*", ctx -> {
            StandarizedResponses.methodNotAllowed(ctx, "Method not exist!");
        });
        web.delete("*", ctx -> {
            StandarizedResponses.methodNotAllowed(ctx, "Method not exist!");
        });
        web.patch("*", ctx -> {
            StandarizedResponses.methodNotAllowed(ctx, "Method not exist!");
        });
        web.put("*", ctx -> {
            StandarizedResponses.methodNotAllowed(ctx, "Method not exist!");
        });
        web.options("*", ctx -> {
            StandarizedResponses.methodNotAllowed(ctx, "Method not exist!");
        });
        web.head("*", ctx -> {
            StandarizedResponses.methodNotAllowed(ctx, "Method not exist!");
        });
    }
}
