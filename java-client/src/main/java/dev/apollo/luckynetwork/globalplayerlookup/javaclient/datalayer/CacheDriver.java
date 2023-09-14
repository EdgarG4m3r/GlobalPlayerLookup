package dev.apollo.luckynetwork.globalplayerlookup.javaclient.datalayer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import dev.apollo.luckynetwork.globalplayerlookup.commons.objects.LookedPlayer;
import dev.apollo.luckynetwork.globalplayerlookup.javaclient.GlobalPlayerLookupClient;
import dev.apollo.luckynetwork.globalplayerlookup.javaclient.datalayer.cache.CacheInvalidator;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class CacheDriver {

    private Cache<UUID, Optional<LookedPlayer>> playerCache;
    private Cache<String, Optional<UUID>> nameCache;
    private CacheInvalidator cacheInvalidator;

    private GlobalPlayerLookupClient instance;

    public CacheDriver(GlobalPlayerLookupClient instance)
    {
        this.instance = instance;
        playerCache = CacheBuilder.newBuilder()
                .maximumSize(instance.getConfig().getCacheConfig().getMaxCacheSize()) // This should depend on memory and expected player count within the expiration time. LuckyNetwork handles more than 90000 players login per day, so 22000 is a good number.
                .expireAfterWrite(instance.getConfig().getCacheConfig().getCacheExpireTime(), instance.getConfig().getCacheConfig().getCacheExpireTimeUnit()) // Depends on average player's playtime. The majority of players in LuckyNetwork play for 1 hours, but gamemodes such as OneBlock, and Mix average playtime is more than 6 hours.
                .concurrencyLevel(instance.getConfig().getCacheConfig().getConcurrentRequests())
                .removalListener((RemovalListener<UUID, Optional<LookedPlayer>>) notification -> {
                    if (notification.getValue().isPresent())
                    {
                        nameCache.invalidate(notification.getValue().get().name());
                    }
                })
                .build();

        nameCache = CacheBuilder.newBuilder()
                .maximumSize(instance.getConfig().getCacheConfig().getMaxCacheSize())
                .expireAfterWrite(instance.getConfig().getCacheConfig().getCacheExpireTime(), instance.getConfig().getCacheConfig().getCacheExpireTimeUnit())
                .concurrencyLevel(instance.getConfig().getCacheConfig().getConcurrentRequests())
                .build();

        cacheInvalidator = new CacheInvalidator(this);
    }

    public @Nullable Optional<LookedPlayer> getPlayer(UUID uuid)
    {
        return playerCache.getIfPresent(uuid);
    }

    public @Nullable Optional<LookedPlayer> getPlayer(String name)
    {
        Optional<UUID> uuid = nameCache.getIfPresent(name);
        if (uuid != null)
        {
            if (uuid.isPresent())
            {
                return playerCache.getIfPresent(uuid.get());
            }
            else
            {
                return Optional.empty();
            }
        }
        return null;

    }

    public Optional<UUID> getPlayerUUID(String name) {
        return nameCache.getIfPresent(name);
    }

    public Cache<String, Optional<UUID>> getNameCache() {
        return nameCache;
    }

    public Cache<UUID, Optional<LookedPlayer>> getPlayerCache() {
        return playerCache;
    }

    public GlobalPlayerLookupClient getInstance() {
        return instance;
    }

    public void shutdown()
    {
        this.cacheInvalidator.shutdown();
        nameCache.invalidateAll();
        playerCache.invalidateAll();
    }
}
