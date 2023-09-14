package dev.apollo.luckynetwork.globalplayerlookup.commons.objects;

import lombok.NonNull;
import org.json.simple.JSONObject;

import java.time.LocalDateTime;
import java.util.UUID;

public record LookedPlayer(@NonNull UUID uuid, @NonNull String name, @NonNull LocalDateTime registered,
                           @NonNull LocalDateTime lastSeen, @NonNull boolean premium) {

    public JSONObject toJSON() {
        JSONObject playerObject = new JSONObject();
        playerObject.put("name", name);
        playerObject.put("uuid", uuid.toString());
        playerObject.put("firstJoin", registered.toString());
        playerObject.put("lastJoin", lastSeen.toString());
        playerObject.put("premium", premium);
        return playerObject;
    }

    public static LookedPlayer fromJSON(JSONObject jsonObject) throws IllegalArgumentException {
        try {
            return new LookedPlayer(UUID.fromString((String) jsonObject.get("uuid")), (String) jsonObject.get("name"),
                    LocalDateTime.parse((String) jsonObject.get("firstJoin")),
                    LocalDateTime.parse((String) jsonObject.get("lastJoin")),
                    (boolean) jsonObject.get("premium"));
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid JSON");
        }
    }
}