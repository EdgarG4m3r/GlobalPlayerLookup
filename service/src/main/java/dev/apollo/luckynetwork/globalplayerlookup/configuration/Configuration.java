package dev.apollo.luckynetwork.globalplayerlookup.configuration;

import dev.apollo.luckynetwork.globalplayerlookup.GlobalPlayerLookupService;
import org.ini4j.Ini;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class Configuration {

    public String webserver_address;
    public int webserver_port;
    public int webserver_timeout;
    public int webserver_rate_limit;
    public String webserver_app_name;
    public String webserver_http_server_name;
    public String webserver_api_key;

    public String instance_type;
    public boolean instance_bypass_redis;

    public int localcache_concurrency;
    public int localcache_max_size;
    public int localcache_expire_time;
    public TimeUnit localcache_expire_time_unit;

    public String globalplayerlookup_username;
    public String globalplayerlookup_token;
    public String globalplayerlookup_address;

    public String mysql_address;
    public int mysql_port;
    public String mysql_database;
    public String mysql_username;
    public String mysql_password;
    public boolean mysql_ssl;
    public String redis_address;
    public int redis_port;
    public String redis_password;
    public String redis_channel;

    //init
    public Configuration() {

        GlobalPlayerLookupService.getLogger().info("Initializing configuration...");

        copy(getClass().getResourceAsStream("/config.ini"), System.getProperty("user.dir")+"/config.ini");

        File configFile = new File(System.getProperty("user.dir")+"/config.ini");
        try
        {
            if(!configFile.exists())
            {
                throw new NullPointerException("Config file not found!");
            }
        }
        catch (NullPointerException e)
        {
            GlobalPlayerLookupService.getLogger().error("An error occurred while loading config! Terminating...");
            e.printStackTrace();
            System.exit(1);
        }

        try
        {
            Ini ini = new Ini(configFile);
            webserver_address = ini.get("webserver", "address");
            webserver_port = Integer.parseInt(ini.get("webserver", "port"));
            webserver_timeout = Integer.parseInt(ini.get("webserver", "timeout"));
            webserver_rate_limit = Integer.parseInt(ini.get("webserver", "rate_limit"));
            webserver_app_name = ini.get("webserver", "app_name");
            webserver_http_server_name = ini.get("webserver", "http_server_name");
            webserver_api_key = ini.get("webserver", "api_key");

            instance_type = ini.get("instance", "type");
            instance_bypass_redis = Boolean.parseBoolean(ini.get("instance", "bypass_redis"));

            localcache_concurrency = Integer.parseInt(ini.get("localcache", "concurrency"));
            localcache_max_size = Integer.parseInt(ini.get("localcache", "max_size"));
            localcache_expire_time = Integer.parseInt(ini.get("localcache", "expire_time"));
            localcache_expire_time_unit = TimeUnit.valueOf(ini.get("localcache", "expire_time_unit"));

            globalplayerlookup_username = ini.get("globalplayerlookup", "username");
            globalplayerlookup_token = ini.get("globalplayerlookup", "token");
            globalplayerlookup_address = ini.get("globalplayerlookup", "address");

            mysql_address = ini.get("mysql", "address");
            mysql_port = Integer.parseInt(ini.get("mysql", "port"));
            mysql_database = ini.get("mysql", "database");
            mysql_username = ini.get("mysql", "username");
            mysql_password = ini.get("mysql", "password");
            mysql_ssl = Boolean.parseBoolean(ini.get("mysql", "ssl"));


            redis_address = ini.get("redis", "address");
            redis_port = Integer.parseInt(ini.get("redis", "port"));
            redis_password = ini.get("redis", "password");
            redis_channel = ini.get("redis", "channel");
        }
        catch (Exception e)
        {
            GlobalPlayerLookupService.getLogger().error("An error occurred while loading config! Terminating...");
            e.printStackTrace();
            System.exit(1);
        }

        GlobalPlayerLookupService.getLogger().info("Configuration initialized!");
        GlobalPlayerLookupService.setLogger(LoggerFactory.getLogger(webserver_app_name));

    }

    private boolean copy(InputStream source , String destination) {
        boolean success = true;

        if (new File(destination).exists()) {
            return false;
        }

        GlobalPlayerLookupService.getLogger().info("Copying default config to " + destination);

        try {
            Files.copy(source, Paths.get(destination));
        } catch (IOException ex) {
            GlobalPlayerLookupService.getLogger().error("Failed to copy file" + source + " to " + destination);
            success = false;
        }

        return success;
    }


}
