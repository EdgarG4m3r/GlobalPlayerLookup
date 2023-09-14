package dev.apollo.luckynetwork.globalplayerlookup.handlers.get.v1;

import dev.apollo.luckynetwork.globalplayerlookup.GlobalPlayerLookupService;
import dev.apollo.luckynetwork.globalplayerlookup.commons.exceptions.APIErrorException;
import dev.apollo.luckynetwork.globalplayerlookup.commons.exceptions.ProxyErrorException;
import dev.apollo.luckynetwork.globalplayerlookup.commons.objects.LookedPlayer;
import dev.apollo.luckynetwork.globalplayerlookup.handlers.HTTPHandler;
import dev.apollo.luckynetwork.globalplayerlookup.response.InputFilter;
import dev.apollo.luckynetwork.globalplayerlookup.response.ParamField;
import dev.apollo.luckynetwork.globalplayerlookup.response.StandarizedResponses;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class Players implements HTTPHandler {
    @Override
    public void handle(Context context) {
        try
        {
            String query = context.pathParam("query");
            UUID uuid = fastUUID(query);
            if (uuid == null) {
                InputFilter.validateNickname("query", ParamField.PATH, context);

                if (context.attribute("hasErrors") != null) {
                    StandarizedResponses.invalidParameter(context);
                    return;
                }

                handlePlayer(context, GlobalPlayerLookupService.instance.getPlayerRepository().getPlayer(query));
            }
            else
            {
                handlePlayer(context, GlobalPlayerLookupService.instance.getPlayerRepository().getPlayer(uuid));
            }

        }
        catch (SQLException e)
        {
            StandarizedResponses.generalFailure(context, 500, "SQL_ERROR", "SQL error occurred while fetching player data");
            GlobalPlayerLookupService.getLogger().error("SQL error occurred while fetching player data", e);
        }
        catch (APIErrorException e)
        {
            StandarizedResponses.generalFailure(context, 500, "API_ERROR", "API error occurred while fetching player data");
            GlobalPlayerLookupService.getLogger().error("API error occurred while fetching player data", e);
        }
        catch (ProxyErrorException e)
        {
            StandarizedResponses.generalFailure(context, 500, "PROXY_ERROR", "Proxy error occurred while fetching player data");
            GlobalPlayerLookupService.getLogger().error("Proxy error occurred while fetching player data", e);

        }
        catch (RuntimeException e)
        {
            StandarizedResponses.generalFailure(context, 500, "RUNTIME_EXCEPTION", "Runtime error occurred while fetching player data");
            GlobalPlayerLookupService.getLogger().error("Runtime error occurred while fetching player data", e);
        }
    }

    private UUID fastUUID (String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void handlePlayer (Context context, Optional<LookedPlayer> playerOptional)
    {
        if (playerOptional.isPresent())
        {
            LookedPlayer player = playerOptional.get();
            StandarizedResponses.success(context, "SUCCESS", "Successfully retrieved player data", "player", player.toJSON());
        }
        else
        {
            StandarizedResponses.generalFailure(context, 404, "PLAYER_NOT_FOUND", "Player not found");
        }
    }

}
