package dev.apollo.luckynetwork.globalplayerlookup.datalayer;

import dev.apollo.luckynetwork.globalplayerlookup.GlobalPlayerLookupService;
import dev.apollo.luckynetwork.globalplayerlookup.commons.exceptions.APIErrorException;
import dev.apollo.luckynetwork.globalplayerlookup.commons.exceptions.ClientErrorException;
import dev.apollo.luckynetwork.globalplayerlookup.commons.exceptions.ProxyErrorException;
import dev.apollo.luckynetwork.globalplayerlookup.commons.objects.LookedPlayer;
import dev.apollo.luckynetwork.globalplayerlookup.javaclient.GlobalPlayerLookupClient;
import dev.apollo.luckynetwork.globalplayerlookup.javaclient.objects.ClientConfig;

import java.util.Optional;
import java.util.UUID;

public class GlobalLookupDriver {

    private GlobalPlayerLookupService instance;
    private GlobalPlayerLookupClient client;

    public GlobalLookupDriver(GlobalPlayerLookupService instance) {
        this.instance = instance;
        this.client = new GlobalPlayerLookupClient(new ClientConfig(
                instance.getConfiguration().globalplayerlookup_username,
                instance.getConfiguration().globalplayerlookup_token,
                instance.getConfiguration().globalplayerlookup_address,
                null //bypass local cache
        ));
    }

    public Optional<LookedPlayer> getPlayer(UUID uuid) throws ProxyErrorException, APIErrorException {
        try
        {
            return client.getPlayerRepository().getPlayer(uuid);
        }
        catch (ClientErrorException e)
        {
            throw new ProxyErrorException(e);
        }
    }

    public Optional<LookedPlayer> getPlayer(String name) throws ProxyErrorException, APIErrorException {
        try
        {
            return client.getPlayerRepository().getPlayer(name);
        }
        catch (ClientErrorException e)
        {
            throw new ProxyErrorException(e);
        }
    }
}
