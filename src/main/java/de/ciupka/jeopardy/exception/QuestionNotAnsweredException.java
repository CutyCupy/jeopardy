package de.ciupka.jeopardy.exception;

public class QuestionNotAnsweredException extends Exception {

    @Override
    public String getMessage() {
        return "Die Frage wurde noch nicht beantwortet!";
    }
    
}
