package de.ciupka.jeopardy.controller.messages;

import de.ciupka.jeopardy.game.GameService;
import de.ciupka.jeopardy.game.Player;

public class PlayerUpdate {

    private String name;
    private int score;

    private boolean connected;
    private boolean active;

    public PlayerUpdate(GameService game, Player p) {
        this.name = p.getName();
        this.score = p.getScore();
        this.connected = !p.isDisconnected();

        this.active = p.equals(game.getCurrentPlayer());
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isActive() {
        return active;
    }

}
