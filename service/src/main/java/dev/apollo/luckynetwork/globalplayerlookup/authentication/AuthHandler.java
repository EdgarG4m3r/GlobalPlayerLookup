package dev.apollo.luckynetwork.globalplayerlookup.authentication;

import dev.apollo.luckynetwork.globalplayerlookup.GlobalPlayerLookupService;
import dev.apollo.luckynetwork.globalplayerlookup.response.StandarizedResponses;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.security.AccessManager;
import io.javalin.security.RouteRole;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class AuthHandler implements AccessManager {

    public AuthHandler() {
        if (GlobalPlayerLookupService.instance.getConfiguration().webserver_api_key.equals("do-not-use-this-key"))
        {
            GlobalPlayerLookupService.getLogger().error("Please change the API key in the config file! The server will now exit.");
            System.exit(0);
        }
    }

    public Role auth(Context ctx)
    {
        if (ctx.header("Authorization") != null)
        {
            String apiKey = ctx.header("Authorization").replace("Bearer ", "");
            if (apiKey.equals(GlobalPlayerLookupService.instance.getConfiguration().webserver_api_key))
            {
                return Role.AUTHENTICATED;
            }
            else
            {
                return Role.UNAUTHENTICATED;
            }
        }
        return Role.UNKNOWN;
    }

    @Override
    public void manage(@NotNull Handler handler, @NotNull Context context, @NotNull Set<? extends RouteRole> set) throws Exception {
        context.header("Server", GlobalPlayerLookupService.instance.getConfiguration().webserver_http_server_name);

        if (set.contains(Role.WEBSOCKET))
        {
            handler.handle(context);
            return;
        }

        Role userRole = auth(context);
        if (set.contains(userRole))
        {
            handler.handle(context);
        }
        if (userRole == Role.UNKNOWN)
        {
            StandarizedResponses.authenticationRequired(context);
        }
        else //If no role is set in routes, we failclose by denying access
        {
            StandarizedResponses.authorizationFailure(context);
        }
    }
}
