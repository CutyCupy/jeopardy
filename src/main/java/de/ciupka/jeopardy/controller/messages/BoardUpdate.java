package de.ciupka.jeopardy.controller.messages;

import de.ciupka.jeopardy.game.Category;
import de.ciupka.jeopardy.game.Player;

public class BoardUpdate {

    private Category[] board;
    private SelectedQuestion selectedQuestion;
    private Player player;

    public BoardUpdate(Category[] board, SelectedQuestion active, Player player) {
        this.board = board;
        this.selectedQuestion = active;
        this.player = player;
    }

    public Category[] getBoard() {
        return board;
    }

    public SelectedQuestion getSelectedQuestion() {
        return selectedQuestion;
    }

    public Player getPlayer() {
        return player;
    }

}
