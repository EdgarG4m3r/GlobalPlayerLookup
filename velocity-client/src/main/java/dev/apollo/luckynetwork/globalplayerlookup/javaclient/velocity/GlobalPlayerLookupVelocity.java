package dev.apollo.luckynetwork.globalplayerlookup.javaclient.velocity;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.apollo.luckynetwork.globalplayerlookup.commons.objects.CacheConfig;
import dev.apollo.luckynetwork.globalplayerlookup.javaclient.GlobalPlayerLookupClient;
import dev.apollo.luckynetwork.globalplayerlookup.javaclient.objects.ClientConfig;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "globalplayerlookupvelocity",
        name = "GlobalPlayerLookupVelocity",
        version = "1.0.0",
        description = "A GlobalPlayerLookup client wrapper for Velocity",
        authors = {"FacedApollo"}
)
public class GlobalPlayerLookupVelocity {
    private static GlobalPlayerLookupClient client;
    private File dataDirectory;
    private CacheConfig cacheConfig;

    @Inject
    public GlobalPlayerLookupVelocity(ProxyServer proxyServer, Logger logger, @DataDirectory final Path dataDirectory)
    {
        this.dataDirectory = dataDirectory.toFile();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

        File configFile = Paths.get(dataDirectory.getPath(), "config.toml").toFile();
        Toml toml = new Toml().read(configFile);


        if (!toml.getBoolean("cache.enabled")) {
            cacheConfig = null;
        } else {
            cacheConfig = new CacheConfig(
                    toml.getLong("cache.max_size").intValue(),
                    toml.getLong("cache.expire_time").intValue(),
                    TimeUnit.valueOf(toml.getString("cache.expire_time_unit")),
                    toml.getLong("cache.concurrency").intValue()
            );
        }

        ClientConfig clientConfig = new ClientConfig(
                toml.getString("api.username"),
                toml.getString("api.token"),
                toml.getString("api.url"),
                cacheConfig
        );

        client = new GlobalPlayerLookupClient(clientConfig);
    }

    //on disable
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        client.shutdown();
    }

    public static GlobalPlayerLookupClient getClient() {
        return client;
    }

}
