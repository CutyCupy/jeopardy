package de.ciupka.jeopardy.game.questions;

import de.ciupka.jeopardy.controller.messages.AnswerUpdateType;
import de.ciupka.jeopardy.game.Player;

public class Answer<T> {

    private Player player;
    private T answer;
    private AnswerUpdateType updateType;

    public Answer(Player p, T a) {
        this.player = p;
        this.answer = a;
        this.updateType = AnswerUpdateType.NO_ANSWER;
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

    public AnswerUpdateType getUpdateType() {
        return updateType;
    }

    public void setUpdateType(AnswerUpdateType updateType) {
        this.updateType = updateType;
    }

}
