package de.ciupka.jeopardy.exception;

public class GameAlreadyStartedException extends Exception {
    @Override
    public String getMessage() {
        return "Das Spiel hat bereits gestartet!";
    }
    
}