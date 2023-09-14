package dev.apollo.luckynetwork.globalplayerlookup.javaclient.repository;

import dev.apollo.luckynetwork.globalplayerlookup.commons.exceptions.APIErrorException;
import dev.apollo.luckynetwork.globalplayerlookup.commons.exceptions.ClientErrorException;
import dev.apollo.luckynetwork.globalplayerlookup.commons.exceptions.ProxyErrorException;
import dev.apollo.luckynetwork.globalplayerlookup.commons.objects.LookedPlayer;
import dev.apollo.luckynetwork.globalplayerlookup.javaclient.GlobalPlayerLookupClient;
import dev.apollo.luckynetwork.globalplayerlookup.javaclient.datalayer.CacheDriver;
import dev.apollo.luckynetwork.globalplayerlookup.javaclient.datalayer.GlobalLookupDriver;

import java.util.Optional;
import java.util.UUID;

public class PlayerRepository {

    private GlobalPlayerLookupClient instance;
    private CacheDriver cacheDriver;
    private GlobalLookupDriver globalLookupDriver;

    public PlayerRepository(GlobalPlayerLookupClient instance) {
        this.instance = instance;
        if (instance.getConfig().getCacheConfig() != null)
        {
            cacheDriver = new CacheDriver(instance);
        }
        globalLookupDriver = new GlobalLookupDriver(instance);
    }

    public Optional<LookedPlayer> getPlayer(UUID uuid) throws ClientErrorException, APIErrorException, ProxyErrorException
    {
        Optional<LookedPlayer> player;
        if (this.cacheDriver != null)
        {
            player = this.cacheDriver.getPlayer(uuid);
            if (player.isPresent())
            {
                return player;
            }
        }

        return globalLookupDriver.getPlayer(uuid.toString());
    }

    public Optional<LookedPlayer> getPlayer(String name) throws ClientErrorException, APIErrorException, ProxyErrorException
    {
        if (this.cacheDriver != null)
        {
            Optional<UUID> uuidOptional = this.cacheDriver.getPlayerUUID(name);
            if (uuidOptional.isPresent())
            {
                return this.getPlayer(uuidOptional.get());
            }
            else
            {
                return Optional.empty();
            }
        }

        return globalLookupDriver.getPlayer(name);
    }

    public GlobalPlayerLookupClient getInstance() {
        return instance;
    }

    public void shutdown()
    {
        if (this.cacheDriver != null)
        {
            this.cacheDriver.shutdown();
        }
        this.globalLookupDriver.shutdown();
    }
}
