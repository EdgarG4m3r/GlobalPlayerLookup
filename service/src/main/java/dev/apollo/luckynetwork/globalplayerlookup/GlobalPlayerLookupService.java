package dev.apollo.luckynetwork.globalplayerlookup;

import dev.apollo.luckynetwork.globalplayerlookup.authentication.AuthHandler;
import dev.apollo.luckynetwork.globalplayerlookup.commons.objects.CacheConfig;
import dev.apollo.luckynetwork.globalplayerlookup.configuration.Configuration;
import dev.apollo.luckynetwork.globalplayerlookup.databases.MySQL;

import dev.apollo.luckynetwork.globalplayerlookup.databases.Redis;
import dev.apollo.luckynetwork.globalplayerlookup.manager.WebsocketManager;
import dev.apollo.luckynetwork.globalplayerlookup.repository.PlayerRepository;
import dev.apollo.luckynetwork.globalplayerlookup.routes.*;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

public class GlobalPlayerLookupService {

    public static GlobalPlayerLookupService instance;
    private static Logger logger = LoggerFactory.getLogger("GlobalPlayerLookupService");

    private Configuration configuration;
    private MySQL mySQL;
    private Redis redis;

    private Javalin webserver;

    private AuthHandler authHandler;

    private WebsocketManager websocketManager;
    private CacheConfig cacheConfig;
    private PlayerRepository playerRepository;


    public static void main(String[] args) {
        if (instance == null)
        {
            instance = new GlobalPlayerLookupService();
            instance.start();
        }
        else
        {
            logger.error("GlobalPlayerLookupService is already running!");
        }
    }

    private void start() {
        logger.info("Starting GlobalPlayerLookupService...");
        loadConfig();
        loadMySQL();
        loadRedis();
        loadManagers();
        loadWebServer();
    }

    private void loadWebServer() {
        logger.info("Starting the API Server...");
        this.authHandler = new AuthHandler();
        this.webserver = Javalin.create(config -> {
            config.requestLogger.http((ctx, executionTimeMs) -> {
                logger.info(ctx.method() + " " + ctx.fullUrl() + " " + ctx.status() + " " + ctx.ip() + " " + executionTimeMs + "ms");
            });
            config.accessManager(authHandler);
        }).start(configuration.webserver_address, configuration.webserver_port);
        logger.info("Registering routes...");

        RouteDELETE.registerRoute(webserver);
        RouteGET.registerRoute(webserver);
        RoutePATCH.registerRoute(webserver);
        RoutePOST.registerRoute(webserver);
        RouteDEFAULT.registerRoute(webserver);
        RouteWS.registerRoute(webserver);

        logger.info("API is now running on " + configuration.webserver_address + ":" + configuration.webserver_port);
    }

    private void loadConfig() {
        logger.info("Loading config... Default config (config.ini) will be created if not found.");
        configuration = new Configuration();
        logger.info("Config loaded!");
    }

    private void loadMySQL() {
        logger.info("Loading MySQL...");
        mySQL = new MySQL(
                configuration.mysql_address,
                String.valueOf(configuration.mysql_port),
                configuration.mysql_database,
                configuration.mysql_username,
                configuration.mysql_password,
                configuration.mysql_ssl
        );
        mySQL.connect();
        logger.info("MySQL loaded!");
    }

    private void loadManagers() {
        logger.info("Loading managers...");
        websocketManager = new WebsocketManager();
        cacheConfig = new CacheConfig();
        playerRepository = new PlayerRepository(this);
        logger.info("Managers loaded!");
    }

    private void loadRedis() {
        logger.info("Loading Redis...");
        //String host, int port, String password, String channel
        redis = new Redis(
                configuration.redis_address,
                configuration.redis_port,
                configuration.redis_password,
                configuration.redis_channel
        );
        redis.connect();
        redis.subscribe();
        logger.info("Redis loaded!");
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        GlobalPlayerLookupService.logger = logger;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public MySQL getMySQL() {
        return mySQL;
    }

    public Redis getRedis() {
        return redis;
    }

    public WebsocketManager getWebsocketManager() {
        return websocketManager;
    }

    public CacheConfig getCacheConfig() {
        return cacheConfig;
    }

    public PlayerRepository getPlayerRepository() {
        return playerRepository;
    }
}