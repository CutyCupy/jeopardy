package de.ciupka.jeopardy.game;

import java.util.Objects;
import java.util.UUID;

/**
 * This class represents a player that is participating in the game.
 * The main purpose of this class is to keep track of the name and score of any
 * given user.
 * 
 * @author Alexander Ciupka
 */
public class Player {

    private String name;
    private UUID id;
    private boolean disconnected;
    private int score;

    public Player(UUID uuid, String name) {
        this.name = name;
        this.id = uuid;
    }

    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID uuid) {
        this.id = uuid;
        disconnected = false;
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public void disconnect() {
        disconnected = true;
    }

    public int getScore() {
        return this.score;
    }

    public void updateScore(int delta) {
        this.score += delta;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Player other = (Player) obj;
        return Objects.equals(name, other.name) &&
                Objects.equals(id, other.id);
    }
}
