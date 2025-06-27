package de.ciupka.jeopardy.controller.messages;

import de.ciupka.jeopardy.game.Category;
import de.ciupka.jeopardy.game.GameService;
import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;
import de.ciupka.jeopardy.game.questions.Answer;

public class BoardUpdate {

    private CategoryUpdate[] categories;
    private QuestionIdentifier selected;

    public BoardUpdate(GameService game, Player player) {

        Category[] board = game.getBoard();

        this.categories = new CategoryUpdate[board.length];
        for (int i = 0; i < board.length; i++) {
            this.categories[i] = new CategoryUpdate(board[i], i, player);
        }
        SelectedQuestion question = game.getSelectedQuestion();

        this.selected = question != null ? question.getIdentifier() : null;
    }

    public CategoryUpdate[] getCategories() {
        return categories;
    }

    public QuestionIdentifier getSelected() {
        return selected;
    }

    public static class CategoryUpdate {

        private String title;
        private String color;

        private QuestionUpdate[] questions;

        public CategoryUpdate(Category cat, int idx, Player player) {
            this.title = cat.getName();
            this.color = cat.getColorCode();

            AbstractQuestion<?>[] questions = cat.getQuestions();

            this.questions = new QuestionUpdate[questions.length];
            for (int j = 0; j < questions.length; j++) {
                this.questions[j] = new QuestionUpdate(new QuestionIdentifier(idx, j), questions[j], player);
            }

        }

        public String getTitle() {
            return title;
        }

        public String getColor() {
            return color;
        }

        public QuestionUpdate[] getQuestions() {
            return questions;
        }

    }

    public static class QuestionUpdate {
        private QuestionIdentifier id;

        private int points;
        private boolean answered;
        private Boolean correct;

        public QuestionUpdate(QuestionIdentifier id, AbstractQuestion<?> question, Player player) {
            this.id = id;

            this.points = question.getPoints();
            this.answered = question.isAnswered();

            Answer<?> answer = question.getAnswerByPlayer(player);
            if (answer != null) {
                this.correct = answer.getCorrect();
            }
        }

        public QuestionIdentifier getId() {
            return id;
        }

        public int getPoints() {
            return points;
        }

        public boolean isAnswered() {
            return answered;
        }

        public Boolean getCorrect() {
            return correct;
        }

    }

}
