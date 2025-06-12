package de.ciupka.jeopardy.controller.messages;

import de.ciupka.jeopardy.game.Category;
import de.ciupka.jeopardy.game.Player;

public class BoardUpdate {

    private Category[] board;
    private QuestionIdentifier active;
    private Player player;

    public BoardUpdate(Category[] board, QuestionIdentifier active, Player player) {
        this.board = board;
        this.active = active;
        this.player = player;
    }

    public Category[] getBoard() {
        return board;
    }

    public QuestionIdentifier getActive() {
        return active;
    }

    public Player getPlayer() {
        return player;
    }

}
