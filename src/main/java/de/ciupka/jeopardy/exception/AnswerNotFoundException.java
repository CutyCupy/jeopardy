package de.ciupka.jeopardy.exception;

import de.ciupka.jeopardy.game.Player;

public class AnswerNotFoundException extends Exception {

    private Player player;

    public AnswerNotFoundException(Player player) {
        this.player = player;
    }

    @Override
    public String getMessage() {
        return String.format("Der Spieler '%s' hat die aktuelle Frage nicht beantwortet!", this.player.getName());
    }
}
