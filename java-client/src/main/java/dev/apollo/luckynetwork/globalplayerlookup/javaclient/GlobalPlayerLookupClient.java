package dev.apollo.luckynetwork.globalplayerlookup.javaclient;

import dev.apollo.luckynetwork.globalplayerlookup.javaclient.objects.ClientConfig;
import dev.apollo.luckynetwork.globalplayerlookup.javaclient.repository.PlayerRepository;

public class GlobalPlayerLookupClient {

    private ClientConfig config;
    private PlayerRepository playerRepository;

    public GlobalPlayerLookupClient(ClientConfig config) {
        this.config = config;
        this.playerRepository = new PlayerRepository(this);

    }

    public ClientConfig getConfig() {
        return config;
    }

    public PlayerRepository getPlayerRepository() {
        return playerRepository;
    }

    public void shutdown()
    {
        playerRepository.shutdown();
    }
}
