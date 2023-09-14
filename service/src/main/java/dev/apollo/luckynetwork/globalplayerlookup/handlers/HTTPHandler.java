package dev.apollo.luckynetwork.globalplayerlookup.handlers;

import io.javalin.http.Context;

public interface HTTPHandler {
    void handle(Context context);
}
