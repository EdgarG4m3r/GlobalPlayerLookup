package dev.apollo.luckynetwork.globalplayerlookup.javaclient.objects;

import dev.apollo.luckynetwork.globalplayerlookup.commons.objects.CacheConfig;
import lombok.Getter;

@Getter
public class ClientConfig {

    private String username;
    private String apiKey;
    private String url;
    private CacheConfig cacheConfig;

    public ClientConfig(String username, String apiKey, String url, CacheConfig cacheConfig) {
        this.username = username;
        this.apiKey = apiKey;
        this.url = url;
        this.cacheConfig = cacheConfig;
    }

    public ClientConfig(String username, String apiKey, String url) {
        this.username = username;
        this.apiKey = apiKey;
        this.url = url;
        this.cacheConfig = new CacheConfig();
    }



}
