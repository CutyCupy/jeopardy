package de.ciupka.jeopardy.controller.messages;

import de.ciupka.jeopardy.game.Player;

public class Answer {

    private Player player;
    private String answer;

    public Answer(Player p, String a) {
        this.player = p;
        this.answer = a;
    }

    public Player getPlayer() {
        return player;
    }

    public String getAnswer() {
        return answer;
    }

}
