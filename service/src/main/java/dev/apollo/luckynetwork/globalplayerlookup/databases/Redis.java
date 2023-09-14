package dev.apollo.luckynetwork.globalplayerlookup.databases;

import dev.apollo.luckynetwork.globalplayerlookup.GlobalPlayerLookupService;
import dev.apollo.luckynetwork.globalplayerlookup.manager.WebsocketManager;
import dev.apollo.luckynetwork.globalplayerlookup.repository.PlayerRepository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Redis {

    private String host;
    private int port;
    private String password;
    private String channel;
    private JedisPoolConfig jedisPoolConfig;
    private JedisPool jedispool;

    private ExecutorService subscriberThread = Executors.newSingleThreadExecutor();

    public Redis(String host, int port, String password, String channel) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.channel = channel;
    }

    public void connect()
    {
        this.jedisPoolConfig = new JedisPoolConfig();
        this.jedisPoolConfig.setMaxTotal(100);
        this.jedisPoolConfig.setMaxIdle(20);
        this.jedisPoolConfig.setMinIdle(1);
        this.jedisPoolConfig.setTestOnBorrow(true);
        this.jedisPoolConfig.setTestOnReturn(true);
        this.jedisPoolConfig.setTestWhileIdle(true);
        this.jedisPoolConfig.setNumTestsPerEvictionRun(-1);
        this.jedisPoolConfig.setBlockWhenExhausted(true);
        this.jedispool = new JedisPool(jedisPoolConfig, host, port, 5000, password, 0);
    }


    public void disconnect()
    {
        jedispool.close();
    }

    public JedisPool getJedisPool() {
        return jedispool;
    }

    public void publish(String message) {
        try (Jedis jedis = jedispool.getResource()) {
            jedis.publish(channel, message);
        }
    }

    public void subscribe()
    {
        subscriberThread.submit(() -> {
            try (Jedis jedis = jedispool.getResource()) {
                jedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        if (message.startsWith("invalidateall"))
                        {
                            GlobalPlayerLookupService.instance.getPlayerRepository().invalidateLocalCache();
                            GlobalPlayerLookupService.instance.getWebsocketManager().sendMessageToAll("invalidateall");
                        }
                        if (message.startsWith("invalidate"))
                        {
                            try
                            {
                                String[] args = message.split(":");
                                if (args.length == 3)
                                {

                                    UUID uuid = UUID.fromString(args[1]);
                                    String name = args[2];
                                    GlobalPlayerLookupService.instance.getPlayerRepository().invalidateLocalCache(uuid, name);
                                    GlobalPlayerLookupService.instance.getWebsocketManager().sendMessageToAll("invalidate:" + uuid.toString() + ":" + name);
                                }
                            }
                            catch (Exception e)
                            {
                                //ignore
                            }
                        }
                    };
                }, channel);
            }
        });
    }


}