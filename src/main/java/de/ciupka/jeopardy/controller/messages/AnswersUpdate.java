package de.ciupka.jeopardy.controller.messages;

import java.util.List;

import de.ciupka.jeopardy.game.Player;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;
import de.ciupka.jeopardy.game.questions.Evaluatable;
import de.ciupka.jeopardy.game.questions.answer.Answer;
import de.ciupka.jeopardy.game.questions.answer.Tool;

public class AnswersUpdate {

    private Tool tool;
    private List<AnswerUpdate> answers;

    public AnswersUpdate() {
    }

    public AnswersUpdate(AbstractQuestion<?> question, boolean master) {
        this.tool = question.getAnswerTool();
        this.answers = question.getAnswers().stream().map(
                (a) -> new AnswerUpdate(!(question instanceof Evaluatable)
                        && (question.isLocked() || tool == Tool.BUZZER), a, master))
                .toList();
    }

    public static class AnswerUpdate {
        private Player player;
        private String answer;

        private Boolean correct;

        private boolean evaluationEnabled;
        private boolean revealed;

        public AnswerUpdate(boolean evaluationEnabled, Answer<?> answer, boolean master) {
            this.player = answer.getPlayer();
            this.correct = answer.getCorrect();
            this.answer = answer.asAnswerText(master);

            this.evaluationEnabled = master && evaluationEnabled && correct == null;
            this.revealed = answer.isRevealed();
        }

        public Player getPlayer() {
            return player;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public Boolean getCorrect() {
            return correct;
        }

        public boolean isEvaluationEnabled() {
            return evaluationEnabled;
        }

        public boolean isRevealed() {
            return revealed;
        }
    }

    public Tool getTool() {
        return tool;
    }

    public List<AnswerUpdate> getAnswers() {
        return answers;
    }

}
