package de.ciupka.jeopardy.game;

import java.util.UUID;

public class Player {

    private String name;
    private UUID uuid;
    private int score;

    public Player(UUID uuid, String name) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getScore() {
        return this.score;
    }
}
