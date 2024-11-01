package ru.govno.client.utils;

import ru.govno.client.utils.DiscordRP;

public class ClientRP {
    private static final ClientRP INSTANCE = new ClientRP();
    DiscordRP discordRP = new DiscordRP();

    public static final ClientRP getInstance() {
        return INSTANCE;
    }

    public void init() {
        this.discordRP.start();
    }

    public void shutdown() {
        this.discordRP.shutdown();
    }

    public DiscordRP getDiscordRP() {
        return this.discordRP;
    }
}

