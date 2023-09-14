package dev.apollo.luckynetwork.globalplayerlookup.repository;

import dev.apollo.luckynetwork.globalplayerlookup.GlobalPlayerLookupService;
import dev.apollo.luckynetwork.globalplayerlookup.commons.exceptions.APIErrorException;
import dev.apollo.luckynetwork.globalplayerlookup.commons.exceptions.ProxyErrorException;
import dev.apollo.luckynetwork.globalplayerlookup.datalayer.CacheDriver;
import dev.apollo.luckynetwork.globalplayerlookup.datalayer.GlobalLookupDriver;
import dev.apollo.luckynetwork.globalplayerlookup.datalayer.RedisDriver;
import dev.apollo.luckynetwork.globalplayerlookup.datalayer.SQLDriver;
import dev.apollo.luckynetwork.globalplayerlookup.commons.objects.LookedPlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisAccessControlException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerRepository {

    private GlobalPlayerLookupService instance;
    private CacheDriver cacheDriver;
    private RedisDriver redisDriver;
    private SQLDriver sqlDriver;
    private GlobalLookupDriver globalLookupDriver;

    public PlayerRepository(GlobalPlayerLookupService instance) {
        this.instance = instance;
        if (instance.getCacheConfig() != null)
        {
            cacheDriver = new CacheDriver(instance);
        }
        if (!instance.getConfiguration().instance_bypass_redis)
        {
            redisDriver = new RedisDriver();
        }
        if (instance.getConfiguration().instance_type.equalsIgnoreCase("proxy"))
        {
            globalLookupDriver = new GlobalLookupDriver(instance);
        }
        else
        {
            sqlDriver = new SQLDriver();
        }
    }

    public Optional<LookedPlayer> getPlayer(UUID uuid) throws SQLException, APIErrorException, ProxyErrorException {
        Optional<LookedPlayer> player;
        if (this.cacheDriver != null)
        {
            player = this.cacheDriver.getPlayer(uuid);
            if (player != null)
            {
                if (player.isPresent())
                {
                    return player;
                }
                else
                {
                    return Optional.empty();
                }
            }
        }

        if (redisDriver != null)
        {
            try (Jedis jedis = GlobalPlayerLookupService.instance.getRedis().getJedisPool().getResource())
            {
                Optional<LookedPlayer> playerRedis = redisDriver.getPlayer(jedis, uuid);
                if (playerRedis != null)
                {
                    if (playerRedis.isPresent())
                    {
                        if (cacheDriver != null)
                        {
                            this.cacheDriver.getPlayerCache().put(uuid, playerRedis);
                            this.cacheDriver.getNameCache().put(playerRedis.get().name(), Optional.of(uuid));
                        }
                        return playerRedis;
                    }
                    else
                    {
                        if (cacheDriver != null)
                        {
                            this.cacheDriver.getPlayerCache().put(uuid, playerRedis);
                        }
                        return playerRedis;
                    }
                }
            }
            catch (JedisAccessControlException e)
            {
                GlobalPlayerLookupService.getLogger().warn("Redis access control exception occurred while fetching player data", e);
            }
        }

        if (sqlDriver != null)
        {
            try (Connection connection = GlobalPlayerLookupService.instance.getMySQL().getConnection())
            {
                Optional<LookedPlayer> playerSQL = sqlDriver.getPlayer(connection, uuid);
                //asyncronously add player to cache
                CompletableFuture.runAsync(() -> {
                    if (redisDriver != null)
                    {
                        try (Jedis jedis = GlobalPlayerLookupService.instance.getRedis().getJedisPool().getResource())
                        {
                            if (playerSQL.isPresent())
                            {
                                jedis.setex("playerlookup-cache:" + uuid.toString(), 120, playerSQL.get().toJSON().toString());
                                jedis.setex("playerlookup-namecache:" + playerSQL.get().name(), 120, uuid.toString());
                            }
                            else
                            {
                                jedis.setex("playerlookup-cache:" + uuid.toString(), 120, "empty");
                            }
                        }
                        catch (JedisAccessControlException e)
                        {
                            GlobalPlayerLookupService.getLogger().warn("Redis access control exception occurred while caching player data", e);
                        }
                    }
                    if (cacheDriver != null)
                    {
                        if (playerSQL.isPresent())
                        {
                            cacheDriver.getPlayerCache().put(uuid, playerSQL);
                            cacheDriver.getNameCache().put(playerSQL.get().name(), Optional.of(uuid));
                        }
                        else
                        {
                            cacheDriver.getPlayerCache().put(uuid, Optional.empty());
                        }
                    }
                });
                return playerSQL;
            }
        }
        else
        {
            Optional<LookedPlayer> playerGlobal = globalLookupDriver.getPlayer(uuid);
            CompletableFuture.runAsync(() -> {
                if (redisDriver != null)
                {
                    try (Jedis jedis = GlobalPlayerLookupService.instance.getRedis().getJedisPool().getResource())
                    {
                        if (playerGlobal.isPresent())
                        {
                            jedis.setex("playerlookup-cache:" + uuid.toString(), 120, playerGlobal.get().toJSON().toString());
                            jedis.setex("playerlookup-namecache:" + playerGlobal.get().name(), 120, uuid.toString());
                        }
                        else
                        {
                            jedis.setex("playerlookup-cache:" + uuid.toString(), 120, "empty");
                        }
                    }
                    catch (JedisAccessControlException e)
                    {
                        GlobalPlayerLookupService.getLogger().warn("Redis access control exception occurred while caching player data", e);
                    }
                }
                if (cacheDriver != null)
                {
                    if (playerGlobal.isPresent())
                    {
                        cacheDriver.getPlayerCache().put(uuid, playerGlobal);
                        cacheDriver.getNameCache().put(playerGlobal.get().name(), Optional.of(uuid));
                    }
                    else
                    {
                        cacheDriver.getPlayerCache().put(uuid, playerGlobal);
                    }
                }
            });
            return globalLookupDriver.getPlayer(uuid);
        }
    }

    public Optional<LookedPlayer> getPlayer(String name) throws SQLException, APIErrorException, ProxyErrorException {
        Optional<UUID> uuid;
        if (cacheDriver != null)
        {
            uuid = cacheDriver.getPlayerUUID(name);
            if (uuid != null)
            {
                if (uuid.isPresent())
                {
                    return getPlayer(uuid.get());
                }
                else
                {
                    return Optional.empty();
                }
            }
        }

        if (redisDriver != null)
        {
            try (Jedis jedis = GlobalPlayerLookupService.instance.getRedis().getJedisPool().getResource())
            {
                Optional<LookedPlayer> playerRedis = redisDriver.getPlayer(jedis, name);
                if (playerRedis != null)
                {
                    if (playerRedis.isPresent())
                    {
                        if (cacheDriver != null)
                        {
                            cacheDriver.getNameCache().put(name, Optional.of(playerRedis.get().uuid()));
                        }
                        return getPlayer(playerRedis.get().uuid());
                    }
                    else
                    {
                        if (cacheDriver != null)
                        {
                            cacheDriver.getNameCache().put(name, Optional.empty());
                        }
                        return playerRedis;
                    }
                }
            }
            catch (JedisAccessControlException e)
            {
                GlobalPlayerLookupService.getLogger().warn("Redis access control exception occurred while fetching player data", e);
            }
        }

        if (sqlDriver != null)
        {
            try (Connection connection = GlobalPlayerLookupService.instance.getMySQL().getConnection()) {
                Optional<LookedPlayer> playerSQL = sqlDriver.getPlayer(connection, name);
                CompletableFuture.runAsync(() -> {
                    if (redisDriver != null) {
                        try (Jedis jedis = GlobalPlayerLookupService.instance.getRedis().getJedisPool().getResource()) {
                            if (playerSQL.isPresent()) {
                                UUID uuid2 = playerSQL.get().uuid();
                                jedis.setex("playerlookup-cache:" + uuid2.toString(), 120, playerSQL.get().toJSON().toString());
                                jedis.setex("playerlookup-namecache:" + name, 120, uuid2.toString());
                            } else {
                                jedis.setex("playerlookup-namecache:" + name, 120, "empty");
                            }
                        }
                        catch (JedisAccessControlException e)
                        {
                            GlobalPlayerLookupService.getLogger().warn("Redis access control exception occurred while caching player data", e);
                        }
                    }
                    if (cacheDriver != null) {
                        if (playerSQL.isPresent()) {
                            UUID uuid2 = playerSQL.get().uuid();
                            cacheDriver.getNameCache().put(name, Optional.of(uuid2));
                            cacheDriver.getPlayerCache().put(uuid2, playerSQL);
                        } else {
                            cacheDriver.getNameCache().put(name, Optional.empty());
                        }
                    }
                });
                return getPlayer(playerSQL.get().uuid());
            }
        }
        else
        {
            Optional<LookedPlayer> playerGlobal = globalLookupDriver.getPlayer(name);
            CompletableFuture.runAsync(() -> {
                if (redisDriver != null)
                {
                    try (Jedis jedis = GlobalPlayerLookupService.instance.getRedis().getJedisPool().getResource())
                    {
                        if (playerGlobal.isPresent())
                        {
                            UUID uuid2 = playerGlobal.get().uuid();
                            jedis.setex("playerlookup-cache:" + uuid2.toString(), 120, playerGlobal.get().toJSON().toString());
                            jedis.setex("playerlookup-namecache:" + name, 120, uuid2.toString());
                        }
                        else
                        {
                            jedis.setex("playerlookup-namecache:" + name, 120, "empty");
                        }
                    }
                    catch (JedisAccessControlException e)
                    {
                        GlobalPlayerLookupService.getLogger().warn("Redis access control exception occurred while caching player data", e);
                    }
                }
                if (cacheDriver != null)
                {
                    if (playerGlobal.isPresent())
                    {
                        UUID uuid2 = playerGlobal.get().uuid();
                        cacheDriver.getNameCache().put(name, Optional.of(uuid2));
                        cacheDriver.getPlayerCache().put(uuid2, playerGlobal);
                    }
                    else
                    {
                        cacheDriver.getNameCache().put(name, Optional.empty());
                    }
                }
            });
            return globalLookupDriver.getPlayer(name);
        }
    }

    public void invalidateLocalCache(UUID uuid, String name)
    {
        cacheDriver.getPlayerCache().invalidate(uuid);
        cacheDriver.getNameCache().invalidate(name);
    }

    public void invalidateLocalCache()
    {
        cacheDriver.getPlayerCache().invalidateAll();
        cacheDriver.getNameCache().invalidateAll();
    }
}
