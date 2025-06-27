package de.ciupka.jeopardy.exception;

public class QuestionAlreadyAnsweredException extends Exception {

    @Override
    public String getMessage() {
        return "Die Frage wurde bereits beantwortet!";
    }
    
}
