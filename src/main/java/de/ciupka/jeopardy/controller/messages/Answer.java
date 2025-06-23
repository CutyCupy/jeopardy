package de.ciupka.jeopardy.controller.messages;

import de.ciupka.jeopardy.game.Player;

public class Answer<T> {

    private Player player;
    private T answer;

    public Answer(Player p, T a) {
        this.player = p;
        this.answer = a;
    }

    public Player getPlayer() {
        return player;
    }

    public T getAnswer() {
        return answer;
    }

    public void setAnswer(T answer) {
        this.answer = answer;
    }
}
