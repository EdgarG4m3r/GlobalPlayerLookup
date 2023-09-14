package dev.apollo.luckynetwork.globalplayerlookup.datalayer;

import dev.apollo.luckynetwork.globalplayerlookup.commons.objects.LookedPlayer;
import dev.apollo.luckynetwork.globalplayerlookup.repository.PlayerRepository;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import redis.clients.jedis.Jedis;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class RedisDriver {

    public @Nullable Optional<LookedPlayer> getPlayer(Jedis jedis, UUID uuid) {
        String data = jedis.get("playerlookup-cache:" + uuid.toString());
        if (data != null)
        {
            if (data.equalsIgnoreCase("empty"))
            {
                return Optional.empty();
            }
            else
            {
                JSONObject jsonObject = null;
                try
                {
                    jsonObject = (JSONObject) new JSONParser().parse(data);
                    return Optional.of(LookedPlayer.fromJSON(jsonObject));
                }
                catch (ParseException e)
                {
                    jedis.del("playerlookup-cache:" + uuid.toString());
                    return null;
                }
                catch (IllegalArgumentException e)
                {
                    jedis.del("playerlookup-cache:" + uuid.toString());
                    return null;
                }
            }
        }
        return null;
    }

    public @Nullable Optional<LookedPlayer> getPlayer(Jedis jedis, String name)
    {
        String uuidStr = jedis.get("playerlookup-namecache:" + name);
        return uuidStr != null ? getPlayer(jedis, UUID.fromString(uuidStr)) : null;
    }

}
