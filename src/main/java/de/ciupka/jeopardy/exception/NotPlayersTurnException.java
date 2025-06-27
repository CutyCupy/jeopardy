package de.ciupka.jeopardy.exception;

public class NotPlayersTurnException extends Exception {

    @Override
    public String getMessage() {
        return "Du bist aktuell nicht am Zug!";
    }
}
