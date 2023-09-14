package dev.apollo.luckynetwork.globalplayerlookup.authentication;

import io.javalin.security.RouteRole;

public enum Role implements RouteRole {
    UNKNOWN, UNAUTHENTICATED, AUTHENTICATED, WEBSOCKET;
}
