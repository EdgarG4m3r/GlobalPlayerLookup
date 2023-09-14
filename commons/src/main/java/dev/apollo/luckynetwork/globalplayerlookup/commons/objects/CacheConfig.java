package dev.apollo.luckynetwork.globalplayerlookup.commons.objects;

import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
public class CacheConfig {
    private int maxCacheSize;
    private int cacheExpireTime;
    private TimeUnit cacheExpireTimeUnit;
    private int concurrentRequests;

    public CacheConfig(int maxCacheSize, int cacheExpireTime, TimeUnit cacheExpireTimeUnit, int concurrentRequests) {
        this.maxCacheSize = maxCacheSize;
        this.cacheExpireTime = cacheExpireTime;
        this.cacheExpireTimeUnit = cacheExpireTimeUnit;
        this.concurrentRequests = concurrentRequests;
    }

    public CacheConfig()
    {
        this.maxCacheSize = 1000;
        this.cacheExpireTime = 6;
        this.cacheExpireTimeUnit = TimeUnit.HOURS;
        this.concurrentRequests = Runtime.getRuntime().availableProcessors();
    }
}
