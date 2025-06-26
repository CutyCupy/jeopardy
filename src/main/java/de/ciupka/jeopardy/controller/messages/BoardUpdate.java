package de.ciupka.jeopardy.controller.messages;

import de.ciupka.jeopardy.game.Category;
import de.ciupka.jeopardy.game.Player;

public class BoardUpdate {

    private Category[] board;
    private SelectedQuestion selectedQuestion;
    private Player currentPlayer;
    private Player mySelf;

    public BoardUpdate(Category[] board, SelectedQuestion active, Player player, Player mySelf) {
        this.board = board;
        this.selectedQuestion = active;
        this.currentPlayer = player;
        this.mySelf = mySelf;
    }

    public Category[] getBoard() {
        return board;
    }

    public SelectedQuestion getSelectedQuestion() {
        return selectedQuestion;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Player getMySelf() {
        return mySelf;
    }
}
