package de.ciupka.jeopardy.exception;

public class NotGameMasterException extends Exception {
 
    @Override
    public String getMessage() {
        return "Nur der Spielleiter darf diese Aktion durchführen!";
    }
}
