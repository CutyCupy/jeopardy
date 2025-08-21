package de.ciupka.jeopardy.game;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import de.ciupka.jeopardy.configs.Views;
import de.ciupka.jeopardy.exception.QuestionNotFoundException;
import de.ciupka.jeopardy.game.questions.AbstractQuestion;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Category {

    @JsonView(Views.Common.class)
    private String name;
    @JsonView(Views.Common.class)
    private String color;
    @JsonView(Views.Common.class)
    private List<AbstractQuestion<?>> questions;
    
    @JsonCreator
    public Category(@JsonProperty("name") String name,
            @JsonProperty("color") String color,
            @JsonProperty("questions") List<AbstractQuestion<?>> questions) {
        this.name = name;
        this.color = color;
        this.questions = questions == null ? new ArrayList<>() : questions;
    }

    public String getName() {
        return name;
    }

    public List<AbstractQuestion<?>> getQuestions() {
        questions.sort((a, b) -> a.getPoints() - b.getPoints());
        return questions;
    }

    public AbstractQuestion<?> getQuestion(int idx) throws QuestionNotFoundException {
        if (idx < 0 || idx >= this.questions.size()) {
            throw new QuestionNotFoundException();
        }
        return this.questions.get(idx);
    }

    public String getColor() {
        return color;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
