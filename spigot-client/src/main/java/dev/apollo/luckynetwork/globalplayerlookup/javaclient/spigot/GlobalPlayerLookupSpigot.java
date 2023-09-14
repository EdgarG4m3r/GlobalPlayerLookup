package dev.apollo.luckynetwork.globalplayerlookup.javaclient.spigot;

import dev.apollo.luckynetwork.globalplayerlookup.commons.objects.CacheConfig;
import dev.apollo.luckynetwork.globalplayerlookup.javaclient.GlobalPlayerLookupClient;
import dev.apollo.luckynetwork.globalplayerlookup.javaclient.objects.ClientConfig;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public class GlobalPlayerLookupSpigot extends JavaPlugin {

    private static GlobalPlayerLookupClient client;

    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        CacheConfig cacheConfig;
        if (!getConfig().getBoolean("cache.enabled"))
        {
            cacheConfig = null;
        }
        else
        {
            cacheConfig = new CacheConfig(
                    getConfig().getInt("cache.max_size"),
                    getConfig().getInt("cache.expire_time"),
                    TimeUnit.valueOf(getConfig().getString("cache.expire_time_unit")),
                    getConfig().getInt("cache.concurrency")
            );
        }
        ClientConfig clientConfig = new ClientConfig(
                getConfig().getString("api.username"),
                getConfig().getString("api.token"),
                getConfig().getString("api.url"),
                cacheConfig
        );

        client = new GlobalPlayerLookupClient(clientConfig);
    }

    @Override
    public void onDisable()
    {
        client.shutdown();
    }

    public static GlobalPlayerLookupClient getClient() {
        return client;
    }
}
