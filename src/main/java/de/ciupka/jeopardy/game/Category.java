package de.ciupka.jeopardy.game;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.ciupka.jeopardy.exception.QuestionNotFoundException;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;

public class Category {

    private String name;
    private String colorCode;
    private List<AbstractQuestion<?>> questions;

    @JsonCreator
    public Category(@JsonProperty("name") String name, 
        @JsonProperty("colorCode") String colorCode,
        @JsonProperty("questions") List<AbstractQuestion<?>> questions) {
        this.name = name;
        this.colorCode = colorCode;
        this.questions = questions;
    }

    public String getName() {
        return name;
    }

    public List<AbstractQuestion<?>> getQuestions() {
        return questions;
    }

    public AbstractQuestion<?> getQuestion(int idx) throws QuestionNotFoundException {
        if (idx < 0 || idx >= this.questions.size()) {
            throw new QuestionNotFoundException();
        }
        return this.questions.get(idx);
    }

    public String getColorCode() {
        return colorCode;
    }
}
