package dev.apollo.luckynetwork.globalplayerlookup.datalayer;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import dev.apollo.luckynetwork.globalplayerlookup.GlobalPlayerLookupService;
import dev.apollo.luckynetwork.globalplayerlookup.commons.objects.LookedPlayer;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CacheDriver {

    private Cache<UUID, Optional<LookedPlayer>> playerCache;
    private Cache<String, Optional<UUID>> nameCache = CacheBuilder.newBuilder()
            .maximumSize(22000)
            .expireAfterWrite(6, TimeUnit.HOURS)
            .concurrencyLevel(Runtime.getRuntime().availableProcessors())
            .build();

    public CacheDriver(GlobalPlayerLookupService instance)
    {
        playerCache = CacheBuilder.newBuilder()
                .maximumSize(instance.getCacheConfig().getMaxCacheSize()) // This should depend on memory and expected player count within the expiration time. LuckyNetwork handles more than 90000 players login per day, so 22000 is a good number.
                .expireAfterWrite(instance.getCacheConfig().getCacheExpireTime(), instance.getCacheConfig().getCacheExpireTimeUnit()) // Depends on average player's playtime. The majority of players in LuckyNetwork play for 1 hours, but gamemodes such as OneBlock, and Mix average playtime is more than 6 hours.
                .concurrencyLevel(instance.getCacheConfig().getConcurrentRequests())
                .removalListener((RemovalListener<UUID, Optional<LookedPlayer>>) notification -> {
                    if (notification.getValue().isPresent())
                    {
                        nameCache.invalidate(notification.getValue().get().name());
                    }
                })
                .build();

        nameCache = CacheBuilder.newBuilder()
                .maximumSize(instance.getCacheConfig().getMaxCacheSize())
                .expireAfterWrite(instance.getCacheConfig().getCacheExpireTime(), instance.getCacheConfig().getCacheExpireTimeUnit())
                .concurrencyLevel(instance.getCacheConfig().getConcurrentRequests())
                .build();
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
}
