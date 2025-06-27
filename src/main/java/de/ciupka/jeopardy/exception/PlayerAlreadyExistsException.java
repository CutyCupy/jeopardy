package de.ciupka.jeopardy.exception;

import de.ciupka.jeopardy.game.Player;

public class PlayerAlreadyExistsException extends Exception {
    
    private Player player;

    public PlayerAlreadyExistsException(Player player) {
        this.player = player;
    }

    @Override
    public String getMessage() {
        return String.format("Der Spieler '%s' existiert bereits!", this.player.getName());
    }
    
}
