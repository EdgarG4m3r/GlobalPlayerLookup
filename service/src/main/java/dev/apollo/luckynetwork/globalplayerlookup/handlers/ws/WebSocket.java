package dev.apollo.luckynetwork.globalplayerlookup.handlers.ws;

import dev.apollo.luckynetwork.globalplayerlookup.GlobalPlayerLookupService;
import dev.apollo.luckynetwork.globalplayerlookup.handlers.WSHandler;
import io.javalin.websocket.WsConfig;

public class WebSocket implements WSHandler {

    @Override
    public void handle(WsConfig ws) {
        ws.onConnect(ctx -> {
            //get from token
            String token = ctx.queryParam("token");
            if (token == null) {
                ctx.send("Invalid token");
                ctx.session.close();
                return;
            }

            if (!token.equals(GlobalPlayerLookupService.instance.getConfiguration().webserver_api_key))
            {
                ctx.send("Invalid token");
                ctx.session.close();
                return;
            }

            GlobalPlayerLookupService.instance.getWebsocketManager().addConnection(ctx);
            ctx.send("Connected");

        });

        ws.onError(ctx -> {
            GlobalPlayerLookupService.instance.getWebsocketManager().removeConnection(ctx);
        });

        ws.onMessage(ctx -> {
            //ignore
        });

        ws.onClose(ctx -> {
            GlobalPlayerLookupService.instance.getWebsocketManager().removeConnection(ctx);
        });

        ws.onBinaryMessage(ctx -> {
           //ignore
        });
    }

}
